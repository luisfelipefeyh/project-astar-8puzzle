package puzzle.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Search-tree node. Holds the state, the parent, the move that produced it, and g/h.
public final class Node {

    private final Board state;
    private final Node parent;
    private final Move moveFromParent;
    private final int g;
    private final int h;

    // Mutable flag for lazy deletion in the priority queue (set true when a better path replaces this entry).
    public boolean stale;

    public Node(Board state, Node parent, Move moveFromParent, int g, int h) {
        this.state = state;
        this.parent = parent;
        this.moveFromParent = moveFromParent;
        this.g = g;
        this.h = h;
        this.stale = false;
    }

    public Board state()           { return state; }
    public Node parent()           { return parent; }
    public Move moveFromParent()   { return moveFromParent; }
    public int g()                 { return g; }
    public int h()                 { return h; }

    public int f() {
        return g + h;
    }

    public List<Move> reconstructPath() {
        List<Move> path = new ArrayList<>();
        Node current = this;
        while (current.moveFromParent != null) {
            path.add(current.moveFromParent);
            current = current.parent;
        }
        Collections.reverse(path);
        return path;
    }
}
