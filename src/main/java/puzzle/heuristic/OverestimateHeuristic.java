package puzzle.heuristic;

import puzzle.model.Board;

// Non-admissible heuristic (variant 2 in the spec): k * Manhattan with k > 1.
// k = 3 by default — clearly overestimates real cost so A* loses optimality but expands fewer nodes.
public final class OverestimateHeuristic implements Heuristic {

    private final int multiplier;

    public OverestimateHeuristic() {
        this(3);
    }

    public OverestimateHeuristic(int multiplier) {
        // TODO: validate multiplier > 1 (otherwise it would still be admissible).
        this.multiplier = multiplier;
    }

    @Override
    public int estimate(Board b) {
        return ManhattanHeuristic.manhattanSum(b) * multiplier;
    }

    @Override
    public String name() {
        return multiplier + "xManhattan";
    }
}
