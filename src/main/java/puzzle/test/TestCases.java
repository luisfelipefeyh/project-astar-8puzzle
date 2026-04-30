package puzzle.test;

import puzzle.model.Board;

// Preset boards used by the comparison/perf table in docs/RESULTS.md.
// Spec requires at minimum: 2 hard, 2 medium, 1 easy for the heuristic comparison.
//
// TODO: replace placeholder arrays with vetted positions whose optimal solution depth matches the target ranges.
//       Goal layout (per spec): 1 2 3 / 4 5 6 / 7 8 0
public final class TestCases {

    private TestCases() {}

    // ~5-10 moves to solve.
    public static final Board EASY_1 = new Board(new int[]{
            1, 2, 3,
            4, 5, 6,
            0, 7, 8   // TODO: replace with a vetted easy case
    });

    // ~15-20 moves to solve.
    public static final Board MEDIUM_1 = new Board(new int[]{
            1, 2, 3,
            4, 0, 6,
            7, 5, 8   // TODO: replace with a vetted medium case
    });

    // ~15-20 moves to solve.
    public static final Board MEDIUM_2 = new Board(new int[]{
            1, 2, 3,
            4, 0, 6,
            7, 5, 8   // TODO: replace with a vetted medium case (distinct from MEDIUM_1)
    });

    // ~25-31 moves to solve. Worst-case 8-puzzle solution depth is 31.
    public static final Board HARD_1 = new Board(new int[]{
            8, 6, 7,
            2, 5, 4,
            3, 0, 1   // TODO: confirm depth and pick another if needed
    });

    // ~25-31 moves to solve.
    public static final Board HARD_2 = new Board(new int[]{
            6, 4, 7,
            8, 5, 0,
            3, 2, 1   // TODO: confirm depth and pick another if needed
    });
}
