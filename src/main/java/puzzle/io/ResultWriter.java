package puzzle.io;

import puzzle.model.Node;
import puzzle.search.SearchResult;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

// Renders a SearchResult to the console (spec items a-d) and to a JSON file (spec item e).
public final class ResultWriter {

    public void printConsoleReport(SearchResult r, PrintStream out) {
        out.println("=== " + r.algorithmLabel() + " ===");
        if (!r.solved()) {
            out.println("No solution found.");
            return;
        }
        out.println("Path   : " + r.path());
        out.println("Length : " + r.pathLength());
        out.println("Visited: " + r.nodesVisited());
        out.println("MaxFrnt: " + r.maxFrontierSize());
        out.printf( "Time   : %.3f s%n", r.elapsedMillis() / 1000.0);
    }

    // Hand-rolled JSON — no external library. Dumps metrics + frontier/closed snapshots (spec item e).
    public void writeJson(SearchResult r, Path file) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"algorithm\": \"").append(esc(r.algorithmLabel())).append("\",\n");
        sb.append("  \"solved\": ").append(r.solved()).append(",\n");
        sb.append("  \"pathLength\": ").append(r.pathLength()).append(",\n");
        sb.append("  \"nodesVisited\": ").append(r.nodesVisited()).append(",\n");
        sb.append("  \"maxFrontierSize\": ").append(r.maxFrontierSize()).append(",\n");
        sb.append("  \"elapsedSeconds\": ")
          .append(String.format("%.3f", r.elapsedMillis() / 1000.0)).append(",\n");
        sb.append("  \"path\": [");
        if (r.path() != null) {
            for (int i = 0; i < r.path().size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append('"').append(r.path().get(i)).append('"');
            }
        }
        sb.append("],\n");
        sb.append("  \"frontierAtEnd\": [");
        List<Node> front = r.frontierAtEnd();
        for (int i = 0; i < front.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(front.get(i).state().hashKey());
        }
        sb.append("],\n");
        sb.append("  \"closedAtEnd\": [");
        List<long[]> cls = r.closedAtEnd();
        for (int i = 0; i < cls.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(cls.get(i)[0]);
        }
        sb.append("]\n}");
        Files.write(file, sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static String esc(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
