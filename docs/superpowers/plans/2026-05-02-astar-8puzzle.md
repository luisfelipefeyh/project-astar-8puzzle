# A* 8-Puzzle Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement a fully functional A* 8-puzzle solver in plain Java supporting 4 search variants (UCS, non-admissible A*, admissible A*, Manhattan+LinearConflict A*) with console output and JSON dump per the UFSC assignment spec.

**Architecture:** A single `BestFirstSearch` engine is parameterized by a `Heuristic` — injecting `ZeroHeuristic` produces UCS; any other gives an A* variant. A `PriorityQueue` (frontier), `HashSet<Long>` (closed), and `HashMap<Long,Node>` (open map for duplicate detection) form the frontier management layer. `solve()` collects all metrics and returns a `SearchResult`; `ResultWriter` renders it to console and JSON.

**Tech Stack:** Plain Java 8+, no external libraries. Build: `javac`/`java`. Tests: JUnit-free runner using `AssertionError` via a custom `check()` helper.

---

## File Map

| Action | Path | Responsibility |
|--------|------|----------------|
| CREATE | `src/test/java/puzzle/PuzzleTest.java` | Assertion-based test runner (no JUnit) |
| MODIFY | `src/main/java/puzzle/model/Board.java` | Implement all stubs |
| MODIFY | `src/main/java/puzzle/model/Node.java` | Implement `f()`, `reconstructPath()` |
| MODIFY | `src/main/java/puzzle/heuristic/ManhattanHeuristic.java` | Implement `manhattanSum()` |
| MODIFY | `src/main/java/puzzle/heuristic/MisplacedTilesHeuristic.java` | Implement `estimate()` |
| MODIFY | `src/main/java/puzzle/heuristic/ManhattanLinearConflictHeuristic.java` | Implement `estimate()` + helpers |
| MODIFY | `src/main/java/puzzle/search/BestFirstSearch.java` | Implement `solve()`, `expand()` |
| MODIFY | `src/main/java/puzzle/io/ResultWriter.java` | Implement `printConsoleReport()`, `writeJson()` |
| MODIFY | `src/main/java/puzzle/Main.java` | Implement CLI |
| MODIFY | `src/main/java/puzzle/test/TestCases.java` | Replace placeholder boards with vetted configs |
| MODIFY | `docs/RESULTS.md` | Fill comparison and performance tables |

---

## Task 1: Test infrastructure

**Files:**
- Create: `src/test/java/puzzle/PuzzleTest.java`

- [ ] **Step 1: Create the test runner skeleton**

```java
package puzzle;

public class PuzzleTest {

    private static int passed = 0;
    private static int failed = 0;

    static void check(boolean condition, String msg) {
        if (condition) {
            System.out.println("PASS: " + msg);
            passed++;
        } else {
            System.err.println("FAIL: " + msg);
            failed++;
        }
    }

    static void checkEq(Object expected, Object actual, String msg) {
        check(expected.equals(actual), msg + " — expected=" + expected + " actual=" + actual);
    }

    public static void main(String[] args) {
        System.out.println("--- PuzzleTest ---");
        // Test methods will be called here as they are added in subsequent tasks.
        summary();
    }

    static void summary() {
        System.out.printf("%nTotal: %d passed, %d failed%n", passed, failed);
        if (failed > 0) System.exit(1);
    }
}
```

- [ ] **Step 2: Compile and verify the runner starts cleanly**

```bash
javac -d out $(find src -name "*.java")
java -cp out puzzle.PuzzleTest
```

Expected output:
```
--- PuzzleTest ---

Total: 0 passed, 0 failed
```

- [ ] **Step 3: Commit**

```bash
git add src/test/java/puzzle/PuzzleTest.java
git commit -m "test: add empty PuzzleTest runner"
```

---

## Task 2: Board — blankIndex, equals, hashCode, toString

**Files:**
- Modify: `src/main/java/puzzle/model/Board.java`
- Modify: `src/test/java/puzzle/PuzzleTest.java`

- [ ] **Step 1: Write failing tests — add testBoard() to PuzzleTest and call it from main()**

Add this method to `PuzzleTest.java` and add `testBoard();` at the top of `main()` before `summary()`:

```java
static void testBoard() {
    // blankIndex
    int[] t1 = {1,2,3,4,0,5,7,8,6};
    checkEq(4, new puzzle.model.Board(t1).blankIndex(), "blankIndex center");

    int[] t2 = {0,1,2,3,4,5,6,7,8};
    checkEq(0, new puzzle.model.Board(t2).blankIndex(), "blankIndex top-left");

    int[] t3 = {1,2,3,4,5,6,7,8,0};
    checkEq(8, new puzzle.model.Board(t3).blankIndex(), "blankIndex bottom-right");

    // equals and hashCode
    puzzle.model.Board b1 = new puzzle.model.Board(new int[]{1,2,3,4,5,6,7,8,0});
    puzzle.model.Board b2 = new puzzle.model.Board(new int[]{1,2,3,4,5,6,7,8,0});
    puzzle.model.Board b3 = new puzzle.model.Board(new int[]{1,2,3,4,0,6,7,5,8});
    check(b1.equals(b2), "equals same state");
    check(!b1.equals(b3), "equals different state");
    checkEq(b1.hashCode(), b2.hashCode(), "hashCode same state");

    // toString contains underscore for blank
    check(b3.toString().contains("_"), "toString uses _ for blank");
}
```

- [ ] **Step 2: Run to confirm failures**

```bash
javac -d out $(find src -name "*.java") && java -cp out puzzle.PuzzleTest
```

Expected: multiple FAIL lines, exit code 1.

- [ ] **Step 3: Implement blankIndex, equals, hashCode, toString in Board.java**

Add imports at the top of `Board.java`:
```java
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
```

Replace the `blankIndex()` stub:
```java
public int blankIndex() {
    for (int i = 0; i < 9; i++) {
        if (tiles[i] == 0) return i;
    }
    throw new IllegalStateException("no blank tile");
}
```

Replace the `equals()` stub:
```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Board)) return false;
    return Arrays.equals(tiles, ((Board) o).tiles);
}
```

Replace the `hashCode()` stub:
```java
@Override
public int hashCode() {
    return Arrays.hashCode(tiles);
}
```

Replace the `toString()` stub:
```java
@Override
public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 9; i++) {
        sb.append(tiles[i] == 0 ? "_" : tiles[i]);
        if (i % 3 == 2) { if (i < 6) sb.append('\n'); }
        else sb.append(' ');
    }
    return sb.toString();
}
```

- [ ] **Step 4: Run tests to verify they pass**

```bash
javac -d out $(find src -name "*.java") && java -cp out puzzle.PuzzleTest
```

Expected: all PASS, exit code 0.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/puzzle/model/Board.java src/test/java/puzzle/PuzzleTest.java
git commit -m "feat: implement Board blankIndex, equals, hashCode, toString"
```

---

## Task 3: Board — legalMoves, apply

**Files:**
- Modify: `src/main/java/puzzle/model/Board.java`
- Modify: `src/test/java/puzzle/PuzzleTest.java`

- [ ] **Step 1: Write failing tests — add testMoves() and call it from main()**

```java
static void testMoves() {
    // Corner (top-left, blank at 0): only DOWN and RIGHT
    puzzle.model.Board topLeft = new puzzle.model.Board(new int[]{0,1,2,3,4,5,6,7,8});
    java.util.List<puzzle.model.Move> tl = topLeft.legalMoves();
    checkEq(2, tl.size(), "top-left corner: 2 moves");
    check(tl.contains(puzzle.model.Move.DOWN), "top-left: DOWN legal");
    check(tl.contains(puzzle.model.Move.RIGHT), "top-left: RIGHT legal");

    // Center (blank at 4): all 4 moves legal
    puzzle.model.Board center = new puzzle.model.Board(new int[]{1,2,3,4,0,5,6,7,8});
    checkEq(4, center.legalMoves().size(), "center: 4 moves");

    // apply UP from center: blank moves up (to index 1), tile 2 comes down (to index 4)
    puzzle.model.Board afterUp = center.apply(puzzle.model.Move.UP);
    checkEq(0, afterUp.tiles()[1], "apply UP: blank at index 1");
    checkEq(2, afterUp.tiles()[4], "apply UP: tile 2 at index 4");

    // apply does not mutate original
    checkEq(0, center.tiles()[4], "apply is non-mutating");
}
```

Add `testMoves();` to `main()`.

- [ ] **Step 2: Run to confirm failures**

```bash
javac -d out $(find src -name "*.java") && java -cp out puzzle.PuzzleTest
```

Expected: FAIL on testMoves cases.

- [ ] **Step 3: Implement legalMoves and apply in Board.java**

Replace the `legalMoves()` stub:
```java
public List<Move> legalMoves() {
    List<Move> moves = new ArrayList<>();
    int b = blankIndex();
    int row = b / 3, col = b % 3;
    if (row > 0) moves.add(Move.UP);
    if (row < 2) moves.add(Move.DOWN);
    if (col > 0) moves.add(Move.LEFT);
    if (col < 2) moves.add(Move.RIGHT);
    return moves;
}
```

Replace the `apply()` stub:
```java
public Board apply(Move m) {
    int b = blankIndex();
    int target;
    switch (m) {
        case UP:    target = b - 3; break;
        case DOWN:  target = b + 3; break;
        case LEFT:  target = b - 1; break;
        default:    target = b + 1; break; // RIGHT
    }
    int[] next = tiles.clone();
    next[b] = next[target];
    next[target] = 0;
    return new Board(next);
}
```

- [ ] **Step 4: Run tests to verify they pass**

```bash
javac -d out $(find src -name "*.java") && java -cp out puzzle.PuzzleTest
```

Expected: all PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/puzzle/model/Board.java src/test/java/puzzle/PuzzleTest.java
git commit -m "feat: implement Board legalMoves and apply"
```

---

## Task 4: Board — isGoal, isSolvable, hashKey

**Files:**
- Modify: `src/main/java/puzzle/model/Board.java`
- Modify: `src/test/java/puzzle/PuzzleTest.java`

- [ ] **Step 1: Write failing tests — add testBoardGoalAndHash() and call from main()**

```java
static void testBoardGoalAndHash() {
    puzzle.model.Board goal = new puzzle.model.Board(new int[]{1,2,3,4,5,6,7,8,0});
    check(goal.isGoal(), "goal state recognized");

    puzzle.model.Board notGoal = new puzzle.model.Board(new int[]{1,2,3,4,5,6,7,0,8});
    check(!notGoal.isGoal(), "non-goal state rejected");

    // Solvable: 0 inversions (goal itself)
    check(goal.isSolvable(), "goal is solvable");

    // Solvable: 2 inversions → even
    puzzle.model.Board two = new puzzle.model.Board(new int[]{1,2,3,4,5,6,0,8,7});
    check(two.isSolvable(), "2-inversion board is solvable");

    // Unsolvable: 1 inversion (swap 1 and 2 in goal) → odd
    puzzle.model.Board one = new puzzle.model.Board(new int[]{2,1,3,4,5,6,7,8,0});
    check(!one.isSolvable(), "1-inversion board is unsolvable");

    // hashKey: identical boards produce identical keys
    checkEq(goal.hashKey(), new puzzle.model.Board(new int[]{1,2,3,4,5,6,7,8,0}).hashKey(),
            "hashKey stable");

    // hashKey: different boards produce different keys
    check(goal.hashKey() != notGoal.hashKey(), "hashKey distinct for distinct states");
}
```

Add `testBoardGoalAndHash();` to `main()`.

- [ ] **Step 2: Run to confirm failures**

```bash
javac -d out $(find src -name "*.java") && java -cp out puzzle.PuzzleTest
```

- [ ] **Step 3: Implement isGoal, isSolvable, hashKey in Board.java**

Add the goal constant after the `tiles` field declaration:
```java
private static final int[] GOAL = {1, 2, 3, 4, 5, 6, 7, 8, 0};
```

Replace the `isGoal()` stub:
```java
public boolean isGoal() {
    return Arrays.equals(tiles, GOAL);
}
```

Replace the `isSolvable()` stub (count inversions among non-blank tiles; even count = solvable):
```java
public boolean isSolvable() {
    int inversions = 0;
    for (int i = 0; i < 9; i++) {
        if (tiles[i] == 0) continue;
        for (int j = i + 1; j < 9; j++) {
            if (tiles[j] == 0) continue;
            if (tiles[i] > tiles[j]) inversions++;
        }
    }
    return inversions % 2 == 0;
}
```

Replace the `hashKey()` stub (packs 9 × 4-bit nibbles into a long; unique for all valid permutations):
```java
public long hashKey() {
    long key = 0;
    for (int i = 0; i < 9; i++) {
        key = (key << 4) | tiles[i];
    }
    return key;
}
```

- [ ] **Step 4: Run tests to verify they pass**

```bash
javac -d out $(find src -name "*.java") && java -cp out puzzle.PuzzleTest
```

Expected: all PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/puzzle/model/Board.java src/test/java/puzzle/PuzzleTest.java
git commit -m "feat: implement Board isGoal, isSolvable, hashKey"
```

---

## Task 5: Node — f(), reconstructPath()

**Files:**
- Modify: `src/main/java/puzzle/model/Node.java`
- Modify: `src/test/java/puzzle/PuzzleTest.java`

- [ ] **Step 1: Write failing tests — add testNode() and call from main()**

```java
static void testNode() {
    puzzle.model.Board b0 = new puzzle.model.Board(new int[]{1,2,3,4,5,6,7,0,8}); // 1 move from goal
    puzzle.model.Board b1 = new puzzle.model.Board(new int[]{1,2,3,4,5,6,0,7,8}); // 2 moves from goal
    puzzle.model.Board b2 = new puzzle.model.Board(new int[]{1,2,3,4,5,6,7,8,0}); // goal

    puzzle.model.Node root = new puzzle.model.Node(b0, null, null, 0, 5);
    puzzle.model.Node n1   = new puzzle.model.Node(b1, root, puzzle.model.Move.LEFT, 1, 3);
    puzzle.model.Node n2   = new puzzle.model.Node(b2, n1,   puzzle.model.Move.RIGHT, 2, 0);

    checkEq(5, root.f(), "f() = g+h for root");
    checkEq(4, n1.f(),   "f() = g+h for n1");
    checkEq(2, n2.f(),   "f() = g+h for n2");

    java.util.List<puzzle.model.Move> path = n2.reconstructPath();
    checkEq(2, path.size(), "path length");
    checkEq(puzzle.model.Move.LEFT,  path.get(0), "path[0] = LEFT");
    checkEq(puzzle.model.Move.RIGHT, path.get(1), "path[1] = RIGHT");

    check(root.reconstructPath().isEmpty(), "root path is empty");
}
```

Add `testNode();` to `main()`.

- [ ] **Step 2: Run to confirm failures**

```bash
javac -d out $(find src -name "*.java") && java -cp out puzzle.PuzzleTest
```

- [ ] **Step 3: Implement f() and reconstructPath() in Node.java**

Add imports to `Node.java`:
```java
import java.util.ArrayList;
import java.util.Collections;
```

Replace the `f()` stub:
```java
public int f() {
    return g + h;
}
```

Replace the `reconstructPath()` stub:
```java
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
```

- [ ] **Step 4: Run tests to verify they pass**

```bash
javac -d out $(find src -name "*.java") && java -cp out puzzle.PuzzleTest
```

Expected: all PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/puzzle/model/Node.java src/test/java/puzzle/PuzzleTest.java
git commit -m "feat: implement Node f() and reconstructPath()"
```

---

## Task 6: ManhattanHeuristic — manhattanSum()

**Files:**
- Modify: `src/main/java/puzzle/heuristic/ManhattanHeuristic.java`
- Modify: `src/test/java/puzzle/PuzzleTest.java`

**Key formula:** Tile `t` (1–8) has goal position `goalRow = (t-1)/3`, `goalCol = (t-1)%3`. Sum `|curRow - goalRow| + |curCol - goalCol|` for all non-blank tiles.

- [ ] **Step 1: Write failing tests — add testManhattan() and call from main()**

```java
static void testManhattan() {
    // Goal state: h=0
    checkEq(0, puzzle.heuristic.ManhattanHeuristic.manhattanSum(
        new puzzle.model.Board(new int[]{1,2,3,4,5,6,7,8,0})), "manhattan(goal)=0");

    // 1 move from goal (blank moved left): tile 8 is misplaced by 1
    checkEq(1, puzzle.heuristic.ManhattanHeuristic.manhattanSum(
        new puzzle.model.Board(new int[]{1,2,3,4,5,6,7,0,8})), "manhattan(1-move)=1");

    // All tiles reversed [8,7,6,5,4,3,2,1,0]: known manhattan = 16
    // tile 8 at (0,0) goal (2,1) → |0-2|+|0-1|=3
    // tile 7 at (0,1) goal (2,0) → |0-2|+|1-0|=3
    // tile 6 at (0,2) goal (1,2) → |0-1|+|2-2|=1
    // tile 5 at (1,0) goal (1,1) → |1-1|+|0-1|=1
    // tile 4 at (1,1) goal (1,0) → |1-1|+|1-0|=1
    // tile 3 at (1,2) goal (0,2) → |1-0|+|2-2|=1
    // tile 2 at (2,0) goal (0,1) → |2-0|+|0-1|=3
    // tile 1 at (2,1) goal (0,0) → |2-0|+|1-0|=3
    // total = 3+3+1+1+1+1+3+3 = 16
    checkEq(16, puzzle.heuristic.ManhattanHeuristic.manhattanSum(
        new puzzle.model.Board(new int[]{8,7,6,5,4,3,2,1,0})), "manhattan(reversed)=16");
}
```

Add `testManhattan();` to `main()`.

- [ ] **Step 2: Run to confirm failures**

```bash
javac -d out $(find src -name "*.java") && java -cp out puzzle.PuzzleTest
```

- [ ] **Step 3: Implement manhattanSum() in ManhattanHeuristic.java**

Replace the `manhattanSum()` stub:
```java
public static int manhattanSum(Board b) {
    int[] tiles = b.tiles();
    int sum = 0;
    for (int i = 0; i < 9; i++) {
        int tile = tiles[i];
        if (tile == 0) continue;
        int goalRow = (tile - 1) / 3;
        int goalCol = (tile - 1) % 3;
        sum += Math.abs(i / 3 - goalRow) + Math.abs(i % 3 - goalCol);
    }
    return sum;
}
```

- [ ] **Step 4: Run tests to verify they pass**

```bash
javac -d out $(find src -name "*.java") && java -cp out puzzle.PuzzleTest
```

Expected: all PASS. Note: `OverestimateHeuristic.estimate()` now also works since it delegates to `manhattanSum`.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/puzzle/heuristic/ManhattanHeuristic.java src/test/java/puzzle/PuzzleTest.java
git commit -m "feat: implement ManhattanHeuristic.manhattanSum()"
```

---

## Task 7: MisplacedTilesHeuristic — estimate()

**Files:**
- Modify: `src/main/java/puzzle/heuristic/MisplacedTilesHeuristic.java`
- Modify: `src/test/java/puzzle/PuzzleTest.java`

**Key rule:** Count non-blank tiles where `tiles[i] != i+1` (for i=0..7, goal tile at position i is i+1; blank is always excluded).

- [ ] **Step 1: Write failing tests — add testMisplaced() and call from main()**

```java
static void testMisplaced() {
    puzzle.heuristic.MisplacedTilesHeuristic h = new puzzle.heuristic.MisplacedTilesHeuristic();

    checkEq(0, h.estimate(
        new puzzle.model.Board(new int[]{1,2,3,4,5,6,7,8,0})), "misplaced(goal)=0");

    // 1 tile misplaced: blank moves left, tile 8 at index 7 instead of 8 → 1 misplaced
    checkEq(1, h.estimate(
        new puzzle.model.Board(new int[]{1,2,3,4,5,6,7,0,8})), "misplaced(1 tile)=1");

    // [8,7,6,5,4,3,2,1,0]: all 8 non-blank tiles are misplaced
    checkEq(8, h.estimate(
        new puzzle.model.Board(new int[]{8,7,6,5,4,3,2,1,0})), "misplaced(all wrong)=8");
}
```

Add `testMisplaced();` to `main()`.

- [ ] **Step 2: Run to confirm failures**

```bash
javac -d out $(find src -name "*.java") && java -cp out puzzle.PuzzleTest
```

- [ ] **Step 3: Implement estimate() in MisplacedTilesHeuristic.java**

Replace the `estimate()` stub:
```java
@Override
public int estimate(Board b) {
    int[] tiles = b.tiles();
    int count = 0;
    for (int i = 0; i < 8; i++) {
        if (tiles[i] != 0 && tiles[i] != i + 1) count++;
    }
    // Position 8 must hold 0 in goal; any non-blank tile there is misplaced
    if (tiles[8] != 0) count++;
    return count;
}
```

- [ ] **Step 4: Run tests to verify they pass**

```bash
javac -d out $(find src -name "*.java") && java -cp out puzzle.PuzzleTest
```

Expected: all PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/puzzle/heuristic/MisplacedTilesHeuristic.java src/test/java/puzzle/PuzzleTest.java
git commit -m "feat: implement MisplacedTilesHeuristic.estimate()"
```

---

## Task 8: ManhattanLinearConflictHeuristic — estimate() + helpers

**Files:**
- Modify: `src/main/java/puzzle/heuristic/ManhattanLinearConflictHeuristic.java`
- Modify: `src/test/java/puzzle/PuzzleTest.java`

**Key rules:**
- A row conflict: two tiles `ti` (at col1) and `tj` (at col2 > col1) are both in their goal row, but `goalCol(ti) > goalCol(tj)`. Each conflict pair adds 2.
- A col conflict: same idea across rows in the same column.
- `h = manhattanSum + 2 * rowConflicts + 2 * colConflicts`
- Always `>= manhattan` (admissible because each conflict requires at least 2 extra moves).

- [ ] **Step 1: Write failing tests — add testLinearConflict() and call from main()**

```java
static void testLinearConflict() {
    puzzle.heuristic.ManhattanLinearConflictHeuristic h =
        new puzzle.heuristic.ManhattanLinearConflictHeuristic();

    checkEq(0, h.estimate(
        new puzzle.model.Board(new int[]{1,2,3,4,5,6,7,8,0})), "lc(goal)=0");

    // [3,2,1,4,5,6,7,8,0]: tiles 1,2,3 all in row 0 with goal row 0, all reversed
    // Conflicts: (3 at col0, 2 at col1): goalCol(3)=2 > goalCol(2)=1 → conflict
    //            (3 at col0, 1 at col2): goalCol(3)=2 > goalCol(1)=0 → conflict
    //            (2 at col1, 1 at col2): goalCol(2)=1 > goalCol(1)=0 → conflict
    // rowConflicts = 3, colConflicts = 0
    // manhattan: tile3 at(0,0) goal(0,2)→2, tile2 at(0,1) goal(0,1)→0, tile1 at(0,2) goal(0,0)→2 = 4
    // lc = 4 + 2*3 = 10
    checkEq(10, h.estimate(
        new puzzle.model.Board(new int[]{3,2,1,4,5,6,7,8,0})), "lc([3,2,1,...])=10");

    // lc must always >= manhattan
    puzzle.model.Board complex = new puzzle.model.Board(new int[]{8,6,7,2,5,4,3,0,1});
    check(h.estimate(complex) >= puzzle.heuristic.ManhattanHeuristic.manhattanSum(complex),
          "lc >= manhattan for complex board");
}
```

Add `testLinearConflict();` to `main()`.

- [ ] **Step 2: Run to confirm failures**

```bash
javac -d out $(find src -name "*.java") && java -cp out puzzle.PuzzleTest
```

- [ ] **Step 3: Implement estimate(), countRowConflicts(), countColConflicts() in ManhattanLinearConflictHeuristic.java**

Replace the entire file body (keep the class declaration):

```java
@Override
public int estimate(Board b) {
    return ManhattanHeuristic.manhattanSum(b)
         + 2 * countRowConflicts(b)
         + 2 * countColConflicts(b);
}

@Override
public String name() {
    return "ManhattanLinearConflict";
}

private static int countRowConflicts(Board b) {
    int[] tiles = b.tiles();
    int conflicts = 0;
    for (int row = 0; row < 3; row++) {
        for (int col1 = 0; col1 < 3; col1++) {
            int ti = tiles[row * 3 + col1];
            if (ti == 0 || (ti - 1) / 3 != row) continue;
            for (int col2 = col1 + 1; col2 < 3; col2++) {
                int tj = tiles[row * 3 + col2];
                if (tj == 0 || (tj - 1) / 3 != row) continue;
                if ((ti - 1) % 3 > (tj - 1) % 3) conflicts++;
            }
        }
    }
    return conflicts;
}

private static int countColConflicts(Board b) {
    int[] tiles = b.tiles();
    int conflicts = 0;
    for (int col = 0; col < 3; col++) {
        for (int row1 = 0; row1 < 3; row1++) {
            int ti = tiles[row1 * 3 + col];
            if (ti == 0 || (ti - 1) % 3 != col) continue;
            for (int row2 = row1 + 1; row2 < 3; row2++) {
                int tj = tiles[row2 * 3 + col];
                if (tj == 0 || (tj - 1) % 3 != col) continue;
                if ((ti - 1) / 3 > (tj - 1) / 3) conflicts++;
            }
        }
    }
    return conflicts;
}
```

- [ ] **Step 4: Run tests to verify they pass**

```bash
javac -d out $(find src -name "*.java") && java -cp out puzzle.PuzzleTest
```

Expected: all PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/puzzle/heuristic/ManhattanLinearConflictHeuristic.java src/test/java/puzzle/PuzzleTest.java
git commit -m "feat: implement ManhattanLinearConflictHeuristic"
```

---

## Task 9: BestFirstSearch — solve() + expand()

**Files:**
- Modify: `src/main/java/puzzle/search/BestFirstSearch.java`
- Modify: `src/test/java/puzzle/PuzzleTest.java`

**Frontier management (key for the video):**
1. After popping: skip stale nodes (`node.stale == true`).
2. Before pushing a neighbor: if already in `closed`, skip. If already in `openMap` with `gExisting <= gNew`, skip; otherwise mark existing stale and push the cheaper node.
3. `maxFrontierSize` is updated after every push.

- [ ] **Step 1: Write failing tests — add testSearch() and call from main()**

```java
static void testSearch() {
    // EASY_1 [1,0,3,5,2,6,4,7,8] is 5 moves from goal — all 4 algorithms should solve it
    puzzle.model.Board easy = new puzzle.model.Board(new int[]{1,0,3,5,2,6,4,7,8});

    puzzle.search.SearchResult r1 = new puzzle.search.BestFirstSearch(
        new puzzle.heuristic.ZeroHeuristic(), "UCS").solve(easy);
    check(r1.solved(), "UCS solves EASY_1");
    checkEq(5, r1.pathLength(), "UCS EASY_1 optimal path=5");

    puzzle.search.SearchResult r2 = new puzzle.search.BestFirstSearch(
        new puzzle.heuristic.MisplacedTilesHeuristic(), "Misplaced").solve(easy);
    check(r2.solved(), "Misplaced solves EASY_1");
    checkEq(5, r2.pathLength(), "Misplaced EASY_1 optimal path=5");

    puzzle.search.SearchResult r3 = new puzzle.search.BestFirstSearch(
        new puzzle.heuristic.ManhattanLinearConflictHeuristic(), "LC").solve(easy);
    check(r3.solved(), "LC solves EASY_1");
    checkEq(5, r3.pathLength(), "LC EASY_1 optimal path=5");

    // Unsolvable board: 1 inversion
    puzzle.model.Board unsolvable = new puzzle.model.Board(new int[]{2,1,3,4,5,6,7,8,0});
    puzzle.search.SearchResult bad = new puzzle.search.BestFirstSearch(
        new puzzle.heuristic.ZeroHeuristic(), "UCS").solve(unsolvable);
    check(!bad.solved(), "UCS rejects unsolvable board");
}
```

Add `testSearch();` to `main()`.

- [ ] **Step 2: Run to confirm failures**

```bash
javac -d out $(find src -name "*.java") && java -cp out puzzle.PuzzleTest
```

- [ ] **Step 3: Add imports to BestFirstSearch.java**

Replace the existing imports block at the top of `BestFirstSearch.java`:
```java
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
```

- [ ] **Step 4: Implement expand() in BestFirstSearch.java**

Replace the `expand()` stub (fix the signature and implement):
```java
private List<Node> expand(Node n) {
    List<Node> successors = new ArrayList<>();
    for (Move move : n.state().legalMoves()) {
        Board next = n.state().apply(move);
        int gNew = n.g() + 1;
        int hNew = heuristic.estimate(next);
        successors.add(new Node(next, n, move, gNew, hNew));
    }
    return successors;
}
```

- [ ] **Step 5: Implement solve() in BestFirstSearch.java**

Replace the `solve()` stub:
```java
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

    int nodesVisited   = 0;
    int maxFrontier    = 1;

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
```

- [ ] **Step 6: Run tests to verify they pass**

```bash
javac -d out $(find src -name "*.java") && java -cp out puzzle.PuzzleTest
```

Expected: all PASS. If EASY_1 path length fails, double-check the board `[1,0,3,5,2,6,4,7,8]` and trace it manually (it was constructed as 5 moves from goal in this plan).

- [ ] **Step 7: Commit**

```bash
git add src/main/java/puzzle/search/BestFirstSearch.java src/test/java/puzzle/PuzzleTest.java
git commit -m "feat: implement BestFirstSearch.solve() and expand()"
```

---

## Task 10: ResultWriter — printConsoleReport() + writeJson()

**Files:**
- Modify: `src/main/java/puzzle/io/ResultWriter.java`
- Modify: `src/test/java/puzzle/PuzzleTest.java`

- [ ] **Step 1: Write failing tests — add testResultWriter() and call from main()**

```java
static void testResultWriter() throws Exception {
    puzzle.model.Board easy = new puzzle.model.Board(new int[]{1,0,3,5,2,6,4,7,8});
    puzzle.search.SearchResult r = new puzzle.search.BestFirstSearch(
        new puzzle.heuristic.ManhattanLinearConflictHeuristic(), "LC").solve(easy);

    puzzle.io.ResultWriter writer = new puzzle.io.ResultWriter();

    // Console report must contain key labels
    java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
    writer.printConsoleReport(r, new java.io.PrintStream(buf));
    String out = buf.toString();
    check(out.contains("LC"),       "report contains algorithm label");
    check(out.contains("Length"),   "report contains path length label");
    check(out.contains("Visited"),  "report contains nodes visited label");

    // JSON file is written and contains expected keys
    java.nio.file.Path tmp = java.nio.file.Files.createTempFile("puzzle-test", ".json");
    writer.writeJson(r, tmp);
    String json = new String(java.nio.file.Files.readAllBytes(tmp));
    check(json.contains("\"algorithm\""),      "json has algorithm key");
    check(json.contains("\"pathLength\""),     "json has pathLength key");
    check(json.contains("\"nodesVisited\""),   "json has nodesVisited key");
    check(json.contains("\"frontierAtEnd\""),  "json has frontierAtEnd key");
    check(json.contains("\"closedAtEnd\""),    "json has closedAtEnd key");
    java.nio.file.Files.deleteIfExists(tmp);
}
```

Add `testResultWriter();` to `main()` (the method now throws Exception — update main signature to `public static void main(String[] args) throws Exception`).

- [ ] **Step 2: Run to confirm failures**

```bash
javac -d out $(find src -name "*.java") && java -cp out puzzle.PuzzleTest
```

- [ ] **Step 3: Update imports in ResultWriter.java**

Replace the existing imports:
```java
import puzzle.model.Node;
import puzzle.search.SearchResult;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
```

- [ ] **Step 4: Implement printConsoleReport() in ResultWriter.java**

Replace the stub:
```java
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
```

- [ ] **Step 5: Implement writeJson() in ResultWriter.java — add `throws IOException` to the signature**

Replace `public void writeJson(SearchResult r, Path file)` with:
```java
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
```

- [ ] **Step 6: Run tests to verify they pass**

```bash
javac -d out $(find src -name "*.java") && java -cp out puzzle.PuzzleTest
```

Expected: all PASS.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/puzzle/io/ResultWriter.java src/test/java/puzzle/PuzzleTest.java
git commit -m "feat: implement ResultWriter printConsoleReport and writeJson"
```

---

## Task 11: Main — CLI wiring

**Files:**
- Modify: `src/main/java/puzzle/Main.java`

- [ ] **Step 1: Implement Main.java**

Replace the entire file:
```java
package puzzle;

import puzzle.heuristic.ManhattanLinearConflictHeuristic;
import puzzle.heuristic.MisplacedTilesHeuristic;
import puzzle.heuristic.OverestimateHeuristic;
import puzzle.heuristic.ZeroHeuristic;
import puzzle.io.ResultWriter;
import puzzle.model.Board;
import puzzle.search.BestFirstSearch;
import puzzle.search.SearchResult;
import puzzle.test.TestCases;

import java.io.IOException;
import java.nio.file.Paths;

public final class Main {

    public static void main(String[] args) throws IOException {
        String algo = null;
        int[] boardTiles = null;

        for (int i = 0; i < args.length - 1; i++) {
            switch (args[i]) {
                case "--algo":
                    algo = args[i + 1];
                    break;
                case "--board":
                    String[] parts = args[i + 1].split(" ");
                    boardTiles = new int[9];
                    for (int j = 0; j < 9; j++) boardTiles[j] = Integer.parseInt(parts[j]);
                    break;
                case "--case":
                    boardTiles = caseBoard(args[i + 1]);
                    break;
            }
        }

        if (algo == null || boardTiles == null) {
            System.err.println("Usage:");
            System.err.println("  java -cp out puzzle.Main --algo <UCS|NON_ADMISSIBLE|MISPLACED|MANHATTAN_LC> --board \"1 2 3 4 0 6 7 5 8\"");
            System.err.println("  java -cp out puzzle.Main --algo <ALGO> --case <EASY_1|MEDIUM_1|MEDIUM_2|HARD_1|HARD_2>");
            System.exit(1);
        }

        Board board = new Board(boardTiles);
        BestFirstSearch search = buildSearch(algo);
        SearchResult result = search.solve(board);

        ResultWriter writer = new ResultWriter();
        writer.printConsoleReport(result, System.out);

        String timestamp = String.valueOf(System.currentTimeMillis());
        java.nio.file.Path jsonPath = Paths.get("run-" + timestamp + ".json");
        writer.writeJson(result, jsonPath);
        System.out.println("JSON: " + jsonPath.toAbsolutePath());
    }

    private static BestFirstSearch buildSearch(String algo) {
        switch (algo) {
            case "UCS":            return new BestFirstSearch(new ZeroHeuristic(), "UCS");
            case "NON_ADMISSIBLE": return new BestFirstSearch(new OverestimateHeuristic(), "3xManhattan");
            case "MISPLACED":      return new BestFirstSearch(new MisplacedTilesHeuristic(), "MisplacedTiles");
            case "MANHATTAN_LC":   return new BestFirstSearch(new ManhattanLinearConflictHeuristic(), "ManhattanLinearConflict");
            default: throw new IllegalArgumentException("Unknown algo: " + algo + ". Valid: UCS, NON_ADMISSIBLE, MISPLACED, MANHATTAN_LC");
        }
    }

    private static int[] caseBoard(String name) {
        switch (name) {
            case "EASY_1":   return TestCases.EASY_1.tiles();
            case "MEDIUM_1": return TestCases.MEDIUM_1.tiles();
            case "MEDIUM_2": return TestCases.MEDIUM_2.tiles();
            case "HARD_1":   return TestCases.HARD_1.tiles();
            case "HARD_2":   return TestCases.HARD_2.tiles();
            default: throw new IllegalArgumentException("Unknown case: " + name);
        }
    }
}
```

- [ ] **Step 2: Compile and smoke-test with EASY_1**

```bash
javac -d out $(find src -name "*.java")
java -cp out puzzle.Main --algo MANHATTAN_LC --case EASY_1
```

Expected output (values will differ):
```
=== ManhattanLinearConflict ===
Path   : [DOWN, RIGHT, ...]
Length : 5
Visited: <some number>
MaxFrnt: <some number>
Time   : 0.00X s
JSON: /path/to/run-<timestamp>.json
```

- [ ] **Step 3: Smoke-test all 4 algorithms with MEDIUM_1**

```bash
java -cp out puzzle.Main --algo UCS            --case MEDIUM_1
java -cp out puzzle.Main --algo NON_ADMISSIBLE --case MEDIUM_1
java -cp out puzzle.Main --algo MISPLACED      --case MEDIUM_1
java -cp out puzzle.Main --algo MANHATTAN_LC   --case MEDIUM_1
```

All should print solved output. UCS and MISPLACED may visit more nodes than MANHATTAN_LC.

- [ ] **Step 4: Run tests to ensure nothing regressed**

```bash
javac -d out $(find src -name "*.java") && java -cp out puzzle.PuzzleTest
```

Expected: all PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/puzzle/Main.java
git commit -m "feat: implement Main CLI wiring for all 4 search variants"
```

---

## Task 12: TestCases boards + RESULTS.md

**Files:**
- Modify: `src/main/java/puzzle/test/TestCases.java`
- Modify: `docs/RESULTS.md`

**Vetted boards** (constructed by reversing moves from goal — depths are guaranteed):

| Constant | Tiles | Depth |
|----------|-------|-------|
| `EASY_1` | `{1,0,3,5,2,6,4,7,8}` | 5 |
| `MEDIUM_1` | `{5,1,3,2,6,8,4,7,0}` | 10 |
| `MEDIUM_2` | `{0,1,8,5,3,2,4,6,7}` | 20 |
| `HARD_1` | `{8,6,7,2,5,4,3,0,1}` | ~27 (verify after solve) |
| `HARD_2` | `{6,4,7,8,5,0,3,2,1}` | ~29 (verify after solve) |

- [ ] **Step 1: Replace TestCases.java with vetted boards**

```java
package puzzle.test;

import puzzle.model.Board;

public final class TestCases {

    private TestCases() {}

    // Depth 5
    public static final Board EASY_1 = new Board(new int[]{
            1, 0, 3,
            5, 2, 6,
            4, 7, 8
    });

    // Depth 10
    public static final Board MEDIUM_1 = new Board(new int[]{
            5, 1, 3,
            2, 6, 8,
            4, 7, 0
    });

    // Depth 20
    public static final Board MEDIUM_2 = new Board(new int[]{
            0, 1, 8,
            5, 3, 2,
            4, 6, 7
    });

    // Depth ~27
    public static final Board HARD_1 = new Board(new int[]{
            8, 6, 7,
            2, 5, 4,
            3, 0, 1
    });

    // Depth ~29
    public static final Board HARD_2 = new Board(new int[]{
            6, 4, 7,
            8, 5, 0,
            3, 2, 1
    });
}
```

- [ ] **Step 2: Run PuzzleTest to verify EASY_1 depth is still 5**

```bash
javac -d out $(find src -name "*.java") && java -cp out puzzle.PuzzleTest
```

Expected: all PASS (the `testSearch()` test uses EASY_1 and asserts pathLength=5).

- [ ] **Step 3: Run all boards × all algorithms and record results**

Run these 16 commands and capture the output:
```bash
for ALGO in UCS NON_ADMISSIBLE MISPLACED MANHATTAN_LC; do
  for CASE in EASY_1 MEDIUM_1 MEDIUM_2 HARD_1 HARD_2; do
    echo "=== $ALGO $CASE ===" && java -cp out puzzle.Main --algo $ALGO --case $CASE
  done
done
```

Note: UCS and NON_ADMISSIBLE on HARD cases may take a very long time or exhaust memory. If UCS on HARD_1/HARD_2 does not finish within 60 s, record "DNF (timeout)" in the table — the spec only requires UCS/NON_ADMISSIBLE on medium and easy.

- [ ] **Step 4: Fill in docs/RESULTS.md**

Replace the placeholder content with actual results. Required minimum:

```markdown
## Heuristic Comparison (h values at start state)

| Board | Depth | MisplacedTiles h | Manhattan h | ManhattanLC h |
|-------|-------|-----------------|-------------|---------------|
| EASY_1 (depth 5) | 5 | ? | ? | ? |
| MEDIUM_1 (depth 10) | 10 | ? | ? | ? |
| MEDIUM_2 (depth 20) | 20 | ? | ? | ? |
| HARD_1 (~depth 27) | ? | ? | ? | ? |
| HARD_2 (~depth 29) | ? | ? | ? | ? |

## Performance Table

| Board | Algorithm | Nodes Visited | Path Length | Max Frontier | Time (s) |
|-------|-----------|--------------|-------------|--------------|----------|
| EASY_1 | UCS | ? | ? | ? | ? |
| EASY_1 | NON_ADMISSIBLE | ? | ? | ? | ? |
| EASY_1 | MISPLACED | ? | ? | ? | ? |
| EASY_1 | MANHATTAN_LC | ? | ? | ? | ? |
| MEDIUM_1 | UCS | ? | ? | ? | ? |
| MEDIUM_1 | NON_ADMISSIBLE | ? | ? | ? | ? |
| MEDIUM_1 | MISPLACED | ? | ? | ? | ? |
| MEDIUM_1 | MANHATTAN_LC | ? | ? | ? | ? |
| HARD_1 | MISPLACED | ? | ? | ? | ? |
| HARD_1 | MANHATTAN_LC | ? | ? | ? | ? |
| HARD_2 | MISPLACED | ? | ? | ? | ? |
| HARD_2 | MANHATTAN_LC | ? | ? | ? | ? |
```

Replace every `?` with the actual measured values from Step 3.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/puzzle/test/TestCases.java docs/RESULTS.md
git commit -m "chore: replace placeholder boards and fill RESULTS.md with measured data"
```

---

## Spec Coverage Checklist

| Requirement | Task |
|-------------|------|
| UCS (variant 1) | Task 9 — `ZeroHeuristic` + `BestFirstSearch` |
| A* non-admissible (variant 2) | Task 9 — `OverestimateHeuristic` (3×Manhattan) |
| A* simple admissible (variant 3) | Task 7 + Task 9 — `MisplacedTilesHeuristic` |
| A* best admissible (variant 4) | Task 8 + Task 9 — `ManhattanLinearConflictHeuristic` |
| (a) nodes visited | Task 9 — tracked in `solve()` |
| (b) path length | Task 5 — `reconstructPath()` |
| (c) execution time | Task 9 — `System.currentTimeMillis()` |
| (d) max frontier size | Task 9 — `maxFrontier` counter |
| (e) JSON dump | Task 10 — `ResultWriter.writeJson()` |
| Heuristics computed mathematically | Tasks 6–8 — formulas, no lookup tables |
| No external libraries | All tasks — vanilla Java only |
| 2 hard + 2 medium + 1 easy boards | Task 12 — `TestCases` |
| `docs/RESULTS.md` comparison table | Task 12 |
