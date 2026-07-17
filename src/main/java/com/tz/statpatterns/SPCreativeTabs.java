package com.tz.statpatterns;

import appeng.core.definitions.AEItems;
import appeng.core.definitions.ItemDefinition;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

public final class SPCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ProbabilityPatternMod.MOD_ID);

    private static final List<ItemDefinition<?>> itemDefs = new ArrayList<>();

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = CREATIVE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.probabilitypattern"))
                    .icon(() -> new ItemStack(AEItems.PROCESSING_PATTERN.get()))
                    .displayItems((parameters, output) -> {
                        for (var itemDefinition : itemDefs) {
                            output.accept(itemDefinition);
                        }
                    })
                    .build());

    public static void add(ItemDefinition<?> itemDef) {
        itemDefs.add(itemDef);
    }

    private SPCreativeTabs() {
    }
}
