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

}
