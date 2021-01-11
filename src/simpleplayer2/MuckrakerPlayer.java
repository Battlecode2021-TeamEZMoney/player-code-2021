package simpleplayer2;

import battlecode.common.*;

public class MuckrakerPlayer {
    static int turnCount = 0;
    static void runMuckraker(RobotController rc) throws GameActionException {
        while(true){
            turnCount++;

            Clock.yield();
        }
    }
}
