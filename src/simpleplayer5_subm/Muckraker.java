package simpleplayer5_subm;

import battlecode.common.*;
import common.DirectionUtils;

class Muckraker extends Attacker {
    private MapLocation enemyHQ = null;
    private int mode = 1;
    private Direction dirTarget;
    private boolean exploreOnly;

    Muckraker(RobotController rcin) throws GameActionException {
        super(rcin); // Don't remove this.
    }

    void run() throws GameActionException {
        dirTarget = DirectionUtils.randomDirectionBiasCardinal();
        exploreOnly = Math.random() > .4;
        getHomeHQ();
        while (true) {
            turnCount++;
            if (rc.canGetFlag(hqID)) {
                parseHQFlag(rc.getFlag(hqID));
            } else {
                mode = 1;
            }

            if (!exploreOnly) {
                switch (mode) {
                    case 2:
                        runAttackCode();
                    default:
                        runSimpleCode();
                }
            } else {
                runSimpleCode();
            }
            Clock.yield();
        }
    }

    private void parseHQFlag(int flag) throws GameActionException {
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

    protected void runAttackCode() throws GameActionException {
        showAnyNearbyAlliedHQOnFlag();
        if (enemyUnitAttackRoutine())
            return;

        if (enemyHQ.isAdjacentTo(rc.getLocation())) {
            tryDirForward90(directionTo(enemyHQ));
        } else {
            tryDirForward180(directionTo(enemyHQ));
        }

    }

    private boolean enemyUnitAttackRoutine() throws GameActionException {
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(sensorRadiusSquared, rc.getTeam().opponent());
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
            if (tempTarget != null && huntOrKill(tempTarget)) {
                return true;
            }
        }
        return false;
    }

    protected void runSimpleCode() throws GameActionException {
        showAnyNearbyAlliedHQOnFlag();
        showAnyNearbyNeutralECOnFlag();
        if (enemyUnitAttackRoutine())
            return;
        if (bounceExploreRoutine())
            return;
    }

    private boolean bounceExploreRoutine() throws GameActionException {
        if (!rc.onTheMap(rc.getLocation().add(dirTarget)) || rc.isLocationOccupied(rc.getLocation().add(dirTarget))) {
            dirTarget = DirectionUtils.random180(dirTarget.opposite());
        }
        return tryDirForward180(dirTarget);
    }

    private boolean tryExpose(int id) throws GameActionException {
        if (rc.canExpose(id)) {
            rc.expose(id);
            return true;
        }
        return false;
    }

    private boolean tryExpose(MapLocation pos) throws GameActionException {
        if (rc.canExpose(pos)) {
            rc.expose(pos);
            return true;
        }
        return false;
    }

    private boolean tryExpose(RobotInfo enemy) throws GameActionException {
        return tryExpose(enemy.getID());
    }

    protected boolean huntOrKill(RobotInfo enemy) throws GameActionException {
        return (withinAttackRange(enemy) && tryExpose(enemy)) || tryDirForward180(directionTo(enemy.getLocation()));
    }

}
