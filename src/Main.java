import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {

    private static final String[] INST = {
            "tsp1_253.txt",
            "tsp2_1248.txt",
            "tsp3_1194.txt",
            "tsp4_7013.txt",
            "tsp5_27603.txt"
    };

    private static final String INST_DIR = "instances";

    private static final int WARMUP_RUNS    =  2;
    private static final int MAX_EXACT_N    = 20;
    private static final int BENCHMARK_RUNS = 10;

    private static class ResultEntry {
        String instName;
        int n;
        int optimal;
        long approxCost;
        double approxAvg;
        Integer exactCost;
        double exactAvg;

        ResultEntry(String instName, int n, int optimal,
                    long approxCost, double approxAvg,
                    Integer exactCost, double exactAvg) {
            this.instName   = instName;
            this.n          = n;
            this.optimal    = optimal;
            this.approxCost = approxCost;
            this.approxAvg  = approxAvg;
            this.exactCost  = exactCost;
            this.exactAvg   = exactAvg;
        }
    }

    public static void main(String[] args) throws IOException {
        String ts = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String logFile = "results_" + ts + ".txt";

        List<ResultEntry> allResults = new ArrayList<>();

        try (BufferedWriter log = new BufferedWriter(new FileWriter(logFile));
             Scanner sc = new Scanner(System.in)) {

            log.write("=== TSP Benchmark " + ts + " ===\n\n");

            if (args.length == 1) {
                String path = args[0];
                System.out.println("Executando instância única via CLI: " + path);
                processInstance(path, log, allResults);
            }
            else {
                while (true) {
                    System.out.println("\n=== TSP Solver ===");
                    System.out.println("Escolha uma instância:");
                    for (int i = 0; i < INST.length; i++) {
                        System.out.printf("  %d - %s%n", i + 1, INST[i]);
                    }
                    System.out.println("  0 - Sair");
                    System.out.print("Opção: ");

                    int opt = sc.nextInt();
                    if (opt == 0) {
                        break;
                    }
                    if (opt < 1 || opt > INST.length) {
                        System.out.println("Opção inválida. Tente novamente.");
                        continue;
                    }

                    // Monta caminho completo: "instances/<arquivo>"
                    String path = Paths.get(INST_DIR, INST[opt - 1]).toString();
                    processInstance(path, log, allResults);
                }
            }

            if (!allResults.isEmpty()) {
                log.write("=== Resumo Final ===\n");
                log.write("| Instância | n  | Ótimo | Heur. | Tempo H   | Erro %    | Exato | Tempo E   |\n");
                log.write("|-----------|----|--------|-------|-----------|-----------|--------|-----------|\n");

                System.out.println("\n=== Resumo Final ===");
                System.out.printf("%-10s %3s %6s %7s %10s %10s %7s %10s%n",
                        "Instância", "n", "Ótimo", "Heur.", "Tempo H", "Erro %", "Exato", "Tempo E");

                for (ResultEntry r : allResults) {
                    double err = 100.0 * (r.approxCost - r.optimal) / r.optimal;
                    String errStr = String.format("%+.2f %%", err);
                    String exactStr = r.exactCost != null ? String.valueOf(r.exactCost) : "—";
                    String exactTimeStr = r.exactCost != null
                            ? String.format("%.3fms", r.exactAvg)
                            : "(pulado)";

                    // Log (Markdown)
                    log.write(String.format(Locale.US,
                            "| %-9s | %2d | %6d | %5d | %8.3fms | %9s | %6s | %9s |\n",
                            r.instName, r.n, r.optimal, r.approxCost, r.approxAvg,
                            errStr, exactStr, exactTimeStr
                    ));

                    // Terminal
                    System.out.printf(Locale.US,
                            "%-10s %2d   %6d %7d %9.3fms %10s %7s %10s%n",
                            r.instName, r.n, r.optimal, r.approxCost, r.approxAvg,
                            errStr, exactStr, exactTimeStr
                    );
                }

                log.write("\n=== Fim do Resumo ===\n");
                System.out.println("=== Fim do Resumo ===");
            }

        }

        System.out.println("Log gravado em: " + logFile);
    }

    private static void processInstance(String path,
                                        BufferedWriter log,
                                        List<ResultEntry> allResults) throws IOException {
        System.out.println("\nLendo instância: " + path + " …");
        int[][] dist = MatrixReader.readMatrix(path);
        int n = dist.length;

        // Extrai nome e valor ótimo do nome do arquivo
        String base = path.substring(path.lastIndexOf(System.getProperty("file.separator")) + 1);
        base = base.substring(0, base.lastIndexOf('.'));
        String[] parts = base.split("_");
        String instName = parts[0];
        int optimal = Integer.parseInt(parts[1]);

        System.out.printf("Instância selecionada: '%s' (n=%d, ótimo=%d)%n", instName, n, optimal);
        log.write("Instância: " + path + " (n=" + n + ")\n");

        // ====== Aproximativo ======
        TSPSolver approx = new ApproxSolver();
        for (int i = 0; i < WARMUP_RUNS; i++) {
            approx.solve(dist);
        }

        long[] timesApprox = new long[BENCHMARK_RUNS];
        long approxCost = 0;
        log.write("  Aproximativo:\n");
        for (int i = 0; i < BENCHMARK_RUNS; i++) {
            long t0 = System.nanoTime();
            TSPSolver.TSPSolution solH = approx.solve(dist);
            timesApprox[i] = System.nanoTime() - t0;
            if (i == 0) {
                approxCost = solH.cost;
                log.write("    custo = " + approxCost + "\n");
            }
        }
        double sumApprox = 0;
        for (long t : timesApprox) {
            sumApprox += t;
        }
        double avgApprox = sumApprox / BENCHMARK_RUNS / 1e6;

        for (int i = 0; i < BENCHMARK_RUNS; i++) {
            log.write(String.format("    run %2d: %.3f ms%n", i + 1, timesApprox[i] / 1e6));
        }
        log.write(String.format("    média: %.3f ms%n%n", avgApprox));
        System.out.printf("  Aproximativo: custo=%d, avg=%.3fms%n", approxCost, avgApprox);

        // ====== Exato ======
        Integer exactCost = null;
        double  avgExact = 0.0;
        if (n <= MAX_EXACT_N) {
            TSPSolver exact = new ExactSolver();
            for (int i = 0; i < WARMUP_RUNS; i++) {
                exact.solve(dist);
            }

            long[] timesExact = new long[BENCHMARK_RUNS];
            log.write("  Exato:\n");
            for (int i = 0; i < BENCHMARK_RUNS; i++) {
                long t0 = System.nanoTime();
                TSPSolver.TSPSolution solE = exact.solve(dist);
                timesExact[i] = System.nanoTime() - t0;
                if (i == 0) {
                    exactCost = (int) solE.cost;
                    log.write("    custo = " + exactCost + "\n");
                }
            }
            double sumExact = 0;
            for (long t : timesExact) {
                sumExact += t;
            }
            avgExact = sumExact / BENCHMARK_RUNS / 1e6;

            for (int i = 0; i < BENCHMARK_RUNS; i++) {
                log.write(String.format("    run %2d: %.3f ms%n", i + 1, timesExact[i] / 1e6));
            }
            log.write(String.format("    média: %.3f ms%n%n", avgExact));
            System.out.printf("  Exato:       custo=%d, avg=%.3fms%n", exactCost, avgExact);
        } else {
            log.write("  Exato: pulado (n = " + n + " > " + MAX_EXACT_N + ")\n\n");
            System.out.printf("  Exato: pulado (n > %d)%n", MAX_EXACT_N);
        }

        log.write("────────────────────────────────────────\n\n");

        allResults.add(new ResultEntry(
                instName, n, optimal,
                approxCost, avgApprox,
                exactCost, avgExact
        ));
    }
}
