package common;
import battlecode.common.*;

public class Constants {
    public static final RobotType[] units = {
        RobotType.POLITICIAN,
        RobotType.SLANDERER,
        RobotType.MUCKRAKER,
    };

    public static final int minID = 9999;
    public static final int totalRounds = 2999;
    public static final int votesToWin = (int) Math.ceil((totalRounds + 1)/2);

    public static final int[] optimalSlandInf = {21,41,63,85,107,130,154,178,203,228,255,282,310,339,368,399,431,463,497,532,568,605,643,683,724,766,810,855,902,949,2147483647};
    public static final int minimumPolInf = 17;
    public static final RobotType[] spawnOrder = {RobotType.MUCKRAKER,RobotType.MUCKRAKER,RobotType.SLANDERER,RobotType.MUCKRAKER,RobotType.MUCKRAKER,RobotType.POLITICIAN,RobotType.POLITICIAN,RobotType.SLANDERER,RobotType.POLITICIAN,RobotType.POLITICIAN,RobotType.POLITICIAN};
}
