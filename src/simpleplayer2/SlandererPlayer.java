package simpleplayer2;

import battlecode.common.*;

public class SlandererPlayer {
    private static RobotController rc;
    private static int turnCount = 0;
    static void runSlanderer(RobotController rc) throws GameActionException {
        SlandererPlayer.rc = rc;
        while(rc.getType().equals(RobotType.SLANDERER)){
            turnCount++;
            if (rc.isReady()){
                //TODO: Handle detecting enemies
                /*RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(999, rc.getTeam().opponent());
                if (nearbyEnemies.length > 0){
                    MapLocation target = runAwayTo(nearbyEnemies);
                } else {
                    Move.tryMove(rc, dirForward180(getTeamGoDir()));
                }*/
                Move.tryMove(rc, Move.dirForward180(rc, getTeamGoDir()));
            }
            Clock.yield();
        }
        PoliticianPlayer.runPolitician(rc, true);
    }

    private static MapLocation runAwayTo(RobotInfo[] runAwayFrom){
        return rc.getLocation(); //TODO: Implement running away
    }

    private static Direction getTeamGoDir(){
        return Move.getTeamGoDir(rc);
    }
}
