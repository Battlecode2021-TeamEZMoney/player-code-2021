package simpleplayer2;

import battlecode.common.*;

public strictfp class RobotPlayer {
    static RobotController rc;

    public static void run(RobotController rc) throws GameActionException {
        while (true) {
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
