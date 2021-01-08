package simpleplayer1_subm;

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
        RobotType toBuild;

        if (rc.getRoundNum() == 1 && rc.getInfluence() > 20) {
            rc.buildRobot(RobotType.SLANDERER, Direction.NORTHWEST, rc.getInfluence() - ((rc.getInfluence() - 1) % 20) - 1);
            if (rc.canBid(3)) rc.bid(3);
            return;
        }

        boolean any_open_pol_spots = false;

        for (int i = 0; i < Constants.stageone_wall.length; i++) {
            int[] dxy = Constants.stageone_wall[i];
            MapLocation pos = rc.getLocation().translate(dxy[0], dxy[1]);
            if (rc.canSenseLocation(pos) && !rc.isLocationOccupied(pos)) {
                        rc.setFlag(10 + i);
                        any_open_pol_spots = true;
                        break;
            }
        }

        if (any_open_pol_spots) {
            toBuild = RobotType.POLITICIAN;
            if (rc.getInfluence() > 69 && rc.canBuildRobot(toBuild, Direction.WEST, rc.getInfluence() - 1)) {
                rc.buildRobot(toBuild, Direction.WEST, rc.getInfluence() - 1);
            }
        }

        if (rc.getTeamVotes() < 1500 && rc.canBid(2)) {
            rc.bid(2);
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
                    hqLocation = robot.getLocation();
                    break;
                }
            }
        }

        if (!rc.isReady())
            return;

        if (storedFlag > 9 && storedFlag < 19) { // defense mode
            /*
             * if (rc.senseNearbyRobots(9, rc.getTeam().opponent()).length != 0) { if
             * (rc.canEmpower(9)) { rc.empower(9); return; } }
             * 
             * RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(25,
             * rc.getTeam().opponent()); if (nearbyEnemies.length != 0) {
             * tryMove(rc.getLocation().directionTo(nearbyEnemies[0].getLocation()));
             * return; }
             */

            int[] dxy = Constants.stageone_wall[storedFlag - 10];
            MapLocation pos = hqLocation.translate(dxy[0], dxy[1]);
            //tryMove(PathFind.get_path_direction(rc, pos));
            tryMove(FuzzPathing.pathTo(rc, pos));

        } else {
            /*Team enemy = rc.getTeam().opponent();
            int actionRadius = rc.getType().actionRadiusSquared;
            RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
            if (attackable.length != 0 && rc.canEmpower(actionRadius)) {
                rc.empower(actionRadius);
                return;
            }
            tryMove(DirectionUtilities.randomDirection());*/
            //System.out.println("Error invalid flag" + storedFlag);
        }
    }

    static void runSlanderer() throws GameActionException {
    }

    static void runMuckraker() throws GameActionException {
    }

    static boolean tryMove(Direction dir) throws GameActionException {
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else
            return false;
    }
}
