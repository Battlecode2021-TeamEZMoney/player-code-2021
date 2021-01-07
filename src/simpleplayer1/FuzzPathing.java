package simpleplayer1;
import battlecode.common.*;

public class FuzzPathing {
    static Direction pathTo(RobotController rc, MapLocation endPos){
        MapLocation startPos = rc.getLocation();
        Direction baseDir = startPos.directionTo(endPos);






        return Direction.CENTER;
    }
}
