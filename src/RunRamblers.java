/**
 * RunRamblers.java
 *
 * Solve a single Ramblers instance from the command line.
 *
 * Usage: java -cp out RunRamblers <map.pgm> <startRow> <startCol> <goalRow> <goalCol> [method] [out.png]
 *   method: bb (default) or one of zero, euclidean, manhattan, ascent, manhattan_ascent, abs_height
 */
public class RunRamblers {

  public static void main(String[] args) throws Exception {
    if (args.length < 5) {
      System.err.println("Usage: java -cp out RunRamblers <map.pgm> <startRow> <startCol> "
          + "<goalRow> <goalCol> [bb|zero|euclidean|manhattan|ascent|manhattan_ascent|abs_height] [out.png]");
      System.exit(1);
    }
    int[][] terrain = new TerrainMap(args[0]).getTmap();
    if (terrain == null) {
      System.err.println("Could not read map " + args[0]);
      System.exit(1);
    }
    RamblerState start = new RamblerState(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
    RamblerState goal = new RamblerState(Integer.parseInt(args[3]), Integer.parseInt(args[4]));
    String method = args.length > 5 ? args[5] : "bb";

    RamblersSearch search = new RamblersSearch(terrain, start, goal);
    SearchResult r = method.equalsIgnoreCase("bb")
        ? search.branchAndBound()
        : search.aStar(Heuristic.fromName(method));

    if (!r.found) {
      System.out.println("No path found.");
      return;
    }
    System.out.println("Method:         " + (method.equalsIgnoreCase("bb") ? "branch and bound" : "A* / " + method));
    System.out.println("Path cost:      " + r.pathCost);
    System.out.println("Path length:    " + r.pathLength);
    System.out.println("Nodes expanded: " + r.nodesExpanded);
    System.out.printf("Efficiency:     %.4f%n", r.efficiency());
    System.out.println("Path:           " + r.path);
    if (args.length > 6) {
      PathRenderer.render(terrain, r, args[6], 12);
      System.out.println("Wrote " + args[6]);
    }
  }
}
