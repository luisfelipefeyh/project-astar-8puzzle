package puzzle.heuristic;

import puzzle.model.Board;

// h1: count of tiles not in their goal slot (blank excluded). Admissible. Range [0, 8].
// Variant 3 in the spec ("simple admissible heuristic").
public final class MisplacedTilesHeuristic implements Heuristic {

    @Override
    public int estimate(Board b) {
        // TODO: scan tiles, count how many non-zero tiles differ from the goal layout.
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public String name() {
        return "MisplacedTiles";
    }
}
