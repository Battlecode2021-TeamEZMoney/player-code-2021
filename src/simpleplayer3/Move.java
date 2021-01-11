package simpleplayer3;

import battlecode.common.*;

public class Move {
    static boolean tryMove(RobotController rc, Direction dir) throws GameActionException {
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }
        return false;
    }

    static Direction dirForward180(RobotController rc, Direction dir) throws GameActionException{
        if(rc.canMove(dir) || !rc.onTheMap(rc.getLocation().add(dir))){
            return dir;
        } else if (rc.canMove(dir.rotateLeft())){
            return dir.rotateLeft();
        } else if (rc.canMove(dir.rotateRight())){
            return dir.rotateRight();
        } else if (rc.canMove(DirectionUtils.rotateLeft90(dir))){
            return DirectionUtils.rotateLeft90(dir);
        } else if (rc.canMove(DirectionUtils.rotateRight90(dir))){
            return DirectionUtils.rotateRight90(dir);
        }
        return dir;
    }

    static Direction dirForward90(RobotController rc, Direction dir) throws GameActionException{
        if(rc.canMove(dir) || !rc.onTheMap(rc.getLocation().add(dir))){
            return dir;
        } else if (rc.canMove(dir.rotateLeft())){
            return dir.rotateLeft();
        } else if (rc.canMove(dir.rotateRight())){
            return dir.rotateRight();
        }
        return dir;
    }

    static Direction getTeamGoDir(RobotController robot){
        return robot.getTeam().equals(Team.A) ? Direction.WEST : Direction.EAST; //TODO: make this smarter than assuming team a is on the left side
    }
}
