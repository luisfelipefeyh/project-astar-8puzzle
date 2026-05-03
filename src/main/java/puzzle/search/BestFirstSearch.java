package puzzle.search;

import puzzle.heuristic.Heuristic;
import puzzle.model.Board;
import puzzle.model.Move;
import puzzle.model.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;

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
        if (!start.isSolvable()) {
            return new SearchResult(label, false, Collections.emptyList(), 0, 0, 0L,
                                    Collections.emptyList(), Collections.emptyList());
        }

        long startTime = System.currentTimeMillis();

        Comparator<Node> cmp = Comparator.comparingInt(Node::f).thenComparingInt(Node::h);
        PriorityQueue<Node> frontier = new PriorityQueue<>(cmp);
        HashMap<Long, Node> openMap  = new HashMap<>();
        HashSet<Long>       closed   = new HashSet<>();

        Node root = new Node(start, null, null, 0, heuristic.estimate(start));
        frontier.add(root);
        openMap.put(start.hashKey(), root);

        int nodesVisited = 0;
        int maxFrontier  = 1;

        while (!frontier.isEmpty()) {
            Node current = frontier.poll();
            if (current.stale) continue;

            long key = current.state().hashKey();
            openMap.remove(key);

            if (closed.contains(key)) continue;
            closed.add(key);
            nodesVisited++;

            if (current.state().isGoal()) {
                long elapsed = System.currentTimeMillis() - startTime;
                List<Node> frontierSnap = new ArrayList<>(frontier);
                List<long[]> closedSnap = new ArrayList<>();
                for (long k : closed) closedSnap.add(new long[]{k});
                return new SearchResult(label, true, current.reconstructPath(),
                                        nodesVisited, maxFrontier, elapsed,
                                        frontierSnap, closedSnap);
            }

            for (Node succ : expand(current)) {
                long succKey = succ.state().hashKey();
                if (closed.contains(succKey)) continue;

                // Frontier check: only add if cheaper than any known path to this state.
                Node existing = openMap.get(succKey);
                if (existing != null && succ.g() >= existing.g()) continue;

                if (existing != null) existing.stale = true;
                frontier.add(succ);
                openMap.put(succKey, succ);
                if (frontier.size() > maxFrontier) maxFrontier = frontier.size();
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;
        return new SearchResult(label, false, Collections.emptyList(), nodesVisited, maxFrontier,
                                elapsed, Collections.emptyList(), Collections.emptyList());
    }

    private List<Node> expand(Node n) {
        List<Node> successors = new ArrayList<>();
        for (Move move : n.state().legalMoves()) {
            Board next = n.state().apply(move);
            successors.add(new Node(next, n, move, n.g() + 1, heuristic.estimate(next)));
        }
        return successors;
    }
}
