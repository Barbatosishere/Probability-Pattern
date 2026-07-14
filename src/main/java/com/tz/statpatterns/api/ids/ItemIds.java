package com.tz.statpatterns.api.ids;

import net.minecraft.resources.ResourceLocation;

import static com.tz.statpatterns.ProbabilityPatternMod.MOD_ID;

public class ItemIds {
    public static final ResourceLocation PROBABILITY_PATTERN_TERMINAL = id ("probability_pattern_terminal");
    public static final ResourceLocation PROBABILITY_PATTERN_PROVIDER_PART = id ("probability_pattern_provider_part");
    public static final ResourceLocation PROBABILITY_PATTERN = id ("probability_pattern");
    private static ResourceLocation id(String id) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, id);
    }
}
