package usqualplayer1_v1;

import battlecode.common.*;
import common.*;
//import java.util.*;
//import simpleplayer4.Robot.FlagCodes;

class Politician extends Attacker {
	private MapLocation enemyHQ = null;
	private MapLocation neutralHQ = null;
	private boolean waiting = Math.random() < 0.1;
	private MapLocation slandCenter = null;

	Politician(RobotController rcin) throws GameActionException {
		super(rcin);
	}

	Politician(Slanderer sland, MapLocation slandCenter) throws GameActionException {
		// TODO: Remember to update these when new common fields are added in the Pawn
		// and Robot classes.
		super(sland.rc);
		this.turnCount = sland.turnCount;
		this.hqLocation = sland.hqLocation;
		this.hqID = sland.hqID;
		this.dirTarget = sland.dirTarget;
		this.slandCenter = slandCenter;
	}

	void run() throws GameActionException {
		while (true) {
			turnCount++;
			if (rc.isReady()) {
				updateHQs();
				if (Math.random() < 0.8) {
					tryOptimalSelfEmpower();
					// int start = Clock.getBytecodesLeft();
					tryOptimalEmpower();
					// System.out.println(Clock.getBytecodesLeft()-start);
				}
				endOfMatchEmpower();
				// start = Clock.getBytecodesLeft();
				if (Clock.getBytecodesLeft() > 6000) {
					if (explorer) {
						runSimpleCode();
					} else if (slandCenter != null) {
						defendSlandCenter();
					} else if (neutralHQ != null) {
						runNeutralCode();
					} else if (enemyHQ != null) {
						runAttackCode();
					} else {
						if (rc.canGetFlag(hqID)) {
							parseHQFlag(rc.getFlag(hqID));
						} else {
							runSimpleCode();
						}
					}
				}
			} else {
				if (rc.canGetFlag(hqID) && slandCenter == null && enemyHQ == null && neutralHQ == null) {
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
		if (neutralHQ == null) {
			for (RobotInfo robot : nearby) {
				if (robot.type.equals(RobotType.ENLIGHTENMENT_CENTER) && robot.team.equals(Team.NEUTRAL)) {
					neutralHQ = robot.location;
					break;
				}
			}
		}
		if (enemyHQ == null) {
			for (RobotInfo robot : nearby) {
				if (robot.type.equals(RobotType.ENLIGHTENMENT_CENTER) && robot.team.equals(enemyTeam)) {
					enemyHQ = robot.location;
					break;
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
			case 6:
				slandCenter = tempLocation;
				defendSlandCenter();
				break;
			default:
				runSimpleCode();
				break;
		}
	}

	private void defendSlandCenter() throws GameActionException {
		if (!rc.isReady() || slandCenter == null) {
			return;
		}

		boolean exitDefendSlandCenter = false;

		if (distanceSquaredTo(slandCenter) <= actionRadiusSquared) {
			empowerIfMuckNearby();
		}
		if (distanceSquaredTo(slandCenter) > actionRadiusSquared && tryDirForward90(directionTo(slandCenter))) {
			return;
		}
		if (distanceSquaredTo(slandCenter) > actionRadiusSquared * 2) {
			exitDefendSlandCenter = true;
		}
		tryDirForward90180(directionTo(slandCenter).opposite());
		if (exitDefendSlandCenter) {
			slandCenter = null;
		}
	}

	private void empowerIfMuckNearby() throws GameActionException {
		for (RobotInfo robot : rc.senseNearbyRobots(sensorRadiusSquared, enemyTeam)) {
			if (robot.type.equals(RobotType.MUCKRAKER)) {
				tryEmpower(distanceSquaredTo(robot));
			}
		}
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

	private void tryOptimalSelfEmpower() throws GameActionException {
		if (hqLocation == null) {
			return;
		}

		if (rc.getEmpowerFactor(allyTeam, 0) > 4 && distanceSquaredTo(hqLocation) <= actionRadiusSquared) {
			HQAttackRoutine(hqLocation);
		}
	}

	private void tryOptimalEmpower() throws GameActionException {
		tryOptimalEmpower(0.7);
	}

	private void tryOptimalEmpower(double empowerThresh) throws GameActionException {
		tryOptimalEmpower(empowerThresh, 6);
	}

	private void tryOptimalEmpower(double empowerThresh, int destThresh) throws GameActionException {
		int[] radAndInfo = optimalEmpowerRadiusAndInfo();
		int rad = radAndInfo[0], inc = radAndInfo[1], numDest = radAndInfo[2];
		if ((rc.getConviction() > 10 && (double) inc / rc.getConviction() >= empowerThresh) || numDest >= destThresh) {
			tryEmpower(rad);
		}
	}

	private void HQAttackRoutine(MapLocation locHQ) throws GameActionException {
		if (rc.getLocation().isAdjacentTo(locHQ)) {
			if (DirectionUtils.isCardinal(directionTo(locHQ))) {
				tryEmpower(1);
			} else {
				if (!tryDirForward90(directionTo(locHQ))) {
					tryEmpower(2);
				}
			}
		} else if (!tryDirForward90(directionTo(locHQ))) {
			if (withinAttackRange(locHQ)) {
				tryEmpower(distanceSquaredTo(locHQ));
			} else {
				tryDirForward180(directionTo(locHQ));
			}
		}
	}

	private void runNeutralCode() throws GameActionException {
		if (!rc.isReady() || neutralHQ == null) {
			return;
		}

		if (!waiting || (rc.canSenseLocation(neutralHQ) && rc.senseRobotAtLocation(neutralHQ).conviction < 25)) {
			HQAttackRoutine(neutralHQ);
		} else {
			if (distanceSquaredTo(neutralHQ) > actionRadiusSquared && tryDirForward90(directionTo(neutralHQ))) {
				return;
			}
			tryDirForward90180(directionTo(neutralHQ).opposite());
		}
	}

	private void runAttackCode() throws GameActionException {
		if (!rc.isReady() || enemyHQ == null) {
			return;
		}

		HQAttackRoutine(enemyHQ);
	}

	private void runSimpleCode() throws GameActionException {
		if (!rc.isReady()) {
			return;
		}

		updateDirIfOnBorder();

		if (!tryDirForward90(dirTarget)) {
			tryDirForward90180(awayFromAllies());
		}

		// if (!tryDirForward90180(awayFromAllies())) {
		// tryDirForward90180(DirectionUtils.randomDirection());
		// }
	}

	private void tryEmpower(int radius) throws GameActionException {
		if (rc.canEmpower(radius)) {
			rc.empower(radius);
		}
	}

	private void endOfMatchEmpower() throws GameActionException {
		if (rc.getRoundNum() > GameConstants.GAME_MAX_NUMBER_OF_ROUNDS - 25) {
			tryEmpower(actionRadiusSquared);
		}
	}

}
