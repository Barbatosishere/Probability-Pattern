package com.tz.statpatterns.blockentity.crafting;

import appeng.api.networking.*;
import appeng.api.orientation.BlockOrientation;
import appeng.api.stacks.AEItemKey;
import appeng.api.util.AECableType;
import appeng.block.AEBaseEntityBlock;
import appeng.block.crafting.PushDirection;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.me.helpers.BlockEntityNodeListener;
import appeng.me.helpers.IGridConnectedBlockEntity;
import appeng.util.SettingsFrom;
import com.tz.statpatterns.api.ids.Components;
import com.tz.statpatterns.block.ProbabilityPatternProviderBlock;
import com.tz.statpatterns.core.definition.SPBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import appeng.api.networking.IGridNodeListener;


public class ProbabilityPatternProviderBlockEntity extends AEBaseBlockEntity implements PatternProviderLogicHost, IGridConnectedBlockEntity {
    private final IManagedGridNode mainNode = createMainNode()
            .setVisualRepresentation(getItemFromBlockEntity())
            .setInWorldNode(true)
            .setTagName("proxy");

    protected IManagedGridNode createMainNode() {
        return GridHelper.createManagedNode(this, BlockEntityNodeListener.INSTANCE);
    }

    public final IManagedGridNode getMainNode() {
        return this.mainNode;
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        this.getMainNode().destroy();
    }

    @Override
    protected void onOrientationChanged(BlockOrientation orientation) {
        super.onOrientationChanged(orientation);
        onGridConnectableSidesChanged();
    }

    /**
     * Call when the return value {@link IGridConnectedBlockEntity#getGridConnectableSides(BlockOrientation)} has
     * changed, to update the grid nodes exposed sides.
     */
    protected final void onGridConnectableSidesChanged() {
        getMainNode().setExposedOnSides(getGridConnectableSides(getOrientation()));
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        this.getMainNode().destroy();
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        scheduleInit(); // Required for onReady to be called
    }
    protected final PatternProviderLogic logic = createLogic();

    public ProbabilityPatternProviderBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        onGridConnectableSidesChanged();
    }

    protected PatternProviderLogic createLogic() {
        return new PatternProviderLogic(getMainNode(), this);
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        this.logic.onMainNodeStateChanged();
    }

    private PushDirection getPushDirection() {
        return getBlockState().getValue(ProbabilityPatternProviderBlock.PUSH_DIRECTION);
    }

    @Override
    public Set<Direction> getGridConnectableSides(BlockOrientation orientation) {
        // In omnidirectional mode, every side is grid-connectable
        var pushDirection = getPushDirection().getDirection();
        if (pushDirection == null) {
            return EnumSet.allOf(Direction.class);
        }
        //ProbabilityPatternMod.LOGGER.info("ProbabilityPatternProviderBlockEntity pushDirection {}", EnumSet.complementOf(EnumSet.of(pushDirection)));
        // Otherwise all sides *except* the target side are connectable
        return EnumSet.complementOf(EnumSet.of(pushDirection));
    }

    @Override
    public PatternProviderLogic getLogic() {
        //ProbabilityPatternMod.LOGGER.info("ProbabilityPatternProviderBlockEntity 调用 getLogic");
        return logic;
    }

    @Override
    public EnumSet<Direction> getTargets() {
        var pushDirection = getPushDirection();
        if (pushDirection.getDirection() == null) {
            return EnumSet.allOf(Direction.class);
        } else {
            return EnumSet.of(pushDirection.getDirection());
        }
    }

    @Override
    public AEItemKey getTerminalIcon() {
        return AEItemKey.of(SPBlocks.PROBABILITY_PATTERN_PROVIDER_BLOCK.stack());
    }

    @Override
    public void exportSettings(SettingsFrom mode, DataComponentMap.Builder builder, @Nullable Player player) {
        super.exportSettings(mode, builder, player);

        if (mode == SettingsFrom.MEMORY_CARD) {
            logic.exportSettings(builder);

            var pushDirection = getPushDirection();
            builder.set(Components.EXPORTED_PUSH_DIRECTION, pushDirection);
        }
    }

    @Override
    public void importSettings(SettingsFrom mode, DataComponentMap input, @Nullable Player player) {
        super.importSettings(mode, input, player);

        if (mode == SettingsFrom.MEMORY_CARD) {
            logic.importSettings(input, player);

            // Restore push direction blockstate
            var pushDirection = input.get(Components.EXPORTED_PUSH_DIRECTION);
            if (pushDirection != null) {
                var level = getLevel();
                if (level != null) {
                    level.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(ProbabilityPatternProviderBlock.PUSH_DIRECTION, pushDirection));
                }
            }
        }
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return SPBlocks.PROBABILITY_PATTERN_PROVIDER_BLOCK.stack();
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        this.logic.addDrops(drops);
    }

    @Override
    public void clearContent() {
        super.clearContent();
        this.logic.clearContent();
    }

    @Override
    public void onReady() {
        super.onReady();
        this.logic.updatePatterns();
        this.getMainNode().create(getLevel(), getBlockEntity().getBlockPos());

        // It is possible that the BlockState depends on the state of the BlockEntity,
        // which might be different after restoring the grid connection compared to
        // the state that was saved to disk. This ensures the BlockState after readying
        // the entity is up-to-date.
        BlockState currentState = getBlockState();
        if (currentState.getBlock() instanceof AEBaseEntityBlock<?> block) {
            BlockState newState = block.getBlockEntityBlockState(currentState, this);
            if (currentState != newState) {
                this.markForUpdate();
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        this.logic.writeToNBT(data, registries);
        this.getMainNode().saveToNBT(data);

    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        this.logic.readFromNBT(data, registries);
        this.getMainNode().loadFromNBT(data);
    }
    @Override
    public void setBlockState(BlockState state) {
        super.setBlockState(state);
        onGridConnectableSidesChanged();
    }
}