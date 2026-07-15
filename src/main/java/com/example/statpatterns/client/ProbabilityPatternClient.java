package com.example.statpatterns.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import appeng.client.gui.style.StyleManager;

import com.example.statpatterns.ProbabilityPatternMod;
import com.example.statpatterns.SPMenus;
import com.example.statpatterns.terminal.ProbabilityPatternTerminalMenu;

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

