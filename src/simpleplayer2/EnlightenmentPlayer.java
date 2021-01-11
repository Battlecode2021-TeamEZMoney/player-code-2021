package simpleplayer2;

import battlecode.common.*;

public class EnlightenmentPlayer {
    static void runEnlightenmentCenter(RobotController rc) throws GameActionException {
        while (true){
            if (rc.getTeamVotes() < 1500 && rc.canBid(1)) {
                rc.bid(1);
            }
        }
    }
}
