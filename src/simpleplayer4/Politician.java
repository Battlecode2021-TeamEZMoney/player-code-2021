package simpleplayer4;

import battlecode.common.*;
import common.*;
//import java.util.*;
//import simpleplayer4.Robot.FlagCodes;

class Politician extends Attacker {
	private MapLocation enemyHQ = null;
	private MapLocation neutralHQ = null;
	private boolean waiting = Math.random() < 0.1;

	Politician(RobotController rcin) throws GameActionException {
		super(rcin); // Don't remove this.
	}

	Politician(Slanderer sland) throws GameActionException {
		// TODO: Remember to update these when new common fields are added in the Pawn
		// and Robot classes.
		super(sland.rc);
		this.turnCount = sland.turnCount;
		this.hqLocation = sland.hqLocation;
		this.hqID = sland.hqID;
		this.dirTarget = sland.dirTarget;
	}

	void run() throws GameActionException {
//		while (hqLocation == null && !tryEmpower(1)) {
//			// getHomeHQ();
//			Clock.yield();
//		}
		while (true) {
			turnCount++;
			if (rc.isReady()) {
				updateHQs();
				if (Math.random() < 0.8) {
					ifOptimalSelfEmpower();
					ifOptimalEmpower();
				}
				endOfMatchEmpower();
				// start = Clock.getBytecodesLeft();
				if (Clock.getBytecodesLeft() > 6000) {
					if (explorer) {
						runSimpleCode();
					} else if (!runNeutralCode() && !runAttackCode()) { // && !runDefendCode()
						if (rc.canGetFlag(hqID)) {
							parseHQFlag(rc.getFlag(hqID));
						} else {
							runSimpleCode();
						}
					}
				}
			} else {
				if (enemyHQ == null && neutralHQ == null && rc.canGetFlag(hqID)) {
					parseHQFlag(rc.getFlag(hqID));
				}
			}
			setNearbyHQFlag();
			// if (Clock.getBytecodesLeft()-start < -4000) System.out.println("Used " +
			// (Clock.getBytecodesLeft()-start));

			Clock.yield();
		}

		// TODO: Implement differentiation for formerly slanderer units as well as
		// converted units.
	}

	private void updateHQs() throws GameActionException {
		if (enemyHQ != null && rc.canSenseLocation(enemyHQ)
				&& !rc.senseRobotAtLocation(enemyHQ).team.equals(enemyTeam)) {
			enemyHQ = null;
		}
		if (neutralHQ != null && rc.canSenseLocation(neutralHQ)
				&& !rc.senseRobotAtLocation(neutralHQ).team.equals(Team.NEUTRAL)) {
			neutralHQ = null;
		}

		RobotInfo[] nearby = rc.senseNearbyRobots();
		for (RobotInfo robot : nearby) {
			if (robot.type.equals(RobotType.ENLIGHTENMENT_CENTER)) {
				if (robot.team.equals(Team.NEUTRAL) && neutralHQ == null) {
					neutralHQ = robot.location;
				} else if (robot.team.equals(enemyTeam) && enemyHQ == null) {
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
			// case 5:
			// defending = true;
			// runDefendCode();
			// break;
			default:
				runSimpleCode();
				break;
		}
	}

	private boolean runDefendCode() throws GameActionException {
		// if (hqLocation == null || distanceSquaredTo(hqLocation) > 4 * actionRadiusSquared) {

		// if (hqLocation == null) {
		// runSimpleCode();
		// } else if (distanceSquaredTo(hqLocation) > actionRadiusSquared)
		// {
		// tryDirForward180(directionTo(hqLocation));
		// } else {
		// RobotInfo[] tempEnemies = rc.senseNearbyRobots(-1, enemyTeam);
		// for (RobotInfo enemy : tempEnemies) {
		// // TODO: Factor in if optimal to empower
		// if (huntOrKill(enemy)) {
		// return;
		// }
		// }
		// tryDirForward180(directionTo(hqLocation));
		// }
		if (!rc.isReady()) {
			return false;
		}
		
		if (hqLocation == null) {
			return runSimpleCode();
		} else if (!defending) {
			return false;
		}

		if (distanceSquaredTo(hqLocation) <= actionRadiusSquared && crowdedByEnemy(hqLocation)) {
			// System.out.println("Crowded");
			return ifOptimalEmpower(0, 0);
		} else if (distanceSquaredTo(hqLocation) > actionRadiusSquared / 2
				&& tryDirForward90(directionTo(hqLocation))) {
			return true;
		} else {
			if (distanceSquaredTo(hqLocation) > actionRadiusSquared) {
				defending = false;
			}
			return tryDirForward180(directionTo(hqLocation).opposite());
		}

		// else if
		// (rc.isLocationOccupied(rc.getLocation().add(directionTo(hqLocation)))) {
		//
		// }
	}

	private int maxIncrease(RobotInfo robot) throws GameActionException {
		if (isEnemy(robot)) {
			switch (robot.type) {
				case ENLIGHTENMENT_CENTER:
					return Integer.MAX_VALUE;
				case POLITICIAN:
					return robot.conviction + robot.influence;
				case MUCKRAKER:
				case SLANDERER:
					return robot.conviction + 1;
				default:
					return 0;
			}
		} else {
			return initialConviction(robot) - robot.conviction;
		}
	}

	private int[] optimalEmpowerRadiusAndInfo() throws GameActionException {
		if (rc.getConviction() <= 10) {
			return new int[] { 0, 0, 0, 0 };
		}
		int maxTotalIncrease = 0, optimalIncRadius = 0, optimalDestroyed = 0;
		RobotInfo[] nearby = rc.senseNearbyRobots(actionRadiusSquared);
		for (int empRadius = 1; empRadius <= actionRadiusSquared; empRadius++) {
			int increase = 0, numAffected = 0, numDestroyed = 0;
			for (RobotInfo robot : nearby) {
				if (distanceSquaredTo(robot) <= empRadius) {
					numAffected++;
				}
			}
			if (numAffected == 0) {
				continue;
			}
			int change = (int) (rc.getConviction() * rc.getEmpowerFactor(allyTeam, 0) - 10) / numAffected;
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

		return new int[] { optimalIncRadius, maxTotalIncrease, optimalDestroyed };
	}

	private boolean ifOptimalSelfEmpower() throws GameActionException {
		if (hqLocation == null) {
			return false;
		}
		if (rc.getEmpowerFactor(allyTeam, 0) > 4
				&& distanceSquaredTo(hqLocation) <= actionRadiusSquared) {
			return HQAttackRoutine(hqLocation);
		}
		return false;
	}

	private boolean ifOptimalEmpower() throws GameActionException {
		return ifOptimalEmpower(0.7);
	}

	private boolean ifOptimalEmpower(double empowerThresh) throws GameActionException {
		return ifOptimalEmpower(empowerThresh, 6);
	}

	private boolean ifOptimalEmpower(double empowerThresh, int destThresh) throws GameActionException {
		int[] radAndInfo = optimalEmpowerRadiusAndInfo();
		int rad = radAndInfo[0], inc = radAndInfo[1], numDest = radAndInfo[2];
		if (rc.getConviction() > 10 && (double) inc / rc.getConviction() >= empowerThresh || numDest >= destThresh) {
			return tryEmpower(rad);
		}
		return false;
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

	protected boolean huntOrKill(RobotInfo enemy) throws GameActionException {
		return (withinAttackRange(enemy) && tryEmpower(distanceSquaredTo(enemy)))
				|| tryDirForward180(directionTo(enemy.getLocation()));
	}

	private boolean runNeutralCode() throws GameActionException {
		if (!rc.isReady()) {
			return false;
		}
		
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
		if (!rc.isReady()) {
			return false;
		}
		
		if (enemyHQ == null) {
			return false;
		}

		return HQAttackRoutine(enemyHQ);
	}

	private boolean runSimpleCode() throws GameActionException {
		if (!rc.isReady()) {
			return false;
		}
		
		return tryDirForward180(awayFromAllies()) || tryDirForward180(DirectionUtils.randomDirection());
	}

	private boolean tryEmpower(int radius) throws GameActionException {
		if (rc.canEmpower(radius)) {
			rc.empower(radius);
			return true;
		}
		return false;
	}

	private boolean endOfMatchEmpower() throws GameActionException {
		if (rc.getRoundNum() > GameConstants.GAME_MAX_NUMBER_OF_ROUNDS - 25) {
			return tryEmpower(actionRadiusSquared);
		}
		return false;
	}

}
