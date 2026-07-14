package com.tz.statpatterns;

import java.util.function.Consumer;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.tz.statpatterns.crafting.EncodedStatisticalPattern;

public final class SPComponents {
    private static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister
            .create(Registries.DATA_COMPONENT_TYPE, ProbabilityPatternMod.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<EncodedStatisticalPattern>> ENCODED_STATISTICAL_PATTERN =
            register("encoded_statistical_pattern",
                    builder -> builder.persistent(EncodedStatisticalPattern.CODEC)
                            .networkSynchronized(EncodedStatisticalPattern.STREAM_CODEC));

    private SPComponents() {
    }

    public static void register(IEventBus modEventBus) {
        COMPONENTS.register(modEventBus);
    }

    private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String name,
            Consumer<DataComponentType.Builder<T>> customizer) {
        var builder = DataComponentType.<T>builder();
        customizer.accept(builder);
        var componentType = builder.build();
        return COMPONENTS.register(name, () -> componentType);
    }
}
