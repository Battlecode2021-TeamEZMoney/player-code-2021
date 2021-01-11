package simpleplayer2;

import battlecode.common.*;

public class PoliticianPlayer {
    static int turnCount = 0;
    static void runPolitician(RobotController rc, boolean wasSlanderer) throws GameActionException {
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
