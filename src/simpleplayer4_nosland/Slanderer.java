package simpleplayer4_nosland;

import java.util.Arrays;
import java.util.List;

import battlecode.common.*;
//import java.util.*;

class Slanderer extends Pawn {
    Politician successor;

    Slanderer(RobotController rcin) throws GameActionException {
        super(rcin); // Don't remove this.
        getHomeHQ();
    }

    void run() throws GameActionException {
        while (rc.getType().equals(RobotType.SLANDERER)) {
            turnCount++;
            if (rc.isReady()) {
                // Handle detecting enemies
                tryDirForward180(awayFromEnemyMuckrakers());
            }
            Clock.yield();
        }
        if (successor == null) {
            successor = new Politician(this);
        }
        successor.run();
    }

    protected Direction awayFromEnemyMuckrakers() throws GameActionException {
        List<RobotInfo> robots = Arrays.asList(rc.senseNearbyRobots(rc.getLocation(), actionRadiusSquared, enemyTeam));
        robots.removeIf(r -> (r.type != RobotType.MUCKRAKER));
        return awayFromRobots(robots);
    }
}
