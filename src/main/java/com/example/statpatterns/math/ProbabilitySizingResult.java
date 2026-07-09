package com.example.statpatterns.math;

public record ProbabilitySizingResult(
        long targetSuccesses,
        long attempts,
        double successProbability,
        double alpha,
        DistributionMode distribution,
        double underproductionRisk) {
}
