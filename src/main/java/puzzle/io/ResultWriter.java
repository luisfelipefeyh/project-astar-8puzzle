package puzzle.io;

import puzzle.search.SearchResult;

import java.io.PrintStream;
import java.nio.file.Path;

// Renders a SearchResult to the console (spec items a-d) and to a JSON file (spec item e).
public final class ResultWriter {

    // Print algorithm label, path, path length, nodes visited, max frontier size, elapsed seconds.
    public void printConsoleReport(SearchResult r, PrintStream out) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    // Hand-rolled JSON (no external library). Dumps metrics + frontierAtEnd + closedAtEnd snapshots.
    public void writeJson(SearchResult r, Path file) {
        // TODO: build the JSON with StringBuilder, then Files.writeString(file, json).
        throw new UnsupportedOperationException("TODO");
    }
}
