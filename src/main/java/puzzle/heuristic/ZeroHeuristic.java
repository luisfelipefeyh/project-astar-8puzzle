package puzzle.heuristic;

import puzzle.model.Board;

// h(n) = 0 — turns A* into Uniform Cost search (variant 1 in the spec).
public final class ZeroHeuristic implements Heuristic {

    @Override
    public int estimate(Board b) {
        return 0;
    }

    @Override
    public String name() {
        return "Zero";
    }
}
