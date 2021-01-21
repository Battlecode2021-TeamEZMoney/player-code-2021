package usqualplayer1;

import battlecode.common.*;
import java.util.*;

class Slanderer extends Pawn {
    Politician successor;
    private MapLocation slandCenter = null;

    Slanderer(RobotController rcin) throws GameActionException {
        super(rcin);
    }

    void run() throws GameActionException {
		while (rc.getType().equals(RobotType.SLANDERER)) {
			turnCount++;
			if (rc.isReady()) {
				if (slandCenter != null && hqLocation != null) {
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
			//setNearbyHQFlag();

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
				runToSlandCenter();
				break;
			default:
				runSimpleCode();
				break;
		}
	}
	
	private void runToSlandCenter() throws GameActionException {
		if (!rc.isReady() || slandCenter == null || hqLocation == null) {
			return;
		}

		if (!tryDirForward90180(awayFromEnemyMuckrakers())) {
			tryDirForward90(directionTo(slandCenter));
		}
	}

	private void runSimpleCode() throws GameActionException {
		if (!rc.isReady()) {
			return;
		}
		
		if (!tryDirForward90180(awayFromEnemyMuckrakers())) {
			if (distanceSquaredTo(hqLocation) < 25) {
				tryDirForward90(directionTo(hqLocation).opposite());
			}
		}
		
	}

    protected Direction awayFromEnemyMuckrakers() throws GameActionException {
        ArrayList<RobotInfo> robots = new ArrayList<>(Arrays.asList(rc.senseNearbyRobots(sensorRadiusSquared, enemyTeam)));
        if (robots.size() == 0) {
        	return Direction.CENTER;
        }
        robots.removeIf(r -> (r.type != RobotType.MUCKRAKER));
        return awayFromRobots(robots);
    }
}