package usqualplayer2;

import battlecode.common.*;

import java.util.*;

abstract class Robot {
    protected RobotController rc;
    protected int turnCount = 0;
    protected final Team allyTeam;
    protected final Team enemyTeam;
    protected final RobotType rcType;
    protected final int actionRadiusSquared;
    protected final int detectionRadiusSquared;
    protected final int sensorRadiusSquared;
    protected final double baseActionCooldown;
    protected int encoded = 0;
    protected MapLocation slandCenter = null;

    Robot(RobotController rcin) {
        this.rc = rcin;
        this.allyTeam = rc.getTeam();
        this.enemyTeam = allyTeam.opponent();
        this.rcType = rc.getType();
        this.actionRadiusSquared = this.rcType.actionRadiusSquared;
        this.detectionRadiusSquared = this.rcType.detectionRadiusSquared;
        this.sensorRadiusSquared = this.rcType.sensorRadiusSquared;
        this.baseActionCooldown = this.rcType.actionCooldown;
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

    static int minMovesLeft(MapLocation from, MapLocation to) {
        return Math.max(Math.abs(from.x - to.x), Math.abs(from.y - to.y));
    }

    protected int flagCodeFromHQTeam(Team team) {
        if (team.equals(enemyTeam)) {
            return FlagCodes.enemyHQ;
        } else if (team.equals(allyTeam)) {
            return FlagCodes.friendlyHQ;
        } else if (team.equals(Team.NEUTRAL)) {
            return FlagCodes.neutralHQ;
        }
        return 0;
    }

    static class FlagCodes {
        public final static int simple = 1;
        public final static int enemyHQ = 2;
        public final static int friendlyHQ = 3;
        public final static int neutralHQ = 4;
        public final static int patrol = 5;
        public final static int slandCenter = 6;
        public final static int empowering = 7;
    }

    protected boolean canSenseEnemy() throws GameActionException {
        return rc.senseNearbyRobots(sensorRadiusSquared, enemyTeam).length > 0;
    }

    protected boolean canSenseEnemyPolitician() throws GameActionException {
        RobotInfo[] nearby = rc.senseNearbyRobots(sensorRadiusSquared, enemyTeam);
        return Arrays.stream(nearby).filter(r -> r.type.equals(RobotType.POLITICIAN)).toArray().length > 0;
    }

    protected boolean canSenseEnemyMuckraker() throws GameActionException {
        RobotInfo[] nearby = rc.senseNearbyRobots(sensorRadiusSquared, enemyTeam);
        return Arrays.stream(nearby).filter(r -> r.type.equals(RobotType.MUCKRAKER)).toArray().length > 0;
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

    protected int numSurrounding(int radius) {
        return rc.senseNearbyRobots(radius).length;
    }

    protected boolean crowdedByEnemy(MapLocation loc) throws GameActionException {
        return rc.senseNearbyRobots(loc, 2, enemyTeam).length >= 5
                || rc.senseNearbyRobots(loc, 5, enemyTeam).length >= 10;
    }

    protected boolean crowded(MapLocation loc) throws GameActionException {
        return rc.senseNearbyRobots(loc, 2, null).length >= 7 || rc.senseNearbyRobots(loc, 5, null).length >= 15;
    }

    protected boolean crowdedByAllyMuckrakers(MapLocation loc) throws GameActionException {
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

    public static double angleBetween(MapLocation center, MapLocation loc1, MapLocation loc2)
            throws GameActionException {
        double angle = Math.atan2(loc2.y - center.y, loc2.x - center.x)
                - Math.atan2(loc1.y - center.y, loc1.x - center.x);
        angle = Math.abs(angle) * 180 / Math.PI;
        return Math.min(angle, 360 - angle);
    }

    public static final <T> void swap(T[] a, int i, int j) {
        T t = a[i];
        a[i] = a[j];
        a[j] = t;
    }

    protected MapLocation oppositePoint(MapLocation center, MapLocation point) {
        return new MapLocation(2 * center.x - point.x, 2 * center.y - point.y);
    }

    String printLoc(MapLocation loc) {
        return "(" + loc.x + ", " + loc.y + ")";
    }
}
