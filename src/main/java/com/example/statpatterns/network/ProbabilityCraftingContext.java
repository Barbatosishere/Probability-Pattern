package com.example.statpatterns.network;

import org.jetbrains.annotations.Nullable;

import appeng.api.stacks.AEKey;

public final class ProbabilityCraftingContext implements AutoCloseable {
    private static final ThreadLocal<Request> CURRENT = new ThreadLocal<>();

    private final Request previous;

    private ProbabilityCraftingContext(Request request) {
        this.previous = CURRENT.get();
        CURRENT.set(request);
    }

    public static ProbabilityCraftingContext push(AEKey what, long amount) {
        return new ProbabilityCraftingContext(new Request(what, amount));
    }

    @Nullable
    public static Request current() {
        return CURRENT.get();
    }

    @Override
    public void close() {
        if (previous == null) {
            CURRENT.remove();
        } else {
            CURRENT.set(previous);
        }
    }

    public record Request(AEKey what, long amount) {
    }
}
