package puzzle.heuristic;

import puzzle.model.Board;

// h3: Manhattan distance + 2 * (linear conflicts). Admissible and dominates Manhattan.
// Variant 4 in the spec ("most precise admissible heuristic").
//
// A linear conflict occurs when two tiles a and b are both in their goal row (or both in their goal column),
// and a is to the right (or below) of b while in the goal a should be to the left (or above) of b.
// Each such pair forces one extra round-trip past the row/column, costing 2 additional moves.
public final class ManhattanLinearConflictHeuristic implements Heuristic {

    @Override
    public int estimate(Board b) {
        return ManhattanHeuristic.manhattanSum(b)
             + 2 * countRowConflicts(b)
             + 2 * countColConflicts(b);
    }

    @Override
    public String name() {
        return "ManhattanLinearConflict";
    }

    // Two tiles conflict in a row when both have their goal in that row but are currently inverted.
    private static int countRowConflicts(Board b) {
        int[] tiles = b.tiles();
        int conflicts = 0;
        for (int row = 0; row < 3; row++) {
            for (int col1 = 0; col1 < 3; col1++) {
                int ti = tiles[row * 3 + col1];
                if (ti == 0 || (ti - 1) / 3 != row) continue;
                for (int col2 = col1 + 1; col2 < 3; col2++) {
                    int tj = tiles[row * 3 + col2];
                    if (tj == 0 || (tj - 1) / 3 != row) continue;
                    if ((ti - 1) % 3 > (tj - 1) % 3) conflicts++;
                }
            }
        }
        return conflicts;
    }

    // Two tiles conflict in a column when both have their goal in that column but are currently inverted.
    private static int countColConflicts(Board b) {
        int[] tiles = b.tiles();
        int conflicts = 0;
        for (int col = 0; col < 3; col++) {
            for (int row1 = 0; row1 < 3; row1++) {
                int ti = tiles[row1 * 3 + col];
                if (ti == 0 || (ti - 1) % 3 != col) continue;
                for (int row2 = row1 + 1; row2 < 3; row2++) {
                    int tj = tiles[row2 * 3 + col];
                    if (tj == 0 || (tj - 1) % 3 != col) continue;
                    if ((ti - 1) / 3 > (tj - 1) / 3) conflicts++;
                }
            }
        }
        return conflicts;
    }
}
