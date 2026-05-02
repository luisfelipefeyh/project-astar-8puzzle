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
        int[] tiles = b.tiles();
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            int tile = tiles[i];
            if (tile == 0) continue;
            int goalRow = (tile - 1) / 3;
            int goalCol = (tile - 1) % 3;
            sum += Math.abs(i / 3 - goalRow) + Math.abs(i % 3 - goalCol);
        }
        return sum;
    }
}
