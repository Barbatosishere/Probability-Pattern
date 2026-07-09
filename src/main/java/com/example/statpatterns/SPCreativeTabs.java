package com.example.statpatterns;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class SPCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister
            .create(Registries.CREATIVE_MODE_TAB, ProbabilityPatternMod.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = CREATIVE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.probabilitypattern"))
                    .icon(() -> SPItems.PROBABILITY_PATTERN.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(SPItems.PROBABILITY_PATTERN.get());
                        output.accept(SPBlocks.PROBABILITY_PATTERN_TERMINAL_ITEM.get());
                    })
                    .build());

    private SPCreativeTabs() {
    }

    public static void register(IEventBus modEventBus) {
        CREATIVE_TABS.register(modEventBus);
    }
}
