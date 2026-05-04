# 8-Puzzle — A* Search

**UFSC — Sistemas Inteligentes | Profa. Nathalia da Cruz Alves**
Luis Felipe de Azambuja Feyh · João Pedro Paixão de Matos Gubert

---

## O que é este projeto?

Implementação do algoritmo A* aplicado ao jogo 8-puzzle em Java puro (sem dependências externas). O tabuleiro 3×3 tem 8 peças numeradas e um espaço vazio; o objetivo é chegar ao estado final movendo as peças até atingir a configuração:

```
1 2 3
4 5 6
7 8 _
```

O projeto implementa **4 variantes de busca** e oferece duas formas de uso: linha de comando e interface web interativa.

---

## Estrutura de arquivos

```
project-astar-8puzzle/
├── src/main/java/puzzle/
│   ├── Main.java                          ← entrada CLI
│   ├── model/
│   │   ├── Board.java                     ← estado do tabuleiro
│   │   ├── Node.java                      ← nó da árvore de busca
│   │   └── Move.java                      ← UP / DOWN / LEFT / RIGHT
│   ├── search/
│   │   ├── SearchAlgorithm.java           ← interface comum
│   │   ├── BestFirstSearch.java           ← motor de busca (CLI)
│   │   ├── TracingSearch.java             ← motor de busca com log (GUI)
│   │   ├── SearchResult.java              ← resultado do BestFirstSearch
│   │   ├── TracingResult.java             ← resultado do TracingSearch
│   │   └── StepSnapshot.java              ← foto de um passo da busca
│   ├── heuristic/
│   │   ├── Heuristic.java                 ← interface
│   │   ├── ZeroHeuristic.java             ← UCS (h = 0)
│   │   ├── MisplacedTilesHeuristic.java   ← peças fora do lugar
│   │   ├── ManhattanHeuristic.java        ← distância Manhattan
│   │   ├── ManhattanLinearConflictHeuristic.java ← Manhattan + conflitos
│   │   └── OverestimateHeuristic.java     ← 3 × Manhattan (não admissível)
│   ├── io/
│   │   └── ResultWriter.java              ← saída no terminal + arquivo JSON
│   ├── ui/
│   │   ├── PuzzleServer.java              ← servidor HTTP (porta 8080)
│   │   ├── CasesHandler.java              ← GET /cases
│   │   └── SolveHandler.java              ← POST /solve
│   └── test/
│       └── TestCases.java                 ← 5 tabuleiros preset (EASY a HARD)
├── src/test/java/puzzle/
│   └── PuzzleTest.java                    ← testes automatizados
├── web/
│   └── index.html                         ← SPA da interface gráfica
└── out/                                   ← bytecode compilado (gerado)
```

---

## Como compilar

Requer JDK 11 ou superior. Nenhuma dependência externa.

```bash
# macOS / Linux
javac -d out $(find src/main/java -name "*.java")

# Windows (PowerShell)
javac -d out (Get-ChildItem -Recurse src/main/java -Filter *.java).FullName
```

Se o JDK estiver no Homebrew (macOS):
```bash
export PATH="/opt/homebrew/opt/openjdk/bin:$PATH"
javac -d out $(find src/main/java -name "*.java")
```

---

## Modo 1 — Linha de Comando

### Uso básico

```bash
java -cp out puzzle.Main --algo <ALGORITMO> --board "1 0 3 5 2 6 4 7 8"
java -cp out puzzle.Main --algo <ALGORITMO> --case <PRESET>
```

**Algoritmos disponíveis (`--algo`):**

| Valor             | Heurística             | Admissível |
|-------------------|------------------------|------------|
| `UCS`             | h = 0 (Custo Uniforme) | sim        |
| `MISPLACED`       | Peças fora do lugar    | sim        |
| `MANHATTAN_LC`    | Manhattan + Conflitos  | sim        |
| `NON_ADMISSIBLE`  | 3 × Manhattan          | **não**    |

**Presets disponíveis (`--case`):**

| Preset     | Profundidade ótima |
|------------|--------------------|
| `EASY_1`   | 5 movimentos       |
| `MEDIUM_1` | 10 movimentos      |
| `MEDIUM_2` | 20 movimentos      |
| `HARD_1`   | ~27 movimentos     |
| `HARD_2`   | ~29 movimentos     |

### Exemplos

```bash
# A* com melhor heurística no caso fácil
java -cp out puzzle.Main --algo MANHATTAN_LC --case EASY_1

# Custo Uniforme com tabuleiro manual
java -cp out puzzle.Main --algo UCS --board "5 1 3 2 6 8 4 7 0"

# Heurística não-admissível (mais rápida, mas pode não ser ótima)
java -cp out puzzle.Main --algo NON_ADMISSIBLE --case HARD_1
```

### Saída no terminal

```
=== ManhattanLinearConflict ===
Path   : [RIGHT, DOWN, LEFT, DOWN, RIGHT]
Length : 5
Visited: 6
MaxFrnt: 5
Time   : 0.001 s
JSON   : run-1746123456789.json
```

O arquivo `run-<timestamp>.json` é gerado automaticamente com o estado da fronteira e dos nós visitados ao final.

---

## Modo 2 — Interface Web Interativa

### Iniciar o servidor

```bash
java -cp out puzzle.ui.PuzzleServer
# ou com porta customizada:
java -cp out puzzle.ui.PuzzleServer --port 9090
```

O browser abre automaticamente em `http://localhost:8080`. O terminal exibe o log de decisões do algoritmo ao vivo enquanto você usa a interface.

### Tela 1 — Escolha do Tabuleiro

**Aba Presets:**
- 5 cards com mini-tabuleiro visual (EASY_1 até HARD_2)
- Cada card mostra a profundidade ótima da solução
- Clique para selecionar

**Aba Interativo:**
- Tabuleiro 3×3 clicável — clique numa peça adjacente ao espaço vazio para movê-la
- Botão **Embaralhar** gera um tabuleiro aleatório solucionável
- Botão **Resetar** volta ao estado inicial
- Badge em tempo real indica se o tabuleiro atual é **Solucionável** ou **Não solucionável** (calculado por contagem de inversões no JS)

Quando um tabuleiro válido estiver selecionado, o botão **Resolver** fica ativo.

### Tela 2 — Animação da Solução

**Coluna esquerda — Tabuleiro:**
- Grid 3×3 com animação CSS ao trocar peças de posição
- Barra de progresso: "Passo N de M"
- Linha com os valores **g**, **h** e **f** do passo atual
- Controles de playback:
  - `⏮` — vai ao início
  - `⏪` — volta um passo
  - `⏸ / ▶` — pause / play automático
  - `⏩` — avança um passo
  - `⏭` — vai ao fim
  - Slider de velocidade (100ms a 2000ms por passo)

**Coluna direita — Painel de Controle:**

*Seletor de algoritmo:* 4 botões (UCS, 3×Manhattan, Misplaced, Manhattan LC). Trocar o algoritmo re-executa `/solve` com o mesmo tabuleiro automaticamente.

*Métricas em tempo real:*
- Nós Visitados
- Tamanho do Caminho
- Fronteira Máxima
- Tempo de execução

*Tabela comparativa:* Botão **"Rodar todos os 4"** dispara as 4 chamadas em paralelo e preenche a tabela. A linha do algoritmo mais eficiente (menor número de nós) fica destacada em verde.

### Gaveta de Internals — como ver o algoritmo por dentro

Na base da Tela 2, clique no botão **"🔍 Ver internals do algoritmo"**.

Abre um painel com duas seções sincronizadas com cada passo da animação:

**Fronteira (top 5):**
- Lista os 5 melhores nós da priority queue no passo atual
- Cada linha mostra o tabuleiro em notação compacta + g / h / f
- O nó com menor f (o que **será expandido no próximo passo**) fica destacado em azul

**Log de decisões:**
- Lista todas as entradas de log acumuladas até o passo atual
- Rola automaticamente para a entrada mais recente
- A entrada do passo atual fica destacada em amarelo
- Mostra: qual nó foi expandido, g/h/f, e por que cada filho foi aceito ou descartado

Avançar ou voltar passo atualiza a gaveta em sincronia.

---

## Como o algoritmo funciona — passo a passo

### O problema

O 8-puzzle tem 9 posições (3×3). A peça `0` representa o espaço vazio. Um movimento consiste em deslizar uma peça adjacente para o espaço vazio. O algoritmo busca a sequência mínima de movimentos até o estado objetivo.

### O motor de busca (`BestFirstSearch`)

É uma **busca best-first** com fila de prioridade. O estado expandido em cada iteração é sempre o nó com menor valor de `f`.

```
f(n) = g(n) + h(n)
        ↑        ↑
   custo real   estimativa até o objetivo
  (movimentos   (calculada pela heurística)
   já feitos)
```

**Algoritmo em pseudocódigo:**
```
fronteira ← {estado_inicial}
visitados ← {}

enquanto fronteira não vazia:
    n ← nó com menor f na fronteira
    se n é objetivo → retorna solução

    para cada movimento legal a partir de n:
        filho ← aplicar movimento
        se filho não foi visitado:
            adiciona filho à fronteira com f = g+1 + h(filho)

    marca n como visitado
```

**Lazy deletion:** quando um nó já na fronteira é encontrado com custo menor, o antigo é marcado como `stale = true` e ignorado quando saído da fila — evita re-heapificar.

### As 4 heurísticas

**1. UCS — Custo Uniforme (`ZeroHeuristic`)**
- `h(n) = 0` sempre
- Garante solução ótima mas explora muito mais nós
- Equivalente a busca em largura com custo

**2. Peças Fora do Lugar (`MisplacedTilesHeuristic`)**
- `h(n)` = quantidade de peças que não estão na posição correta
- Admissível (nunca superestima — cada peça fora precisa de pelo menos 1 movimento)
- Mais fraca que Manhattan

**3. Distância Manhattan (`ManhattanHeuristic`)**
- `h(n)` = soma das distâncias de cada peça à sua posição objetivo, em linhas + colunas
- Peça no índice `i`, objetivo no índice `goal`: `|i/3 - goal/3| + |i%3 - goal%3|`
- Admissível e domina Peças Fora do Lugar

**4. Manhattan + Conflito Linear (`ManhattanLinearConflictHeuristic`) — melhor**
- `h(n) = Manhattan(n) + 2 × conflitos_lineares`
- **Conflito linear:** dois tiles `a` e `b` estão na sua linha (ou coluna) objetivo, mas `a` está à direita de `b` quando no objetivo `a` deveria estar à esquerda. Para se cruzarem, um precisa sair e voltar — custa pelo menos 2 movimentos extras por par conflitante.
- Admissível e domina Manhattan puro

**5. Não-Admissível (`OverestimateHeuristic`)**
- `h(n) = 3 × Manhattan(n)`
- Superestima o custo — pode não encontrar a solução ótima
- Geralmente mais rápida por expandir menos nós, mas sacrifica otimalidade

### Solucionabilidade

Nem todo tabuleiro tem solução. A condição é: **o número de inversões deve ser par**.

Uma inversão é um par `(a, b)` onde `a` aparece antes de `b` no array mas `a > b` (ignorando o espaço vazio). Isso é verificado em `Board.isSolvable()` e também em tempo real no JavaScript da interface.

### O `TracingSearch` — log ao vivo

O `TracingSearch` é uma versão do motor de busca que, a cada expansão:
1. Coleta um `StepSnapshot` (tabuleiro + fronteira top-5 + g/h/f + log)
2. Imprime no terminal o estado sendo expandido e o que aconteceu com cada filho

Isso permite ver o algoritmo "pensando" no terminal enquanto a animação rola no browser.

---

## API REST (para desenvolvedores)

O servidor expõe 2 endpoints:

### `GET /cases`
Retorna os 5 presets como JSON:
```json
{
  "cases": [
    { "name": "EASY_1", "depth": 5, "tiles": [1,0,3,5,2,6,4,7,8] },
    ...
  ]
}
```

### `POST /solve`
**Request:**
```json
{ "tiles": [1,0,3,5,2,6,4,7,8], "algo": "MANHATTAN_LC" }
```
`algo` aceita: `UCS` | `NON_ADMISSIBLE` | `MISPLACED` | `MANHATTAN_LC`

**Response:**
```json
{
  "solved": true,
  "pathLength": 5,
  "nodesVisited": 6,
  "maxFrontierSize": 5,
  "elapsedMs": 2,
  "steps": [
    {
      "stepIndex": 0,
      "board": [1,0,3,5,2,6,4,7,8],
      "move": null,
      "g": 0, "h": 5, "f": 5,
      "visitedCount": 0,
      "logEntry": "[0] Expandindo g=0 h=5 f=5",
      "frontierTop": [
        { "board": [1,2,3,5,0,6,4,7,8], "g": 1, "h": 4, "f": 5 }
      ]
    }
  ]
}
```

Cada elemento de `steps` corresponde a uma expansão do algoritmo. O `frontierTop` mostra os 5 melhores nós da fila de prioridade **antes** daquela expansão.

---

## Testes automatizados

```bash
# Compilar e rodar os testes
javac -d out $(find src -name "*.java")
java -cp out puzzle.PuzzleTest
```

Os testes cobrem:
- Solucionabilidade dos 5 presets
- Goal detection
- hashKey único para boards diferentes
- Movimentos legais em posições de borda
- Todos os 4 algoritmos nos 5 presets (solução encontrada + comprimento ótimo)

---

## Referências

- Russell & Norvig — *Artificial Intelligence: A Modern Approach* (A* e heurísticas para 8-puzzle)
- Luger — *Artificial Intelligence: Structures and Strategies for Complex Problem Solving*
