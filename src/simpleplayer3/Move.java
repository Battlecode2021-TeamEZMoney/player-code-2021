package simpleplayer3;

import battlecode.common.*;
import common.*;

class Move {
    static boolean tryMove(RobotController rc, Direction dir) throws GameActionException {
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }
        return false;
    }

    static Direction dirForward90(RobotController rc, Direction dir) throws GameActionException {
        if (rc.canMove(dir) || !rc.onTheMap(rc.getLocation().add(dir))) {
            return dir;
        } else if (rc.canMove(dir.rotateLeft())) {
            return dir.rotateLeft();
        } else if (rc.canMove(dir.rotateRight())) {
            return dir.rotateRight();
        }
        return dir;
    }

    static boolean tryDirForward90(RobotController rc, Direction dir) throws GameActionException {
        return tryMove(rc, dirForward90(rc, dir));
    }

    static Direction dirForward180(RobotController rc, Direction dir) throws GameActionException {
        if (rc.canMove(dir) || !rc.onTheMap(rc.getLocation().add(dir))) {
            return dir;
        } else if (rc.canMove(dir.rotateLeft())) {
            return dir.rotateLeft();
        } else if (rc.canMove(dir.rotateRight())) {
            return dir.rotateRight();
        } else if (rc.canMove(DirectionUtils.rotateLeft90(dir))) {
            return DirectionUtils.rotateLeft90(dir);
        } else if (rc.canMove(DirectionUtils.rotateRight90(dir))) {
            return DirectionUtils.rotateRight90(dir);
        }
        return dir;
    }

    static boolean tryDirForward180(RobotController rc, Direction dir) throws GameActionException {
        return tryMove(rc, dirForward180(rc, dir));
    }

    static Direction getTeamGoDir(RobotController robot) {
        return robot.getTeam().equals(Team.A) ? Direction.WEST : Direction.EAST; // TODO: make this smarter than
                                                                                 // assuming team a is on the left side
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
