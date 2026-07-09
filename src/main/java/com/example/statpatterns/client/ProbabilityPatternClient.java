package com.example.statpatterns.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import com.example.statpatterns.ProbabilityPatternMod;
import com.example.statpatterns.SPMenus;

@Mod(value = ProbabilityPatternMod.MOD_ID, dist = Dist.CLIENT)
public final class ProbabilityPatternClient {
    public ProbabilityPatternClient(IEventBus modEventBus) {
        modEventBus.addListener(ProbabilityPatternClient::registerScreens);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(SPMenus.PROBABILITY_PATTERN_TERMINAL.get(), ProbabilityPatternTerminalScreen::new);
    }
}
