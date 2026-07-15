package com.example.statpatterns;

import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

import appeng.api.parts.PartModels;

@Mod(ProbabilityPatternMod.MOD_ID)
public final class ProbabilityPatternMod {
    public static final String MOD_ID = "probabilitypattern";

    public ProbabilityPatternMod(IEventBus modEventBus) {
        PartModels.registerModels(List.of(
                id("part/probability_pattern_terminal_off"),
                id("part/probability_pattern_terminal_on")));

        SPComponents.register(modEventBus);
        SPItems.register(modEventBus);
        SPMenus.register(modEventBus);
        SPCreativeTabs.register(modEventBus);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
