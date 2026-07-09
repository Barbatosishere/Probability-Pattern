package com.example.statpatterns.network;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import com.google.common.collect.ImmutableSet;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.level.Level;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.crafting.ICraftingSimulationRequester;
import appeng.api.networking.crafting.ICraftingSubmitResult;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.AEKeyFilter;
import appeng.crafting.CraftingCalculation;

public final class PCraftingService implements ICraftingService {
    private static final ExecutorService CRAFTING_POOL;

    static {
        ThreadFactory factory = runnable -> {
            var thread = new Thread(runnable, "Probability Pattern Crafting Calculator");
            thread.setDaemon(true);
            return thread;
        };
        CRAFTING_POOL = Executors.newCachedThreadPool(factory);
    }

    private final IGrid grid;
    private final ICraftingService delegate;

    public PCraftingService(IGrid grid, ICraftingService delegate) {
        this.grid = grid;
        this.delegate = delegate;
    }

    @Override
    public Future<ICraftingPlan> beginCraftingCalculation(Level level, ICraftingSimulationRequester simRequester,
            AEKey craftWhat, long amount, CalculationStrategy strategy) {
        if (level == null || simRequester == null) {
            throw new IllegalArgumentException("Invalid Crafting Job Request");
        }

        var job = new CraftingCalculation(level, grid, simRequester, new GenericStack(craftWhat, amount), strategy);
        return CRAFTING_POOL.submit(() -> {
            try (var ignored = ProbabilityCraftingContext.push(craftWhat, amount)) {
                return job.run();
            }
        });
    }

    @Override
    public Collection<IPatternDetails> getCraftingFor(AEKey whatToCraft) {
        return delegate.getCraftingFor(whatToCraft);
    }

    @Override
    public void refreshNodeCraftingProvider(IGridNode node) {
        delegate.refreshNodeCraftingProvider(node);
    }

    @Override
    public void addGlobalCraftingProvider(ICraftingProvider cc) {
        delegate.addGlobalCraftingProvider(cc);
    }

    @Override
    public void removeGlobalCraftingProvider(ICraftingProvider cc) {
        delegate.removeGlobalCraftingProvider(cc);
    }

    @Override
    public void refreshGlobalCraftingProvider(ICraftingProvider provider) {
        delegate.refreshGlobalCraftingProvider(provider);
    }

    @Nullable
    @Override
    public AEKey getFuzzyCraftable(AEKey whatToCraft, AEKeyFilter filter) {
        return delegate.getFuzzyCraftable(whatToCraft, filter);
    }

    @Override
    public ICraftingSubmitResult submitJob(ICraftingPlan job, @Nullable ICraftingRequester requestingMachine,
            @Nullable ICraftingCPU target, boolean prioritizePower, IActionSource src) {
        return delegate.submitJob(job, requestingMachine, target, prioritizePower, src);
    }

    @Override
    public ImmutableSet<ICraftingCPU> getCpus() {
        return delegate.getCpus();
    }

    @Override
    public boolean canEmitFor(AEKey what) {
        return delegate.canEmitFor(what);
    }

    @Override
    public Set<AEKey> getCraftables(AEKeyFilter filter) {
        return delegate.getCraftables(filter);
    }

    @Override
    public boolean isRequesting(AEKey what) {
        return delegate.isRequesting(what);
    }

    @Override
    public long getRequestedAmount(AEKey what) {
        return delegate.getRequestedAmount(what);
    }

    @Override
    public boolean isRequestingAny() {
        return delegate.isRequestingAny();
    }
}
