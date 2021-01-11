package pathtesting1;

import battlecode.common.*;
import simpleplayer1.*;

public strictfp class RobotPlayer {
    static RobotController rc;
    static int turnCount;
    static int hqID;
    static int storedFlag;
    static MapLocation hqLocation;
    static Direction curDir = Direction.NORTH;

    /**
     * run() is the method that is called when a robot is instantiated in the
     * Battlecode world. If this method returns, the robot dies!
     **/
    public static void run(RobotController rc) throws GameActionException {
        RobotPlayer.rc = rc;

        turnCount = 0;

        while (true) {
            turnCount += 1;

            try {
                switch (rc.getType()) {
                    case ENLIGHTENMENT_CENTER:
                        runEnlightenmentCenter();
                        break;
                    default: 
                        runStraight();
                }

                Clock.yield();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static void runEnlightenmentCenter() throws GameActionException {
        if (rc.getRoundNum() == 1) {
            rc.buildRobot(RobotType.POLITICIAN, Direction.NORTHWEST, 1);
        }
    }

    static void runStraight() throws GameActionException {
        if (turnCount == 1) {
            for (RobotInfo robot : rc.senseNearbyRobots(2, rc.getTeam())) {
                if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                    hqLocation = robot.getLocation();
                    break;
                }
            }
        }

        if (!rc.isReady())
            return;

        MapLocation pos = hqLocation.translate(-5, 3);
        //tryMove(PathFind.get_path_direction(rc, pos));
        tryMove(PathFind.pathTo(rc, pos));
    }

    static boolean tryMove(Direction dir) throws GameActionException {
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }
        return false;
    }
}
