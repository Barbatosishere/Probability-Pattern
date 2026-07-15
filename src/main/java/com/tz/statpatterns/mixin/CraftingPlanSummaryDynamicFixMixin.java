package com.tz.statpatterns.mixin;

import appeng.api.stacks.KeyCounter;
import appeng.menu.me.crafting.CraftingPlanSummary;
import appeng.menu.me.crafting.CraftingPlanSummaryEntry;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(value = CraftingPlanSummary.class, remap = false)
public abstract class CraftingPlanSummaryDynamicFixMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger(CraftingPlanSummaryDynamicFixMixin.class);

    @Inject(
            method = "fromJob",
            at = @At(value = "RETURN"),
            cancellable = true,
            remap = false
    )
    private static void fixCraftAmountEntries(
            IGrid grid,
            IActionSource actionSource,
            ICraftingPlan job,
            CallbackInfoReturnable<CraftingPlanSummary> cir
    ) {
        CraftingPlanSummary original = cir.getReturnValue();
        Map<AEKey, Long> realUsed = new HashMap<>();

        KeyCounter usedCounter = job.usedItems();
        for (Map.Entry<AEKey, Long> entry : usedCounter) {
            AEKey key = entry.getKey();
            long amount = entry.getValue();
            realUsed.put(key, amount);
            LOGGER.info("[CraftingPlanSummary] real used key: {}, amount: {}", key, amount);
        }

        GenericStack finalOutput = job.finalOutput();
        AEKey finalKey = finalOutput.what();
        long finalAmount = finalOutput.amount();
        LOGGER.info("[CraftingPlanSummary] final output key: {}, amount: {}", finalKey, finalAmount);

        List<CraftingPlanSummaryEntry> fixedEntries = new ArrayList<>();
        for (CraftingPlanSummaryEntry entry : original.getEntries()) {
            AEKey key = entry.getWhat();
            long newCraftAmount;
            if (realUsed.containsKey(key)) {
                newCraftAmount = realUsed.get(key);
            } else if (key.equals(finalKey)) {
                newCraftAmount = finalAmount;
            } else {
                newCraftAmount = entry.getCraftAmount();
            }
            fixedEntries.add(new CraftingPlanSummaryEntry(
                    key,
                    entry.getMissingAmount(),
                    entry.getStoredAmount(),
                    newCraftAmount
            ));
            LOGGER.info("[CraftingPlanSummary] entry key: {}, old craftAmount: {}, new craftAmount: {}",
                    key, entry.getCraftAmount(), newCraftAmount);
        }

        CraftingPlanSummary fixedSummary = new CraftingPlanSummary(
                original.getUsedBytes(),
                original.isSimulation(),
                fixedEntries
        );
        cir.setReturnValue(fixedSummary);
    }
}