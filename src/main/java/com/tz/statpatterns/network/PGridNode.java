
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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.minecraft.CrashReportCategory;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeService;
import appeng.api.networking.IGridVisitor;
import appeng.api.stacks.AEItemKey;
import appeng.api.util.AEColor;

public final class PGridNode implements IGridNode {
    private final IGridNode delegate;
    private final IGrid grid;

    public PGridNode(IGridNode delegate) {
        this.delegate = delegate;
        this.grid = new PGrid(delegate.getGrid());
    }

    @Nullable
    @Override
    public <T extends IGridNodeService> T getService(Class<T> serviceClass) {
        return delegate.getService(serviceClass);
    }

    @Override
    public Object getOwner() {
        return delegate.getOwner();
    }

    @Override
    public void beginVisit(IGridVisitor visitor) {
        delegate.beginVisit(visitor);
    }

    @Override
    public IGrid getGrid() {
        return grid;
    }

    @Override
    public ServerLevel getLevel() {
        return delegate.getLevel();
    }

    @Override
    public Set<Direction> getConnectedSides() {
        return delegate.getConnectedSides();
    }

    @Override
    public Map<Direction, IGridConnection> getInWorldConnections() {
        return delegate.getInWorldConnections();
    }

    @Override
    public List<IGridConnection> getConnections() {
        return delegate.getConnections();
    }

    @Override
    public boolean hasGridBooted() {
        return delegate.hasGridBooted();
    }

    @Override
    public boolean isPowered() {
        return delegate.isPowered();
    }

    @Override
    public boolean meetsChannelRequirements() {
        return delegate.meetsChannelRequirements();
    }

    @Override
    public boolean hasFlag(GridFlags flag) {
        return delegate.hasFlag(flag);
    }

    @Override
    public int getOwningPlayerId() {
        return delegate.getOwningPlayerId();
    }

    @Nullable
    @Override
    public UUID getOwningPlayerProfileId() {
        return delegate.getOwningPlayerProfileId();
    }

    @Override
    public double getIdlePowerUsage() {
        return delegate.getIdlePowerUsage();
    }

    @Nullable
    @Override
    public AEItemKey getVisualRepresentation() {
        return delegate.getVisualRepresentation();
    }

    @Override
    public AEColor getGridColor() {
        return delegate.getGridColor();
    }

    @Override
    public void fillCrashReportCategory(CrashReportCategory category) {
        delegate.fillCrashReportCategory(category);
    }

    @Override
    public int getMaxChannels() {
        return delegate.getMaxChannels();
    }

    @Override
    public int getUsedChannels() {
        return delegate.getUsedChannels();
    }
}
