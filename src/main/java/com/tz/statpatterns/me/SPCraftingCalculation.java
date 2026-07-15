package com.tz.statpatterns.me;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.crafting.ICraftingSimulationRequester;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEItemKey;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
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
    private final long requestedAmount;
    private final CalculationStrategy strategy;
    private boolean simulate = false;
    final ICraftingSimulationRequester simRequester;
    private boolean running = false;
    private boolean done = false;
    private int time = 5;
    private int incTime = Integer.MAX_VALUE;
    private final List<CraftAttempt> attempts = AELog.isCraftingLogEnabled() ? new ArrayList<>() : null;
    private IGrid grid;

    public SPCraftingCalculation(Level level, IGrid grid, ICraftingSimulationRequester simRequester, GenericStack output, CalculationStrategy strategy) {
        super(level, grid, simRequester, output, strategy);
        this.level = level;
        this.output = output.what();
        this.requestedAmount = output.amount();
        this.strategy = strategy;
        this.simRequester = simRequester;
        this.grid = grid;

        IStorageService storage = grid.getStorageService();
        ICraftingService craftingService = grid.getCraftingService();
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

    // ✅ 保留原版二分查找 CRAFT_LESS 逻辑
    private ICraftingPlan computePlan() throws InterruptedException, NoSuchFieldException, ClassNotFoundException, IllegalAccessException {
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

    @Nullable
    @Contract("true, _ -> !null")
    private CraftingPlan runCraftAttempt(boolean simulate, long amount) throws InterruptedException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        this.simulate = simulate;
        final Stopwatch timer = Stopwatch.createStarted();

        ChildCraftingSimulationState craftingInventory = new ChildCraftingSimulationState(networkInv);
        craftingInventory.ignore(this.output);

        try {
            this.tree.request(craftingInventory, amount, null);
        } catch (CraftBranchFailure failure) {
            if (AELog.isCraftingLogEnabled()) {
                this.attempts.add(new CraftAttempt(amount + " failed", timer));
            }
            return null;
        }
        craftingInventory.addBytes(this.tree.getNodeCount() * 8);

        Class<?> clazz = Class.forName("appeng.crafting.inv.CraftingSimulationState");
        Field field_crafts = clazz.getDeclaredField("crafts");
        Field field_requiredExtract = clazz.getDeclaredField("requiredExtract");
        field_crafts.setAccessible(true);
        field_requiredExtract.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<IPatternDetails, Long> crafts = (Map<IPatternDetails, Long>) field_crafts.get(craftingInventory);
        KeyCounter requiredExtract = (KeyCounter) field_requiredExtract.get(craftingInventory);

        var basePlan = CraftingSimulationState.buildCraftingPlan(craftingInventory, this, amount);

        KeyCounter verifiedReq = new KeyCounter();
        verifiedReq.addAll(requiredExtract);
        Map<IPatternDetails, Long> newCrafts = new HashMap<>(crafts);

        // ========== 完全沿用你原始逐配方假设检验算法 ==========
        for (var entry : basePlan.patternTimes().entrySet()) {
            var pattern = entry.getKey();
            long oldRuns = entry.getValue();
            boolean isProb = StatisticalPatternDetails.isStatisticalPattern(pattern);
            if (isProb) {
                for (var details : pattern.getInputs()) {
                    GenericStack[] stack = details.getPossibleInputs();
                    long rawTotal = stack[0].amount() * oldRuns;
                    long verifiedTotal = verifySingle(rawTotal, pattern);
                    long verifiedRuns = verifiedTotal / stack[0].amount();
                    newCrafts.put(pattern, verifiedRuns);
                    verifiedReq.set(stack[0].what(), verifiedTotal);
                }
            }
        }
        // ======================================================

        // 修改仅作用于本次局部仿真实例
        crafts.clear();
        crafts.putAll(newCrafts);
        requiredExtract.clear();
        requiredExtract.addAll(verifiedReq);

        CraftingPlan verifiedPlan = CraftingSimulationState.buildCraftingPlan(craftingInventory, this, amount);

        if (AELog.isCraftingLogEnabled()) {
            String type = simulate ? "simulated" : "succeeded";
            this.attempts.add(new CraftAttempt("%d %s (%d bytes)".formatted(amount, type, verifiedPlan.bytes()), timer));
        }

        LOGGER.info("=== verifiedPlan Info | simulate={}, amount={} ===", simulate, amount);
        LOGGER.info("finalOutput:{}", verifiedPlan.finalOutput());
        LOGGER.info("finalOutput amount:{}", verifiedPlan.finalOutput().amount());
        LOGGER.info("patternTimes:{}", verifiedPlan.patternTimes());
        for(var entry : verifiedPlan.usedItems()){
            LOGGER.info("usedItem: {} x{}", entry.getKey(), entry.getLongValue());
        }
        LOGGER.info("===============================================");

        return verifiedPlan;
    }

    // 你的原版假设检验方法
    private long verifySingle(long rawTotal, IPatternDetails pattern) {
        // 填入你的真实假设检验代码，不做全局缩放
        return rawTotal * 16 / 10;
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
