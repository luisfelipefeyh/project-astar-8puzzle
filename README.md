# project-astar-8puzzle

An implementation of the A\* search algorithm applied to the 8-puzzle game, in plain Java.

## Course

UFSC — Departamento de Informática e Estatística
Curso de Sistemas de Informação — Disciplina de Sistemas Inteligentes
Profa. Nathalia da Cruz Alves
_Trabalho Prático 1 — Métodos de Busca_

## Team

- Luis Felipe de Azambuja Feyh
- TODO: João Pedro Paixão de Matos Gubert

## Spec summary

Four search variants must be implemented:

1. **Uniform Cost** (no heuristic)
2. **A\*** with a **non-admissible** heuristic
3. **A\*** with a **simple admissible** heuristic
4. **A\*** with the **most precise admissible** heuristic the team can produce

For every run the program must output:

- (a) Total nodes visited
- (b) Path length
- (c) Execution time (seconds)
- (d) Maximum frontier (open list) size
- (e) A `.txt` or `.json` file with the frontier and the visited set at termination

Goal state (single solution considered, per spec note 3):

```
1 2 3
4 5 6
7 8 _
```

Constraints: plain Java only, no external libraries; heuristics computed mathematically (no hardcoded lookup tables); no UI grading.

## Project layout

```
project-astar-8puzzle/
├── .gitignore
├── README.md
├── docs/
│   ├── RESULTS.md         # heuristic comparison + perf table
│   └── VIDEO_SCRIPT.md    # talking points for the submission video
└── src/main/java/puzzle/
    ├── Main.java
    ├── model/             # Board, Move, Node
    ├── search/            # SearchAlgorithm, BestFirstSearch, SearchResult
    ├── heuristic/         # Heuristic + 5 implementations
    ├── io/                # ResultWriter (console + JSON)
    └── test/              # TestCases (easy/medium/hard preset boards)
```

## Build & run

Plain `javac`/`java`, no build tool.

```bash
# Compile (from project root, bash / git-bash)
javac -d out $(find src/main/java -name "*.java")

# PowerShell equivalent
javac -d out (Get-ChildItem -Recurse src/main/java -Filter *.java).FullName

# Run
java -cp out puzzle.Main
```

> CLI input format is **TODO** — to be decided in the next pass (flag-based vs preset menu vs stdin).

## Deliverables checklist

- [ ] All four algorithm variants implemented and runnable
- [ ] Console output covers items (a)–(d)
- [ ] JSON dump of frontier + visited at termination (item e)
- [ ] `docs/RESULTS.md` filled with heuristic comparison (≥2 hard, ≥2 medium, ≥1 easy) and perf table for the 4 variants
- [ ] Source `.zip` packaged for submission
- [ ] Video (≤10 min, YouTube/Drive link) covering: methods/relations, frontier management, heuristics description, perf analysis, limitations
- [ ] `docs/VIDEO_SCRIPT.md` finalized before recording

## References

- TODO: Russell & Norvig — _Artificial Intelligence: A Modern Approach_ (A\* and 8-puzzle heuristics)
- TODO: Luger — _Artificial Intelligence: Structures and Strategies for Complex Problem Solving_
- TODO: any other source consulted (cite with URL/repo if applicable)
