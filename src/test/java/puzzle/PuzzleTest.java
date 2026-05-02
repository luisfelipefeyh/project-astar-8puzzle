package puzzle;

public class PuzzleTest {

    private static int passed = 0;
    private static int failed = 0;

    static void check(boolean condition, String msg) {
        if (condition) {
            System.out.println("PASS: " + msg);
            passed++;
        } else {
            System.err.println("FAIL: " + msg);
            failed++;
        }
    }

    static void checkEq(Object expected, Object actual, String msg) {
        check(expected.equals(actual), msg + " — expected=" + expected + " actual=" + actual);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("--- PuzzleTest ---");
        testBoard();
        testMoves();
        testBoardGoalAndHash();
        testNode();
        testManhattan();
        testMisplaced();
        testLinearConflict();
        testSearch();
        testResultWriter();
        summary();
    }

    static void testBoard() {
        checkEq(4, new puzzle.model.Board(new int[]{1,2,3,4,0,5,7,8,6}).blankIndex(),
                "blankIndex center");
        checkEq(0, new puzzle.model.Board(new int[]{0,1,2,3,4,5,6,7,8}).blankIndex(),
                "blankIndex top-left");
        checkEq(8, new puzzle.model.Board(new int[]{1,2,3,4,5,6,7,8,0}).blankIndex(),
                "blankIndex bottom-right");

        puzzle.model.Board b1 = new puzzle.model.Board(new int[]{1,2,3,4,5,6,7,8,0});
        puzzle.model.Board b2 = new puzzle.model.Board(new int[]{1,2,3,4,5,6,7,8,0});
        puzzle.model.Board b3 = new puzzle.model.Board(new int[]{1,2,3,4,0,6,7,5,8});
        check(b1.equals(b2),   "equals same state");
        check(!b1.equals(b3),  "equals different state");
        checkEq(b1.hashCode(), b2.hashCode(), "hashCode same state");
        check(b3.toString().contains("_"), "toString uses _ for blank");
    }

    static void testMoves() {
        puzzle.model.Board topLeft = new puzzle.model.Board(new int[]{0,1,2,3,4,5,6,7,8});
        java.util.List<puzzle.model.Move> tl = topLeft.legalMoves();
        checkEq(2, tl.size(), "top-left corner: 2 moves");
        check(tl.contains(puzzle.model.Move.DOWN),  "top-left: DOWN legal");
        check(tl.contains(puzzle.model.Move.RIGHT), "top-left: RIGHT legal");

        checkEq(4, new puzzle.model.Board(new int[]{1,2,3,4,0,5,6,7,8}).legalMoves().size(),
                "center: 4 moves");

        puzzle.model.Board center = new puzzle.model.Board(new int[]{1,2,3,4,0,5,6,7,8});
        puzzle.model.Board afterUp = center.apply(puzzle.model.Move.UP);
        checkEq(0, afterUp.tiles()[1], "apply UP: blank at index 1");
        checkEq(2, afterUp.tiles()[4], "apply UP: tile 2 at index 4");
        checkEq(0, center.tiles()[4],  "apply is non-mutating");
    }

    static void testBoardGoalAndHash() {
        puzzle.model.Board goal    = new puzzle.model.Board(new int[]{1,2,3,4,5,6,7,8,0});
        puzzle.model.Board notGoal = new puzzle.model.Board(new int[]{1,2,3,4,5,6,7,0,8});
        check(goal.isGoal(),    "goal state recognized");
        check(!notGoal.isGoal(), "non-goal state rejected");

        check(goal.isSolvable(), "goal is solvable");
        // [1,2,3,4,5,0,7,8,6]: non-blank order 1,2,3,4,5,7,8,6 → pairs (7,6),(8,6) = 2 inversions → even
        check(new puzzle.model.Board(new int[]{1,2,3,4,5,0,7,8,6}).isSolvable(),
              "2-inversion board is solvable");
        check(!new puzzle.model.Board(new int[]{2,1,3,4,5,6,7,8,0}).isSolvable(),
              "1-inversion board is unsolvable");

        checkEq(goal.hashKey(),
                new puzzle.model.Board(new int[]{1,2,3,4,5,6,7,8,0}).hashKey(),
                "hashKey stable");
        check(goal.hashKey() != notGoal.hashKey(), "hashKey distinct for distinct states");
    }

    static void testNode() {
        puzzle.model.Board b0 = new puzzle.model.Board(new int[]{1,2,3,4,5,6,7,0,8});
        puzzle.model.Board b1 = new puzzle.model.Board(new int[]{1,2,3,4,5,6,0,7,8});
        puzzle.model.Board b2 = new puzzle.model.Board(new int[]{1,2,3,4,5,6,7,8,0});

        puzzle.model.Node root = new puzzle.model.Node(b0, null, null, 0, 5);
        puzzle.model.Node n1   = new puzzle.model.Node(b1, root, puzzle.model.Move.LEFT,  1, 3);
        puzzle.model.Node n2   = new puzzle.model.Node(b2, n1,   puzzle.model.Move.RIGHT, 2, 0);

        checkEq(5, root.f(), "f() root");
        checkEq(4, n1.f(),   "f() n1");
        checkEq(2, n2.f(),   "f() n2");

        java.util.List<puzzle.model.Move> path = n2.reconstructPath();
        checkEq(2, path.size(), "path length");
        checkEq(puzzle.model.Move.LEFT,  path.get(0), "path[0]");
        checkEq(puzzle.model.Move.RIGHT, path.get(1), "path[1]");
        check(root.reconstructPath().isEmpty(), "root path is empty");
    }

    static void testManhattan() {
        checkEq(0, puzzle.heuristic.ManhattanHeuristic.manhattanSum(
            new puzzle.model.Board(new int[]{1,2,3,4,5,6,7,8,0})), "manhattan(goal)=0");
        checkEq(1, puzzle.heuristic.ManhattanHeuristic.manhattanSum(
            new puzzle.model.Board(new int[]{1,2,3,4,5,6,7,0,8})), "manhattan(1-move)=1");
        // [8,7,6,5,4,3,2,1,0]: sum = 3+3+1+1+1+1+3+3 = 16
        checkEq(16, puzzle.heuristic.ManhattanHeuristic.manhattanSum(
            new puzzle.model.Board(new int[]{8,7,6,5,4,3,2,1,0})), "manhattan(reversed)=16");
    }

    static void testMisplaced() {
        puzzle.heuristic.MisplacedTilesHeuristic h = new puzzle.heuristic.MisplacedTilesHeuristic();
        checkEq(0, h.estimate(new puzzle.model.Board(new int[]{1,2,3,4,5,6,7,8,0})), "misplaced(goal)=0");
        checkEq(1, h.estimate(new puzzle.model.Board(new int[]{1,2,3,4,5,6,7,0,8})), "misplaced(1 tile)=1");
        checkEq(8, h.estimate(new puzzle.model.Board(new int[]{8,7,6,5,4,3,2,1,0})), "misplaced(all wrong)=8");
    }

    static void testLinearConflict() {
        puzzle.heuristic.ManhattanLinearConflictHeuristic h =
            new puzzle.heuristic.ManhattanLinearConflictHeuristic();

        checkEq(0, h.estimate(new puzzle.model.Board(new int[]{1,2,3,4,5,6,7,8,0})), "lc(goal)=0");

        // [3,2,1,4,5,6,7,8,0]: 3 row conflicts in row 0, manhattan=4 → lc=4+6=10
        checkEq(10, h.estimate(new puzzle.model.Board(new int[]{3,2,1,4,5,6,7,8,0})), "lc([3,2,1,...])=10");

        puzzle.model.Board complex = new puzzle.model.Board(new int[]{8,6,7,2,5,4,3,0,1});
        check(h.estimate(complex) >= puzzle.heuristic.ManhattanHeuristic.manhattanSum(complex),
              "lc >= manhattan");
    }

    static void testSearch() {
        puzzle.model.Board easy = new puzzle.model.Board(new int[]{1,0,3,5,2,6,4,7,8}); // depth 5

        puzzle.search.SearchResult r1 = new puzzle.search.BestFirstSearch(
            new puzzle.heuristic.ZeroHeuristic(), "UCS").solve(easy);
        check(r1.solved(),      "UCS solves EASY_1");
        checkEq(5, r1.pathLength(), "UCS EASY_1 path=5");

        puzzle.search.SearchResult r2 = new puzzle.search.BestFirstSearch(
            new puzzle.heuristic.MisplacedTilesHeuristic(), "Misplaced").solve(easy);
        check(r2.solved(),      "Misplaced solves EASY_1");
        checkEq(5, r2.pathLength(), "Misplaced EASY_1 path=5");

        puzzle.search.SearchResult r3 = new puzzle.search.BestFirstSearch(
            new puzzle.heuristic.ManhattanLinearConflictHeuristic(), "LC").solve(easy);
        check(r3.solved(),      "LC solves EASY_1");
        checkEq(5, r3.pathLength(), "LC EASY_1 path=5");

        puzzle.search.SearchResult bad = new puzzle.search.BestFirstSearch(
            new puzzle.heuristic.ZeroHeuristic(), "UCS")
            .solve(new puzzle.model.Board(new int[]{2,1,3,4,5,6,7,8,0}));
        check(!bad.solved(), "UCS rejects unsolvable board");
    }

    static void testResultWriter() throws Exception {
        puzzle.model.Board easy = new puzzle.model.Board(new int[]{1,0,3,5,2,6,4,7,8});
        puzzle.search.SearchResult r = new puzzle.search.BestFirstSearch(
            new puzzle.heuristic.ManhattanLinearConflictHeuristic(), "LC").solve(easy);

        puzzle.io.ResultWriter writer = new puzzle.io.ResultWriter();

        java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
        writer.printConsoleReport(r, new java.io.PrintStream(buf));
        String out = buf.toString();
        check(out.contains("LC"),      "report: algorithm label");
        check(out.contains("Length"),  "report: Length label");
        check(out.contains("Visited"), "report: Visited label");

        java.nio.file.Path tmp = java.nio.file.Files.createTempFile("puzzle-test", ".json");
        writer.writeJson(r, tmp);
        String json = new String(java.nio.file.Files.readAllBytes(tmp));
        check(json.contains("\"algorithm\""),     "json: algorithm key");
        check(json.contains("\"pathLength\""),    "json: pathLength key");
        check(json.contains("\"nodesVisited\""),  "json: nodesVisited key");
        check(json.contains("\"frontierAtEnd\""), "json: frontierAtEnd key");
        check(json.contains("\"closedAtEnd\""),   "json: closedAtEnd key");
        java.nio.file.Files.deleteIfExists(tmp);
    }

    static void summary() {
        System.out.printf("%nTotal: %d passed, %d failed%n", passed, failed);
        if (failed > 0) System.exit(1);
    }
}
