import java.util.ArrayList;
import java.util.List;

/**
 * SearchResult.java
 *
 * Outcome of one quiet search run: the path found, its cost and how much work
 * the search did to find it.
 */
public class SearchResult {

  public final boolean found;
  public final int pathCost;
  public final int pathLength;      // number of states on the path, including start and goal
  public final int nodesExpanded;   // nodes taken off open and expanded, including the goal
  public final List<SearchState> path;
  public final List<SearchState> expanded;

  SearchResult(boolean found, int pathCost, int pathLength, int nodesExpanded,
      List<SearchState> path, List<SearchState> expanded) {
    this.found = found;
    this.pathCost = pathCost;
    this.pathLength = pathLength;
    this.nodesExpanded = nodesExpanded;
    this.path = path;
    this.expanded = expanded;
  }

  static SearchResult failure(int nodesExpanded, List<SearchState> expanded) {
    return new SearchResult(false, -1, 0, nodesExpanded, new ArrayList<SearchState>(), expanded);
  }

  /** The efficiency measure used in COM1005: path length / nodes expanded. */
  public double efficiency() {
    return found ? (double) pathLength / nodesExpanded : 0.0;
  }
}
