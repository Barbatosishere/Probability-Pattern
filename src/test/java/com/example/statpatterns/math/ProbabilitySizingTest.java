package com.example.statpatterns.math;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ProbabilitySizingTest {
    @Test
    void usesNormalApproximationForLargePrecisionProcessorExample() {
        var result = ProbabilitySizing.planAttempts(1000, 0.8, 0.05, 30);

        assertThat(result.distribution()).isEqualTo(DistributionMode.NORMAL_APPROXIMATION);
        assertThat(result.attempts()).isEqualTo(1286);
        assertThat(result.underproductionRisk()).isLessThanOrEqualTo(0.05);
    }

    @Test
    void usesExactBinomialForSmallSamples() {
        var result = ProbabilitySizing.planAttempts(10, 0.8, 0.05, 30);

        assertThat(result.distribution()).isEqualTo(DistributionMode.BINOMIAL);
        assertThat(result.attempts()).isGreaterThanOrEqualTo(10);
        assertThat(ProbabilitySizing.shouldSubmit(10, result.attempts(), 0.8, 0.05, 30)).isTrue();
    }

    @Test
    void calculatesAdditionalAttemptsAfterShortfall() {
        var additional = ProbabilitySizing.additionalAttemptsAfterFailure(1000, 960, 0.8, 0.05, 30);

        assertThat(additional).isGreaterThan(40);
    }
}
