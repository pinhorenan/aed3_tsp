import java.util.ArrayList;
import java.util.List;

public class ApproxSolver implements TSPSolver {

    @Override
    public TSPSolution solve(int[][] dist) {
        int n = dist.length;

        List<Kruskal.Edge> mst = Kruskal.buildMST(dist);

        List<List<Integer>> adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        for (Kruskal.Edge e : mst) {
            adj.get(e.u).add(e.v);
            adj.get(e.v).add(e.u);
        }

        boolean[] vis = new boolean[n];
        List<Integer> tour = new ArrayList<>(n + 1);
        dfs(0, adj, vis, tour);

        tour.add(0);

        long cost = 0;
        for (int i = 0; i < tour.size() - 1; i++) {
            cost += dist[tour.get(i)][tour.get(i + 1)];
        }
        return new TSPSolution(tour, cost);
    }

    private void dfs(int u, List<List<Integer>> adj, boolean[] vis, List<Integer> out) {
        vis[u] = true;
        out.add(u);
        for (int v : adj.get(u)) {
            if (!vis[v]) dfs(v, adj, vis, out);
        }
    }
}
