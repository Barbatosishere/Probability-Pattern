package com.example.statpatterns;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.example.statpatterns.terminal.ProbabilityPatternTerminalBlockEntity;

public final class SPBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister
            .create(Registries.BLOCK_ENTITY_TYPE, ProbabilityPatternMod.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ProbabilityPatternTerminalBlockEntity>> PROBABILITY_PATTERN_TERMINAL =
            BLOCK_ENTITIES.register("probability_pattern_terminal",
                    () -> BlockEntityType.Builder.of(
                            ProbabilityPatternTerminalBlockEntity::new,
                            SPBlocks.PROBABILITY_PATTERN_TERMINAL.get()).build(null));

    private SPBlockEntities() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }
}
