package com.tz.statpatterns;

import java.util.List;

import com.mojang.logging.LogUtils;
import com.tz.statpatterns.api.ids.Components;
import com.tz.statpatterns.core.definition.SPBlockEntities;
import com.tz.statpatterns.core.definition.SPBlocks;
import com.tz.statpatterns.core.definition.SPItems;
import com.tz.statpatterns.core.definition.SPParts;
import com.tz.statpatterns.init.InitCapabilityProviders;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

import appeng.api.parts.PartModels;
import org.slf4j.Logger;

@Mod(ProbabilityPatternMod.MOD_ID)
public final class ProbabilityPatternMod {
    public static final String MOD_ID = "probabilitypattern";
    public static final Logger LOGGER = LogUtils.getLogger();
    public ProbabilityPatternMod(IEventBus modEventBus) {
        PartModels.registerModels(List.of(
                id("part/probability_pattern_terminal_off"),
                id("part/probability_pattern_terminal_on")));


        SPParts.init();
        SPBlocks.DR.register(modEventBus);
        SPBlockEntities.DR.register(modEventBus);
        SPItems.DR.register(modEventBus);
        Components.DR.register(modEventBus);

        SPMenus.register(modEventBus);
        SPCreativeTabs.register(modEventBus);

        modEventBus.addListener(InitCapabilityProviders::register);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
