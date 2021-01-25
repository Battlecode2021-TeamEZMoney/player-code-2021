package usqualplayer2;

import battlecode.common.*;
import java.util.*;

class Slanderer extends Pawn {
	Politician successor;

	Slanderer(RobotController rcin) throws GameActionException {
		super(rcin);
	}

	void run() throws GameActionException {
		while (rc.getType().equals(RobotType.SLANDERER)) {
			turnCount++;
			if (hasOrCanGetHomeHQ()) {
				parseHQFlag(rc.getFlag(hqID));

			}
			if (slandCenter != null) {
				runToSlandCenter();
			} else {
				runSimpleCode();
			}

			setNearbyHQFlag();

			Clock.yield();
		}
		if (successor == null) {
			successor = new Politician(this, slandCenter);
		}
		successor.run();
	}

	private void parseHQFlag(int flag) throws GameActionException {
		MapLocation tempLocation = Encoding.getLocationFromFlag(rc, flag);
		switch (Encoding.getTypeFromFlag(flag)) {
			case 6:
				slandCenter = tempLocation;
				break;
			default:
				break;
		}
	}

	private void runToSlandCenter() throws GameActionException {
		if (!rc.isReady()) {
			return;
		} else if (slandCenter == null) {
			runSimpleCode();
			return;
		}

		if (!tryDirForward090180(awayFromEnemyMuckrakers())) {
			if (tryMove(pathingController.dirToTarget(slandCenter))) {
				return;
			} else {
				if (Math.random() < 0.2) {
					tryDirForward090180(directionTo(slandCenter).opposite());
				}
			}
		}
	}

	private void runSimpleCode() throws GameActionException {
		if (!rc.isReady()) {
			return;
		}

		if (!tryDirForward090180(awayFromEnemyMuckrakers())) {
			if (hqLocation != null && distanceSquaredTo(hqLocation) < 8) {
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