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
        // TODO:
        //   int h = ManhattanHeuristic.manhattanSum(b);
        //   h += 2 * countRowConflicts(b);
        //   h += 2 * countColConflicts(b);
        //   return h;
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public String name() {
        return "ManhattanLinearConflict";
    }

    @SuppressWarnings("unused")
    private static int countRowConflicts(Board b) {
        throw new UnsupportedOperationException("TODO");
    }

    @SuppressWarnings("unused")
    private static int countColConflicts(Board b) {
        throw new UnsupportedOperationException("TODO");
    }
}
