package simpleplayer1;
import battlecode.common.*;
import common.*;

public strictfp class RobotPlayer {
    static RobotController rc;
    static int turnCount;
    static int hqID;
    static int storedFlag;

    /**
     * run() is the method that is called when a robot is instantiated in the
     * Battlecode world. If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
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
                    case POLITICIAN:
                        runPolitician();
                        break;
                    case SLANDERER:
                        runSlanderer();
                        break;
                    case MUCKRAKER:
                        runMuckraker();
                        break;
                }

                Clock.yield();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static void runEnlightenmentCenter() throws GameActionException {
        rc.setFlag(10);
        RobotType toBuild = RobotType.POLITICIAN;
        int influence = 1;
        if (rc.canBuildRobot(toBuild, Direction.NORTH, influence)) {
            rc.buildRobot(toBuild, Direction.NORTH, influence);
        }
    }

    static void runPolitician() throws GameActionException {
        if (turnCount == 1) {
            RobotInfo[] robots = rc.senseNearbyRobots(2, rc.getTeam());
            for (RobotInfo robot : robots) {
                if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                    storedFlag = rc.getFlag(robot.getID());
                    break;
                }
            }
        }

        if (storedFlag == 10) {
            rc.setIndicatorDot(rc.getLocation(), 100, 0, 0);
        } else {
            Team enemy = rc.getTeam().opponent();
            int actionRadius = rc.getType().actionRadiusSquared;
            RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
            if (attackable.length != 0 && rc.canEmpower(actionRadius)) {
                rc.empower(actionRadius);
                return;
            }
            tryMove(randomDirection());
        }
    }

    static void runSlanderer() throws GameActionException {
        tryMove(randomDirection());
    }

    static void runMuckraker() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
            if (robot.type.canBeExposed()) {
                // It's a slanderer... go get them!
                if (rc.canExpose(robot.location)) {
                    rc.expose(robot.location);
                    return;
                }
            }
        }
        tryMove(randomDirection());
    }

    static Direction randomDirection() {
        return enumLists.directions[(int) (Math.random() * enumLists.directions.length)];
    }

    static boolean tryMove(Direction dir) throws GameActionException {
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else
            return false;
    }
}
