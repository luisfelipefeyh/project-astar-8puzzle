package puzzle;

// Entry point. Wires CLI -> chosen algorithm -> ResultWriter.
public final class Main {

    public static void main(String[] args) {
        // TODO: CLI input parsing — format to be decided.
        //   Likely shape: --algo {UCS|NON_ADMISSIBLE|MISPLACED|MANHATTAN_LC} --board "1 2 3 4 0 6 7 5 8"
        //   Alternatively: --case {EASY_1|MEDIUM_1|...} for quick runs against TestCases.
        //   Output: ResultWriter#printConsoleReport to stdout + ResultWriter#writeJson to run-<timestamp>.json.
        System.out.println("Not implemented -- see TODOs (grep -rn TODO src/ README.md)");
    }
}
