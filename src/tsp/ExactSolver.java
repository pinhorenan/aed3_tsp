package tsp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Resolução exata via Held-Karp (DP sobre bitmask).
 * Custo em O(n² · 2ⁿ), memória O(n · 2ⁿ).
 */
public class ExactSolver implements TSPSolver {

    private static final long INF = Long.MAX_VALUE / 4;

    @Override
    public TSPSolution solve(int[][] dist) {
        int n = dist.length;
        int N = 1 << n;

        long[][] dp = new long[N][n];
        int[][] prev = new int[N][n];
        for (long[] row : dp) Arrays.fill(row, INF);

        dp[1][0] = 0;
        prev[1][0] = -1;

        // transição
        for (int mask = 1; mask < N; mask++) {
            for (int u = 0; u < n; u++) {
                if ((mask & (1 << u)) == 0) continue;
                long costU = dp[mask][u];
                if (costU == INF) continue;
                for (int v = 0; v < n; v++) {
                    if ((mask & (1 << v)) != 0) continue;
                    int nextMask = mask | (1 << v);
                    long newCost = costU + dist[u][v];
                    if (newCost < dp[nextMask][v]) {
                        dp[nextMask][v] = newCost;
                        prev[nextMask][v] = u;
                    }
                }
            }
        }

        // encerra ciclo
        long bestCost = INF;
        int last = -1, full = N - 1;
        for (int u = 1; u < n; u++) {
            long c = dp[full][u] + dist[u][0];
            if (c < bestCost) {
                bestCost = c;
                last = u;
            }
        }

        // reconstrói tour
        List<Integer> tour = new ArrayList<>(n + 1);
        if (bestCost < INF) {
            int mask = full, current = last;
            while (current != -1) {
                tour.add(current);
                int p = prev[mask][current];
                mask ^= (1 << current);
                current = p;
            }
            tour.add(0);   // volta ao início
            Collections.reverse(tour);
        }

        return new TSPSolution(tour, bestCost);
    }
}
