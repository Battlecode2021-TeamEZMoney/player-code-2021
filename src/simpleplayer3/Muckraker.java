package simpleplayer3;

import battlecode.common.*;
import common.DirectionUtils;

class Muckraker extends Attacker {
    private MapLocation enemyHQ = null;
    private int mode = 1;
    private Direction dirTarget;

    Muckraker(RobotController rcin) {
        this.rc = rcin;
    }

    void run() throws GameActionException {
        dirTarget = getTeamGoDir().opposite();
        getHomeHQ();
        while (true) {
            turnCount++;
            if (rc.canGetFlag(hqID)) {
                parseHQFlag(rc.getFlag(hqID));
            } else {
                mode = 1;
            }

            switch (mode) {
                case 2:
                    runAttackCode();
                default:
                    runSimpleCode();
            }

            Clock.yield();
        }
    }

    private void parseHQFlag(int flag) {
        MapLocation tempLocation = Encoding.getLocationFromFlag(rc, flag);
        switch (Encoding.getInfoFromFlag(flag)) {
            case 2:
                mode = 2;
                enemyHQ = tempLocation;
                break;
            default:
                mode = 1;
                break;
        }
    }

    private void runAttackCode() throws GameActionException {
        RobotInfo[] nearbyAllies = rc.senseNearbyRobots(999, rc.getTeam());
        for (RobotInfo ally : nearbyAllies) {
            if (ally.type.equals(RobotType.ENLIGHTENMENT_CENTER)) {
                trySetFlag(Encoding.encode(ally.getLocation(), FlagCodes.friendlyHQ));
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
            if (tempTarget != null && tryExpose(tempTarget)) {
                return;
            }
        }

        if (enemyHQ.isAdjacentTo(rc.getLocation())) {
            tryDirForward90(directionTo(enemyHQ));
        } else {
            tryDirForward180(directionTo(enemyHQ));
        }

    }

    private void runSimpleCode() throws GameActionException {
        if (rc.isReady()) {
            RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(999, rc.getTeam().opponent());
            if (nearbyEnemies.length > 0) {
                RobotInfo tempTarget = null;
                for (RobotInfo robot : nearbyEnemies) {
                    if (robot.type.equals(RobotType.ENLIGHTENMENT_CENTER)) {
                        enemyHQ = robot.getLocation();
                        trySetFlag(Encoding.encode(enemyHQ, FlagCodes.enemyHQ));
                        break;
                    } else if (robot.type.equals(RobotType.SLANDERER)) {
                        if ((tempTarget == null || tempTarget.getInfluence() < robot.getInfluence())
                                && rc.canExpose(robot.getID())) {
                            tempTarget = robot;
                        }
                    }
                }
                if (tempTarget != null && tryExpose(tempTarget.getID())) {
                    return;
                }
            }
            if (enemyHQ != null) {
                if (enemyHQ.isAdjacentTo(rc.getLocation())) {
                    tryDirForward90(directionTo(enemyHQ));
                } else {
                    tryDirForward180(directionTo(enemyHQ));
                }

            } else {
                if (!rc.onTheMap(rc.getLocation().add(dirTarget))) {
                    dirTarget = DirectionUtils.random180BiasMiddle(dirTarget.opposite());
                }
                tryDirForward180(dirTarget);
            }
        }
    }

    private boolean tryExpose(int id) throws GameActionException{
        if(rc.canExpose(id)){
            rc.expose(id);
            return true;
        }
        return false;
    }

    private boolean tryExpose(MapLocation pos) throws GameActionException{
        if(rc.canExpose(pos)){
            rc.expose(pos);
            return true;
        }
        return false;
    }

    private boolean tryExpose(RobotInfo enemy) throws GameActionException{
        return tryExpose(enemy.getID());
    }

    protected boolean huntOrKill(RobotInfo enemy) throws GameActionException {
        return (withinAttackRange(enemy) && tryExpose(enemy))
                || tryDirForward180(directionTo(enemy.getLocation()));
    }

}
