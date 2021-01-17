package simpleplayer4_alt;

import battlecode.common.*;
import common.*;
//import java.util.*;
//import simpleplayer4.Robot.FlagCodes;

class Politician extends Attacker {
    private MapLocation enemyHQ = null;
    private MapLocation neutralHQ = null;
    private boolean waiting = Math.random() < 0.1;

    Politician(RobotController rcin) throws GameActionException {
        this.rc = rcin;
        getHomeHQ();
    }

    Politician(Slanderer sland) throws GameActionException {
        // TODO: Remember to update these when new common fields are added in the Pawn
        // and Robot classes.
        this.rc = sland.rc;
        this.turnCount = sland.turnCount;
        this.hqLocation = sland.hqLocation;
        this.hqID = sland.hqID;
        this.dirTarget = sland.dirTarget;
    }

    void run() throws GameActionException {
        while (hqLocation == null && !tryEmpower(1)) {
            //getHomeHQ();
            Clock.yield();
        }
        while (true) {
            turnCount++;
            if (rc.isReady()) {
            	updateHQs();
	            endOfMatchEmpower();
	            ifOptimalEmpower();
	        	if (explorer) {
	        		runSimpleCode();
	        	} else if (!runNeutralCode() && !runAttackCode()) {
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

        // TODO: Implement differentiation for formerly slanderer units as well as
        // converted units.
    }
    
    private void updateHQs() throws GameActionException {
    	if (enemyHQ != null && rc.canSenseLocation(enemyHQ) && !rc.senseRobotAtLocation(enemyHQ).team.equals(rc.getTeam().opponent())) {
    		enemyHQ = null;
    	}
    	if (neutralHQ != null && rc.canSenseLocation(neutralHQ) && !rc.senseRobotAtLocation(neutralHQ).team.equals(Team.NEUTRAL)) {
    		neutralHQ = null;
    	}
    	
    	RobotInfo[] nearby = rc.senseNearbyRobots();
    	for (RobotInfo robot : nearby) {
            if (robot.type.equals(RobotType.ENLIGHTENMENT_CENTER)) {
                if (robot.team.equals(Team.NEUTRAL) && neutralHQ == null) {
                	neutralHQ = robot.location;
                } else if (robot.team.equals(rc.getTeam().opponent()) && enemyHQ == null) {
                	enemyHQ = robot.location;
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
        case 4:
            neutralHQ = tempLocation;
            runNeutralCode();
            break;
        case 5:
            runDefendCode();
            break;
        default:
        	runSimpleCode();
            break;
        }
    }

    private void runDefendCode() throws GameActionException {
    	//if (hqLocation == null || distanceSquaredTo(hqLocation) > 4 * rc.getType().actionRadiusSquared) {
        if (hqLocation == null) {
    		runSimpleCode();
    	} else if (distanceSquaredTo(hqLocation) > rc.getType().actionRadiusSquared) {
            tryDirForward180(directionTo(hqLocation));
        } else {
	        RobotInfo[] tempEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
	        for (RobotInfo enemy : tempEnemies) {
	        	// TODO: Factor in if optimal to empower
	        	if (huntOrKill(enemy)) {
                    return;
                }
	        }
	        tryDirForward180(directionTo(hqLocation));
        }
    }
    
    private int maxIncrease(RobotInfo robot) throws GameActionException {
    	if (isEnemy(robot)) {
        	switch(robot.type) {
    		case ENLIGHTENMENT_CENTER:
    			return Integer.MAX_VALUE;
    		case POLITICIAN:
    			return robot.conviction + robot.influence;
    		case MUCKRAKER:
    		case SLANDERER:
    			return robot.conviction;
    		default:
    			return 0;
        	}
    	} else {
    		return initialConviction(robot) - robot.conviction;
    	}
    }
    
    private int[] optimalEmpowerRadiusAndInfo() throws GameActionException {
    	if (rc.getConviction() <= 10) {
    		return new int[] {0,0,0,0};
    	}
    	int maxTotalIncrease = 0, optimalIncRadius = 0, optimalDestroyed = 0;
    	RobotInfo[] nearby = rc.senseNearbyRobots(rc.getType().actionRadiusSquared);
		for (int empRadius = 1; empRadius <= rc.getType().actionRadiusSquared; empRadius++) {
			int increase = 0, numAffected = 0, numDestroyed = 0;
	    	for (RobotInfo robot : nearby) {
	    		if (distanceSquaredTo(robot) <= empRadius) {
	    			numAffected++;
	    		}
	    	}
	    	if (numAffected == 0) {
	    		continue;
	    	}
	    	int change = (int) (rc.getConviction() * rc.getEmpowerFactor(rc.getTeam(), 0) - 10) / numAffected;
	    	// TODO: factor in remaining undivided conviction, given to later creations
	    	for (RobotInfo robot : nearby) {
	    		if (distanceSquaredTo(robot) <= empRadius) {
	    			increase += Math.min(change, maxIncrease(robot));
	    			if (isEnemy(robot) && robot.conviction < change) {
	    				numDestroyed++;
	    			}
	    		}
	    	}
	    	if (increase > maxTotalIncrease) {
	    		maxTotalIncrease = increase;
	    		optimalIncRadius = empRadius;
	    		optimalDestroyed = numDestroyed;
	    	}
		}
		
		return new int[] {optimalIncRadius, maxTotalIncrease, optimalDestroyed};
    }
    
    private void ifOptimalEmpower2() throws GameActionException {
        int range = 5;
        int nearbyThresh = 4;
        RobotInfo[] tempEnemies = rc.senseNearbyRobots(range, rc.getTeam().opponent());
        
    	double empowerThresh = 0.8;
    	int destThresh = 4;	
        int[] radAndInfo = optimalEmpowerRadiusAndInfo();
        int rad = radAndInfo[0], inc = radAndInfo[1], numDest = radAndInfo[2];
        if (rc.getConviction() > 10 && (double) inc / (rc.getConviction() - 10) >= empowerThresh
        		|| numDest >= destThresh || tempEnemies.length > nearbyThresh) {
        	tryEmpower(rad);
        }
    }
    
    private void ifOptimalEmpower() throws GameActionException {
        int range = 5;
        int nearHQThresh = 1;
        int hqMaxDist = 10;
        int defaultThresh = 4;

        RobotInfo[] tempEnemies = rc.senseNearbyRobots(range, rc.getTeam().opponent());
        if (tempEnemies.length > defaultThresh
                || (tempEnemies.length > nearHQThresh && distanceSquaredTo(hqLocation) <= hqMaxDist)) {
            tryEmpower(range);
        }
    }

    protected boolean huntOrKill(RobotInfo enemy) throws GameActionException {
        return (withinAttackRange(enemy) && tryEmpower(distanceSquaredTo(enemy)))
                || tryDirForward180(directionTo(enemy.getLocation()));
    }
    
    private boolean HQAttackRoutine(MapLocation locHQ) throws GameActionException {
      if (rc.getLocation().isAdjacentTo(locHQ)) {
          if (DirectionUtils.isCardinal(directionTo(locHQ))) {
              return tryEmpower(1);
          } else {
              if (!tryDirForward90(directionTo(locHQ))) {
                  return tryEmpower(2);
              }
          }
      } else if (!tryDirForward180(directionTo(locHQ))) {
    	  if (withinAttackRange(locHQ)) {
	    	  return tryEmpower(distanceSquaredTo(locHQ));
	      } else {
	    	  return false;
	      }
      }
      return true;
  }

    private boolean runNeutralCode() throws GameActionException {
    	if (neutralHQ == null) {
    		return false;
    	} else {
    		if (!waiting) {
    			return HQAttackRoutine(neutralHQ);
    		}
    		
    		if (rc.canSenseLocation(neutralHQ)) {
    			if (rc.senseRobotAtLocation(neutralHQ).conviction < 25) {
    				return HQAttackRoutine(neutralHQ);
    			} else {
    				return tryDirForward180(directionTo(neutralHQ).opposite());
    			}
    		} else {
    			return tryDirForward180(directionTo(neutralHQ));
    		}
    	}
    }

    private boolean runAttackCode() throws GameActionException {
    	if (enemyHQ == null) {
    		return false;
    	} else {
        	return HQAttackRoutine(enemyHQ);
    	}
    }

    private boolean runSimpleCode() throws GameActionException {
    	return tryDirForward180(awayFromAllies());
    }

    private boolean tryEmpower(int radius) throws GameActionException {
        if (rc.canEmpower(radius)) {
            rc.empower(radius);
            return true;
        }
        return false;
    }

    private void endOfMatchEmpower() throws GameActionException {
        if (rc.getRoundNum() > GameConstants.GAME_MAX_NUMBER_OF_ROUNDS - 25) {
            tryEmpower(RobotType.POLITICIAN.actionRadiusSquared);
        }
    }
    
}
