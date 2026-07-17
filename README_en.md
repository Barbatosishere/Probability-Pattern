# Probability Pattern for AE2

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-blue?logo=minecraft)
![NeoForge](https://img.shields.io/badge/NeoForge-21.1.169-orange)
![AE2](https://img.shields.io/badge/AE2-19.2.0-green)
![Java](https://img.shields.io/badge/Java-21-red)
![License](https://img.shields.io/badge/License-LGPL%20v3-blue)

> **🇨🇳 中文版本**: Please refer to [README.md](README.md) — the Chinese version is the default.

**Probability Pattern for AE2** is an independent NeoForge addon for [Applied Energistics 2](https://github.com/AppliedEnergistics/Applied-Energistics-2) on Minecraft 1.21.1. It adds **probability-based encoded patterns** for machines with probabilistic outcomes.

When a machine has a **success rate** (e.g., an 80% chance of producing the target item per craft), ordinary patterns cannot handle the uncertainty. This mod uses **binomial distribution** from statistics to calculate how many attempts are needed to reach the target output with sufficient confidence.

---

## Core Principle

The probability pattern models a machine''s probabilistic output as a **binomial process**:

| Parameter | Meaning | Default |
|-----------|---------|---------|
| **Target amount** (N) | Desired final output quantity | — |
| **Single-attempt success probability** (p) | Probability of success per attempt | 80% |
| **Significance level** (α) | Allowed risk of underproduction | 5% |

The system automatically computes the **required number of attempts** such that the probability of producing fewer than the target amount does not exceed α.

### Computation Method

- **Small targets** (N ≤ 30): Uses the exact binomial lower-tail probability
- **Large targets** (N > 30): Uses a normal approximation for speed with sufficient accuracy

### Example

> **Target: 1000 outputs, single-attempt success probability 80%, α = 0.05**
>
> The normal approximation calculates that **1286 attempts** are needed. At this point, the probability of producing fewer than 1000 items is ≤ 5%.

---

## Usage

### 1. Craft the terminal

Craft a `probabilitypattern:probability_pattern_terminal`, place it, and open its GUI.

### 2. Encode a pattern

| Step | Action |
|------|--------|
| ① | Place **per-attempt** input samples into the 3×3 grid |
| ② | Place the target output item into the output slot |
| ③ | Place a blank `probabilitypattern:probability_pattern` into the pattern slot |
| ④ | Adjust target amount, success probability, and α |
| ⑤ | Press encode to generate the probability pattern |

### 3. Terminal controls

The probability pattern encoding interface adds three controls to the standard AE2 pattern terminal:

- **p%** — Single-attempt success probability, default `80%`
- **a%** — Significance level (allowed underproduction risk), default `5%`
- **N** — Target output quantity

The interface displays the planned attempt count in real time. If parameters are invalid, a red error message is shown.

---

## Mod Info

| Item | Value |
|------|-------|
| Mod ID | `probabilitypattern` |
| Mod Name | Probability Pattern for AE2 |
| Current Version | 0.1.0 |
| Group ID | `com.tz.statpatterns` |

### Dependencies

| Dependency | Version |
|------------|---------|
| Minecraft | 1.21.1 |
| NeoForge | ≥ 21.1.169 |
| Applied Energistics 2 | ≥ 19.x |

---

## Build

Requires **Java 21** to run Gradle.

### Option 1: Build against published AE2 release

Set `ae2_version` in `gradle.properties` to the desired version, then run:

```powershell
.\gradlew.bat build
```

### Option 2: Build against local AE2 source (for debugging)

Place the AE2 source in the parent directory, then add `includeBuild(''..'')` to `settings.gradle` and run:

```powershell
.\gradlew.bat -p statistical-patterns-addon build
```

---

## Implemented Features

- ✅ Independent NeoForge mod metadata and Gradle build
- ✅ AE2 custom encoded pattern item (`probabilitypattern:probability_pattern`)
- ✅ Persistent and network-synced data component for probability patterns
- ✅ Exact binomial distribution and normal approximation sizing logic
- ✅ AE2 pattern terminal encoder integration
- ✅ Crafting CPU retry button: cancels stuck remainder tasks and resubmits unfinished output to the AE2 crafting planner
- ✅ Probability Pattern Provider block and part (cable-attached)

## Planned Features

- ⬜ Storage/network-level tracking of observed returned quantities

---

## Technical Architecture

```
com.tz.statpatterns
├── math/
│   ├── ProbabilitySizing.java          # Core algorithm: computes required attempts
│   ├── ProbabilitySizingResult.java     # Computation result record
│   └── DistributionMode.java            # Distribution mode enum (exact binomial / normal approx)
├── crafting/
│   ├── StatisticalPatternDetails.java   # AE2 pattern detail implementation
│   └── EncodedStatisticalPattern.java   # Encoded probability pattern data component
├── block/
│   └── ProbabilityPatternProviderBlock.java  # Block definition
├── blockentity/crafting/
│   └── ProbabilityPatternProviderBlockEntity.java  # Block entity
├── part/
│   ├── ProbabilityPatternProviderPart.java    # Cable-attached provider part
│   └── ProbabilityPatternTerminalPart.java    # Cable-attached terminal part
├── menu/
│   └── ProbabilityPatternTerminalMenu.java    # Interaction menu
├── client/
│   ├── ProbabilityPatternClient.java          # Client-side registration
│   └── ProbabilityPatternTerminalScreen.java  # Encoding screen rendering
├── core/definition/
│   ├── SPBlocks.java                   # Block registration
│   ├── SPBlockEntities.java            # Block entity registration
│   ├── SPItems.java                    # Item registration
│   └── SPParts.java                    # Part registration
├── init/
│   └── InitCapabilityProviders.java    # Capability registration
├── api/ids/                            # Resource location constants
├── integration/jei/                    # JEI integration
├── ProbabilityPatternMod.java          # Mod entry point
├── SPComponents.java                   # Data component registration
├── SPMenus.java                        # Menu registration
└── SPCreativeTabs.java                 # Creative mode tab
```

---

## License

This project is open-source under the [MIT](LICENSE) license.
