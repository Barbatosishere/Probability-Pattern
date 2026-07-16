package com.tz.statpatterns;

import java.util.List;

import com.tz.statpatterns.api.ids.Components;
import com.tz.statpatterns.core.definition.*;
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

        SPParts.init();
        SPBlockEntities.DR.register(modEventBus);
        Components.DR.register(modEventBus);
        SPItems.DR.register(modEventBus);
        SPBlocks.DR.register(modEventBus);
        SPMenus.register(modEventBus);
        SPCreativeTabs.CREATIVE_TABS.register(modEventBus);

    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
