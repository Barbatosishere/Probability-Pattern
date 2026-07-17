package com.tz.statpatterns.core.definition;

import appeng.core.definitions.ItemDefinition;
import com.google.common.base.Preconditions;
import com.tz.statpatterns.SPCreativeTabs;
import com.tz.statpatterns.api.ids.ItemIds;
import com.tz.statpatterns.api.ids.SPCreativeTabIds;
import com.tz.statpatterns.crafting.ProbabilityPatternItem;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.tz.statpatterns.crafting.StatisticalPatternDetails;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

import static com.tz.statpatterns.ProbabilityPatternMod.MOD_ID;

public final class SPItems {
    public static final DeferredRegister.Items DR = DeferredRegister.createItems(MOD_ID);

    private static final List<ItemDefinition<?>> ITEMS = new ArrayList<>();

    public static final ItemDefinition<Item> PROBABILITY_PATTERN = item("Probability Pattern", ItemIds.PROBABILITY_PATTERN, (p) -> new ProbabilityPatternItem(
            p.stacksTo(64),
            StatisticalPatternDetails::decode,
            StatisticalPatternDetails::getInvalidPatternTooltip));

    private SPItems() {
    }

    static <T extends Item> ItemDefinition<T> item(String name, ResourceLocation id, Function<Item.Properties, T> factory) {
        return item(name, id, factory, SPCreativeTabIds.MAIN);
    }

    static <T extends Item> ItemDefinition<T> item(String name, ResourceLocation id, Function<Item.Properties, T> factory, @Nullable ResourceKey<CreativeModeTab> group) {

        Item.Properties p = new Item.Properties();

        Preconditions.checkArgument(id.getNamespace().equals(MOD_ID), "Can only register for AE2");
        var definition = new ItemDefinition<>(name, DR.registerItem(id.getPath(), factory));

        if (group != null) {
            SPCreativeTabs.add(definition);
        }

        ITEMS.add(definition);

        return definition;
    }
}
