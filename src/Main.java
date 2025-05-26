import tsp.ApproxSolver;
import tsp.ExactSolver;
import tsp.MatrixReader;
import tsp.TSPSolver;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Menu interativo de TSP com benchmarking e log final em forma de tabela.
 */
public class Main {

    // instâncias disponíveis
    private static final String[] INST = {
            "tsp1_253.txt",
            "tsp2_1248.txt",
            "tsp3_1194.txt",
            "tsp4_7013.txt",
            "tsp5_27603.txt"
    };

    // parâmetros de benchmark
    private static final int MAX_EXACT_N     = 20;  // n máximo para solver exato
    private static final int WARMUP_RUNS     = 2;   // rodadas de warm-up
    private static final int BENCHMARK_RUNS  = 10;  // rodadas de medição

    // armazena resultados para o resumo
    private static class ResultEntry {
        String instName;
        int n;
        int optimal;
        long heurCost;
        double heurAvg;
        Integer exactCost; // null se não executado
        double exactAvg;   // só válido se exactCost != null

        ResultEntry(String instName, int n, int optimal,
                    long heurCost, double heurAvg,
                    Integer exactCost, double exactAvg) {
            this.instName  = instName;
            this.n         = n;
            this.optimal   = optimal;
            this.heurCost  = heurCost;
            this.heurAvg   = heurAvg;
            this.exactCost = exactCost;
            this.exactAvg  = exactAvg;
        }
    }

    public static void main(String[] args) throws IOException {
        // timestamp para nome de log único
        String ts = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String logFile = "results_" + ts + ".txt";

        List<ResultEntry> allResults = new ArrayList<>();

        try (BufferedWriter log = new BufferedWriter(new FileWriter(logFile));
             Scanner sc = new Scanner(System.in)) {

            log.write("=== TSP Benchmark " + ts + " ===\n\n");

            while (true) {
                System.out.println("\n=== TSP Solver ===");
                System.out.println("1–5: escolher instância   0: sair");
                System.out.print("Opção: ");
                int opt = sc.nextInt();
                if (opt == 0) break;
                if (opt < 1 || opt > 5) continue;

                String path = INST[opt - 1];
                System.out.println("Lendo " + path + " …");
                int[][] dist = MatrixReader.readMatrix(path);
                int n = dist.length;

                // extrai nome da instância e custo ótimo do nome de arquivo
                String base = path.substring(0, path.lastIndexOf('.')); // ex: "tsp1_253"
                String[] parts = base.split("_");
                String instName = parts[0];           // "tsp1"
                int optimal    = Integer.parseInt(parts[1]); // 253

                log.write("Instância: " + path + " (n=" + n + ")\n");

                // ====== 1) HEURÍSTICA (ApproxSolver) ======
                TSPSolver approx = new ApproxSolver();
                // warm-up
                for (int i = 0; i < WARMUP_RUNS; i++) approx.solve(dist);

                long[] timesH = new long[BENCHMARK_RUNS];
                long heurCost = 0;
                for (int i = 0; i < BENCHMARK_RUNS; i++) {
                    long t0 = System.nanoTime();
                    TSPSolver.TSPSolution solH = approx.solve(dist);
                    timesH[i] = System.nanoTime() - t0;
                    if (i == 0) {
                        heurCost = solH.cost;
                        log.write("  Heurística:\n");
                        log.write("    custo = " + heurCost + "\n");
                    }
                }
                double sumH = 0;
                for (long t : timesH) sumH += t;
                double avgH = sumH / BENCHMARK_RUNS / 1e6;

                for (int i = 0; i < BENCHMARK_RUNS; i++) {
                    log.write(String.format("    run %2d: %.3f ms\n", i+1, timesH[i]/1e6));
                }
                log.write(String.format("    média: %.3f ms\n\n", avgH));
                System.out.printf("Heurística: avg=%.3fms%n", avgH);

                // ====== 2) EXATO (ExactSolver) ======
                Integer exactCost = null;
                double  avgE      = 0.0;
                if (n <= MAX_EXACT_N) {
                    TSPSolver exact = new ExactSolver();
                    for (int i = 0; i < WARMUP_RUNS; i++) exact.solve(dist);

                    long[] timesE = new long[BENCHMARK_RUNS];
                    for (int i = 0; i < BENCHMARK_RUNS; i++) {
                        long t0 = System.nanoTime();
                        TSPSolver.TSPSolution solE = exact.solve(dist);
                        timesE[i] = System.nanoTime() - t0;
                        if (i == 0) {
                            exactCost = (int)solE.cost;
                            log.write("  Exato:\n");
                            log.write("    custo = " + exactCost + "\n");
                        }
                    }
                    double sumE = 0;
                    for (long t : timesE) sumE += t;
                    avgE = sumE / BENCHMARK_RUNS / 1e6;

                    for (int i = 0; i < BENCHMARK_RUNS; i++) {
                        log.write(String.format("    run %2d: %.3f ms\n", i+1, timesE[i]/1e6));
                    }
                    log.write(String.format("    média: %.3f ms\n\n", avgE));
                    System.out.printf("Exato:     avg=%.3fms%n", avgE);
                } else {
                    log.write("  Exato: pulado (n = " + n + " > " + MAX_EXACT_N + ")\n\n");
                    System.out.println("Exato: pulado (n > " + MAX_EXACT_N + ")");
                }

                log.write("────────────────────────────────────────\n\n");
                log.flush();

                // guarda para o resumo
                allResults.add(new ResultEntry(
                        instName, n, optimal,
                        heurCost, avgH,
                        exactCost, avgE
                ));
            }

            // ====== RESUMO FINAL ======
            log.write("=== Resumo Final ===\n");
            log.write("| Instância | n  | Ótimo | Heurística | Erro %      | Exato |\n");
            log.write("|-----------|----|-------|------------|-------------|-------|\n");
            System.out.println("\n=== Resumo Final ===");
            System.out.printf("%-11s %-3s %-5s %-10s %-10s %-6s%n",
                    "Instância","n","Ótimo","Heurística","Erro %","Exato");

            for (ResultEntry r : allResults) {
                double err = 100.0 * (r.heurCost - r.optimal) / r.optimal;
                String errStr = String.format("%+.2f %%", err);
                String exato = (r.exactCost!=null ? r.exactCost.toString() : "—");
                // linha no log
                log.write(String.format("| **%s**    | %2d | %5d | %10d | **%8s** | %5s |\n",
                        r.instName, r.n, r.optimal,
                        r.heurCost, errStr, exato));
                // linha na tela
                System.out.printf("%-11s %2d   %5d   %10d   %8s   %5s%n",
                        r.instName, r.n, r.optimal,
                        r.heurCost, errStr, exato);
            }
            log.write("\n=== Fim do Resumo ===\n");
            System.out.println("=== Fim do Resumo ===");
        }

        System.out.println("Log gravado em: results_" + ts + ".txt");
    }
}
