package com.example.statpatterns.crafting;

import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsTooltip;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;

import com.example.statpatterns.SPComponents;
import com.example.statpatterns.SPItems;
import com.example.statpatterns.math.ProbabilitySizing;
import com.example.statpatterns.math.ProbabilitySizingResult;

public final class StatisticalPatternDetails implements IPatternDetails {
    private final AEItemKey definition;
    private final EncodedStatisticalPattern encoded;
    private final ProbabilitySizingResult sizing;
    private final IInput[] inputs;
    private final List<GenericStack> outputs;

    private StatisticalPatternDetails(AEItemKey definition, EncodedStatisticalPattern encoded) {
        this.definition = Objects.requireNonNull(definition, "definition");
        this.encoded = Objects.requireNonNull(encoded, "encoded");
        this.sizing = ProbabilitySizing.planAttempts(
                encoded.output().amount(),
                encoded.successProbability(),
                encoded.alpha(),
                encoded.smallSampleLimit());
        this.inputs = encoded.inputsPerAttempt().stream()
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
        return new StatisticalPatternDetails(what, encoded);
    }

    public static ItemStack encode(List<GenericStack> inputsPerAttempt, GenericStack output,
            double successProbability, double alpha) {
        var stack = new ItemStack(SPItems.PROBABILITY_PATTERN.get());
        stack.set(SPComponents.ENCODED_STATISTICAL_PATTERN.get(),
                new EncodedStatisticalPattern(inputsPerAttempt, output, successProbability, alpha, 30));
        return stack;
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
    public AEItemKey getDefinition() {
        return definition;
    }

    @Override
    public IInput[] getInputs() {
        return inputs;
    }

    @Override
    public List<GenericStack> getOutputs() {
        return outputs;
    }

    @Override
    public PatternDetailsTooltip getTooltip(Level level, TooltipFlag flags) {
        var tooltip = new PatternDetailsTooltip(PatternDetailsTooltip.OUTPUT_TEXT_PRODUCES);
        tooltip.addInputsAndOutputs(this);
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

    @Override
    public int hashCode() {
        return definition.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof StatisticalPatternDetails other && definition.equals(other.definition);
    }

    private static final class Input implements IInput {
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
