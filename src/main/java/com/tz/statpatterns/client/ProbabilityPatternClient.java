package com.tz.statpatterns.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import appeng.client.gui.style.StyleManager;

import com.tz.statpatterns.ProbabilityPatternMod;
import com.tz.statpatterns.core.definition.SPItems;
import com.tz.statpatterns.SPMenus;
import com.tz.statpatterns.menu.ProbabilityPatternTerminalMenu;

@EventBusSubscriber(modid = ProbabilityPatternMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ProbabilityPatternClient {
    private ProbabilityPatternClient() {
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.<ProbabilityPatternTerminalMenu, ProbabilityPatternTerminalScreen>register(
                SPMenus.PROBABILITY_PATTERN_TERMINAL.get(),
                (menu, playerInventory, title) -> new ProbabilityPatternTerminalScreen(
                        menu,
                        playerInventory,
                        title,
                        StyleManager.loadStyleDoc("/screens/terminals/pattern_encoding_terminal.json")));
    }
}

