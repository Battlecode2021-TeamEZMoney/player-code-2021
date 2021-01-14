package simpleplayer3;

import java.util.ArrayList;
import java.util.Arrays;

import battlecode.common.*;

class SlandererPlayer extends Pawn{
    PoliticianPlayer successor;

    SlandererPlayer(RobotController rcin){
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
                    Move.tryMove(rc, RunAway.runAwayDirection(rc, nearbyEnemies));
                } else {
                    Move.tryDirForward180(rc, getTeamGoDir());
                }
            }
            Clock.yield();
        }
        if (successor == null){
            successor = new PoliticianPlayer(this);
        }
        successor.run();
        
    }

    private Direction getTeamGoDir() {
        return Move.getTeamGoDir(rc);
    }

}
