package simpleplayer1;

import battlecode.common.*;
import common.*;

public strictfp class RobotPlayer {
    static RobotController rc;
    static int turnCount;
    static int hqID;
    static int storedFlag;
    static Direction curDir = Direction.NORTH;

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
        if (rc.getInfluence() > 69 && rc.canBuildRobot(toBuild, Direction.WEST, rc.getInfluence() - 1)) {
            rc.buildRobot(toBuild, Direction.WEST, rc.getInfluence() - 1);
        }

        if (rc.getTeamVotes() < 1500 && rc.getInfluence() > 0) {
            rc.bid(1);
        }

        if (turnCount % 10 == 0) {
            System.out.println(rc.getInfluence());
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

        if (!rc.isReady())
            return;
        
        if (storedFlag == 10) {
            if (rc.senseNearbyRobots(9, rc.getTeam().opponent()).length != 0) {
                if (rc.canEmpower(9)) {
                    rc.empower(9);
                    return;
                }
            }
            RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(25, rc.getTeam().opponent());
            if (nearbyEnemies.length != 0) {
                tryMove(rc.getLocation().directionTo(nearbyEnemies[0].getLocation()));
                return;
            }
            PathFind.lazy_path_to(rc, new MapLocation(10005, 23926));

            // if (!tryMove(curDir)) {
            //     curDir = clockwiseTurnCardinal(curDir);
            // }

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

    static Direction clockwiseTurnCardinal(Direction dir) {
        switch (dir) {
            case WEST:
                return Direction.NORTH;
            case NORTHWEST:
                return Direction.NORTH;
            case NORTH:
                return Direction.EAST;
            case NORTHEAST:
                return Direction.EAST;
            case EAST:
                return Direction.SOUTH;
            case SOUTHEAST:
                return Direction.SOUTH;
            case SOUTH:
                return Direction.WEST;
            case SOUTHWEST:
                return Direction.WEST;
            default:
                return Direction.CENTER;
        }
    }
}
