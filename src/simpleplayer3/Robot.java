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
}
