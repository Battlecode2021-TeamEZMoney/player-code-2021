package simpleplayer1_subm;
import battlecode.common.*;

public class Constants {
    public static final RobotType[] units = {
        RobotType.POLITICIAN,
        RobotType.SLANDERER,
        RobotType.MUCKRAKER,
    };

    public static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };

    public static final int[][] stageone_wall = {
        {-3, 3}, {3, 3}, {-3, -3}, {3, -3}, {0, 3}, {-3, 0}, {3, 0}, {0, -3}
    };

    public static final int[][] stageone_slanderers = {
        {-1, 1}, {1, -1}, {1, 1}, {-1, -1}
    };
    
}
