package simpleplayer2;

import battlecode.common.*;

public class PoliticianPlayer {
    private static RobotController rc;
    static int turnCount = 0;
    static void runPolitician(RobotController rcin, boolean wasSlanderer) throws GameActionException {
        PoliticianPlayer.rc = rcin;
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
