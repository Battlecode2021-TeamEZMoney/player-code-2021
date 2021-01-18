package simpleplayer5;

import java.util.Arrays;

import javax.swing.text.html.HTMLWriter;

import battlecode.common.*;
import common.DirectionUtils;

class Politician extends Attacker {
    private int mode = 1;
    private final boolean wasSlanderer;

	Politician(RobotController rcin) throws GameActionException {
        super(rcin); // Don't remove this.
        wasSlanderer = false;
	}

	Politician(Slanderer sland) throws GameActionException {
		// TODO: Remember to update these when new common fields are added in the Pawn
		// and Robot classes.
		super(sland.rc); // Don't remove this.
		this.turnCount = sland.turnCount;
		this.hqLocation = sland.hqLocation;
		this.hqID = sland.hqID;
        this.dirTarget = sland.dirTarget;
        wasSlanderer = true;
	}

    void run() throws GameActionException {
        dirTarget = getTeamGoDir().opposite();
        getHomeHQ();
        while (hqLocation == null && !tryEmpower(1)) {
            Clock.yield();
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

        // TODO: Implement differentiation for formerly slanderer units as well as
        // converted units.
    }

    private void parseHQFlag(int flag) throws GameActionException {
        MapLocation tempLocation = Encoding.getLocationFromFlag(rc, flag);
        switch (Encoding.getInfoFromFlag(flag)) {
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

    private void runDefendCode() throws GameActionException {
        RobotInfo[] tempEnemies = rc.senseNearbyRobots(999, rc.getTeam().opponent());
        for (RobotInfo enemy : tempEnemies) {
            if (enemy.getType().equals(RobotType.MUCKRAKER)) {
                if (huntOrKill(enemy)) {
                    return;
                }
            }
        }
        if (distanceSquaredTo(hqLocation) > 11) {
            tryDirForward180(directionTo(hqLocation));
        }
    }

    protected boolean huntOrKill(RobotInfo enemy) throws GameActionException {
        return (withinAttackRange(enemy) && tryEmpower(distanceSquaredTo(enemy)))
                || tryDirForward180(directionTo(enemy.getLocation()));
    }

    private void runNeutralCode() throws GameActionException {
        showAnyNearbyAlliedHQOnFlag();
        densityDestruct();
        if (neutralHQAttackRoutine())
                return;
    }

    private boolean neutralHQAttackRoutine() throws GameActionException {
        if (neutralTarget.isAdjacentTo(rc.getLocation())) {
            if (DirectionUtils.isCardinal(directionTo(neutralTarget)) && tryEmpower(1)) {
                return true;
            } else {
                if (tryDirForward90(directionTo(neutralTarget)) || tryEmpower(2)) {
                    return true;
                }
            }
        } else if (tryDirForward180(directionTo(neutralTarget))) {
            return true;
        }
        return false;
    }

    private void runAttackCode() throws GameActionException {
        RobotInfo[] neutralECs = rc.senseNearbyRobots(sensorRadiusSquared, Team.NEUTRAL);
        if (neutralECs.length > 0) {
            neutralTarget = neutralECs[0].getLocation();
            trySetFlag(Encoding.encode(neutralTarget, FlagCodes.neutralHQ));
            if (neutralHQAttackRoutine())
                return;
        }

        showAnyNearbyAlliedHQOnFlag();

        densityDestruct();

        if (enemyHQAttackRoutine())
            return;
    }

    private void runSimple() throws GameActionException {
        if (rc.isReady()) {
            densityDestruct();

            RobotInfo[] neutralECs = rc.senseNearbyRobots(999, Team.NEUTRAL);
            if (neutralECs.length > 0) {
                neutralTarget = neutralECs[0].getLocation();
                if (neutralTarget.isAdjacentTo(rc.getLocation())) {
                    if (Arrays.asList(Direction.cardinalDirections()).contains(directionTo(neutralTarget))) {
                        tryEmpower(1);
                    } else {
                        if (!tryDirForward90(directionTo(neutralTarget))) {
                            tryEmpower(2);
                        }
                        ;
                    }
                } else {
                    tryDirForward180(directionTo(neutralTarget));
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
                if (distanceSquaredTo(hqLocation) < 11 && tryDirForward180(directionTo(hqLocation).opposite())) {
                } else if (distanceSquaredTo(hqLocation) < 45 && rc.isLocationOccupied(rc.getLocation().add(directionTo(hqLocation))) && tryDirForward180(directionTo(hqLocation).opposite())){
                } else if (!tryDirForward180(DirectionUtils.randomDirection())){
                    tryDirForward180(DirectionUtils.randomDirection());
                }
            }
        }
    }

    private boolean enemyHQAttackRoutine() throws GameActionException {
        if (distanceSquaredTo(enemyHQ) <= RobotType.POLITICIAN.actionRadiusSquared
                && rc.isLocationOccupied(rc.getLocation().add(directionTo(enemyHQ)))
                && tryEmpower(distanceSquaredTo(enemyHQ))) {
            return true;
        } else if ((enemyHQ.isAdjacentTo(rc.getLocation()) && tryDirForward90(directionTo(enemyHQ))) || tryDirForward180(directionTo(enemyHQ))) {
            return true;
        }

        return false;
    }

    private boolean tryEmpower(int radius) throws GameActionException {
        if (rc.canEmpower(radius)) {
            rc.empower(radius);
            return true;
        }
        return false;
    }

    private void endOfMatchDestruct() throws GameActionException {
        while (rc.getRoundNum() > GameConstants.GAME_MAX_NUMBER_OF_ROUNDS - 25) {
            tryEmpower(RobotType.POLITICIAN.actionRadiusSquared);
        }
    }

    private void densityDestruct() throws GameActionException {
        int range = 5;
        int nearHQThresh = 1;
        int hqMaxDist = 10;
        int defaultThresh = 4;

        RobotInfo[] tempEnemies = rc.senseNearbyRobots(range, rc.getTeam().opponent());
        if (tempEnemies.length > defaultThresh
                || (tempEnemies.length > nearHQThresh && distanceSquaredTo(hqLocation) <= hqMaxDist)) {
            tryEmpower(range);
        }
    }
}
