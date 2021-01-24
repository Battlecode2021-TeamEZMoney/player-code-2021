package usqualplayer1_subm4;

import battlecode.common.*;

class Muckraker extends Attacker {
	private MapLocation enemyHQ = null;

	Muckraker(RobotController rcin) throws GameActionException {
		super(rcin);
	}

	void run() throws GameActionException {
		while (true) {
			turnCount++;
			if (rc.isReady()) {
				updateHQs();
				if (explorer) {
					runSimpleCode();
				} else if (enemyHQ != null) {
					runAttackCode();
				} else if (defending && hqLocation != null) {
					runDefendCode();
				} else {
					if (rc.canGetFlag(hqID)) {
						parseHQFlag(rc.getFlag(hqID));
					} else {
						runSimpleCode();
					}
				}
			} else {
				if (enemyHQ == null && !defending && rc.canGetFlag(hqID)) {
					parseHQFlag(rc.getFlag(hqID));
				}
			}
			setNearbyHQFlag();

			Clock.yield();
		}
	}

	private void updateHQs() throws GameActionException {
		if (enemyHQ != null && rc.canSenseLocation(enemyHQ)
				&& !rc.senseRobotAtLocation(enemyHQ).team.equals(enemyTeam)) {
			enemyHQ = null;
		}
		if (enemyHQ == null) {
			RobotInfo[] nearby = rc.senseNearbyRobots();
			for (RobotInfo robot : nearby) {
				if (robot.type.equals(RobotType.ENLIGHTENMENT_CENTER)) {
					if (robot.team.equals(enemyTeam)) {
						enemyHQ = robot.location;
					}
				}
			}
		}
	}

	private void parseHQFlag(int flag) throws GameActionException {
		MapLocation tempLocation = Encoding.getLocationFromFlag(rc, flag);
		switch (Encoding.getTypeFromFlag(flag)) {
			case 2:
				enemyHQ = tempLocation;
				runAttackCode();
				break;
			case 5:
				defending = true;
				runDefendCode();
				break;
			default:
				runSimpleCode();
				break;
		}
	}

	private void runDefendCode() throws GameActionException {
		if (!rc.isReady() || !defending || hqLocation == null) {
			return;
		}

		if (distanceSquaredTo(hqLocation) > actionRadiusSquared / 2
				&& tryDirForward90(directionTo(hqLocation))) {
			return;
		}
		
		if (distanceSquaredTo(hqLocation) > actionRadiusSquared) {
			defending = false;
		}
		tryDirForward90180(directionTo(hqLocation).opposite());
	}

	private boolean huntOrExposeSlanderer() throws GameActionException {
		RobotInfo[] nearby = rc.senseNearbyRobots();
		int maxExposeInf = 0, maxSenseInf = 0;
		MapLocation robotExposeLoc = Constants.origin, robotSenseLoc = Constants.origin;
		
		for (RobotInfo robot : nearby) {
			if (robot.type.equals(RobotType.SLANDERER) && robot.team.equals(enemyTeam)) {
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
			tryDirForward90(directionTo(robotSenseLoc));
			return true;
		}
		return false;
	}

	private void HQAttackRoutine(MapLocation locHQ) throws GameActionException {
		if (huntOrExposeSlanderer()) {
			return;
		} else if (distanceSquaredTo(locHQ) > actionRadiusSquared && tryDirForward90180(directionTo(locHQ))) {
			return;
		} else {
			tryDirForward90180(directionTo(locHQ).opposite());
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
		
		if (!huntOrExposeSlanderer() && !tryDirForward90(dirTarget)) {
			tryDirForward90180(awayFromAllies());
		}
		
		// Other attempts to prevent muckraker cramming
//		return huntOrExposeSlanderer() || tryDirForward90(dirTarget = awayFromAllies())
//				|| tryDirForward90180(dirTarget = DirectionUtils.randomDirection());
		
//		return huntOrExposeSlanderer() || tryDirForward90(dirTarget)
//				|| tryDirForward90(dirTarget = awayFromAllies())
//				|| tryDirForward90180(dirTarget = DirectionUtils.randomDirection());
		
//		if (rc.getRoundNum() < Constants.MAX_ROUNDS) {
//			return huntOrExposeSlanderer() || tryDirForward90(dirTarget)
//					|| tryDirForward90180(dirTarget = DirectionUtils.randomDirection());
//		} else {
//			return huntOrExposeSlanderer() || tryDirForward90(dirTarget) || tryDirForward90180(awayFromAllies())
//					|| tryDirForward90180(dirTarget = DirectionUtils.randomDirection());
//		}
	
	}

	private boolean tryExpose(MapLocation pos) throws GameActionException {
		if (rc.canExpose(pos)) {
			rc.expose(pos);
			return true;
		}
		return false;
	}

}
