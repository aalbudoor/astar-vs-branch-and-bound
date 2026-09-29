import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * PathRenderer.java
 *
 * Draws a terrain map as a greyscale PNG with the expanded cells shaded, the
 * solution path in red, and the start and goal marked.
 */
public class PathRenderer {

  private static final Color EXPANDED = new Color(66, 133, 244);
  private static final Color PATH = new Color(220, 40, 40);
  private static final Color START = new Color(20, 170, 60);
  private static final Color GOAL = new Color(250, 190, 0);

  public static void render(int[][] map, SearchResult result, String filename, int scale)
      throws IOException {
    int rows = map.length;
    int cols = map[0].length;
    BufferedImage img = new BufferedImage(cols * scale, rows * scale, BufferedImage.TYPE_INT_RGB);

    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < cols; c++) {
        int g = map[r][c];
        fill(img, r, c, scale, new Color(g, g, g));
      }
    }
    for (SearchState s : result.expanded) {
      RamblerState rs = (RamblerState) s;
      int g = map[rs.getX()][rs.getY()];
      fill(img, rs.getX(), rs.getY(), scale, blend(new Color(g, g, g), EXPANDED, 0.55));
    }
    List<SearchState> path = result.path;
    for (SearchState s : path) {
      RamblerState rs = (RamblerState) s;
      fill(img, rs.getX(), rs.getY(), scale, PATH);
    }
    if (!path.isEmpty()) {
      RamblerState a = (RamblerState) path.get(0);
      RamblerState b = (RamblerState) path.get(path.size() - 1);
      fill(img, a.getX(), a.getY(), scale, START);
      fill(img, b.getX(), b.getY(), scale, GOAL);
    }
    ImageIO.write(img, "png", new File(filename));
  }

  private static void fill(BufferedImage img, int r, int c, int scale, Color color) {
    int rgb = color.getRGB();
    for (int y = r * scale; y < (r + 1) * scale; y++) {
      for (int x = c * scale; x < (c + 1) * scale; x++) {
        img.setRGB(x, y, rgb);
      }
    }
  }

  private static Color blend(Color base, Color tint, double t) {
    return new Color(
        (int) Math.round(base.getRed() * (1 - t) + tint.getRed() * t),
        (int) Math.round(base.getGreen() * (1 - t) + tint.getGreen() * t),
        (int) Math.round(base.getBlue() * (1 - t) + tint.getBlue() * t));
  }
}
