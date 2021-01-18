package simpleplayer2;

import java.util.ArrayList;
import java.util.Arrays;

import battlecode.common.*;

public class SlandererPlayer {
    private static RobotController rc;
    private static MapLocation hqLocation;
    private static int hqID;
    private static int turnCount = 0;

    static void runSlanderer(RobotController rcin) throws GameActionException {
        SlandererPlayer.rc = rcin;
        if (turnCount == 0) {
            RobotInfo[] robots = rc.senseNearbyRobots(2, rc.getTeam());
            for (RobotInfo robot : robots) {
                if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                    hqLocation = robot.getLocation();
                    hqID = robot.getID();
                    break;
                }
            }
        }
        while (rc.getType().equals(RobotType.SLANDERER)) {
            turnCount++;
            if (rc.isReady()) {
                // Handle detecting enemies
                int sensorRadius = rc.getType().sensorRadiusSquared;
                ArrayList<RobotInfo> nearbyEnemies = new ArrayList<RobotInfo>(
                        Arrays.asList(rc.senseNearbyRobots(sensorRadius, rc.getTeam().opponent())));
                nearbyEnemies.removeIf(e -> (!e.getType().equals(RobotType.MUCKRAKER)));
                if (nearbyEnemies.size() > 0) {
                    Move.tryMove(rc, RunAway.runAwayDirection(rc, nearbyEnemies));
                } else {
                    Move.tryMove(rc, Move.dirForward180(rc, getTeamGoDir()));
                }
            }
            Clock.yield();
        }
        PoliticianPlayer.hqID = hqID;
        PoliticianPlayer.hqLocation = hqLocation;
        PoliticianPlayer.runPolitician(rc, true);
    }

    private static MapLocation runAwayTo(RobotInfo[] runAwayFrom) {
        return rc.getLocation(); // TODO: Implement running away
    }

    private static Direction getTeamGoDir() {
        return Move.getTeamGoDir(rc);
    }
}
