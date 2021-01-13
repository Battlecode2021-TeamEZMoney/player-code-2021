package simpleplayer2_subm;

import battlecode.common.*;

public class MuckrakerPlayer {
    private static RobotController rc;
    private static MapLocation enemyHQ = null;
    private static MapLocation hqLocation;
    private static int hqID;
    private static int turnCount = 0;
    private static int mode = 1;
    private static Direction dirTarget;

    static void runMuckraker(RobotController rcin) throws GameActionException {
        MuckrakerPlayer.rc = rcin;
        dirTarget = Move.getTeamGoDir(rc).opposite();
        if (turnCount == 0) {
            RobotInfo[] robots = rc.senseNearbyRobots(2, rc.getTeam());
            for (RobotInfo robot : robots) {
                if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                    hqLocation = robot.getLocation();
                    hqID = robot.getID();
                    break;
                }
            }
        }
        while (true) {
            turnCount++;
            if(rc.canGetFlag(hqID)){
                parseHQFlag(rc.getFlag(hqID));
            } else {
                mode = 1;
            }

            switch (mode) {
                case 2: runAttackCode();
                default: runSimpleCode();
            }

            Clock.yield();
        }
    }

    private static void parseHQFlag(int flag){
        MapLocation tempLocation = Encoding.decodeLocation(rc, flag);
        switch(Encoding.decodeInfo(flag)){
            case 2: mode = 2;
                    enemyHQ = tempLocation;
                    break;
            default: mode = 1;
                    break;
        }
    }

    private static void runAttackCode() throws GameActionException{
        RobotInfo[] nearbyAllies = rc.senseNearbyRobots(999, rc.getTeam());
        for(RobotInfo ally : nearbyAllies){
            if (ally.type.equals(RobotType.ENLIGHTENMENT_CENTER)) {
                Encoding.trySetFlag(rc, Encoding.encode(ally.getLocation(), Codes.friendlyHQ));
            }
        }
        
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(999, rc.getTeam().opponent());
        if (nearbyEnemies.length > 0) {
            RobotInfo tempTarget = null;
            for (RobotInfo robot : nearbyEnemies) {
                if (robot.type.equals(RobotType.SLANDERER)) {
                    if ((tempTarget == null || tempTarget.getInfluence() < robot.getInfluence())
                            && rc.canExpose(robot.getID())) {
                        tempTarget = robot;
                    }
                }
            }
            if (tempTarget != null && rc.canExpose(tempTarget.getID())) {
                rc.expose(tempTarget.getID());
                return;
            }
        }
        
        if(enemyHQ.isAdjacentTo(rc.getLocation())){
            Move.tryMove(rc, Move.dirForward90(rc, rc.getLocation().directionTo(enemyHQ)));
        } else {
            Move.tryMove(rc, Move.dirForward180(rc, rc.getLocation().directionTo(enemyHQ)));
        }

    }

    private static void runSimpleCode() throws GameActionException{
        if (rc.isReady()) {
            RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(999, rc.getTeam().opponent());
            if (nearbyEnemies.length > 0) {
                RobotInfo tempTarget = null;
                for (RobotInfo robot : nearbyEnemies) {
                    if (robot.type.equals(RobotType.ENLIGHTENMENT_CENTER)) {
                        enemyHQ = robot.getLocation();
                        Encoding.trySetFlag(rc, Encoding.encode(enemyHQ, Codes.enemyHQ));
                        break;
                    } else if (robot.type.equals(RobotType.SLANDERER)) {
                        if ((tempTarget == null || tempTarget.getInfluence() < robot.getInfluence())
                                && rc.canExpose(robot.getID())) {
                            tempTarget = robot;
                        }
                    }
                }
                if (tempTarget != null && rc.canExpose(tempTarget.getID())) {
                    rc.expose(tempTarget.getID());
                    return;
                }
            }
            if (enemyHQ != null) {
                if(enemyHQ.isAdjacentTo(rc.getLocation())){
                    Move.tryMove(rc, Move.dirForward90(rc, rc.getLocation().directionTo(enemyHQ)));
                } else {
                    Move.tryMove(rc, Move.dirForward180(rc, rc.getLocation().directionTo(enemyHQ)));
                }
                
            } else {
                if(!rc.onTheMap(rc.getLocation().add(dirTarget))){
                    dirTarget = dirTarget.opposite();
                }
                Move.tryMove(rc, Move.dirForward180(rc, dirTarget));
            }
        }
    }
}
