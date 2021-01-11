package common;

import battlecode.common.Direction;

public class DirectionUtilities {
    public static Direction randomDirection() {
        return Direction.values()[(int) (Math.random() * Direction.values().length)];
    }

    public static Direction rotateRight90(Direction dir) {
        return dir.rotateRight().rotateRight();
    }

    public static Direction rotateLeft90(Direction dir) {
        return dir.rotateLeft().rotateLeft();
    }
}
