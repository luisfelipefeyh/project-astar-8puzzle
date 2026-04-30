package puzzle.model;

import java.util.List;

// 3x3 8-puzzle board. Tiles 1..8 plus 0 = blank.
// Internal representation: int[9], index = row * 3 + col.
public final class Board {

    private final int[] tiles;

    public Board(int[] tiles) {
        // TODO: validate length == 9 and that values are a permutation of 0..8
        this.tiles = tiles.clone();
    }

    public int[] tiles() {
        return tiles.clone();
    }

    // Index (0..8) of the blank tile.
    public int blankIndex() {
        throw new UnsupportedOperationException("TODO");
    }

    // Moves the blank can legally make from the current state.
    public List<Move> legalMoves() {
        throw new UnsupportedOperationException("TODO");
    }

    // Returns a NEW Board with the given move applied. Does not mutate this.
    public Board apply(Move m) {
        throw new UnsupportedOperationException("TODO");
    }

    // True iff this board equals the goal: 1 2 3 / 4 5 6 / 7 8 _.
    public boolean isGoal() {
        throw new UnsupportedOperationException("TODO");
    }

    // Solvability check via inversion count parity (8-puzzle: solvable iff inversions are even).
    public boolean isSolvable() {
        throw new UnsupportedOperationException("TODO");
    }

    // Compact hash key for closed/open lookup. TODO: pack 9 nibbles into a long.
    public long hashKey() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public boolean equals(Object o) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("TODO");
    }
}
