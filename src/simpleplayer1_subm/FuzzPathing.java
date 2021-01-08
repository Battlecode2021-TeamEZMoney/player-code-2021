package simpleplayer1_subm;
import battlecode.common.*;

public class FuzzPathing {
    private static int checkDepth = 1;
    public static Direction pathTo(RobotController rc, MapLocation endPos) throws GameActionException{
        
        Direction tempDir = rc.getLocation().directionTo(endPos).rotateLeft();
        Direction outDir = tempDir.rotateLeft();
        int outDirTime = pathTime(rc, rc.getLocation(), endPos, outDir, checkDepth);
        
        for (int i = 0; i < 4; i++){
            int tempDirTime = pathTime(rc, rc.getLocation(), endPos, tempDir, checkDepth);
            if (tempDirTime < outDirTime || (tempDirTime == outDirTime && tempDir == rc.getLocation().directionTo(endPos))){
                outDir = tempDir;
                outDirTime = tempDirTime;
            }
            tempDir = tempDir.rotateRight();
        }

        return outDir;
    }

    private static int pathTime(RobotController rc, MapLocation startPos, MapLocation endPos, Direction startDir, int turnsleft) throws GameActionException{
        MapLocation newPos = startPos.add(startDir);
        if (rc.getLocation().equals(endPos) || newPos.equals(endPos)) {
            return 0;
        } else if (rc.getLocation().isAdjacentTo(newPos) && (!rc.onTheMap(newPos) || rc.getLocation().isAdjacentTo(endPos) || rc.isLocationOccupied(newPos))){
            return 1000;
        } else if (turnsleft > 0 && rc.canSenseLocation(newPos)){ 
                return (int) (Math.ceil(rc.getType().actionCooldown / rc.sensePassability(newPos))) + pathTime(rc, newPos, endPos, newPos.directionTo(endPos), turnsleft - 1);
        } 
        return Math.max(Math.abs(startPos.x - endPos.x), Math.abs(startPos.y - endPos.y)) * (int) (Math.ceil(rc.getType().actionCooldown / .2));
    }
}
