package com.example.statpatterns;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(ProbabilityPatternMod.MOD_ID)
public final class ProbabilityPatternMod {
    public static final String MOD_ID = "probabilitypattern";

    public ProbabilityPatternMod(IEventBus modEventBus) {
        SPComponents.register(modEventBus);
        SPItems.register(modEventBus);
        SPBlocks.register(modEventBus);
        SPBlockEntities.register(modEventBus);
        SPMenus.register(modEventBus);
        SPCreativeTabs.register(modEventBus);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
