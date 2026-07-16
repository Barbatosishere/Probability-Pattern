package com.tz.statpatterns.mixin;

import com.tz.statpatterns.core.definition.SPItems;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import appeng.menu.slot.RestrictedInputSlot;

@Mixin(RestrictedInputSlot.class)
public abstract class RestrictedInputSlotMixin {

    @Shadow
    @Final
    private RestrictedInputSlot.PlacableItemType which;

    @Inject(method = "mayPlace", at = @At("RETURN"), cancellable = true)
    private void allowProbabilityPattern(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        // Also accept our probability pattern in blank pattern slots
        if (!cir.getReturnValue()
                && which == RestrictedInputSlot.PlacableItemType.BLANK_PATTERN
                && stack.is(SPItems.PROBABILITY_PATTERN.get())) {
            cir.setReturnValue(true);
        }
    }
}
