import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Kruskal {

    public static class Edge {
        public final int u, v, w;

        public Edge(int u, int v, int w) {
            this.u = u;
            this.v = v;
            this.w = w;
        }
    }

    public static class DisjointSet {
        private final int[] parent, rank;

        public DisjointSet(int n) {
            parent = new int[n]; rank = new int[n];
            for (int i = 0; i < n; i++) parent[i] = i;
        }

        public int find(int x) {
            if (parent[x] != x) parent[x] = find(parent[x]);
            return parent[x];
        }

        public void union(int a, int b) {
            int ra = find(a), rb = find(b);
            if (ra == rb) return;
            if (rank[ra] < rank[rb]) parent[ra] = rb;
            else if (rank[ra] > rank[rb]) parent[rb] = ra;
            else { parent[rb] = ra; rank[ra]++; }
        }
    }

    public static List<Edge> buildMST(int[][] dist) {
        int n = dist.length;
        List<Edge> edges = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                edges.add(new Edge(i, j, dist[i][j]));
            }
        }
        edges.sort(Comparator.comparingInt(e -> e.w));

        DisjointSet ds = new DisjointSet(n);
        List<Edge> mst = new ArrayList<>(n -1);
        for (Edge e : edges) {
            if (ds.find(e.u) != ds.find(e.v)) {
                ds.union(e.u, e.v);
                mst.add(e);
                if (mst.size() == n - 1) break;
            }
        }
        return mst;
    }
}
