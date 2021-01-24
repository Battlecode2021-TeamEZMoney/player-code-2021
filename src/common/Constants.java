package common;

import battlecode.common.*;
import java.util.*;

public class Constants {
        public static final RobotType[] units = { RobotType.POLITICIAN, RobotType.SLANDERER, RobotType.MUCKRAKER, };

        public static final int minID = 9999;
        public static final int MAX_ROUNDS = GameConstants.GAME_MAX_NUMBER_OF_ROUNDS;
        public static final int VOTES_TO_WIN = MAX_ROUNDS / 2 + 1;

        public static final MapLocation origin = new MapLocation(0, 0);
        public static final Integer[] optimalSlandInfArray = { 21, 41, 63, 85, 107, 130, 154, 178, 203, 228, 255, 282,
                        310, 339, 368, 399, 431, 463, 497, 532, 568, 605, 643, 683, 724, 766, 810, 855, 902, 949,
                        Integer.MAX_VALUE };
        public static final TreeSet<Integer> optimalSlandInfSet = new TreeSet<Integer>(Arrays.asList(optimalSlandInfArray));
        public static final int minimumPolInf = 16;
        public static final RobotType[] spawnOrder = { RobotType.MUCKRAKER, RobotType.MUCKRAKER, RobotType.MUCKRAKER,
                        RobotType.MUCKRAKER, RobotType.MUCKRAKER, RobotType.POLITICIAN, RobotType.POLITICIAN,
                        RobotType.MUCKRAKER, RobotType.POLITICIAN, RobotType.POLITICIAN, RobotType.POLITICIAN,
                        RobotType.SLANDERER };
}
