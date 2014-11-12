package olga.oskina.metrics;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by olgaoskina
 * 12 November 2014.
 */
public class Metrics {
    private final static double LN_2 = Math.log(2);
    private final static double P_BREAK = 0.15;

    protected static int[] prepareScores(int n, int[] scores) {
        if (n <= 0 || scores == null) {
            throw new IllegalArgumentException("Invalid input");
        }
        if (scores.length > n) {
            System.out.println("Provided more scores then necessary. Last " + (scores.length - n) + " scores are ignored");
        }
        if (scores.length != n) {
            // expand `scores` up to array of length N. Undefined elements considered to be 0
            int[] temp = new int[n];
            System.arraycopy(scores, 0, temp, 0, Math.min(scores.length, n));
            scores = temp;
        }
        return scores;
    }

    protected static double log2(int val) {
        return Math.log(val) / LN_2;
    }

    // DCG

    public static double calcDCG(int n, int[] scores) {
        return calcDCG(prepareScores(n, scores));
    }

    protected static double calcDCG(int[] scores) {
        double ans = scores[0];
        for (int i = 1; i < scores.length; i++) {
            if (scores[i] != 0) {
                ans += scores[i] / log2(i + 1);
            }
        }
        return ans;
    }

    // NDCG

    public static double calcNDCG(int n, int[] scores) {
        return calcNDCG(prepareScores(n, scores));
    }

    protected static double calcNDCG(int[] scores) {
        int[] sortedScores = new int[scores.length];
        System.arraycopy(scores, 0, sortedScores, 0, scores.length);
        Arrays.sort(sortedScores);
		ArrayUtils.reverse(sortedScores);

        double dcg = calcDCG(scores);
        double idcg = calcDCG(sortedScores);

        return dcg / idcg;
    }

    // PFound

    public static double calcPFound(int n, int[] scores) {
        return calcPFound(prepareScores(n, scores));
    }

    protected static double calcPFound(int[] scores) {
        double ans = 0;
        double lastPLook = 1;
		double threshold = NumberUtils.max(scores) * 0.90;

        for (int a : scores) {
			double pRel = a >= threshold ? 0.40 : 0.00;
            ans += lastPLook * pRel;
            lastPLook = lastPLook * (1 - pRel) * (1 - P_BREAK);
        }
        return ans;
    }
}
