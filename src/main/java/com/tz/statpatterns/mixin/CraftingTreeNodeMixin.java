package com.tz.statpatterns.mixin;

import java.util.ArrayList;
import java.util.Collection;

import com.tz.statpatterns.crafting.StatisticalPatternDetails;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.stacks.AEKey;
import appeng.crafting.CraftingTreeNode;

/**
 * Intercepts ICraftingService.getCraftingFor() inside
 * CraftingTreeNode.buildChildPatterns() to wrap every
 * StatisticalPatternDetails with forRequest(node.amount).
 *
 * This handles both top-level AND intermediate probability patterns
 * in chain crafting, because each CraftingTreeNode knows its own
 * required amount (this.amount), which is the scaled input from the
 * parent pattern.
 */
@Mixin(CraftingTreeNode.class)
public abstract class CraftingTreeNodeMixin {

    @Shadow
    private long amount;

    @Redirect(
            method = "buildChildPatterns",
            at = @At(value = "INVOKE",
                    target = "Lappeng/api/networking/crafting/ICraftingService;getCraftingFor(Lappeng/api/stacks/AEKey;)Ljava/util/Collection;"))
    private Collection<IPatternDetails> wrapPatternsForNode(ICraftingService service, AEKey whatToCraft) {
        var patterns = service.getCraftingFor(whatToCraft);
        var result = new ArrayList<IPatternDetails>(patterns.size());
        for (var p : patterns) {
            if (p instanceof StatisticalPatternDetails spd && this.amount > 0) {
                result.add(spd.forRequest(this.amount));
            } else {
                result.add(p);
            }
        }
        return result;
    }
}
