package common;

import battlecode.common.Direction;

public class DirectionUtilities {
    public static Direction randomDirection() {
        return Constants.directions[(int) (Math.random() * Constants.directions.length)];
    }

    public static Direction clockwiseTurnCardinal(Direction dir) {
        switch (dir) {
            case WEST:
                return Direction.NORTH;
            case NORTHWEST:
                return Direction.NORTH;
            case NORTH:
                return Direction.EAST;
            case NORTHEAST:
                return Direction.EAST;
            case EAST:
                return Direction.SOUTH;
            case SOUTHEAST:
                return Direction.SOUTH;
            case SOUTH:
                return Direction.WEST;
            case SOUTHWEST:
                return Direction.WEST;
            default:
                return Direction.CENTER;
        }
    }
}
