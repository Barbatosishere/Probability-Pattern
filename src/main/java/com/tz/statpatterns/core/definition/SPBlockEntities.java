package com.tz.statpatterns.core.definition;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.tz.statpatterns.ProbabilityPatternMod.MOD_ID;

public final class SPBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> DR = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);

    private SPBlockEntities() {
    }
}
