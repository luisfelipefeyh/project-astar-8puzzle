package puzzle.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// 3x3 8-puzzle board. Tiles 1..8 plus 0 = blank.
// Internal representation: int[9], index = row * 3 + col.
public final class Board {

    private static final int[] GOAL = {1, 2, 3, 4, 5, 6, 7, 8, 0};

    private final int[] tiles;

    public Board(int[] tiles) {
        this.tiles = tiles.clone();
    }

    public int[] tiles() {
        return tiles.clone();
    }

    public int blankIndex() {
        for (int i = 0; i < 9; i++) {
            if (tiles[i] == 0) return i;
        }
        throw new IllegalStateException("no blank tile");
    }

    public List<Move> legalMoves() {
        List<Move> moves = new ArrayList<>();
        int b = blankIndex();
        int row = b / 3, col = b % 3;
        if (row > 0) moves.add(Move.UP);
        if (row < 2) moves.add(Move.DOWN);
        if (col > 0) moves.add(Move.LEFT);
        if (col < 2) moves.add(Move.RIGHT);
        return moves;
    }

    // Returns a NEW Board with the given move applied. Does not mutate this.
    public Board apply(Move m) {
        int b = blankIndex();
        int target;
        switch (m) {
            case UP:    target = b - 3; break;
            case DOWN:  target = b + 3; break;
            case LEFT:  target = b - 1; break;
            default:    target = b + 1; break; // RIGHT
        }
        int[] next = tiles.clone();
        next[b] = next[target];
        next[target] = 0;
        return new Board(next);
    }

    public boolean isGoal() {
        return Arrays.equals(tiles, GOAL);
    }

    // Solvable iff the number of inversions among non-blank tiles is even.
    public boolean isSolvable() {
        int inversions = 0;
        for (int i = 0; i < 9; i++) {
            if (tiles[i] == 0) continue;
            for (int j = i + 1; j < 9; j++) {
                if (tiles[j] == 0) continue;
                if (tiles[i] > tiles[j]) inversions++;
            }
        }
        return inversions % 2 == 0;
    }

    // Packs 9 × 4-bit nibbles into a long — unique for all valid permutations of 0..8.
    public long hashKey() {
        long key = 0;
        for (int i = 0; i < 9; i++) {
            key = (key << 4) | tiles[i];
        }
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Board)) return false;
        return Arrays.equals(tiles, ((Board) o).tiles);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(tiles);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            sb.append(tiles[i] == 0 ? "_" : tiles[i]);
            if (i % 3 == 2) { if (i < 6) sb.append('\n'); }
            else sb.append(' ');
        }
        return sb.toString();
    }
}
