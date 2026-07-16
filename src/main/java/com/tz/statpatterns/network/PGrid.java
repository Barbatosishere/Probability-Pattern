package com.tz.statpatterns.network;

import java.io.IOException;
import java.util.Set;

import com.google.gson.stream.JsonWriter;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridService;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.events.GridEvent;

public final class PGrid implements IGrid {
    private final IGrid delegate;
    private final ICraftingService craftingService;

    public PGrid(IGrid delegate) {
        this.delegate = delegate;
        this.craftingService = new PCraftingService(this, delegate.getCraftingService());
    }

    @Override
    public <C extends IGridService> C getService(Class<C> iface) {
        if (iface == ICraftingService.class) {
            return iface.cast(craftingService);
        }
        return delegate.getService(iface);
    }

    @Override
    public <T extends GridEvent> T postEvent(T ev) {
        return delegate.postEvent(ev);
    }

    @Override
    public Iterable<Class<?>> getMachineClasses() {
        return delegate.getMachineClasses();
    }

    @Override
    public Iterable<IGridNode> getMachineNodes(Class<?> machineClass) {
        return delegate.getMachineNodes(machineClass);
    }

    @Override
    public <T> Set<T> getMachines(Class<T> machineClass) {
        return delegate.getMachines(machineClass);
    }

    @Override
    public <T> Set<T> getActiveMachines(Class<T> machineClass) {
        return delegate.getActiveMachines(machineClass);
    }

    @Override
    public Iterable<IGridNode> getNodes() {
        return delegate.getNodes();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public IGridNode getPivot() {
        return delegate.getPivot();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public void export(JsonWriter jsonWriter) throws IOException {
        delegate.export(jsonWriter);
    }
}
