
/*
 * Probability Pattern for AE2
 * Copyright (C) 2026 TaoLe-si
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
