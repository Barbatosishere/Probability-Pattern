package com.tz.statpatterns.mixin;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingSimulationRequester;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.crafting.CraftingCalculation;
import appeng.me.service.CraftingService;
import com.tz.statpatterns.me.SPCraftingCalculation;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Mixin(value = CraftingService.class, remap = false)
public abstract class CraftingServiceMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger(CraftingServiceMixin.class);

    @Shadow
    private IGrid grid;

    @Shadow
    private static final ExecutorService CRAFTING_POOL;
    static {
        final ThreadFactory factory = ar -> {
            final Thread crafting = new Thread(ar, "Mixin Crafting Calculator");
            crafting.setDaemon(true);
            return crafting;
        };

        CRAFTING_POOL = Executors.newCachedThreadPool(factory);
    }

    @ModifyVariable(
            method = "beginCraftingCalculation",
            at = @At(value = "STORE", ordinal = 0),
            ordinal = 0
    )
    private CraftingCalculation replaceCalculationJob(
            CraftingCalculation originalJob,
            Level level,
            ICraftingSimulationRequester simRequester,
            AEKey what,
            long amount,
            CalculationStrategy strategy
    ) {
        //LOGGER.info("[CRAFT MIXIN] Replace calculation job | target: {} x {}", what, amount);
        GenericStack requestStack = new GenericStack(what, amount);

        if (level == null || simRequester == null) {
            throw new IllegalArgumentException("Invalid Crafting Job Request");
        }

        final CraftingCalculation job = new SPCraftingCalculation(level, grid, simRequester, new GenericStack(what, amount), strategy);

        return job;
    }
}