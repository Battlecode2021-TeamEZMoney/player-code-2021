package sprintplayer2_sprint2;

import battlecode.common.*;

class Muckraker extends Attacker {
	private MapLocation enemyHQ = null;

	Muckraker(RobotController rcin) throws GameActionException {
		super(rcin); // Don't remove this.
	}

	void run() throws GameActionException {
		while (true) {
			turnCount++;
			if (rc.isReady()) {
				updateHQs();
				if (explorer) {
					runSimpleCode();
				} else if (!runAttackCode() && !runDefendCode()) {
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
		switch (Encoding.getInfoFromFlag(flag)) {
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

	private boolean runDefendCode() throws GameActionException {
		if (!rc.isReady()) {
			return false;
		}

		if (hqLocation == null) {
			return runSimpleCode();
		} else if (!defending) {
			return false;
		}

		if (distanceSquaredTo(hqLocation) > actionRadiusSquared / 2 && tryDirForward90(directionTo(hqLocation))) {
			return true;
		} else {
			if (distanceSquaredTo(hqLocation) > actionRadiusSquared) {
				defending = false;
			}
			return tryDirForward180(directionTo(hqLocation).opposite());
		}
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
			return tryDirForward90(directionTo(robotSenseLoc));
		}
		return false;
	}

	private boolean HQAttackRoutine(MapLocation locHQ) throws GameActionException {
		// if (huntOrExposeSlanderer()) {
		// return true;
		// } else if (distanceSquaredTo(locHQ) > actionRadiusSquared) {
		// return tryDirForward180(directionTo(locHQ));
		// }
		//// else {
		//// return tryDirForward180(awayFromAllies());
		//// }
		// return false;

		if (huntOrExposeSlanderer()) {
			return true;
		} else if (distanceSquaredTo(locHQ) > actionRadiusSquared && tryDirForward90(directionTo(locHQ))) {
			return true;
		} else {
			return tryDirForward180(directionTo(locHQ).opposite());
		}
	}

	private boolean runAttackCode() throws GameActionException {
		if (!rc.isReady()) {
			return false;
		}

		if (enemyHQ == null) {
			return false;
		} else {
			return HQAttackRoutine(enemyHQ);
		}
	}

	private Direction muckAwayFromAllies() throws GameActionException {
		Direction away_dir = awayFromAllies();
		if (!rc.onTheMap(rc.getLocation().add(away_dir))) {
			away_dir = away_dir.opposite();
		}
		if (DirectionUtils.within45Degrees(dirTarget, away_dir)) {
			return dirTarget;
		}
		return away_dir;
	}

	private boolean runSimpleCode() throws GameActionException {
		if (!rc.isReady()) {
			return false;
		}

		return huntOrExposeSlanderer() || tryDirForward180(dirTarget)
				|| tryDirForward180(dirTarget = DirectionUtils.randomDirection());
		// return huntOrExposeSlanderer() || tryDirForward180(muckAwayFromAllies())
		// || tryDirForward180(DirectionUtils.randomDirection());
	}

	private boolean tryExpose(int id) throws GameActionException {
		if (rc.canExpose(id)) {
			rc.expose(id);
			return true;
		}
		return false;
	}

	private boolean tryExpose(MapLocation pos) throws GameActionException {
		if (rc.canExpose(pos)) {
			rc.expose(pos);
			return true;
		}
		return false;
	}

	private boolean tryExpose(RobotInfo enemy) throws GameActionException {
		return tryExpose(enemy.getID());
	}

	protected boolean huntOrKill(RobotInfo enemy) throws GameActionException {
		return (withinAttackRange(enemy) && tryExpose(enemy)) || tryDirForward180(directionTo(enemy.getLocation()));
	}

}
