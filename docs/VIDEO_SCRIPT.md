# Video script (≤ 10 minutes)

> Per spec, the video must briefly explain the items below. Use this file as a script/checklist before recording.

## 1. Main methods/functions and their relation to A\*

TODO — point at:
- `puzzle.search.BestFirstSearch#solve` (main loop)
- `puzzle.heuristic.Heuristic` (interface) and the four implementations
- `puzzle.model.Board#legalMoves` and `apply` (state transitions)
- `puzzle.model.Node#reconstructPath` (path output)

## 2. Frontier management — checks before adding a state

TODO — show the two guards in `BestFirstSearch#solve`:
1. **Closed-skip**: if the neighbor's hash is in the closed set with an equal-or-better g, skip.
2. **Open-improve**: if already in the open map with an equal-or-better g, skip; otherwise replace and mark the previous open entry stale (lazy deletion).

Also explain: tie-breaking in the priority queue (lower h on equal f).

## 3. Heuristics — description, value range, precision comparison

TODO — for each heuristic:
- **ZeroHeuristic** (UCS): always 0.
- **MisplacedTilesHeuristic** (h1, simple admissible): count of tiles not in goal slot, range [0, 8].
- **ManhattanHeuristic** (h2): sum of grid distances of each tile to its goal, range [0, ~24].
- **ManhattanLinearConflictHeuristic** (h3, best admissible): h2 + 2 × linear conflicts.
- **OverestimateHeuristic** (non-admissible): k × Manhattan with k > 1 (e.g., 3).

Cite Russell & Norvig §3.6 / Luger for theoretical background.

Show the comparison table from `docs/RESULTS.md` (≥2 hard, ≥2 medium, ≥1 easy).

## 4. Performance analysis

TODO — walk through the table in `docs/RESULTS.md` and comment:
- Why UCS and the non-admissible variant fail or balloon on hard cases.
- Why Manhattan+LC dominates Misplaced (smaller node count, similar/optimal path length).
- Effect on max frontier size and elapsed time.

## 5. Limitations / objectives not met

TODO — fill last. State exactly what was attempted vs. what shipped, and any known issues.
