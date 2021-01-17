package simpleplayer4_v1;

import battlecode.common.*;

abstract class Robot {
    protected RobotController rc;
    protected int turnCount = 0;

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
                } else if (robot.team.equals(rc.getTeam().opponent())) {
                	flagCode = FlagCodes.enemyHQ;
                } else if (robot.team.equals(rc.getTeam())) {
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
    }

//    Direction getTeamGoDir() {
//        // TODO: make this smarter than assuming team a is on the left side
//        return rc.getTeam().equals(Team.A) ? Direction.WEST : Direction.EAST;
//    }
    
    protected boolean canSenseEnemy() {
    	return rc.senseNearbyRobots(-1, rc.getTeam().opponent()).length > 0;
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
}
