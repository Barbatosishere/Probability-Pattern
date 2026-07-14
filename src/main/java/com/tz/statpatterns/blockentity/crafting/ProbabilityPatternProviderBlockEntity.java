package com.tz.statpatterns.blockentity.crafting;

import appeng.api.ids.AEComponents;
import appeng.api.networking.*;
import appeng.api.orientation.BlockOrientation;
import appeng.api.stacks.AEItemKey;
import appeng.api.util.AECableType;
import appeng.block.crafting.PatternProviderBlock;
import appeng.block.crafting.PushDirection;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.util.SettingsFrom;
import com.tz.statpatterns.ProbabilityPatternMod;
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


public class ProbabilityPatternProviderBlockEntity extends AENetworkedBlockEntity implements PatternProviderLogicHost {
    protected final PatternProviderLogic logic = createLogic();

    public ProbabilityPatternProviderBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    protected PatternProviderLogic createLogic() {
        return new PatternProviderLogic(this.getMainNode(), this);
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
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        this.logic.writeToNBT(data, registries);
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        this.logic.readFromNBT(data, registries);
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    @Override
    public PatternProviderLogic getLogic() {
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
    public void setBlockState(BlockState state) {
        super.setBlockState(state);
        onGridConnectableSidesChanged();

        ProbabilityPatternMod.LOGGER.info("ProbabilityPatternProviderBlockEntity setBlockState {}", state);
    }
}