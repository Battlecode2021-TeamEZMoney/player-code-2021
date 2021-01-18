package simpleplayer5;

import battlecode.common.*;

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

    boolean showAnyNearbyAlliedHQOnFlag() throws GameActionException {
        RobotInfo[] nearbyAllies = rc.senseNearbyRobots(rc.getType().detectionRadiusSquared, rc.getTeam());
        for (RobotInfo ally : nearbyAllies) {
            if (ally.type.equals(RobotType.ENLIGHTENMENT_CENTER)) {
                return trySetFlag(Encoding.encode(ally.getLocation(), FlagCodes.friendlyHQ));
            }
        }
        return false;
    }

    boolean showAnyNearbyNeutralECOnFlag() throws GameActionException {
        RobotInfo[] neutralECs = rc.senseNearbyRobots(sensorRadiusSquared, Team.NEUTRAL);
        if (neutralECs.length > 0) {
            return trySetFlag(Encoding.encode(neutralECs[0].getLocation(), FlagCodes.neutralHQ));
        }
        return false;
    }

    static class FlagCodes {
        public static int simple = 1;
        public static int enemyHQ = 2;
        public static int friendlyHQ = 3;
        public static int neutralHQ = 4;
        public static int patrol = 5;
    }

    Direction getTeamGoDir() {
        // TODO: make this smarter than assuming team a is on the left side
        return rc.getTeam().equals(Team.A) ? Direction.WEST : Direction.EAST;
    }

    protected int distanceSquaredTo(RobotInfo otherRobot) {
        return distanceSquaredTo(otherRobot.getLocation());
    }

    protected int distanceSquaredTo(MapLocation pos) {
        return rc.getLocation().distanceSquaredTo(pos);
    }

    protected Direction directionTo(RobotInfo otherRobot) {
        return directionTo(otherRobot.getLocation());
    }

    protected Direction directionTo(MapLocation pos) {
        return rc.getLocation().directionTo(pos);
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

    protected boolean isAlly(RobotInfo robot) throws GameActionException {
        return robot.team.equals(allyTeam);
    }

    protected boolean isEnemy(RobotInfo robot) throws GameActionException {
        return robot.team.equals(enemyTeam);
    }

    protected boolean isNeutral(RobotInfo robot) throws GameActionException {
        return robot.team.equals(Team.NEUTRAL);
    }
}
