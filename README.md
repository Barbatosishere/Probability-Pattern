# Statistical Patterns for AE2

Independent NeoForge addon for Applied Energistics 2 on Minecraft 1.21.1.

The first implemented item is `probabilitypattern:probability_pattern`. It is an AE2 encoded pattern item backed by a custom data component. The pattern treats a probabilistic machine recipe as a binomial process:

- target output amount is the desired number of successes;
- each configured input is consumed once per attempt;
- small targets use the exact binomial lower tail;
- large targets use a normal approximation;
- attempts are increased until the chance of producing fewer than the target amount is at most `alpha`.

For the example "1000 outputs, 80% success probability, alpha 0.05", the normal approximation plans 1286 attempts.

## Encoding

Craft a `probabilitypattern:probability_pattern_terminal` and open it. Put per-attempt input samples into the 3x3 grid, put the target output item into the output slot, put a blank `probabilitypattern:probability_pattern` into the pattern slot, adjust target amount, success probability and alpha, then press encode.

The AE2 pattern terminal processing panel exposes two small controls:

- `p`: single-attempt success probability, default `80%`;
- `a`: significance level / allowed underproduction risk, default `5%`.

## Build

Use Java 21 to run Gradle. Java 25 currently fails during Gradle/Groovy configuration for this toolchain.

From the AE2 checkout root:

```powershell
.\gradlew.bat -p statistical-patterns-addon build
```

This project is independent, but `settings.gradle` uses a Gradle composite build to substitute the AE2 dependency with the checked-out AE2 source in `..`. To build against a published AE2 release instead, remove the `includeBuild('..')` block and set `ae2_version` in `gradle.properties`.

## Current Scope

Implemented:

- independent NeoForge mod metadata and Gradle build;
- AE2 custom encoded pattern item;
- persistent and network-synced data component for statistical patterns;
- exact binomial and normal-approximation sizing logic;
- tests for the 1000-at-80% example;
- AE2 pattern terminal encoder hook;
- Crafting CPU retry button that cancels the stuck remainder and resubmits the remaining final output through AE2's own crafting planner.

Still to wire:

- storage/network tracking of observed returned quantities.
