package simpleplayer3;

import java.util.Arrays;

import battlecode.common.*;
import common.Constants;
import common.DirectionUtils;

public class PoliticianPlayer {
    private static RobotController rc;
    private static MapLocation enemyHQ = null;
    static MapLocation hqLocation = null;
    private static MapLocation neutralTarget;
    static int hqID;
    private static int turnCount = 0;
    private static int mode = 1;
    private static Direction dirTarget;

    static void runPolitician(RobotController rcin, boolean wasSlanderer) throws GameActionException {
        PoliticianPlayer.rc = rcin;
        dirTarget = Move.getTeamGoDir(rc).opposite();
        RobotInfo[] robots = rc.senseNearbyRobots(2, rc.getTeam());
        for (RobotInfo robot : robots) {
            if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                hqLocation = robot.getLocation();
                hqID = robot.getID();
                break;
            }
        }
        if (hqLocation == null) {
            while (!tryEmpower(1)) {
                Clock.yield();
            }

        }
        while (true) {
            turnCount++;
            endOfMatchDestruct();
            if (rc.canGetFlag(hqID)) {
                parseHQFlag(rc.getFlag(hqID));
            } else {
                mode = 1;
            }
            switch (mode) {
                case 2:
                    runAttackCode();
                    break;
                case 4:
                    runNeutralCode();
                    break;
                case 5:
                    runDefendCode();
                    break;
                default:
                    runSimple();
                    break;
            }

            Clock.yield();
        }

        // TODO: Implement differentiation for formerly slanderer units as well as converted units.
    }

    private static void parseHQFlag(int flag) {
        MapLocation tempLocation = Encoding.decodeLocation(rc, flag);
        switch (Encoding.decodeInfo(flag)) {
            case 2:
                mode = 2;
                enemyHQ = tempLocation;
                break;
            case 4:
                mode = 4;
                neutralTarget = tempLocation;
                break;
            case 5:
                mode = 5;
                break;
            default:
                mode = 1;
                break;
        }
    }

    private static void runDefendCode() throws GameActionException {
        RobotInfo[] tempEnemies = rc.senseNearbyRobots(999, rc.getTeam().opponent());
        for (RobotInfo enemy : tempEnemies) {
            if (enemy.getType().equals(RobotType.MUCKRAKER)) {
                if (enemy.getLocation().distanceSquaredTo(rc.getLocation()) > 9) {
                    Move.tryDirForward180(rc, rc.getLocation().directionTo(enemy.getLocation()));
                } else {
                    tryEmpower(9);
                }
                return;
            }
        }
        if (rc.getLocation().distanceSquaredTo(hqLocation) > 11) {
            Move.tryDirForward180(rc, rc.getLocation().directionTo(hqLocation));
        }
    }

    private static void runNeutralCode() throws GameActionException {
        FlagUtils.signalAnyNearbyAlliedHQ(rc);
        densityDestruct();


        if (neutralTarget.isAdjacentTo(rc.getLocation())) {
            if (DirectionUtils.isCardinal(rc.getLocation().directionTo(neutralTarget))) {
                tryEmpower(1);
            } else {
                if (!Move.tryDirForward90(rc, rc.getLocation().directionTo(neutralTarget))) {
                    tryEmpower(2);
                }
                ;
            }
        } else {
            Move.tryDirForward180(rc, rc.getLocation().directionTo(neutralTarget));
        }
    }

    private static void runAttackCode() throws GameActionException {
        FlagUtils.signalAnyNearbyAlliedHQ(rc);

        densityDestruct();

        if (enemyHQAttackRoutine())
            return;
    }

    private static void runSimple() throws GameActionException {
        if (rc.isReady()) {
            densityDestruct();

            
            RobotInfo[] neutralECs = rc.senseNearbyRobots(999, Team.NEUTRAL);
            if (neutralECs.length > 0) {
                neutralTarget = neutralECs[0].getLocation();
                if (neutralTarget.isAdjacentTo(rc.getLocation())) {
                    if (Arrays.asList(Direction.cardinalDirections())
                            .contains(rc.getLocation().directionTo(neutralTarget))) {
                        tryEmpower(1);
                    } else {
                        if (!Move.tryDirForward90(rc, rc.getLocation().directionTo(neutralTarget))) {
                            tryEmpower(2);
                        }
                        ;
                    }
                } else {
                    Move.tryDirForward180(rc, rc.getLocation().directionTo(neutralTarget));
                }
                return;
            }
            RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(999, rc.getTeam().opponent());
            if (nearbyEnemies.length > 0) {
                for (RobotInfo robot : nearbyEnemies) {
                    if ((enemyHQ == null || Math.random() < 0.05)
                            && robot.type.equals(RobotType.ENLIGHTENMENT_CENTER)) {
                        enemyHQ = robot.getLocation();
                    }
                }
            }
            if (enemyHQ != null) {
                if (enemyHQAttackRoutine())
                    return;
            } else {
                if (!rc.onTheMap(rc.getLocation().add(dirTarget))) {
                    dirTarget = dirTarget.opposite();
                }
                Move.tryDirForward180(rc, dirTarget);
            }
        }
    }

    private static boolean enemyHQAttackRoutine() throws GameActionException {
        if (rc.getLocation().distanceSquaredTo(enemyHQ) <= RobotType.POLITICIAN.actionRadiusSquared
                && rc.isLocationOccupied(rc.getLocation().add(rc.getLocation().directionTo(enemyHQ)))
                && tryEmpower(rc.getLocation().distanceSquaredTo(enemyHQ))) {
        } else if (enemyHQ.isAdjacentTo(rc.getLocation())) {
            Move.tryDirForward90(rc, rc.getLocation().directionTo(enemyHQ));
        } else {
            Move.tryDirForward180(rc, rc.getLocation().directionTo(enemyHQ));
        }

        return true;
    }

    private static boolean tryEmpower(int radius) throws GameActionException{
        if (rc.canEmpower(radius)) {
            rc.empower(radius);
            return true;
        }
        return false;
    }

    private static void endOfMatchDestruct() throws GameActionException{
        while(rc.getRoundNum() > GameConstants.GAME_MAX_NUMBER_OF_ROUNDS - 25){
            tryEmpower(RobotType.POLITICIAN.actionRadiusSquared);
        }
    }

    private static void densityDestruct() throws GameActionException{
        int range = 5;
        int nearHQThresh = 1;
        int hqMaxDist = 10;
        int defaultThresh = 4;

        RobotInfo[] tempEnemies = rc.senseNearbyRobots(range, rc.getTeam().opponent());
        if (tempEnemies.length > defaultThresh
                || (tempEnemies.length > nearHQThresh && rc.getLocation().distanceSquaredTo(hqLocation) <= hqMaxDist)) {
            tryEmpower(range);
        }
    }
}
