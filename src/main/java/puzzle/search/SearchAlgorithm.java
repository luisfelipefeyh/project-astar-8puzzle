package puzzle.search;

import puzzle.model.Board;

// Common entry point for every search variant (UCS and the three A* flavors).
public interface SearchAlgorithm {
    SearchResult solve(Board start);
}
