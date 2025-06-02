import java.util.List;

public interface TSPSolver {
    TSPSolution solve(int[][] dist);

    class TSPSolution {
        public final List<Integer> tour;
        public final long cost;
        public TSPSolution(List<Integer> tour, long cost) {
            this.tour = tour;
            this.cost = cost;
        }
    }
}
