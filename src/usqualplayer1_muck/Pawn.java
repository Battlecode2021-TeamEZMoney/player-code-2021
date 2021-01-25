package usqualplayer1_muck;

import battlecode.common.*;
import common.*;

import java.util.*;

abstract class Pawn extends Robot {
    protected MapLocation hqLocation = null;
    protected int hqID;
    protected Direction dirTarget = Direction.CENTER;
    protected boolean explorer = false;
    protected boolean defending = false;

    Pawn(RobotController rcin) throws GameActionException {
        super(rcin);
        getHomeHQ();
    }

    static Pawn unitFromRobotController(RobotController rc) throws Exception {
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
                dirTarget = directionTo(hqLocation).opposite();
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
        if (dir.equals(Direction.CENTER)) {
            return dir;
        }
        Direction[] possDirs = { dir, dir.rotateLeft(), dir.rotateRight() };
        if (Math.random() < 0.5) {
            swap(possDirs, 1, 2);
        }
        return maxPassabilityDir(possDirs);
    }

    protected boolean tryDirForward90(Direction dir) throws GameActionException {
        return tryMove(dirForward90(dir));
    }

    protected Direction dirForward180(Direction dir) throws GameActionException {
        if (dir.equals(Direction.CENTER)) {
            return dir;
        }
        Direction[] possDirs = { dir, dir.rotateLeft(), dir.rotateRight(), DirectionUtils.rotateLeft90(dir),
                DirectionUtils.rotateRight90(dir) };
        if (Math.random() < 0.5) {
            swap(possDirs, 1, 2);
        }
        if (Math.random() < 0.5) {
            swap(possDirs, 3, 4);
        }
        return maxPassabilityDir(possDirs);
    }

    protected Direction maxPassabilityDir(Direction[] possDirs) throws GameActionException {
        Direction bestDir = possDirs[0];
        double maxPassability = 0;
        for (Direction curDir : possDirs) {
            if (rc.canMove(curDir)) {
                double curPassability = rc.sensePassability(rc.getLocation().add(curDir));
                if (curPassability > maxPassability) {
                    maxPassability = curPassability;
                    bestDir = curDir;
                }
            }
        }
        return bestDir;
    }

    protected boolean tryDirForward180(Direction dir) throws GameActionException {
        return tryMove(dirForward180(dir));
    }

    protected boolean tryDirForward90180(Direction dir) throws GameActionException {
        return tryDirForward90(dir) || tryDirForward180(dir);
    }

    protected boolean tryDirForward090180(Direction dir) throws GameActionException {
        return tryMove(dir) || tryDirForward90180(dir);
    }
    
    protected boolean tryDirForward090(Direction dir) throws GameActionException {
        return tryMove(dir) || tryDirForward90(dir);
    }

    protected Direction awayFromRobots(List<RobotInfo> robots) throws GameActionException {
        MapLocation location = new MapLocation(0, 0);
        if (robots.size() == 0) {
            return Direction.CENTER; // return dirTarget;
        }
        for (RobotInfo robot : robots) {
            location = location.translate(robot.location.x, robot.location.y);
        }

        location = new MapLocation(location.x / robots.size(), location.y / robots.size());
        return directionTo(location).opposite();
        // return awayFromRobots1AndTowardsRobots2(robots, Collections.emptyList());
    }

    // protected Direction awayFromRobots1AndTowardsRobots2(List<RobotInfo> robots1,
    // List<RobotInfo> robots2) throws GameActionException {
    // MapLocation location = new MapLocation(0, 0);
    // if (robots1.isEmpty() && robots2.isEmpty()) {
    // return Direction.CENTER; //return dirTarget;
    // }
    // for (RobotInfo robot : robots1) {
    // location = location.translate(robot.location.x, robot.location.y);
    // }
    // for (RobotInfo robot : robots2) {
    // MapLocation oppositeLoc = oppositePoint(rc.getLocation(), robot.location);
    // location = location.translate(oppositeLoc.x, oppositeLoc.y);
    // }
    //
    // int size = robots1.size() + robots2.size();
    // location = new MapLocation(location.x / size, location.y / size);
    // return directionTo(location).opposite();
    // }

    protected Direction awayFromAllies() throws GameActionException {
        return awayFromRobots(Arrays.asList(rc.senseNearbyRobots(sensorRadiusSquared, allyTeam)));
    }

    protected Direction towardsRobots(List<RobotInfo> robots) throws GameActionException {
        return awayFromRobots(robots).opposite();
        // return awayFromRobots1AndTowardsRobots2(Collections.emptyList(), robots);
    }

    protected void updateDirIfOnBorder() throws GameActionException {
        for (Direction dir : DirectionUtils.cardinalDirections) {
            if (!rc.onTheMap(rc.getLocation().add(dir))) {
                dir = dir.opposite();
                switch ((int) (Math.random() * 3)) {
                    case 0:
                        dirTarget = dir.rotateLeft();
                        return;
                    case 1:
                        dirTarget = dir.rotateRight();
                        return;
                    case 2:
                    default:
                        dirTarget = dir;
                }
            }
        }
    }
}