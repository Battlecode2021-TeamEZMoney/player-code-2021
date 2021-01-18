package simpleplayer4;

import battlecode.common.*;
import common.*;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

abstract class Pawn extends Robot {
    protected MapLocation hqLocation = null;
    protected int hqID;
    protected Direction dirTarget = Direction.CENTER;
    protected boolean explorer = false;
    protected boolean defending = false;
    protected int actionRadiusSquared;
    protected int detectionRadiusSquared;
    protected int sensorRadiusSquared;

    Pawn() {

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
        RobotInfo[] robots = rc.senseNearbyRobots(2, rc.getTeam());
        for (RobotInfo robot : robots) {
            if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                hqLocation = robot.getLocation();
                hqID = robot.getID();
                dirTarget = Encoding.getDirFromFlag(rc.getFlag(robot.ID));
                explorer = Encoding.getExplorerFromFlag(rc.getFlag(robot.ID));
                return true;
            }
        }
        return false;
    }

    protected boolean tryMove(Direction dir) throws GameActionException {
        // System.out.println("Trying move");
        if (rc.canMove(dir)) {
            // System.out.println("Possible");
            rc.move(dir);
            // System.out.println("Moved");
            return true;
        }
        // System.out.println("Can't Move");
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

    protected Direction awayFromRobots(List<RobotInfo> robots) throws GameActionException {
        MapLocation location = new MapLocation(0, 0);
        if (robots.size() == 0)
            return dirTarget;
        for (RobotInfo robot : robots) {
            location = location.translate(robot.location.x, robot.location.y);
        }

        location = new MapLocation(location.x / robots.size(), location.y / robots.size());
        Direction away_dir = directionTo(location).opposite();

        if (!rc.onTheMap(rc.getLocation().add(away_dir))) {
            away_dir = away_dir.opposite();
        }
        if (DirectionUtils.within45Degrees(dirTarget, away_dir)) {
            return dirTarget;
        }
        return away_dir;
    }

    protected Direction awayFromAllies() throws GameActionException {
        return awayFromRobots(
                Arrays.asList(rc.senseNearbyRobots(rc.getLocation(), rc.getType().sensorRadiusSquared, rc.getTeam())));
    }

    // Direction best_dir = Direction.CENTER;
    // double minDist = Double.MAX_VALUE;
    // RobotInfo[] allies = rc.senseNearbyRobots(-1, rc.getTeam());
    // //System.out.println(allies.length);
    // for (Direction dir : DirectionUtils.nonCenterDirections) {
    // if (rc.canMove(dir)) {
    // double totalDist = 0;
    // for (RobotInfo ally : allies) {
    // totalDist += (double) 1 / rc.getLocation().distanceSquaredTo(ally.location);
    // }
    // if (totalDist < minDist) {
    // minDist = totalDist;
    // best_dir = dir;
    // }
    // }
    // }
    // return best_dir;
}
