/**
 * RamblersSearch.java
 *
 * A Ramblers problem instance: a terrain map, a start and a goal. Solve it with
 * branch and bound, or with A* under a chosen heuristic.
 */
public class RamblersSearch extends Search {

  public static final String BRANCH_AND_BOUND = "branchAndBound";
  public static final String A_STAR = "AStar";

  private final int[][] terrain;
  private final RamblerState start;
  private final RamblerState goal;

  public RamblersSearch(int[][] terrain, RamblerState start, RamblerState goal) {
    this.terrain = terrain;
    this.start = start;
    this.goal = goal;
  }

  /** Branch and bound: expand the open node with the lowest cost so far. */
  public SearchResult branchAndBound() {
    RamblerState.configure(terrain, goal, Heuristic.ZERO);
    return runSearchQuiet(start, BRANCH_AND_BOUND);
  }

  /** A*: expand the open node with the lowest cost so far plus heuristic estimate. */
  public SearchResult aStar(Heuristic h) {
    RamblerState.configure(terrain, goal, h);
    return runSearchQuiet(start, A_STAR);
  }
}
