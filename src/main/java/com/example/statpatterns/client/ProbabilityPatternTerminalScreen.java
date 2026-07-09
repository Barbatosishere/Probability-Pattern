package com.example.statpatterns.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.client.gui.me.items.PatternEncodingTermScreen;
import appeng.client.gui.style.ScreenStyle;

import com.example.statpatterns.terminal.ProbabilityPatternTerminalMenu;

public class ProbabilityPatternTerminalScreen extends PatternEncodingTermScreen<ProbabilityPatternTerminalMenu> {
    private static final int PROBABILITY_FIELD_WIDTH = 58;
    private static final int BATCH_FIELD_WIDTH = 48;
    private static final int FIELD_HEIGHT = 14;
    private static final int TITLE_TO_PROBABILITY_GAP = 16;
    private static final int LABEL_TO_FIELD_GAP = 4;
    private static final int FIELD_TO_LABEL_GAP = 10;
    private static final int INVENTORY_TITLE_GAP = 12;

    private final Inventory playerInventory;
    private EditBox probabilityField;
    private EditBox batchField;

    public ProbabilityPatternTerminalScreen(ProbabilityPatternTerminalMenu menu, Inventory playerInventory,
            Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.playerInventory = playerInventory;
    }

    @Override
    public void init() {
        super.init();

        probabilityField = new EditBox(font,
                probabilityFieldX(),
                fieldY(),
                PROBABILITY_FIELD_WIDTH,
                FIELD_HEIGHT,
                Component.translatable("gui.probabilitypattern.probability"));
        probabilityField.setMaxLength(8);
        probabilityField.setValue(formatProbability(menu.getProbability()));
        probabilityField.setResponder(value -> {
            var parsed = parseProbability(value);
            if (parsed != null) {
                menu.setProbability(parsed);
            }
        });
        addRenderableWidget(probabilityField);

        batchField = new EditBox(font,
                batchFieldX(),
                fieldY(),
                BATCH_FIELD_WIDTH,
                FIELD_HEIGHT,
                Component.translatable("gui.probabilitypattern.batch"));
        batchField.setMaxLength(7);
        batchField.setValue(Long.toString(menu.getTargetBatch()));
        batchField.setResponder(value -> {
            var parsed = parseBatch(value);
            if (parsed != null) {
                menu.setTargetBatch(parsed);
            }
        });
        addRenderableWidget(batchField);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (probabilityField != null) {
            probabilityField.setX(probabilityFieldX());
            probabilityField.setY(fieldY());
        }
        if (batchField != null) {
            batchField.setX(batchFieldX());
            batchField.setY(fieldY());
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        var probabilityLabel = Component.translatable("gui.probabilitypattern.probability");
        guiGraphics.drawString(font, probabilityLabel, probabilityLabelX(), labelY(), 0x404040, false);
        probabilityField.render(guiGraphics, mouseX, mouseY, partialTick);

        var batchLabel = Component.translatable("gui.probabilitypattern.batch");
        guiGraphics.drawString(font, batchLabel, batchLabelX(), labelY(), 0x404040, false);
        batchField.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (probabilityField != null && !probabilityField.isFocused()) {
            var current = formatProbability(menu.getProbability());
            if (!current.equals(probabilityField.getValue())) {
                probabilityField.setValue(current);
            }
        }
        if (batchField != null && !batchField.isFocused()) {
            var current = Long.toString(menu.getTargetBatch());
            if (!current.equals(batchField.getValue())) {
                batchField.setValue(current);
            }
        }
    }

    private int probabilityLabelX() {
        return leftPos + playerInventoryLeftX() + font.width(playerInventoryTitle) + TITLE_TO_PROBABILITY_GAP;
    }

    private int probabilityFieldX() {
        var label = Component.translatable("gui.probabilitypattern.probability");
        return probabilityLabelX() + font.width(label) + LABEL_TO_FIELD_GAP;
    }

    private int batchLabelX() {
        return probabilityFieldX() + PROBABILITY_FIELD_WIDTH + FIELD_TO_LABEL_GAP;
    }

    private int batchFieldX() {
        var label = Component.translatable("gui.probabilitypattern.batch");
        return batchLabelX() + font.width(label) + LABEL_TO_FIELD_GAP;
    }

    private int fieldY() {
        return labelY() - 3;
    }

    private int labelY() {
        return topPos + playerInventoryTopY() - INVENTORY_TITLE_GAP;
    }

    private int playerInventoryLeftX() {
        var minX = Integer.MAX_VALUE;
        for (var slot : menu.slots) {
            if (slot.container == playerInventory) {
                minX = Math.min(minX, slot.x);
            }
        }
        return minX == Integer.MAX_VALUE ? inventoryLabelX : minX;
    }

    private int playerInventoryTopY() {
        var minY = Integer.MAX_VALUE;
        for (var slot : menu.slots) {
            if (slot.container == playerInventory) {
                minY = Math.min(minY, slot.y);
            }
        }
        return minY == Integer.MAX_VALUE ? inventoryLabelY + 12 : minY;
    }

    private static String formatProbability(double probability) {
        return "%.4f".formatted(probability);
    }

    private static Double parseProbability(String value) {
        try {
            var parsed = Double.parseDouble(value.trim());
            if (parsed > 0.0 && parsed <= 1.0) {
                return parsed;
            }
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    private static Long parseBatch(String value) {
        try {
            var parsed = Long.parseLong(value.trim());
            if (parsed > 0) {
                return parsed;
            }
        } catch (NumberFormatException ignored) {
        }
        return null;
    }
}
