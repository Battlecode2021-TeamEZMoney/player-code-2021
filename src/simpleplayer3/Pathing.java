package simpleplayer3;

import battlecode.common.*;
import common.DirectionUtils;

public abstract strictfp class PathingOne {
  
  public static Direction pathingDir( RobotController rc, MapLocation dest ) {
    MapLocation here = rc.getLocation;
    Direction wayTo = here.directionTo( dest );
    
    if ( here.isWithinDistanceSquared( dest, 2 ) ) return wayTo;
    
    double[] speeds = { 0.0, 0.0, 0.0 };
    Direction right = wayTo.rotateRight(), left = wayTo.rotateLeft();
    
    switch wayTo {
      case Direction.NORTH:
      case Direction.WEST:
      case Direction.EAST:
      case Direction.SOUTH:
        if rc.canMove( left ) speeds[0] = sensePassability( here.add( left ) );
        if rc.canMove( wayTo ) speeds[1] = sensePassability( here.add( wayTo ) );
        if rc.canMove( right ) speeds[2] = sensePassability( here.add( right ) );
        break;
      default:
        if rc.canMove( left ) speeds[0] = sensePassability( here.add( left ) ) * 0.7;
        if rc.canMove( wayTo ) speeds[1] = sensePassability( here.add( wayTo ) );
        if rc.canMove( right ) speeds[2] = sensePassability( here.add( right ) ) * 0.7;
    }
    
    if ( ( speeds[0] > speeds[1] ) && ( speeds[0] > speeds[2] ) ) return left;
    if ( speeds[2] > speeds[1] ) return right;
    if ( speeds[2] > 0.0 ) return wayTo;
    // If the code gets here, speeds == { 0.0, 0.0, 0.0 } and the three spots in front are all blocked.
    
    Direction l90 = left.rotateLeft(), r90 = right.rotateRight;
    if rc.canMove( l90 ) speeds[0] = sensePassability( here.add( l90 ) );
    if rc.canMove( r90 ) speeds[1] = sensePassability( here.add( r90 ) );
    if ( speeds[0] > speeds[1] ) return l90;
    if ( speeds[1] > 0.0 ) return r90;
    
    return center;
    
  }
}
