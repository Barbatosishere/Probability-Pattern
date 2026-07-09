package com.example.statpatterns.terminal;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

import com.example.statpatterns.SPBlockEntities;

public class ProbabilityPatternTerminalBlockEntity extends BlockEntity implements MenuProvider {
    public static final int INPUT_SLOT_START = 0;
    public static final int INPUT_SLOT_COUNT = 9;
    public static final int OUTPUT_SLOT = 9;
    public static final int PATTERN_SLOT = 10;
    public static final int SLOT_COUNT = 11;

    private int targetAmount = 1000;
    private int successProbabilityPermyriad = 8000;
    private int alphaPermyriad = 500;

    private final ItemStackHandler inventory = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public ProbabilityPatternTerminalBlockEntity(BlockPos pos, BlockState blockState) {
        super(SPBlockEntities.PROBABILITY_PATTERN_TERMINAL.get(), pos, blockState);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public int getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(int targetAmount) {
        this.targetAmount = Math.clamp(targetAmount, 1, 1_000_000);
        setChanged();
    }

    public int getSuccessProbabilityPermyriad() {
        return successProbabilityPermyriad;
    }

    public void setSuccessProbabilityPermyriad(int successProbabilityPermyriad) {
        this.successProbabilityPermyriad = Math.clamp(successProbabilityPermyriad, 1, 10_000);
        setChanged();
    }

    public int getAlphaPermyriad() {
        return alphaPermyriad;
    }

    public void setAlphaPermyriad(int alphaPermyriad) {
        this.alphaPermyriad = Math.clamp(alphaPermyriad, 1, 9999);
        setChanged();
    }

    public void openMenu(ServerPlayer player) {
        player.openMenu(this, buffer -> buffer.writeBlockPos(worldPosition));
    }

    public void dropContents(Level level, BlockPos pos) {
        for (int i = 0; i < inventory.getSlots(); i++) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), inventory.getStackInSlot(i));
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.probabilitypattern.probability_pattern_terminal");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ProbabilityPatternTerminalMenu(containerId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.putInt("targetAmount", targetAmount);
        tag.putInt("successProbabilityPermyriad", successProbabilityPermyriad);
        tag.putInt("alphaPermyriad", alphaPermyriad);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        targetAmount = tag.getInt("targetAmount");
        successProbabilityPermyriad = tag.getInt("successProbabilityPermyriad");
        alphaPermyriad = tag.getInt("alphaPermyriad");
    }
}
