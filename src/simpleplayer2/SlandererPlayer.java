package simpleplayer2;

import battlecode.common.*;

public class SlandererPlayer {
    static void runSlanderer(RobotController rc) throws GameActionException {
        while(rc.getType() == RobotType.SLANDERER){


            Clock.yield();
        }
        PoliticianPlayer.runPolitician(rc, true);
    }
}
