package com.example.statpatterns.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingService;
import appeng.crafting.CraftingTreeNode;

import com.example.statpatterns.network.PCraftingService;
import com.example.statpatterns.network.ProbabilityCraftingContext;

/**
 * Intercepts IGrid.getCraftingService() inside CraftingTreeNode.buildChildPatterns()
 * to return PCraftingService when a ProbabilityCraftingContext is active.
 *
 * Without this, child nodes bypass PGrid entirely because buildChildPatterns
 * obtains the grid via simRequester.getGridNode().getGrid(), which returns the
 * real (unwrapped) Grid.
 */
@Mixin(CraftingTreeNode.class)
public abstract class CraftingTreeNodeMixin {

    @Redirect(
            method = "buildChildPatterns",
            at = @At(value = "INVOKE",
                    target = "Lappeng/api/networking/IGrid;getCraftingService()Lappeng/api/networking/crafting/ICraftingService;"))
    private ICraftingService wrapChildCraftingService(IGrid grid) {
        var ctx = ProbabilityCraftingContext.current();
        if (ctx != null) {
            return new PCraftingService(grid, grid.getCraftingService());
        }
        return grid.getCraftingService();
    }
}
