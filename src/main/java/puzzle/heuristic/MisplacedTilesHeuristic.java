package puzzle.heuristic;

import puzzle.model.Board;

// h1: count of tiles not in their goal slot (blank excluded). Admissible. Range [0, 8].
// Variant 3 in the spec ("simple admissible heuristic").
public final class MisplacedTilesHeuristic implements Heuristic {

    @Override
    public int estimate(Board b) {
        int[] tiles = b.tiles();
        int count = 0;
        for (int i = 0; i < 8; i++) {
            if (tiles[i] != 0 && tiles[i] != i + 1) count++;
        }
        // Position 8 must hold 0 in goal; any non-blank tile there is misplaced.
        if (tiles[8] != 0) count++;
        return count;
    }

    @Override
    public String name() {
        return "MisplacedTiles";
    }
}
