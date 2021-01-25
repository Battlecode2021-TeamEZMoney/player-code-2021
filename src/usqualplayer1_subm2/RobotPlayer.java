package usqualplayer1_subm2;

import battlecode.common.*;

public strictfp class RobotPlayer {
    static RobotController rc;

    public static void run(RobotController rc) throws GameActionException {
        while (true) {
            try {
                Robot.robotFromRobotController(rc).run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}