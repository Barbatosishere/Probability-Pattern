package com.example.statpatterns.terminal;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

import appeng.api.networking.IGridNode;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.parts.PartModel;
import appeng.parts.encoding.PatternEncodingTerminalPart;

import com.example.statpatterns.ProbabilityPatternMod;
import com.example.statpatterns.SPMenus;
import com.example.statpatterns.network.PGridNode;

public class ProbabilityPatternTerminalPart extends PatternEncodingTerminalPart {
    public static final ResourceLocation MODEL_OFF = ProbabilityPatternMod.id("part/probability_pattern_terminal_off");
    public static final ResourceLocation MODEL_ON = ProbabilityPatternMod.id("part/probability_pattern_terminal_on");

    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, MODEL_OFF, MODEL_STATUS_OFF);
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_ON);
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_HAS_CHANNEL);

    public ProbabilityPatternTerminalPart(IPartItem<?> partItem) {
        super(partItem);
    }

    @Override
    public MenuType<?> getMenuType(Player player) {
        return SPMenus.PROBABILITY_PATTERN_TERMINAL.get();
    }

    @Override
    public IPartModel getStaticModels() {
        return selectModel(MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL);
    }

    @Override
    public IGridNode getActionableNode() {
        var original = super.getActionableNode();
        if (original == null) {
            return null;
        }
        return new PGridNode(original);
    }
}
