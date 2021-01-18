package simpleplayer3;

import battlecode.common.*;
import common.DirectionUtils;

public abstract strictfp class Pathing {
  
  public static Direction pathingDir( RobotController rc, MapLocation dest ) {
    MapLocation here = rc.getLocation;
    Direction wayTo = here.directionTo( dest );
    
    if rc.canSenseLocation( dest ) {
      if ( here.isWithinDistanceSquared( dest, 2 ) ) return center;
      
    }
    
    double[] speeds = { 0.0, 0.0, 0.0 };
    Direction[] dirs;
    
    switch wayTo {
      case Direction.NORTH:
      case Direction.WEST:
      case Direction.EAST:
      case Direction.SOUTH:
        
      default:
        
    }
    
  }
}
