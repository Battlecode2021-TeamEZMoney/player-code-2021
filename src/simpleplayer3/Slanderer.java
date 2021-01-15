package simpleplayer3;

import java.util.ArrayList;
import java.util.Arrays;

import battlecode.common.*;

class Slanderer extends Pawn {
    Politician successor;

    Slanderer(RobotController rcin) {
        this.rc = rcin;
    }

    void run() throws GameActionException {
        getHomeHQ();
        while (rc.getType().equals(RobotType.SLANDERER)) {
            turnCount++;
            if (rc.isReady()) {
                // Handle detecting enemies
                int sensorRadius = rc.getType().sensorRadiusSquared;
                ArrayList<RobotInfo> nearbyEnemies = new ArrayList<RobotInfo>(
                        Arrays.asList(rc.senseNearbyRobots(sensorRadius, rc.getTeam().opponent())));
                nearbyEnemies.removeIf(e -> (!e.getType().equals(RobotType.MUCKRAKER)));
                if (nearbyEnemies.size() > 0) {
                    tryMove(runAwayDirection(nearbyEnemies));
                } else {
                    tryDirForward180(getTeamGoDir());
                }
            }
            Clock.yield();
        }
        if (successor == null) {
            successor = new Politician(this);
        }
        successor.run();

    }

    Direction runAwayDirection(ArrayList<RobotInfo> robots) {
        MapLocation location = new MapLocation(0, 0);
        for (RobotInfo robot : robots) {
            location = location.translate(robot.location.x, robot.location.y);
        }
        MapLocation avgLocation = new MapLocation(location.x / robots.size(), location.y / robots.size());
        return directionTo(avgLocation).opposite();
    }
}
