package puzzle.search;

import puzzle.model.Move;
import puzzle.model.Node;

import java.util.List;

// Output bundle for a single search run. Maps 1:1 to spec items (a)-(d) plus the path and a snapshot for item (e).
public final class SearchResult {

    private final String algorithmLabel;
    private final boolean solved;
    private final List<Move> path;             // sequence of blank moves from start to goal
    private final int nodesVisited;            // (a)
    private final int maxFrontierSize;         // (d)
    private final long elapsedMillis;          // (c) — pathLength = path.size() covers (b)
    private final List<Node> frontierAtEnd;    // (e) snapshot
    private final List<long[]> closedAtEnd;    // (e) snapshot — TODO: decide final shape (long hash list vs full boards)

    public SearchResult(String algorithmLabel,
                        boolean solved,
                        List<Move> path,
                        int nodesVisited,
                        int maxFrontierSize,
                        long elapsedMillis,
                        List<Node> frontierAtEnd,
                        List<long[]> closedAtEnd) {
        this.algorithmLabel = algorithmLabel;
        this.solved = solved;
        this.path = path;
        this.nodesVisited = nodesVisited;
        this.maxFrontierSize = maxFrontierSize;
        this.elapsedMillis = elapsedMillis;
        this.frontierAtEnd = frontierAtEnd;
        this.closedAtEnd = closedAtEnd;
    }

    public String algorithmLabel()       { return algorithmLabel; }
    public boolean solved()              { return solved; }
    public List<Move> path()             { return path; }
    public int pathLength()              { return path == null ? -1 : path.size(); }
    public int nodesVisited()            { return nodesVisited; }
    public int maxFrontierSize()         { return maxFrontierSize; }
    public long elapsedMillis()          { return elapsedMillis; }
    public List<Node> frontierAtEnd()    { return frontierAtEnd; }
    public List<long[]> closedAtEnd()    { return closedAtEnd; }
}
