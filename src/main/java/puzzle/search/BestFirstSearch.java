package puzzle.search;

import puzzle.heuristic.Heuristic;
import puzzle.model.Board;

// Generic best-first search engine. Inject ZeroHeuristic for Uniform Cost; any other Heuristic gives an A* variant.
public final class BestFirstSearch implements SearchAlgorithm {

    private final Heuristic heuristic;
    private final String label;

    public BestFirstSearch(Heuristic heuristic, String label) {
        this.heuristic = heuristic;
        this.label = label;
    }

    public Heuristic heuristic() { return heuristic; }
    public String label()        { return label; }

    @Override
    public SearchResult solve(Board start) {
        // TODO — implementation outline (to be filled in next pass):
        //
        //   PriorityQueue<Node> frontier  ordered by (f asc, h asc)
        //   HashSet<Long>      closed
        //   HashMap<Long, Node> openMap   // best-known Node currently in frontier per state hash
        //
        //   Frontier checks BEFORE adding a neighbor (to mention in the video):
        //     1) closed-skip: if neighbor.hash in closed AND gNew >= storedG, skip.
        //     2) open-improve: if neighbor.hash in openMap AND gNew >= openMap.get.g, skip;
        //                       otherwise push the new node, mark the previous one stale.
        //
        //   Lazy deletion: when polling, if node.stale, continue.
        //   Track maxFrontier = max(maxFrontier, frontier.size()) after each push.
        //   On goal: stop, build SearchResult with reconstructed path + snapshots of frontier/closed.
        //   On unsolvable input (Board#isSolvable == false): return SearchResult with solved=false, empty path.
        throw new UnsupportedOperationException("TODO");
    }

    // Expand a node into successor nodes. TODO.
    @SuppressWarnings("unused")
    private void expand(/* Node n, ... */) {
        throw new UnsupportedOperationException("TODO");
    }
}
