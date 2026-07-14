package com.tz.statpatterns.part;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import appeng.api.networking.IGridNodeListener;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.util.AECableType;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.menu.locator.MenuLocators;
import appeng.util.SettingsFrom;
import com.tz.statpatterns.core.SP;
import com.tz.statpatterns.core.definition.SPParts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;

import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEItemKey;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.PatternProviderMenu;
import appeng.parts.AEBasePart;
import appeng.parts.PartModel;

import com.tz.statpatterns.ProbabilityPatternMod;
import com.tz.statpatterns.core.definition.SPBlocks;
import org.jetbrains.annotations.Nullable;

public class ProbabilityPatternProviderPart extends AEBasePart implements PatternProviderLogicHost {

    public static final ResourceLocation MODEL_BASE = ProbabilityPatternMod.id("part/pattern_provider_base");

    @PartModels
    public static final PartModel MODELS_OFF = new PartModel(MODEL_BASE, ProbabilityPatternMod.id("part/interface_off"));
    @PartModels
    public static final PartModel MODELS_ON = new PartModel(MODEL_BASE, ProbabilityPatternMod.id("part/interface_on"));
    @PartModels
    public static final PartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, ProbabilityPatternMod.id("part/interface_has_channel"));

    protected final PatternProviderLogic logic = createLogic();

    public ProbabilityPatternProviderPart(IPartItem<?> partItem) {
        super(partItem);
    }

    protected PatternProviderLogic createLogic() {
        return new PatternProviderLogic(this.getMainNode(), this);
    }

    @Override
    public void saveChanges() {
        getHost().markForSave();
    }

    @Override
    public AEItemKey getTerminalIcon() {
        return AEItemKey.of(getPartItem());
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        super.onMainNodeStateChanged(reason);
        this.logic.onMainNodeStateChanged();
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(2, 2, 14, 14, 14, 16);
        bch.addBox(5, 5, 12, 11, 11, 14);
    }

    @Override
    public void readFromNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.readFromNBT(data, registries);
        this.logic.readFromNBT(data, registries);
    }

    @Override
    public void writeToNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.writeToNBT(data, registries);
        this.logic.writeToNBT(data, registries);
    }

    @Override
    public void addToWorld() {
        super.addToWorld();
        this.logic.updatePatterns();
    }

    @Override
    public void addAdditionalDrops(List<ItemStack> drops, boolean wrenched) {
        super.addAdditionalDrops(drops, wrenched);
        this.logic.addDrops(drops);
    }

    @Override
    public void clearContent() {
        super.clearContent();
        logic.clearContent();
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 4;
    }

    @Override
    public void exportSettings(SettingsFrom mode, DataComponentMap.Builder builder) {
        super.exportSettings(mode, builder);

        if (mode == SettingsFrom.MEMORY_CARD) {
            logic.exportSettings(builder);
        }
    }

    @Override
    public void importSettings(SettingsFrom mode, DataComponentMap input, @Nullable Player player) {
        super.importSettings(mode, input, player);

        if (mode == SettingsFrom.MEMORY_CARD) {
            logic.importSettings(input, player);
        }
    }

    @Override
    public void onNeighborChanged(BlockGetter level, BlockPos pos, BlockPos neighbor) {
        logic.updateRedstoneState();
    }

    @Override
    public boolean onUseWithoutItem(Player p, Vec3 pos) {
        if (!p.getCommandSenderWorld().isClientSide()) {
            openMenu(p, MenuLocators.forPart(this));
        }
        return true;
    }

    @Override
    public PatternProviderLogic getLogic() {
        return logic;
    }

    @Override
    public EnumSet<Direction> getTargets() {
        return EnumSet.of(this.getSide());
    }

    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) {
            return MODELS_HAS_CHANNEL;
        } else if (this.isPowered()) {
            return MODELS_ON;
        } else {
            return MODELS_OFF;
        }
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return SPParts.ProbabilityPatternProviderPart.stack();
    }
}
