package simpleplayer3;

import battlecode.common.*;

public abstract strictfp class Pathing {

  /**
   * One deep pathfinding method
   */
  public static Direction pathingDirOne(RobotController rc, MapLocation dest) throws GameActionException {
    MapLocation here = rc.getLocation();
    Direction wayTo = here.directionTo(dest);

    if (here.isWithinDistanceSquared(dest, 2))
      return wayTo;

    double[] speeds = { 0.0, 0.0, 0.0 };
    Direction right = wayTo.rotateRight(), left = wayTo.rotateLeft();

    switch (wayTo) {
      case NORTH:
      case WEST:
      case EAST:
      case SOUTH:
        if (rc.canMove(left))
          speeds[0] = rc.sensePassability(here.add(left));
        if (rc.canMove(wayTo))
          speeds[1] = rc.sensePassability(here.add(wayTo));
        if (rc.canMove(right))
          speeds[2] = rc.sensePassability(here.add(right));
        break;
      default:
        if (rc.canMove(left))
          speeds[0] = rc.sensePassability(here.add(left)) * 0.7;
        if (rc.canMove(wayTo))
          speeds[1] = rc.sensePassability(here.add(wayTo));
        if (rc.canMove(right))
          speeds[2] = rc.sensePassability(here.add(right)) * 0.7;
    }

    if ((speeds[0] > speeds[1]) && (speeds[0] > speeds[2]))
      return left;
    if (speeds[2] > speeds[1])
      return right;
    if (speeds[2] > 0.0)
      return wayTo;
    // If the code gets here, speeds == { 0.0, 0.0, 0.0 } and the three spots in
    // front are all blocked.

    Direction l90 = left.rotateLeft(), r90 = right.rotateRight();
    if (rc.canMove(r90))
      return r90;
    return l90;

  }

}
