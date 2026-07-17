
/*
 * Probability Pattern for AE2
 * Copyright (C) 2026 TaoLe-si
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.tz.statpatterns.network;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Future;

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
import appeng.api.storage.AEKeyFilter;

public final class PCraftingService implements ICraftingService {

    private final ICraftingService delegate;

    public PCraftingService(IGrid grid, ICraftingService delegate) {
        this.delegate = delegate;
    }

    @Override
    public Future<ICraftingPlan> beginCraftingCalculation(Level level,
                                                          ICraftingSimulationRequester simRequester, AEKey craftWhat, long amount,
                                                          CalculationStrategy strategy) {
        return delegate.beginCraftingCalculation(level, simRequester, craftWhat, amount, strategy);
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
    public ICraftingSubmitResult submitJob(ICraftingPlan job,
                                           @Nullable ICraftingRequester requestingMachine, @Nullable ICraftingCPU target,
                                           boolean prioritizePower, IActionSource src) {
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
