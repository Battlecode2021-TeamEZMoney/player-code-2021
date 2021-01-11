package simpleplayer2;

import battlecode.common.*;

public class SlandererPlayer {
    static int turnCount = 0;
    static void runSlanderer(RobotController rc) throws GameActionException {
        while(rc.getType().equals(RobotType.SLANDERER)){
            turnCount++;

            Clock.yield();
        }
        PoliticianPlayer.runPolitician(rc, true);
    }
}
