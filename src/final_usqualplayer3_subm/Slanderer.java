package final_usqualplayer3_subm;

import battlecode.common.*;

import java.util.*;

class Slanderer extends Pawn {
	Politician successor;

	Slanderer(RobotController rcin) throws GameActionException {
		super(rcin);
	}

	/**
	 * Searches for a nearby HQ and receives its task from the stored HQ.
	 * Accordingly runs to the slandCenter or performs a default (simple) routine.
	 */
	void run() throws GameActionException {
		while (rc.getType().equals(RobotType.SLANDERER)) {
			turnCount++;
			if (slandCenter != null && rc.canGetFlag(hqID)) {
				parseHQFlagSland(rc.getFlag(hqID));
			}

			if (rc.isReady()) {
				if (slandCenter != null) {
					runToSlandCenter();
				} else {
					if (rc.canGetFlag(hqID)) {
						parseHQFlag(rc.getFlag(hqID));
					} else {
						runSimpleCode();
					}
				}
			} else {
				if (slandCenter == null && rc.canGetFlag(hqID)) {
					parseHQFlag(rc.getFlag(hqID));
				}
			}
			setNearbyHQFlag();

			Clock.yield();
		}
		if (successor == null) {
			successor = new Politician(this);
		}
		successor.run();
	}

	private void parseHQFlag(int flag) throws GameActionException {
		MapLocation tempLocation = Encoding.getLocationFromFlag(rc, flag);
		switch (Encoding.getTypeFromFlag(flag)) {
		case 6:
			slandCenter = tempLocation;
			runToSlandCenter();
			break;
		default:
			runSimpleCode();
			break;
		}
	}

	private void runToSlandCenter() throws GameActionException {
		if (!rc.isReady() || slandCenter == null) {
			return;
		}

		if (!tryDirForward090180(awayFromEnemyMuckrakers())) {
			if (distanceSquaredTo(slandCenter) > -1 && tryDirForward090180(directionTo(slandCenter))) {
				return;
			} else {
				if (Math.random() < 0.2) {
					tryDirForward090180(directionTo(slandCenter).opposite());
				}
			}
		}
	}

	private void runSimpleCode() throws GameActionException {
		if (!rc.isReady() || hqLocation == null) {
			return;
		}

		if (!tryDirForward090180(awayFromEnemyMuckrakers())) {
			if (distanceSquaredTo(hqLocation) < 8) {
				tryDirForward90(directionTo(hqLocation).opposite());
			}
		}

	}

	protected Direction awayFromEnemyMuckrakers() throws GameActionException {
		ArrayList<RobotInfo> robots = new ArrayList<>(
				Arrays.asList(rc.senseNearbyRobots(sensorRadiusSquared, enemyTeam)));
		if (robots.size() == 0) {
			return Direction.CENTER;
		}
		robots.removeIf(r -> (r.type != RobotType.MUCKRAKER));
		return awayFromRobots(robots);
	}
}