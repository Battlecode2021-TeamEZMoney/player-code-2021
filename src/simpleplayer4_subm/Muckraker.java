package simpleplayer4_subm;

import battlecode.common.*;

class Muckraker extends Attacker {
    private MapLocation enemyHQ = null;

    Muckraker(RobotController rcin) throws GameActionException {
        this.rc = rcin;
        getHomeHQ();
    }

    void run() throws GameActionException {
        while (true) {
            turnCount++;
            if (rc.isReady()) {
                updateHQs();
            	if (explorer) {
            		runSimpleCode();
            	} else if (!runAttackCode()) {
		            if (rc.canGetFlag(hqID)) {
		                parseHQFlag(rc.getFlag(hqID));
		            } else {
		            	runSimpleCode();
		            }
	            }
            }
            setNearbyHQFlag();
            
            Clock.yield();
        }
    }
    
    private void updateHQs() throws GameActionException {
    	if (enemyHQ != null && rc.canSenseLocation(enemyHQ) && !rc.senseRobotAtLocation(enemyHQ).team.equals(rc.getTeam().opponent())) {
    		enemyHQ = null;
    	}
    	if (enemyHQ == null) {
	    	RobotInfo[] nearby = rc.senseNearbyRobots();
	    	for (RobotInfo robot : nearby) {
	            if (robot.type.equals(RobotType.ENLIGHTENMENT_CENTER)) {
	                if (robot.team.equals(rc.getTeam().opponent())) {
	                	enemyHQ = robot.location;
	                }
	            }
	        }
    	}
    }

    private void parseHQFlag(int flag) throws GameActionException {
        MapLocation tempLocation = Encoding.getLocationFromFlag(rc, flag);
        switch (Encoding.getInfoFromFlag(flag)) {
        case 2:
            enemyHQ = tempLocation;
            runAttackCode();
            break;
        default:
            runSimpleCode();
            break;
        }
    }
    
    private boolean huntOrExposeSlanderer() throws GameActionException {
    	RobotInfo[] nearby = rc.senseNearbyRobots();
    	int maxExposeInf = 0, maxSenseInf = 0;
    	MapLocation robotExposeLoc = Constants.origin, robotSenseLoc = Constants.origin;
    	for (RobotInfo robot : nearby) {
            if (robot.type.equals(RobotType.SLANDERER) && robot.team.equals(rc.getTeam().opponent())) {
            	if (robot.influence > maxSenseInf) {
            		maxSenseInf = robot.influence;
            		robotSenseLoc = robot.location;
            	}
        		if (rc.canExpose(robot.location) && robot.influence > maxExposeInf) {
            		maxExposeInf = robot.influence;
            		robotExposeLoc = robot.location;
            	}
            }
        }
    	if (robotExposeLoc != Constants.origin) {
    		return tryExpose(robotExposeLoc);
    	} else if (robotSenseLoc != Constants.origin) {
    		return tryDirForward90(directionTo(robotSenseLoc));
    	}
    	return false;
    }
    
    private boolean HQAttackRoutine(MapLocation locHQ) throws GameActionException {
    	if (huntOrExposeSlanderer()) {
    		return true;
    	} else if (distanceSquaredTo(locHQ) > rc.getType().actionRadiusSquared) {
        	return tryDirForward180(directionTo(locHQ));
        }
//    	else {
//        	return tryDirForward180(awayFromAllies());
//        }
    	return false;
    }
    
    private boolean runAttackCode() throws GameActionException {
    	if (enemyHQ == null) {
    		return false;
    	} else {
        	return HQAttackRoutine(enemyHQ);
    	}
    }
    
    private boolean runSimpleCode() throws GameActionException {
    	return huntOrExposeSlanderer() || tryDirForward180(awayFromAllies()) || tryDirForward180(DirectionUtils.randomDirection());
    }

    private boolean tryExpose(int id) throws GameActionException {
        if(rc.canExpose(id)){
            rc.expose(id);
            return true;
        }
        return false;
    }

    private boolean tryExpose(MapLocation pos) throws GameActionException {
        if(rc.canExpose(pos)){
            rc.expose(pos);
            return true;
        }
        return false;
    }

    private boolean tryExpose(RobotInfo enemy) throws GameActionException {
        return tryExpose(enemy.getID());
    }

    protected boolean huntOrKill(RobotInfo enemy) throws GameActionException {
        return (withinAttackRange(enemy) && tryExpose(enemy))
                || tryDirForward180(directionTo(enemy.getLocation()));
    }

}
