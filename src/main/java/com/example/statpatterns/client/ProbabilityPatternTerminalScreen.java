package com.example.statpatterns.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import com.example.statpatterns.math.ProbabilitySizing;
import com.example.statpatterns.terminal.ProbabilityPatternTerminalMenu;

public class ProbabilityPatternTerminalScreen extends AbstractContainerScreen<ProbabilityPatternTerminalMenu> {
    private static final ResourceLocation AE2_PATTERN_BACKGROUND = ResourceLocation.fromNamespaceAndPath(
            "ae2", "textures/guis/pattern.png");

    private static final Component INPUTS = Component.translatable("container.probabilitypattern.inputs");
    private static final Component OUTPUT = Component.translatable("container.probabilitypattern.output");
    private static final Component PATTERN = Component.translatable("container.probabilitypattern.pattern");
    private static final Component ENCODE = Component.translatable("container.probabilitypattern.encode");
    private static final Component PLAN = Component.translatable("container.probabilitypattern.plan");
    private static final Component INVALID = Component.translatable("container.probabilitypattern.invalid");

    private EditBox probabilityInput;
    private EditBox alphaInput;
    private EditBox targetInput;

    public ProbabilityPatternTerminalScreen(ProbabilityPatternTerminalMenu menu, Inventory playerInventory,
            Component title) {
        super(menu, playerInventory, title);
        imageWidth = 195;
        imageHeight = 197;
        titleLabelX = 8;
        titleLabelY = 6;
        inventoryLabelX = 17;
        inventoryLabelY = 104;
    }

    @Override
    protected void init() {
        super.init();

        probabilityInput = addInput(89, fromBottom(126), 38, "p", "%.0f".formatted(menu.getSuccessProbability() * 100.0));
        alphaInput = addInput(89, fromBottom(112), 38, "a", "%.0f".formatted(menu.getAlpha() * 100.0));
        targetInput = addInput(89, fromBottom(98), 46, "N", Integer.toString(menu.getTargetAmount()));

        addRenderableWidget(Button.builder(ENCODE, button -> click(ProbabilityPatternTerminalMenu.BUTTON_ENCODE))
                .bounds(leftPos + 147, topPos + fromBottom(145), 38, 18)
                .build());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(AE2_PATTERN_BACKGROUND, leftPos, topPos, imageWidth, 17, 0, 0, imageWidth, 17, 256, 256);
        guiGraphics.blit(AE2_PATTERN_BACKGROUND, leftPos, topPos + 17, imageWidth, 180, 0, 71, imageWidth, 180, 256,
                256);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, title, titleLabelX, titleLabelY, 0xE6EDF3, false);
        guiGraphics.drawString(font, INPUTS, 24, fromBottom(177), 0x404040, false);
        guiGraphics.drawString(font, OUTPUT, 107, fromBottom(177), 0x404040, false);
        guiGraphics.drawString(font, PATTERN, 145, fromBottom(137), 0x404040, false);
        guiGraphics.drawString(font, Component.literal("p%"), 77, fromBottom(125), 0x404040, false);
        guiGraphics.drawString(font, Component.literal("a%"), 77, fromBottom(111), 0x404040, false);
        guiGraphics.drawString(font, Component.literal("N"), 77, fromBottom(97), 0x404040, false);
        renderPlan(guiGraphics);
        guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void addButton(int x, int y, int width, int height, String label, int id) {
        addRenderableWidget(Button.builder(Component.literal(label), button -> click(id))
                .bounds(leftPos + x, topPos + y, width, height)
                .build());
    }

    private void click(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            if (id == ProbabilityPatternTerminalMenu.BUTTON_ENCODE) {
                syncInputFields();
            }
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    private static int fromBottom(int bottom) {
        return 197 - bottom;
    }

    private EditBox addInput(int x, int y, int width, String label, String value) {
        var input = new EditBox(font, leftPos + x, topPos + y, width, 12, Component.literal(label));
        input.setMaxLength(12);
        input.setValue(value);
        addRenderableWidget(input);
        return input;
    }

    private void syncInputFields() {
        var target = parseTarget(targetInput.getValue(), menu.getTargetAmount());
        var probability = parsePermyriad(probabilityInput.getValue(), menu.getSuccessProbability());
        var alpha = Math.min(9999, parsePermyriad(alphaInput.getValue(), menu.getAlpha()));
        clickRaw(ProbabilityPatternTerminalMenu.BUTTON_SET_TARGET_BASE + target);
        clickRaw(ProbabilityPatternTerminalMenu.BUTTON_SET_PROBABILITY_BASE + probability);
        clickRaw(ProbabilityPatternTerminalMenu.BUTTON_SET_ALPHA_BASE + alpha);
    }

    private void clickRaw(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    private void renderPlan(GuiGraphics guiGraphics) {
        try {
            var target = parseTarget(targetInput.getValue(), menu.getTargetAmount());
            var probability = parsePermyriad(probabilityInput.getValue(), menu.getSuccessProbability()) / 10_000.0;
            var alpha = parsePermyriad(alphaInput.getValue(), menu.getAlpha()) / 10_000.0;
            var sizing = ProbabilitySizing.planAttempts(target, probability, alpha, 30);
            guiGraphics.drawString(font, PLAN.copy().append(": " + sizing.attempts()), 77, fromBottom(83), 0x404040,
                    false);
        } catch (RuntimeException ignored) {
            guiGraphics.drawString(font, INVALID, 77, fromBottom(83), 0xAA0000, false);
        }
    }

    private static int parseTarget(String text, int fallback) {
        try {
            return Math.clamp(Integer.parseInt(text.trim()), 1, 1_000_000);
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    private static int parsePermyriad(String text, double fallback) {
        try {
            var cleaned = text.trim();
            if (cleaned.endsWith("%")) {
                cleaned = cleaned.substring(0, cleaned.length() - 1).trim();
            }
            var value = Double.parseDouble(cleaned);
            var probability = value > 1.0 ? value / 100.0 : value;
            return Math.clamp((int) Math.round(probability * 10_000.0), 1, 10_000);
        } catch (RuntimeException ignored) {
            return Math.clamp((int) Math.round(fallback * 10_000.0), 1, 10_000);
        }
    }
}
