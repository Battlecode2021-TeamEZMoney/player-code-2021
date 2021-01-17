package simpleplayer4_v1;

import battlecode.common.*;
import common.*;
import java.util.function.Function;

abstract class Pawn extends Robot {
    protected MapLocation hqLocation = null;
    protected int hqID;
    protected Direction dirTarget = Direction.CENTER;
	protected boolean explorer = false;

    static Pawn unitFromRobotController(RobotController rc) throws Exception {
    	//getHomeHQ();
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
    
    protected Direction awayFromNearby(Function<RobotInfo, Boolean> condition) throws GameActionException {
    	RobotInfo[] nearby = rc.senseNearbyRobots();
        MapLocation location = new MapLocation(0, 0);
        int numRobots = 0;
        for (RobotInfo robot : nearby) {
        	if (condition.apply(robot)) {
	            location = location.translate(robot.location.x, robot.location.y);
	            numRobots++;
        	}
        }
        if (numRobots == 0) return dirTarget;
        MapLocation avgLocation = new MapLocation(location.x / numRobots, location.y / numRobots);
        return directionTo(avgLocation).opposite();
    }
    
    protected Direction awayFromAllies() throws GameActionException {
    	Function<RobotInfo, Boolean> allies = robot -> robot.team == rc.getTeam();
        return awayFromNearby(allies);
    }
    
    protected Direction awayFromEnemyMuckrakers() throws GameActionException {
    	Function<RobotInfo, Boolean> enemyMuckrakers = robot -> (robot.type == RobotType.MUCKRAKER && robot.team == rc.getTeam().opponent());
        return awayFromNearby(enemyMuckrakers);
    }
    
//	Direction best_dir = Direction.CENTER;
//	double minDist = Double.MAX_VALUE;
//	RobotInfo[] allies = rc.senseNearbyRobots(-1, rc.getTeam());
//	//System.out.println(allies.length);
//	for (Direction dir : DirectionUtils.nonCenterDirections) {
//		if (rc.canMove(dir)) {
//    		double totalDist = 0;
//    		for (RobotInfo ally : allies) {
//    			totalDist += (double) 1 / rc.getLocation().distanceSquaredTo(ally.location);
//    		}
//    		if (totalDist < minDist) {
//    			minDist = totalDist;
//    			best_dir = dir;
//    		}
//		}
//	}
//	return best_dir;
}
