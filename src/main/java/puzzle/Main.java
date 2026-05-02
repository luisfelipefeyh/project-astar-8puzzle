package puzzle;

import puzzle.heuristic.ManhattanLinearConflictHeuristic;
import puzzle.heuristic.MisplacedTilesHeuristic;
import puzzle.heuristic.OverestimateHeuristic;
import puzzle.heuristic.ZeroHeuristic;
import puzzle.io.ResultWriter;
import puzzle.model.Board;
import puzzle.search.BestFirstSearch;
import puzzle.search.SearchResult;
import puzzle.test.TestCases;

import java.io.IOException;
import java.nio.file.Paths;

// Entry point. Wires CLI -> chosen algorithm -> ResultWriter.
// Usage:
//   java -cp out puzzle.Main --algo <UCS|NON_ADMISSIBLE|MISPLACED|MANHATTAN_LC> --board "1 2 3 4 0 6 7 5 8"
//   java -cp out puzzle.Main --algo <ALGO> --case <EASY_1|MEDIUM_1|MEDIUM_2|HARD_1|HARD_2>
public final class Main {

    public static void main(String[] args) throws IOException {
        String algo = null;
        int[] boardTiles = null;

        for (int i = 0; i < args.length - 1; i++) {
            switch (args[i]) {
                case "--algo":
                    algo = args[i + 1];
                    break;
                case "--board":
                    String[] parts = args[i + 1].split(" ");
                    boardTiles = new int[9];
                    for (int j = 0; j < 9; j++) boardTiles[j] = Integer.parseInt(parts[j]);
                    break;
                case "--case":
                    boardTiles = caseBoard(args[i + 1]);
                    break;
            }
        }

        if (algo == null || boardTiles == null) {
            System.err.println("Usage:");
            System.err.println("  java -cp out puzzle.Main --algo <UCS|NON_ADMISSIBLE|MISPLACED|MANHATTAN_LC> --board \"1 2 3 4 0 6 7 5 8\"");
            System.err.println("  java -cp out puzzle.Main --algo <ALGO> --case <EASY_1|MEDIUM_1|MEDIUM_2|HARD_1|HARD_2>");
            System.exit(1);
        }

        Board board = new Board(boardTiles);
        BestFirstSearch search = buildSearch(algo);
        SearchResult result = search.solve(board);

        ResultWriter writer = new ResultWriter();
        writer.printConsoleReport(result, System.out);

        java.nio.file.Path jsonPath = Paths.get("run-" + System.currentTimeMillis() + ".json");
        writer.writeJson(result, jsonPath);
        System.out.println("JSON : " + jsonPath.toAbsolutePath());
    }

    private static BestFirstSearch buildSearch(String algo) {
        switch (algo) {
            case "UCS":            return new BestFirstSearch(new ZeroHeuristic(),                     "UCS");
            case "NON_ADMISSIBLE": return new BestFirstSearch(new OverestimateHeuristic(),             "3xManhattan");
            case "MISPLACED":      return new BestFirstSearch(new MisplacedTilesHeuristic(),           "MisplacedTiles");
            case "MANHATTAN_LC":   return new BestFirstSearch(new ManhattanLinearConflictHeuristic(),  "ManhattanLinearConflict");
            default: throw new IllegalArgumentException("Unknown algo: " + algo +
                     ". Valid: UCS, NON_ADMISSIBLE, MISPLACED, MANHATTAN_LC");
        }
    }

    private static int[] caseBoard(String name) {
        switch (name) {
            case "EASY_1":   return TestCases.EASY_1.tiles();
            case "MEDIUM_1": return TestCases.MEDIUM_1.tiles();
            case "MEDIUM_2": return TestCases.MEDIUM_2.tiles();
            case "HARD_1":   return TestCases.HARD_1.tiles();
            case "HARD_2":   return TestCases.HARD_2.tiles();
            default: throw new IllegalArgumentException("Unknown case: " + name);
        }
    }
}
