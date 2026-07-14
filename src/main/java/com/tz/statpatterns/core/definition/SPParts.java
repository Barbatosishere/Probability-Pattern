package com.tz.statpatterns.core.definition;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartModels;
import appeng.core.definitions.ColoredItemDefinition;
import appeng.core.definitions.ItemDefinition;
import appeng.items.parts.ColoredPartItem;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;
import com.tz.statpatterns.api.ids.ItemIds;
import com.tz.statpatterns.part.ProbabilityPatternProviderPart;
import com.tz.statpatterns.part.ProbabilityPatternTerminalPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.tz.statpatterns.core.definition.SPItems.item;

public class SPParts {
    public static final List<ColoredItemDefinition<?>> COLORED_PARTS = new ArrayList<>();

    public static final ItemDefinition<PartItem<ProbabilityPatternTerminalPart>> ProbabilityPatternTerminalPart = createPart("Probability Pattern Terminal Part", ItemIds.PROBABILITY_PATTERN_TERMINAL, ProbabilityPatternTerminalPart.class, ProbabilityPatternTerminalPart::new);
    public static final ItemDefinition<PartItem<ProbabilityPatternProviderPart>> ProbabilityPatternProviderPart = createPart("Probability Pattern Provider Part", ItemIds.PROBABILITY_PATTERN_PROVIDER_PART, ProbabilityPatternProviderPart.class, ProbabilityPatternProviderPart::new);

    private static <T extends IPart> ItemDefinition<PartItem<T>> createPart(
            String englishName,
            ResourceLocation id,
            Class<T> partClass,
            Function<IPartItem<T>, T> factory) {

        PartModels.registerModels(PartModelsHelper.createModels(partClass));
        return item(englishName, id, props -> new PartItem<>(props, partClass, factory));
    }

    private static <T extends IPart> ItemDefinition<PartItem<T>> createCustomPartItem(
            String englishName,
            ResourceLocation id,
            Class<T> partClass,
            Function<Item.Properties, PartItem<T>> factory) {

        PartModels.registerModels(PartModelsHelper.createModels(partClass));
        return item(englishName, id, factory);
    }

    // Used to control in which order static constructors are called
    public static void init() {
    }
}
