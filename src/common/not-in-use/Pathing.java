import battlecode.common.*;

public abstract strictfp class Pathing {
  
  /**
   * Which direction rc should go to run towards dir, looking only one deep
   * 
   * @throws GameActionException
   */
  public static Direction goThatWayOne(RobotController rc, Direction dir) throws GameActionException {
    MapLocation here = rc.getLocation();
    double[] speeds = { 0.0, 0.0, 0.0 };
    Direction right = dir.rotateRight(), left = dir.rotateLeft();

    switch (dir) {
      case NORTH:
      case WEST:
      case EAST:
      case SOUTH:
        if (rc.canMove(left))
          speeds[0] = rc.sensePassability(here.add(left));
        if (rc.canMove(dir))
          speeds[1] = rc.sensePassability(here.add(dir));
        if (rc.canMove(right))
          speeds[2] = rc.sensePassability(here.add(right));
        break;
      default:
        if (rc.canMove(left))
          speeds[0] = rc.sensePassability(here.add(left)) * 0.7;
        if (rc.canMove(dir))
          speeds[1] = rc.sensePassability(here.add(dir));
        if (rc.canMove(right))
          speeds[2] = rc.sensePassability(here.add(right)) * 0.7;
    }

    if ((speeds[0] > speeds[1]) && (speeds[0] > speeds[2]))
      return left;
    if (speeds[2] > speeds[1])
      return right;
    if (speeds[2] > 0.0)
      return dir;
    // If the code gets here, speeds == { 0.0, 0.0, 0.0 } and the three spots in
    // front are all blocked.

    Direction l90 = left.rotateLeft(), r90 = right.rotateRight();
    if (rc.canMove(r90))
      return r90;
    return l90;
  }

  /**
   * One deep pathfinding method
   */
  public static Direction pathingDirOne(RobotController rc, MapLocation dest) throws GameActionException {
    MapLocation here = rc.getLocation();
    Direction wayTo = here.directionTo(dest);

    if (here.isWithinDistanceSquared(dest, 2))
      return wayTo;
    
    return Pathing.goThatWayOne(rc, wayTo);
  }

}
