package simpleplayer4;

import battlecode.common.*;
import common.*;

import java.util.Arrays;
import java.util.List;

abstract class Pawn extends Robot {
    protected MapLocation hqLocation = null;
    protected int hqID;
    protected Direction dirTarget = Direction.CENTER;
    protected boolean explorer = false;
    protected boolean defending = false;


    Pawn(RobotController rcin) throws GameActionException {
        super(rcin); // Don't remove this.
        getHomeHQ();
    }

    static Pawn unitFromRobotController(RobotController rc) throws Exception {
        // getHomeHQ();
        switch (rc.getType()) {
            case POLITICIAN:
                return new Politician(rc);
            case MUCKRAKER:
                return new Muckraker(rc);
            case SLANDERER:
                return new Slanderer(rc);
            default:
                throw new Exception(rc.getType() + "is not a valid pawn type.");
        }
    }

    protected boolean getHomeHQ() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots(2, allyTeam);
        for (RobotInfo robot : robots) {
            if (robot.type.equals(RobotType.ENLIGHTENMENT_CENTER)) {
                hqLocation = robot.getLocation();
                hqID = robot.getID();
                dirTarget = robotType.equals(RobotType.SLANDERER) ?
                		Direction.CENTER : Encoding.getDirFromFlag(rc.getFlag(robot.ID));
                explorer = Encoding.getExplorerFromFlag(rc.getFlag(robot.ID));
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
//        if (rc.canMove(dir) || !rc.onTheMap(rc.getLocation().add(dir))) {
    	if (rc.canMove(dir) || dir.equals(Direction.CENTER)) {
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
        //if (rc.canMove(dir) || !rc.onTheMap(rc.getLocation().add(dir))) {
    	if (rc.canMove(dir) || dir.equals(Direction.CENTER)) {
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

    protected Direction awayFromRobots(List<RobotInfo> robots) throws GameActionException {
        MapLocation location = new MapLocation(0, 0);
        if (robots.size() == 0) {
            return dirTarget;
        }
        for (RobotInfo robot : robots) {
            location = location.translate(robot.location.x, robot.location.y);
        }

        location = new MapLocation(location.x / robots.size(), location.y / robots.size());
        return directionTo(location).opposite();
    }

    protected Direction awayFromAllies() throws GameActionException {
        return awayFromRobots(Arrays.asList(rc.senseNearbyRobots(-1, allyTeam)));
    }
}
