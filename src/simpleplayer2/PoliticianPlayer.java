package simpleplayer2;

import java.util.Arrays;

import battlecode.common.*;
import common.Constants;

public class PoliticianPlayer {
    private static RobotController rc;
    private static MapLocation enemyHQ = null;
    private static MapLocation hqLocation;
    private static int hqID;
    private static int turnCount = 0;
    private static int mode = 1;
    static void runPolitician(RobotController rcin, boolean wasSlanderer) throws GameActionException {
        PoliticianPlayer.rc = rcin;
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
            switch(mode){
                case 2:
                case 4:
                case 5:
                default: runSimple();
            }
            
            Clock.yield();
        }



        //TODO: Implement differentiation for formerly slanderer units
        /*if(wasSlanderer){
            while(true){
                turnCount++;

                Clock.yield();
            }
        } else {
            while(true){
                turnCount++;

                Clock.yield();
            }
        }*/
    }

    private static void parseHQFlag(int flag){
        MapLocation tempLocation = Encoding.decodeLocation(rc, flag);
        switch(Encoding.decodeInfo(flag)){
            case 2: mode = 2;
                    enemyHQ = tempLocation;
                    break;
            case 4: mode = 4;
                    enemyHQ = tempLocation;
                    break;
            case 5: mode = 5;
                    break;
            default: mode = 1;
                    break;
        }
    }

    private static void runSimple() throws GameActionException{
        if (rc.isReady()) {
            if (rc.getRoundNum() > Constants.totalRounds - 25){
                rc.empower(RobotType.POLITICIAN.actionRadiusSquared);
            } 
            RobotInfo[] tempEnemies = rc.senseNearbyRobots(5, rc.getTeam().opponent());
            if ((tempEnemies.length > 4 || (tempEnemies.length > 1 && rc.getLocation().distanceSquaredTo(hqLocation) <= 10)) && rc.canEmpower(5)){
                rc.empower(5);
            }
            MapLocation neutralTarget;
            RobotInfo[] neutralECs = rc.senseNearbyRobots(999, Team.NEUTRAL);
            if (neutralECs.length > 0){
                neutralTarget = neutralECs[0].getLocation();
                if(neutralTarget.isAdjacentTo(rc.getLocation())){
                    if(Arrays.asList(Direction.cardinalDirections()).contains(rc.getLocation().directionTo(neutralTarget))){
                        rc.empower(0);
                    } else {
                        Move.tryMove(rc, Move.dirForward90(rc, rc.getLocation().directionTo(neutralTarget)));
                    }
                } else {
                    Move.tryMove(rc, Move.dirForward180(rc, rc.getLocation().directionTo(neutralTarget)));
                }
                return;
            }
            RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(999, rc.getTeam().opponent());
            if (nearbyEnemies.length > 0) {
                //RobotInfo tempTarget = null;
                for (RobotInfo robot : nearbyEnemies) {
                    if ((enemyHQ == null || Math.random() < 0.05)
                            && robot.type.equals(RobotType.ENLIGHTENMENT_CENTER)) {
                        enemyHQ = robot.getLocation();
                    }/* else if (robot.type.equals(RobotType.SLANDERER)) {
                        if ((tempTarget == null || tempTarget.getInfluence() < robot.getInfluence())
                                && rc.canExpose(robot.getID())) {
                            tempTarget = robot;
                        }
                    }*/
                }/*
                if (tempTarget != null && rc.canExpose(tempTarget.getID())) {
                    rc.expose(tempTarget.getID());
                    break currentRound;
                }*/
            }
            if (enemyHQ != null) {
                if(rc.getLocation().distanceSquaredTo(enemyHQ) <= RobotType.POLITICIAN.actionRadiusSquared){
                    if(rc.isLocationOccupied(rc.getLocation().add(rc.getLocation().directionTo(enemyHQ))) && rc.canEmpower(RobotType.POLITICIAN.actionRadiusSquared)){
                        rc.empower(rc.getLocation().distanceSquaredTo(enemyHQ));
                        return;
                    }
                }
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
}
