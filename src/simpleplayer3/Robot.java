package simpleplayer3;

import battlecode.common.*;

abstract class Robot {
    protected RobotController rc;
    protected int turnCount = 0;

    abstract void run() throws GameActionException;

    static Robot robotFromRobotController(RobotController rc) throws Exception{
        switch(rc.getType()){
            case POLITICIAN: 
            case MUCKRAKER: 
            case SLANDERER: return Pawn.unitFromRobotController(rc);
            case ENLIGHTENMENT_CENTER: return new EnlightenmentPlayer(rc);
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

    static class FlagCodes {
        public static int simple = 1;
        public static int enemyHQ = 2;
        public static int friendlyHQ = 3;
        public static int neutralHQ = 4;
        public static int patrol = 5;
    }
}
