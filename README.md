# TSP Solver Benchmark

Este repositório contém um programa Java para resolver o Problema do Caixeiro Viajante (TSP) de duas maneiras:

1. **Solver Aproximativo** (heurística).
2. **Solver Exato** (força bruta com poda, limitado a instâncias pequenas).

O objetivo é comparar, para cada instância, o custo (distância total) e o tempo médio de execução de ambas as abordagens.

---

## 📁 Estrutura de Pastas

```
/meu-projeto
│
├─ /src
│   ├─ Main.java
│   ├─ MatrixReader.java
│   ├─ TSPSolver.java
│   ├─ ApproxSolver.java
│   ├─ ExactSolver.java
│   └─ Kruskal.java
│
└─ /instances
    ├─ tsp1_253.txt
    ├─ tsp2_1248.txt
    ├─ tsp3_1194.txt
    ├─ tsp4_7013.txt
    └─ tsp5_27603.txt
```

* **/src/**: código fonte Java.
* **/instances/**: arquivos de teste (.txt) contendo a matriz de distâncias e o valor ótimo no nome do arquivo.

---

## 📖 Descrição Geral

1. **MatrixReader.java**

   * Lê um arquivo `.txt` que contém uma matriz de distâncias simétrica.
   * Cada instância deve ter o seguinte formato:

     ```
     n
     d11 d12 … d1n
     d21 d22 … d2n
     …
     dn1 dn2 … dnn
     ```

     onde `n` é o número de vértices (cidades) e cada `dij` é a distância de i para j.

2. **TSPSolver.java**

   * Interface que define o método:

     ```java
     TSPSolution solve(int[][] dist);
     ```
   * `TSPSolution` contém, no mínimo:

     * `long cost;` → custo da rota encontrada.
    

3. **ApproxSolver.java** (Heurística Aproximativa)

   * Implementa uma aproximação simples (por exemplo, algoritmo guloso + melhoria local).
   * Objetivo: obter uma solução rápida, embora não ótima, para instâncias de qualquer tamanho.

4. **ExactSolver.java** (Força Bruta / Branch & Bound)

   * Executa busca exaustiva (com poda, se implementada) para encontrar o ciclo exato de menor custo.
   * **Limite**: só roda quando `n ≤ 20` (configuração interna “MAX\_EXACT\_N”), pois tempo cresce exponencialmente.

5. **Main.java**

   * Gerencia:

     * Modo **interativo** (menu CLI) para escolher uma das instâncias em `/instances/`.
     * Modo **argumento único**: se passado 1 parâmetro (`java Main caminho/para/instância.txt`), roda apenas essa instância sem exibir menu.
   * Para cada instância escolhida:

     1. Lê a matriz de distâncias via `MatrixReader.readMatrix(path)`.
     2. Extrai, do nome do arquivo, o identificador da instância e o valor ótimo (por ex.: `tsp3_1194.txt` → instName = “tsp3”, ótimo = 1194).
     3. Executa o **Warm-Up** (executa o solver aproximativo e o exato 2 vezes cada, descartando os resultados, apenas para “aquecer” a JIT).
     4. Roda o **Bench-Mark** (10 repetições) para:

        * **Heurística (aproximativo)**: mede custo (primeira run) e tempo de cada run.
        * **Exato** (apenas se `n ≤ MAX_EXACT_N`): mede custo (primeira run) e tempo de cada run, caso contrário escreve “pulado”.
     5. Escreve, num arquivo de **log** (`results_<timestamp>.txt`), todos os detalhes de custo e tempo de cada run.
     6. Exibe no **console** um resumo simplificado para aquela instância (custo e tempo médio).
     7. Acumula resultados numa lista para, ao final, imprimir o **Resumo Final** (tabela com instância, n, ótimo, custo Heurístico, tempo médio Heurístico, erro %, custo Exato, tempo médio Exato).

---

## ⚙ Como Compilar

### 1. Criar diretório de saída

Na raiz do projeto, execute:

```sh
mkdir bin
```

### 2. Compilar todos os arquivos Java

```sh
javac -d bin src/*.java
```

* A opção `-d bin` faz com que os arquivos `.class` sejam gerados dentro da pasta `bin/`, respeitando pacotes (se houver).
* Se existirem subpacotes sob `src/`, utilize:

  ```sh
  javac -d bin $(find src -name "*.java")
  ```

  (ou equivalente no Windows: `dir /s /b src\*.java | javac -d bin @-`).

---

## ▶️ Como Executar

### A. Rodar via menu (sem argumentos)

Depois de compilado:

```sh
cd /caminho/para/projeto
java -cp bin Main
```

**Exemplo de fluxo**:

```
=== TSP Solver ===
Escolha uma instância:
  1 - tsp1_253.txt
  2 - tsp2_1248.txt
  3 - tsp3_1194.txt
  4 - tsp4_7013.txt
  5 - tsp5_27603.txt
  0 - Sair
Opção: 3
```

* O programa lerá `instances/tsp3_1194.txt`, exibirá:

  ```
  Lendo instância: instances/tsp3_1194.txt …
  Instância selecionada: 'tsp3' (n=15, ótimo=1194)
    Aproximativo: custo=1424, avg=0,223ms
    Exato:       custo=1194, avg=19,549ms
  ```
* Volta ao menu para selecionar outra instância ou `0` para sair.
* Ao final (após digitar 0), exibe o **Resumo Final**.

### B. Rodar passando uma instância direta

Se quiser rodar **sem** menu:

```sh
java -cp bin Main instances/tsp2_1248.txt
```

* O programa executa apenas essa instância, grava detalhes em `results_<timestamp>.txt` e imprime no console algo como:

  ```
  Executando instância única via CLI: instances/tsp2_1248.txt

  Lendo instância: instances/tsp2_1248.txt …
  Instância selecionada: 'tsp2' (n=6, ótimo=1248)
    Aproximativo: custo=1272, avg=0,023ms
    Exato:       custo=1248, avg=0,048ms

  === Resumo Final ===
  Instância     n  Ótimo  Heur.   Tempo H     Erro %    Exato   Tempo E
  tsp2          6    1248     1272   0,023ms     +1,92 %   1248     0,048ms
  === Fim do Resumo ===

  Log gravado em: results_20250602_162808.txt
  ```

---

## 📝 Formato do Log

O arquivo de log é criado em tempo de execução como `results_<yyyyMMdd_HHmmss>.txt`.
Exemplo de conteúdo (resumido):

```
=== TSP Benchmark 20250602_162808 ===

Instância: instances/tsp3_1194.txt (n=15)
  Aproximativo:
    custo = 1424
    run  1: 0,210 ms
    run  2: 0,230 ms
    ...
    média: 0,223 ms

  Exato:
    custo = 1194
    run  1: 19,500 ms
    run  2: 19,600 ms
    ...
    média: 19,549 ms

────────────────────────────────────────

=== Resumo Final ===
| Instância | n  | Ótimo | Heur. | Tempo H   | Erro %    | Exato | Tempo E   |
|-----------|----|--------|-------|-----------|-----------|--------|-----------|
| **tsp3**  | 15 |   1194 |   1424 |   0,223ms |  +19,26 % |  1194 |   19,549ms |
=== Fim do Resumo ===
```

* As tabelas são escritas em formato Markdown-like, para facilitar leitura posterior.

---

## 📊 Resumo dos Algoritmos

1. **Heurística Aproximativa (`ApproxSolver`)**

   * Exemplo de abordagem possível (gulosa + 2-opt).
   * Tempo: **O(n²)** ou **O(n³)**, dependendo da implementação.
   * Retorna uma solução válida, mas não garante ótima.

2. **Solver Exato (`ExactSolver`)**

   * Implementa uma busca em todas as permutações de vértices, possivelmente com poda pelo custo parcial.
   * Tempo: \~**O(n!)**, viável apenas para `n ≤ 20`.
   * Retorna a rota exata de menor custo.

3. **Comparação**

   * O programa mede, para cada instância:

     * **Custo Heurístico** (1ª run de `ApproxSolver`)
     * **Média de tempo Heurístico** (10 runs)
     * **Custo Exato** (1ª run de `ExactSolver`, se permitido)
     * **Média de tempo Exato** (10 runs, se permitido)
   * Calcula o **Erro %**:

     ```
     Erro % = 100 × (custoHeurístico − ótimo) / ótimo
     ```

---

## 💻 Exemplo de Uso Completo

1. **Compilar**:

   ```sh
   mkdir bin
   javac -d bin src/*.java
   ```

2. **Rodar (menu)**:

   ```sh
   java -cp bin Main
   ```

   * Escolher “3” para `tsp3_1194.txt`, depois “0” para sair.

3. **Rodar (argumento único)**:

   ```sh
   java -cp bin Main instances/tsp5_27603.txt
   ```

---

## 🎯 Resultados Esperados

Após terminar a execução (menu ou único), haverá:

1. **Arquivo de log** `results_<timestamp>.txt` com execução detalhada.
2. **Resumo Final** no console, por exemplo:

   ```
   === Resumo Final ===
   Instância     n  Ótimo  Heur.   Tempo H     Erro %    Exato   Tempo E
   tsp1         11    253     269   0,189ms     +6,32 %    253     1,791ms
   tsp2          6   1248    1272   0,023ms     +1,92 %   1248     0,048ms
   tsp3         15   1194    1424   0,223ms    +19,26 %   1194    19,549ms
   tsp4         44   7013    8402   0,787ms    +19,81 %      —    (pulado)
   tsp5         29  27603   34902   0,287ms    +26,44 %      —    (pulado)
   === Fim do Resumo ===
   ```

Use estes resultados para avaliar a qualidade da heurística comparada ao método exato (quando possível).

---

## 🛠 Requisitos e Dependências

* **JDK 8+** (ou qualquer versão Java compatível).
* Nenhuma biblioteca externa adicional (apenas bibliotecas padrão Java).

---

## 📄 Considerações Finais

Este programa foi desenvolvido como **trabalho acadêmico** para comparar desempenho (custo e tempo) de algoritmos de TSP.

Para dúvidas ou comentários pontuais, revise o código-fonte ou entre em contato com o autor do trabalho.

---
