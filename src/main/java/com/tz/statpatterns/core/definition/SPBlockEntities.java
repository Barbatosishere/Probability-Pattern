package com.tz.statpatterns.core.definition;

import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.ClientTickingBlockEntity;
import appeng.blockentity.ServerTickingBlockEntity;
import appeng.core.definitions.BlockDefinition;
import appeng.core.definitions.DeferredBlockEntityType;
import com.google.common.base.Preconditions;
import com.tz.statpatterns.ProbabilityPatternMod;
import com.tz.statpatterns.blockentity.crafting.ProbabilityPatternProviderBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import appeng.block.AEBaseEntityBlock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.tz.statpatterns.ProbabilityPatternMod.MOD_ID;


/**
 * Block Entity type registration following AE2's pattern from AEBlockEntities.java.
 * 
 * Key difference from standard NeoForge: Must call block.setBlockEntity() to populate
 * the blockEntityClass field, which is used by AENetworkedBlockEntity.onReady() for
 * instance checking (this.blockEntityClass.isInstance(this)).
 */
@SuppressWarnings("null")
public final class SPBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> DR = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);
    private static final List<DeferredBlockEntityType<?>> BLOCK_ENTITY_TYPES = new ArrayList<>();

    public static final DeferredBlockEntityType<ProbabilityPatternProviderBlockEntity> PROBABILITY_PATTERN_PROVIDER_BLOCKENTITY = create("probability_pattern_provider", ProbabilityPatternProviderBlockEntity.class, ProbabilityPatternProviderBlockEntity::new, SPBlocks.PROBABILITY_PATTERN_PROVIDER_BLOCK);

    @SuppressWarnings("unchecked")
    @SafeVarargs
    private static <T extends AEBaseBlockEntity> DeferredBlockEntityType<T> create(String shortId,
                                                                                   Class<T> entityClass,
                                                                                   BlockEntityFactory<T> factory,
                                                                                   BlockDefinition<? extends AEBaseEntityBlock<?>>... blockDefinitions) {
        Preconditions.checkArgument(blockDefinitions.length > 0);

        var deferred = DR.register(shortId, () -> {
            AtomicReference<BlockEntityType<T>> typeHolder = new AtomicReference<>();
            BlockEntityType.BlockEntitySupplier<T> supplier = (blockPos, blockState) -> factory.create(typeHolder.get(),
                    blockPos, blockState);

            var blocks = Arrays.stream(blockDefinitions)
                    .map(BlockDefinition::block)
                    .toArray(AEBaseEntityBlock[]::new);

            var type = BlockEntityType.Builder.of(supplier, blocks).build(null);
            typeHolder.setPlain(type); // Makes it available to the supplier used above

            AEBaseBlockEntity.registerBlockEntityItem(type, blockDefinitions[0].asItem());

            // If the block entity classes implement specific interfaces, automatically register them
            // as tickers with the blocks that create that entity.
            BlockEntityTicker<T> serverTicker = null;
            if (ServerTickingBlockEntity.class.isAssignableFrom(entityClass)) {
                serverTicker = (level, pos, state, entity) -> {
                    ((ServerTickingBlockEntity) entity).serverTick();
                };
            }
            BlockEntityTicker<T> clientTicker = null;
            if (ClientTickingBlockEntity.class.isAssignableFrom(entityClass)) {
                clientTicker = (level, pos, state, entity) -> {
                    ((ClientTickingBlockEntity) entity).clientTick();
                };
            }

            for (var block : blocks) {
                AEBaseEntityBlock<T> baseBlock = (AEBaseEntityBlock<T>) block;
                baseBlock.setBlockEntity(entityClass, type, clientTicker, serverTicker);
            }

            return type;
        });

        var result = new DeferredBlockEntityType<>(entityClass, deferred);
        BLOCK_ENTITY_TYPES.add(result);
        return result;
    }

    @FunctionalInterface
    interface BlockEntityFactory<T extends AEBaseBlockEntity> {
        T create(BlockEntityType<T> type, BlockPos pos, BlockState state);
    }
    private SPBlockEntities() {
        // Prevent instantiation
    }
}
