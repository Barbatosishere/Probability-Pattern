package com.example.statpatterns;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.example.statpatterns.terminal.ProbabilityPatternTerminalBlock;

public final class SPBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ProbabilityPatternMod.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ProbabilityPatternMod.MOD_ID);

    public static final DeferredBlock<Block> PROBABILITY_PATTERN_TERMINAL = BLOCKS.register(
            "probability_pattern_terminal",
            () -> new ProbabilityPatternTerminalBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()));

    public static final DeferredItem<Item> PROBABILITY_PATTERN_TERMINAL_ITEM = ITEMS.register(
            "probability_pattern_terminal",
            () -> new BlockItem(PROBABILITY_PATTERN_TERMINAL.get(), new Item.Properties()));

    private SPBlocks() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
    }
}
