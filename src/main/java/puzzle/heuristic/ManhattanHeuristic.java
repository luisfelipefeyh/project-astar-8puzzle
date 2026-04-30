package puzzle.heuristic;

import puzzle.model.Board;

// Sum of Manhattan distances from each non-blank tile to its goal position.
// Admissible. Also exposes a static helper so other heuristics can reuse the sum without instantiation.
public final class ManhattanHeuristic implements Heuristic {

    @Override
    public int estimate(Board b) {
        return manhattanSum(b);
    }

    @Override
    public String name() {
        return "Manhattan";
    }

    // Reusable building block for OverestimateHeuristic and ManhattanLinearConflictHeuristic.
    public static int manhattanSum(Board b) {
        // TODO: for each tile (1..8), compute |row - goalRow| + |col - goalCol| and sum.
        throw new UnsupportedOperationException("TODO");
    }
}
