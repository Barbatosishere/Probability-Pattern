package com.tz.statpatterns.mixin;

import com.tz.statpatterns.network.PGrid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import appeng.api.networking.IGrid;
import appeng.me.service.CraftingService;

/**
 * Hooks into CraftingService.beginCraftingCalculation:
 * wraps the grid with PGrid so getCraftingService()
 * returns PCraftingService during tree building.
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
