import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * Experiment.java
 *
 * Compares branch and bound with A* under each heuristic on the course map
 * (tmc.pgm) and three seeded synthetic maps. For every map it samples
 * start/goal pairs, runs every method on each pair, and writes:
 *
 *   results/runs.csv            one row per (map, pair, method)
 *   results/summary.csv         per-map, per-method aggregates
 *   results/figures/*.png       expanded nodes and paths for one example pair
 *
 * Usage: java -cp out Experiment [repoRoot]
 */
public class Experiment {

  static final long PAIR_SEED = 2021L;

  // Methods in the order they appear in the outputs. null = branch and bound.
  static final Heuristic[] METHODS = {
      null, Heuristic.EUCLIDEAN, Heuristic.MANHATTAN, Heuristic.ASCENT,
      Heuristic.MANHATTAN_ASCENT, Heuristic.ABS_HEIGHT };

  static String methodName(Heuristic h) {
    return h == null ? "BranchAndBound" : "AStar-" + h.label();
  }

  static class MapSpec {
    final String name;
    final int[][] terrain;
    final int pairs;
    final int minDistance;

    MapSpec(String name, int[][] terrain, int pairs, int minDistance) {
      this.name = name;
      this.terrain = terrain;
      this.pairs = pairs;
      this.minDistance = minDistance;
    }
  }

  public static void main(String[] args) throws IOException {
    String root = args.length > 0 ? args[0] : ".";
    File results = new File(root, "results");
    File figures = new File(results, "figures");
    File synthetic = new File(root, "maps/synthetic");
    figures.mkdirs();
    synthetic.mkdirs();

    List<MapSpec> maps = new ArrayList<MapSpec>();
    maps.add(new MapSpec("tmc16", new TerrainMap(new File(root, "maps/tmc.pgm").getPath()).getTmap(), 200, 8));
    maps.add(synthetic(synthetic, "smooth64", 64, 8, 8.0, 16.0, 0.0, 11L));
    maps.add(synthetic(synthetic, "rolling64", 64, 30, 3.0, 8.0, 4.0, 12L));
    maps.add(synthetic(synthetic, "rugged64", 64, 120, 1.5, 4.0, 12.0, 13L));

    warmUp(maps.get(1));

    PrintWriter runs = new PrintWriter(new File(results, "runs.csv"));
    runs.println("map,pair,start_r,start_c,goal_r,goal_c,method,admissible,found,path_cost,"
        + "optimal_cost,optimal,cost_excess_pct,path_len,expanded,expanded_ratio,efficiency,h_start_ratio,time_ms");

    PrintWriter summary = new PrintWriter(new File(results, "summary.csv"));
    summary.println("map,method,n,mean_expanded,median_expanded,mean_expanded_ratio,"
        + "pct_optimal,mean_cost_excess_pct,max_cost_excess_pct,mean_efficiency,mean_time_ms,mean_h_start_ratio");

    for (MapSpec spec : maps) {
      runMap(spec, runs, summary);
    }
    runs.close();
    summary.close();

    writeWide(new File(results, "summary.csv"), new File(results, "expanded_ratio_wide.csv"), 5);
    writeWide(new File(results, "summary.csv"), new File(results, "pct_optimal_wide.csv"), 6);
    writeWide(new File(results, "summary.csv"), new File(results, "h_start_ratio_wide.csv"), 11);
    exampleFigures(maps.get(1).terrain, figures);
    System.out.println("Done. Results in " + results.getPath());
  }

  static MapSpec synthetic(File dir, String name, int size, int hills, double minSigma,
      double maxSigma, double noise, long seed) throws IOException {
    int[][] t = TerrainGenerator.generate(size, hills, minSigma, maxSigma, noise, seed);
    String comment = String.format(Locale.ROOT, "%s size=%d hills=%d sigma=[%.1f,%.1f] noise=%.1f seed=%d",
        name, size, hills, minSigma, maxSigma, noise, seed);
    TerrainGenerator.writePgm(t, new File(dir, name + ".pgm").getPath(), comment);
    return new MapSpec(name, t, 100, size / 2);
  }

  /** Run every method a few times first so JIT compilation does not skew the timings. */
  static void warmUp(MapSpec spec) {
    Random rng = new Random(1L);
    int n = spec.terrain.length;
    for (int i = 0; i < 5; i++) {
      RamblersSearch s = new RamblersSearch(spec.terrain,
          new RamblerState(rng.nextInt(n), rng.nextInt(n)), new RamblerState(rng.nextInt(n), rng.nextInt(n)));
      for (Heuristic h : METHODS) {
        if (h == null) s.branchAndBound(); else s.aStar(h);
      }
    }
  }

  static void runMap(MapSpec spec, PrintWriter runs, PrintWriter summary) {
    int rows = spec.terrain.length;
    int cols = spec.terrain[0].length;
    Random rng = new Random(PAIR_SEED + spec.name.hashCode());

    Map<String, List<double[]>> stats = new LinkedHashMap<String, List<double[]>>();
    for (Heuristic h : METHODS) stats.put(methodName(h), new ArrayList<double[]>());

    int pair = 0;
    while (pair < spec.pairs) {
      int sr = rng.nextInt(rows), sc = rng.nextInt(cols);
      int gr = rng.nextInt(rows), gc = rng.nextInt(cols);
      if (Math.abs(sr - gr) + Math.abs(sc - gc) < spec.minDistance) continue;

      RamblersSearch search = new RamblersSearch(spec.terrain, new RamblerState(sr, sc), new RamblerState(gr, gc));

      SearchResult[] res = new SearchResult[METHODS.length];
      double[] ms = new double[METHODS.length];
      for (int m = 0; m < METHODS.length; m++) {
        long t0 = System.nanoTime();
        res[m] = METHODS[m] == null ? search.branchAndBound() : search.aStar(METHODS[m]);
        ms[m] = (System.nanoTime() - t0) / 1e6;
      }

      SearchResult bb = res[0];
      if (!bb.found) throw new IllegalStateException("Branch and bound failed on " + spec.name);
      for (int m = 0; m < METHODS.length; m++) {
        SearchResult r = res[m];
        boolean optimal = r.found && r.pathCost == bb.pathCost;
        double excess = 100.0 * (r.pathCost - bb.pathCost) / bb.pathCost;
        double ratio = (double) r.nodesExpanded / bb.nodesExpanded;
        boolean admissible = METHODS[m] == null || METHODS[m].isAdmissible();
        // share of the true optimal cost that the heuristic sees from the start
        double hRatio = 0.0;
        if (METHODS[m] != null) {
          RamblerState goal = new RamblerState(gr, gc);
          RamblerState.configure(spec.terrain, goal, METHODS[m]);
          hRatio = (double) METHODS[m].estimate(new RamblerState(sr, sc), goal) / bb.pathCost;
        }
        runs.println(String.format(Locale.ROOT, "%s,%d,%d,%d,%d,%d,%s,%b,%b,%d,%d,%b,%.3f,%d,%d,%.4f,%.4f,%.4f,%.3f",
            spec.name, pair, sr, sc, gr, gc, methodName(METHODS[m]), admissible, r.found, r.pathCost,
            bb.pathCost, optimal, excess, r.pathLength, r.nodesExpanded, ratio, r.efficiency(), hRatio, ms[m]));
        stats.get(methodName(METHODS[m])).add(new double[] {
            r.nodesExpanded, ratio, optimal ? 1 : 0, excess, r.efficiency(), ms[m], hRatio });
      }
      pair++;
    }

    for (Map.Entry<String, List<double[]>> e : stats.entrySet()) {
      List<double[]> rowsOf = e.getValue();
      int n = rowsOf.size();
      double[] expanded = new double[n];
      double sumExp = 0, sumRatio = 0, sumOpt = 0, sumExcess = 0, maxExcess = 0, sumEff = 0, sumMs = 0, sumH = 0;
      for (int i = 0; i < n; i++) {
        double[] v = rowsOf.get(i);
        expanded[i] = v[0];
        sumExp += v[0]; sumRatio += v[1]; sumOpt += v[2]; sumExcess += v[3];
        maxExcess = Math.max(maxExcess, v[3]); sumEff += v[4]; sumMs += v[5]; sumH += v[6];
      }
      Arrays.sort(expanded);
      double median = n % 2 == 1 ? expanded[n / 2] : (expanded[n / 2 - 1] + expanded[n / 2]) / 2;
      summary.println(String.format(Locale.ROOT, "%s,%s,%d,%.1f,%.1f,%.4f,%.1f,%.3f,%.3f,%.4f,%.3f,%.4f",
          spec.name, e.getKey(), n, sumExp / n, median, sumRatio / n, 100 * sumOpt / n,
          sumExcess / n, maxExcess, sumEff / n, sumMs / n, sumH / n));
    }
    System.out.println("Finished " + spec.name + " (" + spec.pairs + " pairs)");
  }

  /**
   * Pivot one summary column into a map x method table for plotting. Method
   * columns get short names (bb, euclidean, ...) because pgfplots column names
   * cannot contain '+'.
   */
  static void writeWide(File summaryCsv, File out, int column) throws IOException {
    List<String> lines = java.nio.file.Files.readAllLines(summaryCsv.toPath());
    Map<String, List<String>> byMap = new LinkedHashMap<String, List<String>>();
    for (String line : lines.subList(1, lines.size())) {
      String[] f = line.split(",");
      if (!byMap.containsKey(f[0])) byMap.put(f[0], new ArrayList<String>());
      byMap.get(f[0]).add(f[column]);
    }
    PrintWriter w = new PrintWriter(out);
    StringBuilder header = new StringBuilder("map");
    for (Heuristic h : METHODS) {
      header.append(',').append(h == null ? "bb" : h.name().toLowerCase(Locale.ROOT));
    }
    w.println(header);
    for (Map.Entry<String, List<String>> e : byMap.entrySet()) {
      StringBuilder row = new StringBuilder(e.getKey());
      for (String v : e.getValue()) row.append(',').append(v);
      w.println(row);
    }
    w.close();
  }

  /** Expanded cells and paths for one example pair on the smooth map. */
  static void exampleFigures(int[][] terrain, File dir) throws IOException {
    RamblersSearch s = new RamblersSearch(terrain, new RamblerState(10, 50), new RamblerState(52, 12));
    Heuristic[] shown = { null, Heuristic.MANHATTAN, Heuristic.MANHATTAN_ASCENT, Heuristic.ABS_HEIGHT };
    PrintWriter w = new PrintWriter(new File(dir, "example.csv"));
    w.println("method,path_cost,expanded");
    for (Heuristic h : shown) {
      SearchResult r = h == null ? s.branchAndBound() : s.aStar(h);
      PathRenderer.render(terrain, r, new File(dir, "example_" + methodName(h) + ".png").getPath(), 6);
      w.println(methodName(h) + "," + r.pathCost + "," + r.nodesExpanded);
    }
    w.close();
  }
}
