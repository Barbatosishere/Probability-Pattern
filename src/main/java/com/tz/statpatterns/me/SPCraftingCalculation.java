package com.tz.statpatterns.me;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingSimulationRequester;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.core.AELog;
import appeng.crafting.CraftBranchFailure;
import appeng.crafting.CraftingCalculation;
import appeng.crafting.CraftingPlan;
import appeng.crafting.CraftingTreeNode;
import appeng.crafting.inv.ChildCraftingSimulationState;
import appeng.crafting.inv.CraftingSimulationState;
import appeng.crafting.inv.NetworkCraftingSimulationState;
import appeng.hooks.ticking.TickHandler;
import com.google.common.base.Stopwatch;
import com.tz.statpatterns.crafting.StatisticalPatternDetails;
import com.tz.statpatterns.mixin.CraftingServiceMixin;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static appeng.api.config.Actionable.MODULATE;
import static com.tz.statpatterns.me.ProbMaterialVerifier.verifySingle;

public class SPCraftingCalculation extends CraftingCalculation {
    private static final Logger LOGGER = LoggerFactory.getLogger(SPCraftingCalculation.class);
    private NetworkCraftingSimulationState networkInv;
    private final Level level;
    private final KeyCounter missing = new KeyCounter();
    private final Object monitor = new Object();
    private final Stopwatch watch = Stopwatch.createUnstarted();
    private SPCraftingTreeNode tree;
    private final AEKey output;
    // The initially requested amount of "output", may be reduced depending on the strategy used
    private final long requestedAmount;
    private final CalculationStrategy strategy;
    private boolean simulate = false;
    final ICraftingSimulationRequester simRequester;
    private boolean running = false;
    private boolean done = false;
    private int time = 5;
    private int incTime = Integer.MAX_VALUE;
    private final List<SPCraftingCalculation.CraftAttempt> attempts = AELog.isCraftingLogEnabled() ? new ArrayList<>() : null;
    private IGrid grid;

    public SPCraftingCalculation(Level level, IGrid grid, ICraftingSimulationRequester simRequester, GenericStack output, CalculationStrategy strategy) {
        super(level, grid, simRequester, output, strategy);
        this.level = level;
        this.output = output.what();
        this.requestedAmount = output.amount();
        this.strategy = strategy;
        this.simRequester = simRequester;
        this.grid = grid;

        var storage = grid.getStorageService();
        var craftingService = grid.getCraftingService();
        this.networkInv = new NetworkCraftingSimulationState(storage, simRequester.getActionSource());

        this.tree = new SPCraftingTreeNode(craftingService, this, this.output, 1, null, -1);
    }

    void addMissing(AEKey what, long amount) {
        missing.add(what, amount);
    }

    public ICraftingPlan run() {
        try {
            TickHandler.instance().registerCraftingSimulation(this.level, this);
            this.handlePausing();
            var plan = computePlan();
            this.logCraftingJob(plan);


            return plan;
        } catch (Exception ex) {
            AELog.info(ex, "Exception during crafting calculation.");
            throw new RuntimeException(ex);
        } finally {
            this.finish();
        }
    }

    private ICraftingPlan computePlan() throws InterruptedException {
        var fullAmountPlan = runCraftAttempt(false, requestedAmount);
        if (fullAmountPlan != null) {
            // Success with full amount!
            return fullAmountPlan;
        }

        if (strategy == CalculationStrategy.CRAFT_LESS) {
            // Try crafting less if possible using binary search.
            long successfulAmount = 0;
            ICraftingPlan successfulPlan = null;
            for (long increment = Long.highestOneBit(requestedAmount); increment > 0; increment /= 2) {
                long testAmount = successfulAmount + increment;
                if (testAmount < requestedAmount) {
                    var plan = runCraftAttempt(false, testAmount);
                    if (plan != null) {
                        // Success! :)
                        successfulAmount = testAmount;
                        successfulPlan = plan;
                    }
                }
            }

            // Found a successful plan! :)
            if (successfulPlan != null) {
                return successfulPlan;
            }
        }

        // Couldn't find a successful plan -> simulate.
        return runCraftAttempt(true, requestedAmount);
    }

    /**
     * @return null on failure
     */
    @Nullable
    @Contract("true, _ -> !null") // the calculation can't fail if simulated
    private CraftingPlan runCraftAttempt(boolean simulate, long amount) throws InterruptedException {
        this.simulate = simulate;

        final Stopwatch timer = Stopwatch.createStarted();

        ChildCraftingSimulationState craftingInventory = new ChildCraftingSimulationState(networkInv);
        craftingInventory.ignore(this.output);

        try {
            this.tree.request(craftingInventory, amount, null);
        } catch (CraftBranchFailure failure) {
            if (AELog.isCraftingLogEnabled()) {
                this.attempts.add(new SPCraftingCalculation.CraftAttempt(amount + " failed", timer));
            }
            return null;
        }
        craftingInventory.addBytes(this.tree.getNodeCount() * 8);


        var plan = CraftingSimulationState.buildCraftingPlan(craftingInventory, this, amount);

        for (var entry : plan.patternTimes().entrySet()) {
            var pattern = entry.getKey();
            long runs = entry.getValue();
            boolean isProb = StatisticalPatternDetails.isStatisticalPattern(pattern);
            if (isProb) {
                // 计算目标产出总量 = 每轮产出 × 执行次数
                long outputPerRun = 1;
                var outputs = pattern.getOutputs();
                if (!outputs.isEmpty()) {
                    outputPerRun = outputs.get(0).amount();
                }
                long totalOutput = runs * outputPerRun;
                long safeAttempts = verifySingle(totalOutput, pattern);
                for (var details : pattern.getInputs()) {
                    GenericStack[] stack = details.getPossibleInputs();
                    long multiplier = details.getMultiplier();
                    long baseAmount = multiplier * runs;
                    long adjustedAmount = multiplier * safeAttempts;
                    for (var i : plan.usedItems()) {
                        var item = i.getKey();
                        if (stack[0].what().equals(item) && i.getLongValue() == baseAmount) {
                            i.setValue(adjustedAmount);
                            craftingInventory.extract(item, adjustedAmount - baseAmount, Actionable.MODULATE);
                        }
                    }
                }
            }
        }
        if (AELog.isCraftingLogEnabled()) {
            String type = simulate ? "simulated" : "succeeded";
            this.attempts.add(new SPCraftingCalculation.CraftAttempt("%d %s (%d bytes)".formatted(amount, type, plan.bytes()), timer));
        }

        return CraftingSimulationState.buildCraftingPlan(craftingInventory, this, amount);
    }

    void handlePausing() throws InterruptedException {
        if (this.incTime > 100) {
            this.incTime = 0;

            synchronized (this.monitor) {
                if (this.watch.elapsed(TimeUnit.MICROSECONDS) > this.time) {
                    this.running = false;
                    this.watch.stop();
                    this.monitor.notify();
                }

                if (!this.running) {
                    AELog.craftingDebug("crafting job will now sleep");

                    while (!this.running) {
                        this.monitor.wait();
                    }

                    AELog.craftingDebug("crafting job now active");
                }
            }

            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
        }
        this.incTime++;
    }

    private void finish() {
        synchronized (this.monitor) {
            this.running = false;
            this.done = true;
            this.monitor.notify();
        }
    }

    public boolean isSimulation() {
        return this.simulate;
    }

    public AEKey getOutput() {
        return output;
    }

    public KeyCounter getMissingItems() {
        return missing;
    }

    Level getLevel() {
        return this.level;
    }

    /**
     * returns true if this needs more simulation.
     *
     * @param micros microseconds of simulation
     * @return true if this needs more simulation
     */
    public boolean simulateFor(int micros) {
        this.time = micros;

        synchronized (this.monitor) {
            if (this.done) {
                return false;
            }

            this.watch.reset();
            this.watch.start();
            this.running = true;

            AELog.craftingDebug("main thread is now going to sleep");

            this.monitor.notify();

            while (this.running) {
                try {
                    this.monitor.wait();
                } catch (InterruptedException ignored) {
                }
            }

            AELog.craftingDebug("main thread is now active");
        }

        return true;
    }

    private void logCraftingJob(ICraftingPlan plan) {
        if (AELog.isCraftingLogEnabled()) {
            ;
            var actionSource = this.simRequester.getActionSource();
            String actionSourceName;

            if (actionSource != null && actionSource.player().isPresent()) {
                var player = actionSource.player().get();
                actionSourceName = player.toString();
            } else if (actionSource != null && actionSource.machine().isPresent()) {
                var machineSource = actionSource.machine().get();
                var actionableNode = machineSource.getActionableNode();
                actionSourceName = actionableNode != null ? actionableNode.toString() : machineSource.toString();
            } else {
                actionSourceName = "[unknown source]";
            }

            StringBuilder message = new StringBuilder();
            message.append("CraftingCalculation issued by %s requesting [%dx%s] breakdown:\n".formatted(
                    actionSourceName, this.requestedAmount, this.output));
            for (var attempt : this.attempts) {
                message.append(" - %s in %d ms\n".formatted(
                        attempt.description, attempt.stopwatch.elapsed(TimeUnit.MILLISECONDS)));
            }
            message.append(" - final plan: %d (%d bytes)".formatted(plan.finalOutput().amount(), plan.bytes()));

            AELog.crafting(message.toString());
        }
    }

    public boolean hasMultiplePaths() {
        return this.tree.hasMultiplePaths();
    }

    private record CraftAttempt(String description, Stopwatch stopwatch) {
    }
}
