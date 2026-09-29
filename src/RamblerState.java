import java.util.ArrayList;

/**
 * RamblerState.java
 *
 * A position (row, column) on a terrain map. The rambler moves one cell up,
 * down, left or right. Moving to a cell that is not higher costs 1; climbing
 * costs 1 plus the height gained.
 *
 * The terrain, the goal and the heuristic are shared by every state of a
 * search, so they are stored statically instead of being copied into each state.
 */
public class RamblerState extends SearchState {

  private final int row;
  private final int col;

  private static int[][] terrain;
  private static int rows;
  private static int columns;
  private static RamblerState goal;
  private static Heuristic heuristic = Heuristic.ZERO;

  public RamblerState(int row, int col) {
    this.row = row;
    this.col = col;
  }

  /** Set up the problem shared by all states: terrain, goal and heuristic. */
  public static void configure(int[][] map, RamblerState goalState, Heuristic h) {
    terrain = map;
    rows = map.length;
    columns = map[0].length;
    if (!inBounds(goalState.row, goalState.col)) {
      throw new IllegalArgumentException("Goal " + goalState + " is outside a "
          + rows + "x" + columns + " map");
    }
    goal = goalState;
    heuristic = h;
  }

  static boolean inBounds(int r, int c) {
    return r >= 0 && r < rows && c >= 0 && c < columns;
  }

  public int getX() {
    return row;
  }

  public int getY() {
    return col;
  }

  int getHeight() {
    return terrain[row][col];
  }

  /** Cost of stepping from this state to a neighbouring state. */
  int cost(RamblerState next) {
    int climb = next.getHeight() - getHeight();
    return climb > 0 ? 1 + climb : 1;
  }

  @Override
  boolean goalPredicate(Search searcher) {
    return sameState(goal);
  }

  @Override
  ArrayList<SearchState> getSuccessors(Search searcher) {
    ArrayList<SearchState> successors = new ArrayList<SearchState>();
    int[][] moves = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } }; // down, up, right, left
    for (int[] m : moves) {
      int r = row + m[0];
      int c = col + m[1];
      if (inBounds(r, c)) {
        RamblerState next = new RamblerState(r, c);
        next.localCost = cost(next);
        next.estRemCost = heuristic.estimate(next, goal);
        successors.add(next);
      }
    }
    return successors;
  }

  @Override
  boolean sameState(SearchState other) {
    RamblerState s = (RamblerState) other;
    return s.row == row && s.col == col;
  }

  @Override
  public String toString() {
    return "(" + row + "," + col + ")";
  }
}
