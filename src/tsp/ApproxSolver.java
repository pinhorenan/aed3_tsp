package tsp;

import java.util.ArrayList;
import java.util.List;

/**
 * 2-aproximação para TSP;
 *  1) MST de Kruskal
 *  2) DFS pré-ordem para obter o tour
 *  3) fecha o ciclo voltando ao início
 */
public class ApproxSolver implements TSPSolver {

    @Override
    public TSPSolution solve(int[][] dist) {
        int n = dist.length;

        // 1) MST
        List<Kruskal.Edge> mst = Kruskal.buildMST(dist);

        // 2) Monta a lista de adjacências
        List<List<Integer>> adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        for (Kruskal.Edge e : mst) {
            adj.get(e.u).add(e.v);
            adj.get(e.v).add(e.u);
        }

        // 3) DFS pré-ordem
        boolean[] vis = new boolean[n];
        List<Integer> tour = new ArrayList<>(n + 1);
        dfs(0, adj, vis, tour);

        // 4) fecha o ciclo voltando ao vértice 0
        tour.add(0);

        // 5) calcula o custo
        long cost = 0;
        for (int i = 0; i < tour.size() - 1; i++) {
            cost += dist[tour.get(i)][tour.get(i + 1)];
        }
        return new TSPSolution(tour, cost);
    }

    /** Visita em pré-ordem a "árvore" representada por adj */
    private void dfs(int u, List<List<Integer>> adj, boolean[] vis, List<Integer> out) {
        vis[u] = true;
        out.add(u);
        for (int v : adj.get(u)) {
            if (!vis[v]) dfs(v, adj, vis, out);
        }
    }
}
