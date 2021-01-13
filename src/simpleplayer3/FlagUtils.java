package simpleplayer3;

import battlecode.common.*;

public class FlagUtils {
    static boolean signalAnyNearbyAlliedHQ(RobotController rc) throws GameActionException{
        RobotInfo[] nearbyAllies = rc.senseNearbyRobots(rc.getType().detectionRadiusSquared, rc.getTeam());
        for (RobotInfo ally : nearbyAllies) {
            if (ally.type.equals(RobotType.ENLIGHTENMENT_CENTER)) {
                return Encoding.trySetFlag(rc, Encoding.encode(ally.getLocation(), Codes.friendlyHQ));
            }
        }
        return false;
    }
}
