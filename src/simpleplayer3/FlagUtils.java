package simpleplayer3;

import battlecode.common.*;

class FlagUtils {
    static boolean signalAnyNearbyAlliedHQ(RobotController rc) throws GameActionException {
        RobotInfo[] nearbyAllies = rc.senseNearbyRobots(rc.getType().detectionRadiusSquared, rc.getTeam());
        for (RobotInfo ally : nearbyAllies) {
            if (ally.type.equals(RobotType.ENLIGHTENMENT_CENTER)) {
                return Encoding.trySetFlag(rc, Encoding.encode(ally.getLocation(), Codes.friendlyHQ));
            }
        }
        return false;
    }

    static class Codes {
        public static int simple = 1;
        public static int enemyHQ = 2;
        public static int friendlyHQ = 3;
        public static int neutralHQ = 4;
        public static int patrol = 5;
    }
}
