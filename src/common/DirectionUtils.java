package common;
import battlecode.common.Direction;

public class DirectionUtils {
    public static final Direction[] nonCenterDirections = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };

    public static Direction randomDirection() {
        return nonCenterDirections[(int) (Math.random() * nonCenterDirections.length)];
    }

    public static Direction rotateRight90(Direction dir) {
        return dir.rotateRight().rotateRight();
    }

    public static Direction rotateLeft90(Direction dir) {
        return dir.rotateLeft().rotateLeft();
    }

    public static Direction rotateRightSnapCardinal(Direction dir){
        switch(dir){
            case NORTH: return Direction.EAST;
            case NORTHEAST: return Direction.EAST;
            case EAST: return Direction.SOUTH;
            case SOUTHEAST: return Direction.SOUTH;
            case SOUTH: return Direction.WEST;
            case SOUTHWEST: return Direction.WEST;
            case WEST: return Direction.NORTH;
            case NORTHWEST: return Direction.NORTH;
            default: return Direction.CENTER;
        }
    }

    public static Direction rotateLeftSnapCardinal(Direction dir){
        switch(dir){
            case NORTH: return Direction.WEST;
            case NORTHEAST: return Direction.NORTH;
            case EAST: return Direction.NORTH;
            case SOUTHEAST: return Direction.EAST;
            case SOUTH: return Direction.EAST;
            case SOUTHWEST: return Direction.SOUTH;
            case WEST: return Direction.SOUTH;
            case NORTHWEST: return Direction.WEST;
            default: return Direction.CENTER;
        }
    }

    public static Direction rotateRightSnapDiagonal(Direction dir){
        switch(dir){
            case NORTH: return Direction.NORTHEAST;
            case NORTHEAST: return Direction.SOUTHEAST;
            case EAST: return Direction.SOUTHEAST;
            case SOUTHEAST: return Direction.SOUTHWEST;
            case SOUTH: return Direction.SOUTHWEST;
            case SOUTHWEST: return Direction.NORTHWEST;
            case WEST: return Direction.NORTHWEST;
            case NORTHWEST: return Direction.NORTHEAST;
            default: return Direction.CENTER;
        }
    }

    public static Direction rotateLeftSnapDiagonal(Direction dir){
        switch(dir){
            case NORTH: return Direction.NORTHWEST;
            case NORTHEAST: return Direction.NORTHWEST;
            case EAST: return Direction.NORTHEAST;
            case SOUTHEAST: return Direction.NORTHEAST;
            case SOUTH: return Direction.SOUTHEAST;
            case SOUTHWEST: return Direction.SOUTHEAST;
            case WEST: return Direction.SOUTHWEST;
            case NORTHWEST: return Direction.SOUTHWEST;
            default: return Direction.CENTER;
        }
    }
}
