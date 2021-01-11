package simpleplayer2;

import battlecode.common.*;

public class MuckrakerPlayer {
    private static RobotController rc;
    static int turnCount = 0;
    static void runMuckraker(RobotController rc) throws GameActionException {
        MuckrakerPlayer.rc = rc;
        while(true){
            turnCount++;

            Clock.yield();
        }
    }
}
