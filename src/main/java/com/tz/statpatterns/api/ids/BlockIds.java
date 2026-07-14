package com.tz.statpatterns.api.ids;

import net.minecraft.resources.ResourceLocation;

import static com.tz.statpatterns.ProbabilityPatternMod.MOD_ID;

public class BlockIds {
    public static final ResourceLocation PROBABILITY_PATTERN_PROVIDER = id ("probability_pattern_provider");
    private static ResourceLocation id(String id) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, id);
    }
}
