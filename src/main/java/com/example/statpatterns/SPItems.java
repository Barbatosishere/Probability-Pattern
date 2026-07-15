package com.example.statpatterns;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.items.parts.PartItem;

import com.example.statpatterns.crafting.StatisticalPatternDetails;
import com.example.statpatterns.terminal.ProbabilityPatternTerminalPart;

public final class SPItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ProbabilityPatternMod.MOD_ID);

    public static final DeferredItem<Item> PROBABILITY_PATTERN = ITEMS.register("probability_pattern",
            () -> PatternDetailsHelper.encodedPatternItemBuilder(StatisticalPatternDetails::decode)
                    .itemProperties(new Item.Properties().stacksTo(1))
                    .invalidPatternTooltip(StatisticalPatternDetails::getInvalidPatternTooltip)
                    .build());

    public static final DeferredItem<PartItem<ProbabilityPatternTerminalPart>> PROBABILITY_PATTERN_TERMINAL =
            ITEMS.register("probability_pattern_terminal",
                    () -> new PartItem<>(
                            new Item.Properties(),
                            ProbabilityPatternTerminalPart.class,
                            ProbabilityPatternTerminalPart::new));

    private SPItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
