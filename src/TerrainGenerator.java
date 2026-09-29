import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

/**
 * TerrainGenerator.java
 *
 * Seeded synthetic terrain: a sum of Gaussian hills plus optional per-cell
 * noise, rescaled to heights 0..255. Few wide hills give gentle terrain; many
 * narrow hills with noise give rugged terrain where height matters more.
 */
public class TerrainGenerator {

  public static int[][] generate(int size, int hills, double minSigma, double maxSigma,
      double noise, long seed) {
    Random rng = new Random(seed);
    double[][] z = new double[size][size];

    for (int k = 0; k < hills; k++) {
      double cr = rng.nextDouble() * size;
      double cc = rng.nextDouble() * size;
      double sigma = minSigma + rng.nextDouble() * (maxSigma - minSigma);
      double amp = 0.3 + 0.7 * rng.nextDouble();
      for (int r = 0; r < size; r++) {
        for (int c = 0; c < size; c++) {
          double d2 = (r - cr) * (r - cr) + (c - cc) * (c - cc);
          z[r][c] += amp * Math.exp(-d2 / (2 * sigma * sigma));
        }
      }
    }

    double min = Double.MAX_VALUE;
    double max = -Double.MAX_VALUE;
    for (int r = 0; r < size; r++) {
      for (int c = 0; c < size; c++) {
        min = Math.min(min, z[r][c]);
        max = Math.max(max, z[r][c]);
      }
    }

    int[][] map = new int[size][size];
    for (int r = 0; r < size; r++) {
      for (int c = 0; c < size; c++) {
        double h = (z[r][c] - min) / (max - min) * 255.0 + noise * rng.nextGaussian();
        map[r][c] = (int) Math.round(Math.max(0, Math.min(255, h)));
      }
    }
    return map;
  }

  /** Write a map as a plain (P2) PGM that TerrainMap can read back. */
  public static void writePgm(int[][] map, String filename, String comment) throws IOException {
    FileWriter w = new FileWriter(filename, false);
    try {
      w.write("P2\n# " + comment + "\n");
      w.write(map[0].length + " " + map.length + "\n255\n");
      for (int[] row : map) {
        StringBuilder sb = new StringBuilder();
        for (int c = 0; c < row.length; c++) {
          if (c > 0) sb.append(' ');
          sb.append(row[c]);
        }
        w.write(sb.append('\n').toString());
      }
    } finally {
      w.close();
    }
  }
}
