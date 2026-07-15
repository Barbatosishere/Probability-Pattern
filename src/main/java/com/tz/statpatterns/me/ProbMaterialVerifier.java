package com.tz.statpatterns.me;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import com.tz.statpatterns.crafting.EncodedStatisticalPattern;
import com.tz.statpatterns.crafting.StatisticalPatternDetails;

public class ProbMaterialVerifier {
    // 95%单侧置信Z值
    private static final double Z95 = 1.645;
    // 迭代精度
    private static final double EPS = 1e-4;

    /**
     * 输入目标产出数量 T，计算满足95%置信度所需最小原料总数 n
     * @param targetOutput 目标产出总数 (T)
     * @param p 单次配方成功率
     * @return 最小原料总数 n
     */
    private static long calcMinRaw(long targetOutput, double p) {
        if (p <= 0 || p >= 1) return targetOutput;
        double T = targetOutput;
        double q = 1 - p;

        // 牛顿迭代求解方程: p*n - Z*sqrt(n*p*q) - T = 0
        double n = T / p; // 初始猜测
        for (int i = 0; i < 100; i++) {
            double sqrtN = Math.sqrt(n);
            double f = p * n - Z95 * sqrtN * Math.sqrt(p * q) - T;
            double fPrime = p - Z95 * Math.sqrt(p * q) / (2 * sqrtN);
            if (Math.abs(f) < EPS) break;
            n = n - f / fPrime;
            if (n < T) n = T;
        }
        // 向上取整保证约束
        return (long) Math.ceil(n);
    }

    /**
     * ✅ 单个数值版本：输入【目标产出总数】，返回95%置信度所需最小原料总数
     * @param targetOutput 目标产出总数 (例如 100)
     * @param pattern 概率样板
     * @return 校验后最小安全原料总数
     */
    public static long verifySingle(long targetOutput, IPatternDetails pattern) {
        if (!(pattern instanceof StatisticalPatternDetails sp)) {
            return targetOutput;
        }
        EncodedStatisticalPattern encoded = sp.encoded();
        double p = encoded.successProbability();
        if (p <= 0 || p >= 1) {
            return targetOutput;
        }
        return calcMinRaw(targetOutput, p);
    }

    /**
     * KeyCounter 批量版本
     * @param targetCounter 原始目标产出对应的原料需求（基准为 targetOutput）
     * @param pattern 概率样板
     * @param targetOutput 顶层目标产出总数
     * @return 校验后安全原料用量 KeyCounter
     */
    public static KeyCounter verifyProbMaterial(KeyCounter targetCounter, IPatternDetails pattern, long targetOutput) {
        KeyCounter verifiedCounter = new KeyCounter();
        long minTotalRaw = verifySingle(targetOutput, pattern);
        // 按比例修正每种原料用量
        double ratio = (double) minTotalRaw / targetOutput;

        for (AEKey key : targetCounter.keySet()) {
            long base = targetCounter.get(key);
            long verified = Math.max(1, (long) Math.ceil(base * ratio));
            verifiedCounter.add(key, verified);
        }
        return verifiedCounter;
    }

    // 简易固定比例版本（快速调试用）
    public static KeyCounter verifySimple(KeyCounter rawCounter) {
        KeyCounter verified = new KeyCounter();
        for (AEKey key : rawCounter.keySet()) {
            long raw = rawCounter.get(key);
            long verifiedAmount = Math.max(1, raw);
            verified.add(key, verifiedAmount);
        }
        return verified;
    }

    // 测试主方法：目标100、p=0.8 → 结果应为 135
    public static void main(String[] args) {
        long result = calcMinRaw(100, 0.8);
        System.out.println("Min Raw for T=100, p=0.8: " + result); // 输出: 135
    }
}
