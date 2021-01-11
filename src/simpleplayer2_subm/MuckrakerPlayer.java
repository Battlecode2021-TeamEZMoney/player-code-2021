package simpleplayer2_subm;

import battlecode.common.*;

public class MuckrakerPlayer {
    private static RobotController rc;
    private static MapLocation enemyHQ = null;
    private static int turnCount = 0;

    static void runMuckraker(RobotController rcin) throws GameActionException {
        MuckrakerPlayer.rc = rcin;
        while (true) {
            turnCount++;
            currentRound: {
                if (rc.isReady()) {
                    RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(999, rc.getTeam().opponent());
                    if (nearbyEnemies.length > 0) {
                        RobotInfo tempTarget = null;
                        for (RobotInfo robot : nearbyEnemies) {
                            if ((enemyHQ == null || Math.random() < 0.05)
                                    && robot.type.equals(RobotType.ENLIGHTENMENT_CENTER)) {
                                enemyHQ = robot.getLocation();
                            } else if (robot.type.equals(RobotType.SLANDERER)) {
                                if ((tempTarget == null || tempTarget.getInfluence() < robot.getInfluence())
                                        && rc.canExpose(robot.getID())) {
                                    tempTarget = robot;
                                }
                            }
                        }
                        if (tempTarget != null && rc.canExpose(tempTarget.getID())) {
                            rc.expose(tempTarget.getID());
                            break currentRound;
                        }
                    }
                    if (enemyHQ != null) {
                        if(enemyHQ.isAdjacentTo(rc.getLocation())){
                            Move.tryMove(rc, Move.dirForward90(rc, rc.getLocation().directionTo(enemyHQ)));
                        } else {
                            Move.tryMove(rc, Move.dirForward180(rc, rc.getLocation().directionTo(enemyHQ)));
                        }
                        
                    } else {
                        Move.tryMove(rc, Move.dirForward180(rc, Move.getTeamGoDir(rc).opposite()));
                    }
                }
            }
            Clock.yield();
        }
    }
}
