
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

import java.util.ArrayList;
import java.util.Collection;

import com.tz.statpatterns.crafting.StatisticalPatternDetails;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.stacks.AEKey;
import appeng.crafting.CraftingTreeNode;

/**
 * Hooks into CraftingTreeNode to wrap StatisticalPatternDetails with
 * the correct total requested amount at every tree level.
 *
 * CraftingTreeNode.amount is the per-attempt template amount (always 1
 * for our Input class). The actual total is requestedAmount * amount,
 * calculated as totalRequestedItems after buildChildPatterns() returns.
 * We capture requestedAmount just before buildChildPatterns() and
 * compute the same total, then inject it into forRequest().
 */
@Mixin(CraftingTreeNode.class)
public abstract class CraftingTreeNodeMixin {

    @Shadow
    private long amount;

    @Unique
    private long probabilityTotalRequested;

    /**
     * Capture requestedAmount *just before* buildChildPatterns() is called.
     * At this point requestedAmount has been reduced by any items extracted
     * from inventory, so this reflects the amount that actually needs crafting.
     */
    @ModifyVariable(
            method = "request",
            at = @At(value = "INVOKE",
                    target = "Lappeng/crafting/CraftingTreeNode;buildChildPatterns()V"),
            argsOnly = true)
    private long captureRequested(long requestedAmount) {
        this.probabilityTotalRequested = requestedAmount * this.amount;
        return requestedAmount;
    }

    /**
     * Wrap every StatisticalPatternDetails with forRequest(total), where
     * total is the totalRequestedItems that would be computed right after
     * buildChildPatterns() returns (= requestedAmount * amount).
     */
    @Redirect(
            method = "buildChildPatterns",
            at = @At(value = "INVOKE",
                    target = "Lappeng/api/networking/crafting/ICraftingService;getCraftingFor(Lappeng/api/stacks/AEKey;)Ljava/util/Collection;"))
    private Collection<IPatternDetails> wrapPatternsForNode(ICraftingService service, AEKey whatToCraft) {
        var patterns = service.getCraftingFor(whatToCraft);
        if (this.probabilityTotalRequested <= 0) {
            return patterns;
        }
        var result = new ArrayList<IPatternDetails>(patterns.size());
        for (var p : patterns) {
            if (p instanceof StatisticalPatternDetails spd) {
                result.add(spd.forRequest(this.probabilityTotalRequested));
            } else {
                result.add(p);
            }
        }
        return result;
    }
}
