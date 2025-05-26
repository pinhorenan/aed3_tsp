package tsp;

import java.util.List;

/**
 * Interface comum para todos os solvers de TSP.
 */
public interface TSPSolver {

    /**
     * Recebe matriz de distâncias e devolve a solução:
     *  - tour: lista de vértices na ordem em que são visitados
     *  - cost: custo total
     */
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
