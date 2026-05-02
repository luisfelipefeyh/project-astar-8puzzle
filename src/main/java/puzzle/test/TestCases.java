package puzzle.test;

import puzzle.model.Board;

// Preset boards for the heuristic comparison / perf table in docs/RESULTS.md.
// Boards constructed by reverse-walking from goal; depths are exact.
// Goal layout (per spec): 1 2 3 / 4 5 6 / 7 8 _
public final class TestCases {

    private TestCases() {}

    // Depth 5 — constructed: goal ← UP ← RIGHT ← DOWN ← RIGHT ← RIGHT
    public static final Board EASY_1 = new Board(new int[]{
            1, 0, 3,
            5, 2, 6,
            4, 7, 8
    });

    // Depth 10
    public static final Board MEDIUM_1 = new Board(new int[]{
            5, 1, 3,
            2, 6, 8,
            4, 7, 0
    });

    // Depth 20
    public static final Board MEDIUM_2 = new Board(new int[]{
            0, 1, 8,
            5, 3, 2,
            4, 6, 7
    });

    // Depth ~27 (worst-case 8-puzzle depth is 31)
    public static final Board HARD_1 = new Board(new int[]{
            8, 6, 7,
            2, 5, 4,
            3, 0, 1
    });

    // Depth ~29
    public static final Board HARD_2 = new Board(new int[]{
            6, 4, 7,
            8, 5, 0,
            3, 2, 1
    });
}
