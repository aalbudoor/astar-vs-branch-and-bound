/**
 * Heuristic.java
 *
 * Estimates of the remaining cost from a Ramblers state to the goal.
 *
 * Step cost model (see RamblerState.cost): moving to a neighbour costs 1 if the
 * neighbour is not higher, and 1 + (climb) otherwise. Every path from n to the
 * goal therefore costs at least
 *   (number of steps) + (total ascent) >= manhattan(n, goal) + max(0, h_goal - h_n),
 * which is why MANHATTAN, EUCLIDEAN, ASCENT and MANHATTAN_ASCENT never
 * overestimate. ABS_HEIGHT charges for descents too, so it can overestimate
 * (a one-step descent of 100 costs 1 but is estimated at 100).
 */
public enum Heuristic {

  /** h = 0. A* with this heuristic behaves like branch and bound. */
  ZERO("Zero", true) {
    int estimate(RamblerState s, RamblerState goal) {
      return 0;
    }
  },

  /** Straight-line grid distance, rounded down so it stays admissible. */
  EUCLIDEAN("Euclidean", true) {
    int estimate(RamblerState s, RamblerState goal) {
      int dx = s.getX() - goal.getX();
      int dy = s.getY() - goal.getY();
      return (int) Math.floor(Math.sqrt(dx * dx + dy * dy));
    }
  },

  /** Minimum number of 4-connected steps to the goal. */
  MANHATTAN("Manhattan", true) {
    int estimate(RamblerState s, RamblerState goal) {
      return Math.abs(s.getX() - goal.getX()) + Math.abs(s.getY() - goal.getY());
    }
  },

  /** Height that still has to be climbed; ignores horizontal distance. */
  ASCENT("Ascent", true) {
    int estimate(RamblerState s, RamblerState goal) {
      return Math.max(0, goal.getHeight() - s.getHeight());
    }
  },

  /** Manhattan distance plus remaining ascent. Dominates MANHATTAN and ASCENT. */
  MANHATTAN_ASCENT("Manhattan+Ascent", true) {
    int estimate(RamblerState s, RamblerState goal) {
      return MANHATTAN.estimate(s, goal) + ASCENT.estimate(s, goal);
    }
  },

  /** |h_goal - h_n|, a plausible-looking height heuristic. Not admissible: it charges for descents. */
  ABS_HEIGHT("AbsHeight", false) {
    int estimate(RamblerState s, RamblerState goal) {
      return Math.abs(goal.getHeight() - s.getHeight());
    }
  };

  private final String label;
  private final boolean admissible;

  Heuristic(String label, boolean admissible) {
    this.label = label;
    this.admissible = admissible;
  }

  public String label() {
    return label;
  }

  public boolean isAdmissible() {
    return admissible;
  }

  abstract int estimate(RamblerState s, RamblerState goal);

  public static Heuristic fromName(String name) {
    for (Heuristic h : values()) {
      if (h.name().equalsIgnoreCase(name) || h.label.equalsIgnoreCase(name)) {
        return h;
      }
    }
    throw new IllegalArgumentException("Unknown heuristic '" + name + "'. Options: "
        + java.util.Arrays.toString(values()));
  }
}
