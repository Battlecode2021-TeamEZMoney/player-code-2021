package simpleplayer2;

import battlecode.common.*;
import common.*;

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
    
    /*
    static Direction dirForward90(RobotController rc, Direction dir) throws GameActionException{
        double[] movenesses = [trySenseMoveness(dir.rotateLeft()), trySenseMoveness(dir), trySenseMoveness(dir.rotateRight())]
        switch (dir){
            case NORTH:
            case EAST:
            case SOUTH:
            case WEST:
                if ( (movenesses[1] > movenesses[0]) && (movenesses[1] > movenesses[2]) )
                    return dir;
                else if (movenesses[0] > movenesses[2])
                    return dir.rotateLeft();
                else if (movenesses[2] > 0.0)
                    return dir.rotateRight();
                return Direction.CENTER;
            default:
                if ( (movenesses[1] > movenesses[0] * 0.5) && (movenesses[1] > movenesses[2] * 0.5) )
                    return dir;
                else if (movenesses[0] > movenesses[2])
                    return dir.rotateLeft();
                else if (movenesses[2] > 0.0)
                    return dir.rotateRight();
                return Direction.CENTER;
        }
    }
    
    double trySenseMoveness(Direction dir){
        if canMove(dir)
            return this.sensePassability(this.adjacentLocation(dir));
        else return 0.0;
    }
    */
    
    static Direction getTeamGoDir(RobotController robot){
        return robot.getTeam().equals(Team.A) ? Direction.WEST : Direction.EAST; //TODO: make this smarter than assuming team a is on the left side
    }
}
