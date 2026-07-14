package com.tz.statpatterns.crafting;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsTooltip;
import appeng.api.ids.AEComponents;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.pattern.AEProcessingPattern;
import appeng.crafting.pattern.EncodedProcessingPattern;

import com.tz.statpatterns.SPComponents;
import com.tz.statpatterns.core.definition.SPItems;
import com.tz.statpatterns.math.ProbabilitySizing;
import com.tz.statpatterns.math.ProbabilitySizingResult;

public final class StatisticalPatternDetails extends AEProcessingPattern {
    private final EncodedStatisticalPattern encoded;
    private final ProbabilitySizingResult sizing;
    private final IInput[] scaledInputs;
    private final List<GenericStack> outputs;

    private StatisticalPatternDetails(AEItemKey definition, EncodedStatisticalPattern encoded) {
        super(definition);
        this.encoded = Objects.requireNonNull(encoded, "encoded");
        this.sizing = ProbabilitySizing.planAttempts(
                encoded.output().amount(),
                encoded.successProbability(),
                encoded.alpha(),
                encoded.smallSampleLimit());
        this.scaledInputs = encoded.inputsPerAttempt().stream()
                .map(input -> new Input(input.what(), Math.multiplyExact(input.amount(), sizing.attempts())))
                .toArray(IInput[]::new);
        this.outputs = List.of(encoded.output());
    }

    @Nullable
    public static StatisticalPatternDetails decode(AEItemKey what, Level level) {
        if (what == null || what.getItem() != SPItems.PROBABILITY_PATTERN.get()) {
            return null;
        }
        var encoded = what.get(SPComponents.ENCODED_STATISTICAL_PATTERN.get());
        if (encoded == null) {
            throw new IllegalArgumentException("Missing statistical pattern component.");
        }
        if (what.get(AEComponents.ENCODED_PROCESSING_PATTERN) == null) {
            throw new IllegalArgumentException("Missing AE2 processing pattern component.");
        }
        return new StatisticalPatternDetails(what, encoded);
    }

    public static ItemStack encode(List<GenericStack> sparseInputs, List<GenericStack> sparseOutputs,
            double successProbability, double alpha) {
        return encode(sparseInputs, sparseOutputs, successProbability, alpha, 1000);
    }

    public static ItemStack encode(List<GenericStack> sparseInputs, List<GenericStack> sparseOutputs,
            double successProbability, double alpha, long targetBatch) {
        var output = sparseOutputs.stream().filter(Objects::nonNull).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("At least one output is required."));
        var compactInputs = sparseInputs.stream().filter(Objects::nonNull).toList();
        if (compactInputs.isEmpty()) {
            throw new IllegalArgumentException("At least one input is required.");
        }

        var stack = new ItemStack(SPItems.PROBABILITY_PATTERN.get());
        stack.set(AEComponents.ENCODED_PROCESSING_PATTERN, new EncodedProcessingPattern(sparseInputs, sparseOutputs));
        stack.set(SPComponents.ENCODED_STATISTICAL_PATTERN.get(),
                new EncodedStatisticalPattern(compactInputs, output, successProbability, alpha, 30, targetBatch));
        return stack;
    }

    public static ItemStack encode(List<GenericStack> inputsPerAttempt, GenericStack output,
            double successProbability, double alpha) {
        return encode(new ArrayList<>(inputsPerAttempt), List.of(output), successProbability, alpha, 1000);
    }

    public static ItemStack encode(List<GenericStack> inputsPerAttempt, GenericStack output,
            double successProbability, double alpha, long targetBatch) {
        return encode(new ArrayList<>(inputsPerAttempt), List.of(output), successProbability, alpha, targetBatch);
    }

    public static PatternDetailsTooltip getInvalidPatternTooltip(ItemStack stack, Level level,
            @Nullable Exception cause, TooltipFlag flags) {
        var tooltip = new PatternDetailsTooltip(PatternDetailsTooltip.OUTPUT_TEXT_PRODUCES);
        var encoded = stack.get(SPComponents.ENCODED_STATISTICAL_PATTERN.get());
        if (encoded != null) {
            encoded.inputsPerAttempt().forEach(tooltip::addInput);
            tooltip.addOutput(encoded.output());
        }
        return tooltip;
    }

    @Override
    public IInput[] getInputs() {
        return scaledInputs;
    }

    @Override
    public List<GenericStack> getOutputs() {
        return outputs;
    }

    @Override
    public void pushInputsToExternalInventory(KeyCounter[] inputHolder, PatternInputSink inputSink) {
        var allInputs = new KeyCounter();
        for (var counter : inputHolder) {
            allInputs.addAll(counter);
        }

        for (var input : encoded.inputsPerAttempt()) {
            var key = input.what();
            var amount = Math.multiplyExact(input.amount(), sizing.attempts());
            var available = allInputs.get(key);
            if (available < amount) {
                throw new RuntimeException("Expected at least %d of %s when pushing probability pattern, but only %d available"
                        .formatted(amount, key, available));
            }
            inputSink.pushInput(key, amount);
            allInputs.remove(key, amount);
        }
    }

    @Override
    public PatternDetailsTooltip getTooltip(Level level, TooltipFlag flags) {
        var tooltip = new PatternDetailsTooltip(PatternDetailsTooltip.OUTPUT_TEXT_PRODUCES);
        tooltip.addInputsAndOutputs(this);
        tooltip.addProperty(Component.translatable("probabilitypattern.tooltip.target_batch"),
                Component.literal(Long.toString(encoded.targetBatch())));
        tooltip.addProperty(Component.translatable("probabilitypattern.tooltip.target_output"),
                Component.literal(Long.toString(encoded.output().amount())));
        tooltip.addProperty(Component.translatable("probabilitypattern.tooltip.success_probability"),
                Component.literal("%.2f%%".formatted(encoded.successProbability() * 100.0)));
        tooltip.addProperty(Component.translatable("probabilitypattern.tooltip.alpha"),
                Component.literal("%.2f%%".formatted(encoded.alpha() * 100.0)));
        tooltip.addProperty(Component.translatable("probabilitypattern.tooltip.trials"),
                Component.literal(Long.toString(sizing.attempts())));
        tooltip.addProperty(Component.translatable("probabilitypattern.tooltip.distribution"),
                Component.literal(sizing.distribution().serializedName()));
        tooltip.addProperty(Component.translatable("probabilitypattern.tooltip.failure_probability"),
                Component.literal("%.4f%%".formatted(sizing.underproductionRisk() * 100.0)));
        return tooltip;
    }

    public ProbabilitySizingResult sizing() {
        return sizing;
    }

    public double successProbability() {
        return encoded.successProbability();
    }

    private static final class Input implements IPatternDetails.IInput {
        private final GenericStack[] template;
        private final long multiplier;

        private Input(AEKey key, long amount) {
            this.template = new GenericStack[] { new GenericStack(key, 1) };
            this.multiplier = amount;
        }

        @Override
        public GenericStack[] getPossibleInputs() {
            return template;
        }

        @Override
        public long getMultiplier() {
            return multiplier;
        }

        @Override
        public boolean isValid(AEKey input, Level level) {
            return input.matches(template[0]);
        }

        @Nullable
        @Override
        public AEKey getRemainingKey(AEKey template) {
            return null;
        }
    }
}


