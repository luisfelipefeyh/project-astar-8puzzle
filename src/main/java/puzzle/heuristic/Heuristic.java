package puzzle.heuristic;

import puzzle.model.Board;

// Heuristic estimate of the cost-to-go from b to the goal.
public interface Heuristic {
    int estimate(Board b);
    String name();
}
