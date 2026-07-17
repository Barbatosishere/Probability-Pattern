
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
