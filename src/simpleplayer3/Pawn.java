package simpleplayer3;

import battlecode.common.*;

abstract class Pawn extends Robot{
    MapLocation hqLocation;
    int hqID;

    static Pawn unitFromRobotController(RobotController rc) throws Exception{
        switch(rc.getType()){
            case POLITICIAN: return new PoliticianPlayer(rc);
            case MUCKRAKER: return new MuckrakerPlayer(rc);
            case SLANDERER: return new SlandererPlayer(rc);
            default:
                throw new Exception(rc.getType() + "is not a valid pawn type.");
        }
    }

    boolean getHomeHQ() {
        RobotInfo[] robots = rc.senseNearbyRobots(2, rc.getTeam());
        for (RobotInfo robot : robots) {
            if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                hqLocation = robot.getLocation();
                hqID = robot.getID();
                return true;
            }
        }
        return false;
    }
}
