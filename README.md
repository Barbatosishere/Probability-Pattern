# Probability Pattern for AE2 — 概率样板

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-blue?logo=minecraft)
![NeoForge](https://img.shields.io/badge/NeoForge-21.1.169-orange)
![AE2](https://img.shields.io/badge/AE2-19.2.0-green)
![Java](https://img.shields.io/badge/Java-21-red)
![License](https://img.shields.io/badge/License-LGPL%20v3-blue)

> **English version**: [README_en.md](README_en.md)

**Probability Pattern for AE2** 是 [Applied Energistics 2](https://github.com/AppliedEnergistics/Applied-Energistics-2) 的一个独立 NeoForge 扩展 Mod，为 Minecraft 1.21.1 增加"概率样板"功能。

当机器有**成功率**的概念（例如每次合成有 80% 概率产出目标物品）时，普通样板无法处理这种不确定性。这个 Mod 使用统计学中的**二项分布**来计算需要多少次尝试才能以足够高的置信度达到目标产出量。

---

## 核心原理

概率样板将机器的随机产出建模为**二项过程**：

| 参数 | 含义 | 默认值 |
|------|------|--------|
| **目标数量** (N) | 你想要获得的最终产出数 | — |
| **单次成功率** (p) | 每次尝试产出成功的概率 | 80% |
| **显著性水平** (α) | 允许的产出不足风险 | 5% |

系统自动计算**所需尝试次数**，使得产出低于目标数量的概率不超过 α。

### 计算方式

- **小目标**（N ≤ 30）：使用精确的二项分布左尾概率计算
- **大目标**（N > 30）：使用正态近似，计算速度快且精度足够

### 示例

> **目标：1000 个产物，单次成功率 80%，α = 0.05**
>
> 正态近似计算出需要 **1286 次尝试**，此时产出不足 1000 个的概率 ≤ 5%。

---

## 使用方法

### 1. 合成终端

合成 `probabilitypattern:probability_pattern_terminal`（概率样板终端），放置并打开。

### 2. 编码样板

| 步骤 | 说明 |
|------|------|
| ① 放入材料 | 将**单次尝试**所需的材料样本放入 3×3 输入格 |
| ② 放入产物 | 将目标产物放入输出槽 |
| ③ 放入空白样板 | 将空白 `probabilitypattern:probability_pattern` 放入样板槽 |
| ④ 调整参数 | 设置目标数量、成功率和 α |
| ⑤ 点击编码 | 生成概率样板 |

### 3. 终端控件

概率样板的编码界面在 AE2 标准图案终端的基础上增加了三个小控件：

- **p%** — 单次尝试成功率，默认 `80%`
- **a%** — 显著性水平（允许的产出不足风险），默认 `5%`
- **N** — 目标产出数量

界面会实时显示计划尝试次数（Plan），如果参数无效则显示红色错误提示。

---

## 模组信息

| 项目 | 值 |
|------|-----|
| Mod ID | `probabilitypattern` |
| 模组名称 | Probability Pattern for AE2 |
| 当前版本 | 0.1.0 |
| 开发群组 | `com.tz.statpatterns` |

### 依赖

| 依赖 | 版本 |
|------|------|
| Minecraft | 1.21.1 |
| NeoForge | ≥ 21.1.169 |
| Applied Energistics 2 | ≥ 19.x |

---

## 构建

需要 **Java 21** 来运行 Gradle。

### 方式一：针对发布版本构建

在 `gradle.properties` 中设置 `ae2_version` 为需要的版本号，然后运行：

```powershell
.\gradlew.bat build
```

### 方式二：针对本地 AE2 源码构建（推荐调试）

将 AE2 源码放在上级目录，在 `settings.gradle` 中添加 `includeBuild(''..'')`，然后运行：

```powershell
.\gradlew.bat -p statistical-patterns-addon build
```

---

## 已实现功能

- ✅ 独立的 NeoForge 模组元数据和 Gradle 构建
- ✅ AE2 自定义编码样板物品（`probabilitypattern:probability_pattern`）
- ✅ 持久化且网络同步的概率样板数据组件
- ✅ 精确二项分布与正态近似计算逻辑
- ✅ AE2 图案终端编码器集成
- ✅ Crafting CPU 重试按钮：取消卡住的剩余任务，将未完成的产出重新提交给 AE2 合成规划器
- ✅ 概率样板供应器方块和线缆部件

## 待实现功能

- ⬜ 存储/网络层面追踪观察到的实际返还数量

---

## 技术架构

```
com.tz.statpatterns
├── math/
│   ├── ProbabilitySizing.java          # 核心算法：计算所需尝试次数
│   ├── ProbabilitySizingResult.java     # 计算结果记录
│   └── DistributionMode.java            # 分布模式枚举（精确二项/正态近似）
├── crafting/
│   ├── StatisticalPatternDetails.java   # AE2 样板详情实现
│   └── EncodedStatisticalPattern.java   # 编码后概率样板的数据组件
├── block/
│   └── ProbabilityPatternProviderBlock.java  # 方块定义
├── blockentity/crafting/
│   └── ProbabilityPatternProviderBlockEntity.java  # 方块实体
├── part/
│   ├── ProbabilityPatternProviderPart.java    # 线缆附着供应器部件
│   └── ProbabilityPatternTerminalPart.java    # 线缆附着终端部件
├── menu/
│   └── ProbabilityPatternTerminalMenu.java    # 交互菜单
├── client/
│   ├── ProbabilityPatternClient.java          # 客户端注册
│   └── ProbabilityPatternTerminalScreen.java  # 编码界面渲染
├── core/definition/
│   ├── SPBlocks.java                   # 方块注册
│   ├── SPBlockEntities.java            # 方块实体注册
│   ├── SPItems.java                    # 物品注册
│   └── SPParts.java                    # 部件注册
├── init/
│   └── InitCapabilityProviders.java    # Capability 注册
├── api/ids/                            # 资源路径常量
├── integration/jei/                    # JEI 集成
├── ProbabilityPatternMod.java          # Mod 主入口
├── SPComponents.java                   # 数据组件注册
├── SPMenus.java                        # 菜单注册
└── SPCreativeTabs.java                 # 创造模式标签页
```

---

## 许可证

本项目基于 [MIT](LICENSE) 许可证开源。
