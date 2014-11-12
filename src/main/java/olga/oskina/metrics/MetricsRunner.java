package olga.oskina.metrics;

import java.util.Scanner;

/**
 * Created by olgaoskina
 * 12 November 2014.
 */
public class MetricsRunner {

    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        while (true) {
            System.out.print("Enter N:      ");
            int n = Integer.parseInt(s.nextLine());
            if (n == 0) {
                break;
            }

            System.out.print("Enter scores: ");
            String line = s.nextLine();
            String[] tokens = line.split(" ");
            int[] nums = new int[tokens.length];
            for (int i = 0; i < nums.length; i++) {
                nums[i] = Integer.parseInt(tokens[i]);
            }

            int[] scores = Metrics.prepareScores(n, nums);
            double dcg = Metrics.calcDCG(scores);
            double ndcg = Metrics.calcNDCG(scores);
            double pfound = Metrics.calcPFound(scores);

            System.out.println("DCG:    " + dcg);
            System.out.println("NDCG:   " + ndcg);
            System.out.println("PFound: " + pfound);

            System.out.println();
        }
        s.close();
    }
}
