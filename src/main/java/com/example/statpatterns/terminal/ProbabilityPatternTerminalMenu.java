package com.example.statpatterns.terminal;

import java.util.ArrayList;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;

import com.example.statpatterns.SPBlocks;
import com.example.statpatterns.SPItems;
import com.example.statpatterns.SPMenus;
import com.example.statpatterns.crafting.StatisticalPatternDetails;

public class ProbabilityPatternTerminalMenu extends AbstractContainerMenu {
    public static final int BUTTON_ENCODE = 0;
    public static final int BUTTON_PROBABILITY_DOWN = 1;
    public static final int BUTTON_PROBABILITY_UP = 2;
    public static final int BUTTON_ALPHA_DOWN = 3;
    public static final int BUTTON_ALPHA_UP = 4;
    public static final int BUTTON_TARGET_DOWN = 5;
    public static final int BUTTON_TARGET_UP = 6;
    public static final int BUTTON_SET_PROBABILITY_BASE = 10_000;
    public static final int BUTTON_SET_ALPHA_BASE = 30_000;
    public static final int BUTTON_SET_TARGET_BASE = 100_000;

    private static final int PROBABILITY_STEP = 500;
    private static final int ALPHA_STEP = 100;
    private static final int TARGET_STEP = 64;
    private static final int SCREEN_HEIGHT = 197;

    private final ProbabilityPatternTerminalBlockEntity host;
    private final ContainerLevelAccess access;

    public ProbabilityPatternTerminalMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, playerInventory, getHost(playerInventory, buffer.readBlockPos()));
    }

    public ProbabilityPatternTerminalMenu(int containerId, Inventory playerInventory,
            ProbabilityPatternTerminalBlockEntity host) {
        super(SPMenus.PROBABILITY_PATTERN_TERMINAL.get(), containerId);
        this.host = host;
        this.access = ContainerLevelAccess.create(host.getLevel(), host.getBlockPos());

        var inventory = host.getInventory();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                addSlot(new SlotItemHandler(inventory,
                        ProbabilityPatternTerminalBlockEntity.INPUT_SLOT_START + row * 3 + col,
                        24 + col * 18,
                        fromBottom(158) + row * 18));
            }
        }

        addSlot(new SlotItemHandler(inventory, ProbabilityPatternTerminalBlockEntity.OUTPUT_SLOT, 109, fromBottom(158)));
        addSlot(new SlotItemHandler(inventory, ProbabilityPatternTerminalBlockEntity.PATTERN_SLOT, 147, fromBottom(118)) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(SPItems.PROBABILITY_PATTERN.get());
            }
        });

        addPlayerInventory(playerInventory, 17, 115);
        addDataSlots(new HostData());
    }

    public int getTargetAmount() {
        return host.getTargetAmount();
    }

    public double getSuccessProbability() {
        return host.getSuccessProbabilityPermyriad() / 10_000.0;
    }

    public double getAlpha() {
        return host.getAlphaPermyriad() / 10_000.0;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id >= BUTTON_SET_TARGET_BASE && id <= BUTTON_SET_TARGET_BASE + 1_000_000) {
            host.setTargetAmount(id - BUTTON_SET_TARGET_BASE);
            broadcastChanges();
            return true;
        }
        if (id >= BUTTON_SET_ALPHA_BASE && id <= BUTTON_SET_ALPHA_BASE + 9_999) {
            host.setAlphaPermyriad(id - BUTTON_SET_ALPHA_BASE);
            broadcastChanges();
            return true;
        }
        if (id >= BUTTON_SET_PROBABILITY_BASE && id <= BUTTON_SET_PROBABILITY_BASE + 10_000) {
            host.setSuccessProbabilityPermyriad(id - BUTTON_SET_PROBABILITY_BASE);
            broadcastChanges();
            return true;
        }

        switch (id) {
            case BUTTON_ENCODE -> encodePattern();
            case BUTTON_PROBABILITY_DOWN -> host.setSuccessProbabilityPermyriad(
                    host.getSuccessProbabilityPermyriad() - PROBABILITY_STEP);
            case BUTTON_PROBABILITY_UP -> host.setSuccessProbabilityPermyriad(
                    host.getSuccessProbabilityPermyriad() + PROBABILITY_STEP);
            case BUTTON_ALPHA_DOWN -> host.setAlphaPermyriad(host.getAlphaPermyriad() - ALPHA_STEP);
            case BUTTON_ALPHA_UP -> host.setAlphaPermyriad(host.getAlphaPermyriad() + ALPHA_STEP);
            case BUTTON_TARGET_DOWN -> host.setTargetAmount(host.getTargetAmount() - TARGET_STEP);
            case BUTTON_TARGET_UP -> host.setTargetAmount(host.getTargetAmount() + TARGET_STEP);
            default -> {
                return false;
            }
        }
        broadcastChanges();
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        var slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        var source = slot.getItem();
        var copy = source.copy();
        if (index < ProbabilityPatternTerminalBlockEntity.SLOT_COUNT) {
            if (!moveItemStackTo(source, ProbabilityPatternTerminalBlockEntity.SLOT_COUNT, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (source.is(SPItems.PROBABILITY_PATTERN.get())) {
            if (!moveItemStackTo(source, ProbabilityPatternTerminalBlockEntity.PATTERN_SLOT,
                    ProbabilityPatternTerminalBlockEntity.PATTERN_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(source, ProbabilityPatternTerminalBlockEntity.INPUT_SLOT_START,
                ProbabilityPatternTerminalBlockEntity.OUTPUT_SLOT + 1, false)) {
            return ItemStack.EMPTY;
        }

        if (source.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, SPBlocks.PROBABILITY_PATTERN_TERMINAL.get());
    }

    private void encodePattern() {
        var inventory = host.getInventory();
        var pattern = inventory.getStackInSlot(ProbabilityPatternTerminalBlockEntity.PATTERN_SLOT);
        var output = inventory.getStackInSlot(ProbabilityPatternTerminalBlockEntity.OUTPUT_SLOT);
        if (!pattern.is(SPItems.PROBABILITY_PATTERN.get()) || output.isEmpty()) {
            return;
        }

        var inputs = new ArrayList<GenericStack>();
        for (int i = 0; i < ProbabilityPatternTerminalBlockEntity.INPUT_SLOT_COUNT; i++) {
            var input = inventory.getStackInSlot(ProbabilityPatternTerminalBlockEntity.INPUT_SLOT_START + i);
            if (!input.isEmpty()) {
                inputs.add(new GenericStack(AEItemKey.of(input), input.getCount()));
            }
        }
        if (inputs.isEmpty()) {
            return;
        }

        var encoded = StatisticalPatternDetails.encode(
                inputs,
                new GenericStack(AEItemKey.of(output), host.getTargetAmount()),
                getSuccessProbability(),
                getAlpha());
        inventory.setStackInSlot(ProbabilityPatternTerminalBlockEntity.PATTERN_SLOT, encoded);
    }

    private void addPlayerInventory(Inventory playerInventory, int x, int y) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, x + col * 18, y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, x + col * 18, y + 58));
        }
    }

    private static int fromBottom(int bottom) {
        return SCREEN_HEIGHT - bottom;
    }

    private static ProbabilityPatternTerminalBlockEntity getHost(Inventory inventory, net.minecraft.core.BlockPos pos) {
        if (inventory.player.level().getBlockEntity(pos) instanceof ProbabilityPatternTerminalBlockEntity host) {
            return host;
        }
        throw new IllegalStateException("Missing probability pattern terminal at " + pos);
    }

    private final class HostData implements ContainerData {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> host.getTargetAmount();
                case 1 -> host.getSuccessProbabilityPermyriad();
                case 2 -> host.getAlphaPermyriad();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> host.setTargetAmount(value);
                case 1 -> host.setSuccessProbabilityPermyriad(value);
                case 2 -> host.setAlphaPermyriad(value);
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
