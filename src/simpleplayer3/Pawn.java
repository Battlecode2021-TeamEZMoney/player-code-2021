package simpleplayer3;

import battlecode.common.*;
import common.DirectionUtils;

abstract class Pawn extends Robot{
    protected MapLocation hqLocation;
    protected int hqID;

    static Pawn unitFromRobotController(RobotController rc) throws Exception{
        switch(rc.getType()){
            case POLITICIAN: return new PoliticianPlayer(rc);
            case MUCKRAKER: return new MuckrakerPlayer(rc);
            case SLANDERER: return new SlandererPlayer(rc);
            default:
                throw new Exception(rc.getType() + "is not a valid pawn type.");
        }
    }

    protected boolean getHomeHQ() {
        RobotInfo[] robots = rc.senseNearbyRobots(2, rc.getTeam());
        for (RobotInfo robot : robots) {
            if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                hqLocation = robot.getLocation();
                hqID = robot.getID();
                return true;
            }
        }
        return false;
    }

    protected boolean tryMove(Direction dir) throws GameActionException {
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }
        return false;
    }

    protected Direction dirForward90(Direction dir) throws GameActionException {
        if (rc.canMove(dir) || !rc.onTheMap(rc.getLocation().add(dir))) {
            return dir;
        } else if (rc.canMove(dir.rotateLeft())) {
            return dir.rotateLeft();
        } else if (rc.canMove(dir.rotateRight())) {
            return dir.rotateRight();
        }
        return dir;
    }

    protected boolean tryDirForward90(Direction dir) throws GameActionException {
        return tryMove(dirForward90(dir));
    }

    protected Direction dirForward180(Direction dir) throws GameActionException {
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

    protected boolean tryDirForward180(Direction dir) throws GameActionException {
        return tryMove(dirForward180(dir));
    }
}
