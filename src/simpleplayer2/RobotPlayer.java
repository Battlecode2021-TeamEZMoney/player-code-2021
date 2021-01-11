package simpleplayer2;

import battlecode.common.*;

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
                        EnlightenmentPlayer.runEnlightenmentCenter(rc);
                        break;
                    case POLITICIAN:
                        PoliticianPlayer.runPolitician(rc, false);
                        break;
                    case SLANDERER:
                        SlandererPlayer.runSlanderer(rc);
                        break;
                    case MUCKRAKER:
                        MuckrakerPlayer.runMuckraker(rc);
                        break;
                }

                Clock.yield();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
