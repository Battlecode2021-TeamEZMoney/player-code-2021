package sprintplayer4;

import battlecode.common.*;

import java.util.*;


abstract class Robot {
    protected RobotController rc;
    protected int turnCount = 0;
    protected final Team allyTeam;
    protected final Team enemyTeam;
    protected final RobotType robotType;
    protected final int actionRadiusSquared;
    protected final int detectionRadiusSquared;
    protected final int sensorRadiusSquared;

    Robot(RobotController rcin){
        this.rc = rcin;
        this.allyTeam = rc.getTeam();
        this.enemyTeam = allyTeam.opponent();
        this.robotType = rc.getType();
        this.actionRadiusSquared = this.robotType.actionRadiusSquared;
        this.detectionRadiusSquared = this.robotType.detectionRadiusSquared;
        this.sensorRadiusSquared = this.robotType.sensorRadiusSquared;
    }

    abstract void run() throws GameActionException;

    static Robot robotFromRobotController(RobotController rc) throws Exception {
        switch (rc.getType()) {
            case POLITICIAN:
            case MUCKRAKER:
            case SLANDERER:
                return Pawn.unitFromRobotController(rc);
            case ENLIGHTENMENT_CENTER:
                return new EnlightenmentCenter(rc);
            default:
                throw new Exception(rc.getType() + "is not a valid robot type.");
        }
    }

    protected boolean trySetFlag(int newFlag) throws GameActionException {
        if (rc.canSetFlag(newFlag)) {
            rc.setFlag(newFlag);
            return true;
        } else if (rc.canSetFlag(0)) {
            rc.setFlag(0);
        }
        return false;
    }

    void setNearbyHQFlag() throws GameActionException {
        int encoded = -1;
        RobotInfo[] nearby = rc.senseNearbyRobots();
        for (RobotInfo robot : nearby) {
            if (robot.type.equals(RobotType.ENLIGHTENMENT_CENTER)) {
                int flagCode = 0;
                if (robot.team.equals(Team.NEUTRAL)) {
                    flagCode = FlagCodes.neutralHQ;
                } else if (robot.team.equals(enemyTeam)) {
                    flagCode = FlagCodes.enemyHQ;
                } else if (robot.team.equals(allyTeam)) {
                    flagCode = FlagCodes.friendlyHQ;
                }
                encoded = Encoding.encode(robot.getLocation(), flagCode);
                if (Math.random() < 0.4)
                    break;
            }
        }
        if (encoded != -1) {
            trySetFlag(encoded);
        }
    }

    static class FlagCodes {
        public static int simple = 1;
        public static int enemyHQ = 2;
        public static int friendlyHQ = 3;
        public static int neutralHQ = 4;
        public static int patrol = 5;
        public static int slandCenter = 6;
    }

    protected boolean canSenseEnemy() throws GameActionException {
        return rc.senseNearbyRobots(sensorRadiusSquared, enemyTeam).length > 0;
    }
    
    protected boolean canSenseEnemyPolitician() throws GameActionException {
    	RobotInfo[] nearby = rc.senseNearbyRobots(sensorRadiusSquared, enemyTeam);
    	return Arrays.stream(nearby).filter(r -> r.type.equals(RobotType.POLITICIAN)).toArray().length > 0;
    }

    protected int distanceSquaredTo(RobotInfo otherRobot) throws GameActionException {
        return distanceSquaredTo(otherRobot.getLocation());
    }

    protected int distanceSquaredTo(MapLocation pos) throws GameActionException {
        return rc.getLocation().distanceSquaredTo(pos);
    }

    protected Direction directionTo(RobotInfo otherRobot) throws GameActionException {
        return directionTo(otherRobot.getLocation());
    }

    protected Direction directionTo(MapLocation pos) throws GameActionException {
        return rc.getLocation().directionTo(pos);
    }

    protected boolean isAlly(RobotInfo robot) throws GameActionException {
        return robot.team.equals(allyTeam);
    }

    protected boolean isEnemy(RobotInfo robot) throws GameActionException {
        return robot.team.equals(enemyTeam);
    }

    protected boolean isNeutral(RobotInfo robot) throws GameActionException {
        return robot.team.equals(Team.NEUTRAL);
    }

    static protected int initialConviction(RobotInfo robot) throws GameActionException {
        switch (robot.type) {
            case ENLIGHTENMENT_CENTER:
                return Integer.MAX_VALUE;
            case SLANDERER:
            case POLITICIAN:
                return robot.influence;
            case MUCKRAKER:
                return (int) Math.ceil(robot.influence * 0.7);
            default:
                return 0;
        }
    }

    protected boolean crowdedByEnemy(MapLocation loc) {
        return rc.senseNearbyRobots(loc, 2, enemyTeam).length >= 5
                || rc.senseNearbyRobots(loc, 5, enemyTeam).length >= 10;
    }

    protected boolean crowded(MapLocation loc) {
        return rc.senseNearbyRobots(loc, 2, null).length >= 7 || rc.senseNearbyRobots(loc, 5, null).length >= 15;
    }

    protected boolean crowdedByAllyMuckrakers(MapLocation loc) {
        int numMucks2 = 0, numMucks5 = 0;
        for (RobotInfo robot : rc.senseNearbyRobots(loc, 2, allyTeam)) {
            if (robot.type.equals(RobotType.MUCKRAKER)) {
                numMucks2++;
            }
        }
        for (RobotInfo robot : rc.senseNearbyRobots(loc, 5, allyTeam)) {
            if (robot.type.equals(RobotType.MUCKRAKER)) {
                numMucks5++;
            }
        }
        return numMucks2 >= 5 || numMucks5 >= 10;
    }

    String printLoc(MapLocation loc) throws GameActionException {
        return "(" + loc.x + ", " + loc.y + ")";
    }
}
