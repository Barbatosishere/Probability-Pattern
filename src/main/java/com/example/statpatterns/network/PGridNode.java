package com.example.statpatterns.network;

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
