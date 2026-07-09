package com.example.statpatterns;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.example.statpatterns.terminal.ProbabilityPatternTerminalMenu;

public final class SPMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister
            .create(Registries.MENU, ProbabilityPatternMod.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<ProbabilityPatternTerminalMenu>> PROBABILITY_PATTERN_TERMINAL =
            MENUS.register("probability_pattern_terminal",
                    () -> IMenuTypeExtension.create(ProbabilityPatternTerminalMenu::new));

    private SPMenus() {
    }

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}
