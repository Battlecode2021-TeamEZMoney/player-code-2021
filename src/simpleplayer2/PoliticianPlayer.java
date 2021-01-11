package simpleplayer2;

import battlecode.common.*;

public class PoliticianPlayer {
    private static RobotController rc;
    static int turnCount = 0;
    static void runPolitician(RobotController rc, boolean wasSlanderer) throws GameActionException {
        PoliticianPlayer.rc = rc;
        if(wasSlanderer){
            while(true){
                turnCount++;

                Clock.yield();
            }
        } else {
            while(true){
                turnCount++;

                Clock.yield();
            }
        }
    }
}
