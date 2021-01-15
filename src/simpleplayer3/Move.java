package simpleplayer3;

import battlecode.common.*;
import common.*;

class Move {




    static Direction getTeamGoDir(RobotController robot) {
        // TODO: make this smarter than assuming team a is on the left side
        return robot.getTeam().equals(Team.A) ? Direction.WEST : Direction.EAST; 
    }

    /*
     * static boolean pathfindTo(RobotController rc, MapLocation destination) throws
     * GameActionException{ dir = rc.getLocation().directionTo(destination) if
     * destination.isWithinDistanceSquared(rc.getLocation(), 2) tryMove(rc, dir);
     * double[] movenesses = [trySenseMoveness(rc, dir.rotateLeft()),
     * trySenseMoveness(rc, dir), trySenseMoveness(rc, dir.rotateRight())] switch
     * (dir){ case NORTH: case EAST: case SOUTH: case WEST: if ( (movenesses[1] >
     * movenesses[0]) && (movenesses[1] > movenesses[2]) ) rc.move(dir); return
     * true; else if (movenesses[0] > movenesses[2]) rc.move(dir.rotateLeft());
     * return true; else if (movenesses[2] > 0.0) rc.move(dir.rotateRight()); return
     * true; return false; default: if ( (movenesses[1] > movenesses[0] * 0.5) &&
     * (movenesses[1] > movenesses[2] * 0.5) ) rc.move(dir); return true; else if
     * (movenesses[0] > movenesses[2]) rc.move(dir.rotateLeft()); return true; else
     * if (movenesses[2] > 0.0) rc.move(dir.rotateRight()); return true; return
     * false; } }
     * 
     * static double trySenseMoveness(RobotController rc, Direction dir){ if
     * rc.canMove(dir) return rc.sensePassability(rc.adjacentLocation(dir)); else
     * return 0.0; }
     */
}
