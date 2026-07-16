package com.tz.statpatterns.mixin;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.tz.statpatterns.network.PGrid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.world.level.Level;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingSimulationRequester;
import appeng.api.stacks.AEKey;
import appeng.me.service.CraftingService;

/**
 * Two injections into CraftingService.beginCraftingCalculation:
 * 1. @ModifyArg: wraps the grid with PGrid so getCraftingService()
 *    returns PCraftingService during tree building
 * 2. @Redirect: wraps the submitted Callable with ProbabilityCraftingContext
 *    so patterns auto-size for the total requested amount
 */
@Mixin(CraftingService.class)
public abstract class CraftingServiceMixin {

    @ModifyArg(
            method = "beginCraftingCalculation",
            at = @At(value = "INVOKE",
                    target = "Lappeng/crafting/CraftingCalculation;<init>(Lnet/minecraft/world/level/Level;Lappeng/api/networking/IGrid;Lappeng/api/networking/crafting/ICraftingSimulationRequester;Lappeng/api/stacks/GenericStack;Lappeng/api/networking/crafting/CalculationStrategy;)V"),
            index = 1)
    private IGrid wrapGrid(IGrid grid) {
        return new PGrid(grid);
    }
}