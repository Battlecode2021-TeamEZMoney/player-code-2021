package pathtestingplayer;

import java.util.Map;

import battlecode.common.*;

public strictfp class RobotPlayer {
    static RobotController rc;
    static int mostBytecode = 0;
    static Direction lastDir = Direction.CENTER;
    static MapLocation pos = null;
    static MapLocation oldOpLL = new MapLocation(0, 0);
    static MapLocation oldOpL = new MapLocation(0, 0);
    static MapLocation oldOpM = new MapLocation(0, 0);
    static MapLocation oldOpR = new MapLocation(0, 0);
    static MapLocation oldOpRR = new MapLocation(0, 0);

    public static void run(RobotController rc) throws GameActionException {
        RobotPlayer.rc = rc;

        switch (rc.getType()) {
            case ENLIGHTENMENT_CENTER:
                runEnlightenmentCenter();
                break;
            default:
                runStraight();
        }
    }

    static void runEnlightenmentCenter() throws GameActionException {
        while (true) {
            switch (rc.getRoundNum()) {
                case 1:
                case 200:
                case 400:
                case 600:
                    rc.buildRobot(RobotType.POLITICIAN, Direction.SOUTHEAST, 1);
            }
            Clock.yield();
        }
    }

    static void runStraight() throws GameActionException {
        int turnCount = 1;
        int firstRound = rc.getRoundNum();
        for (RobotInfo robot : rc.senseNearbyRobots(2, rc.getTeam())) {
            if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                pos = robot.getLocation().translate(25, -40);
                break;
            }
        }

        main: {
            while (!rc.getLocation().equals(pos)) {
                turnCount++;
                if (rc.isReady()) {
                    switch (firstRound - 1) {
                        case 1:
                            goStraight();
                            break;
                        case 200:
                            best90();
                            break;
                        case 400:
                            best180();
                            break;
                        case 600:
                            best90heur();
                            break;
                        default:
                            break main;
                    }
                }
                Clock.yield();
            }
            System.out.println((firstRound - 1) + " finished in " + turnCount + " turns with a maximum of " + mostBytecode
                    + " bytecode.");
            while (true) {
                tryMove(Direction.SOUTH);
                Clock.yield();
            }
        }
        System.out.println("Error, invalid first round.");
    }

    static void goStraight() throws GameActionException {
        int startBytecode = Clock.getBytecodeNum();
        tryMove(rc.getLocation().directionTo(pos));

        int totalBytecode = Clock.getBytecodeNum() - startBytecode;
        mostBytecode = totalBytecode < mostBytecode ? mostBytecode : totalBytecode;
    }

    static void best180() throws GameActionException {
        int startBytecode = Clock.getBytecodeNum();
        tryMove(bestDir180(rc.getLocation(), furthestSensibleTile(rc.getLocation(), pos)));
        int totalBytecode = Clock.getBytecodeNum() - startBytecode;
        mostBytecode = totalBytecode > mostBytecode ? totalBytecode : mostBytecode;
    }

    static Direction bestDir180(MapLocation from, MapLocation to) throws GameActionException {
        Direction dirM = from.directionTo(to);
        double tempCost = Double.MAX_VALUE - 1;
        Direction tempDir = dirM;
        Direction oppLastDir = lastDir.opposite();
        if (!rc.getLocation().isAdjacentTo(to)) {
            Direction dirL = dirM.rotateLeft();
            Direction dirLL = dirL.rotateLeft();
            Direction dirR = dirM.rotateRight();
            Direction dirRR = dirR.rotateRight();
            MapLocation posLL = rc.getLocation().add(dirLL);
            MapLocation posL = rc.getLocation().add(dirL);
            MapLocation posM = rc.getLocation().add(dirM);
            MapLocation posR = rc.getLocation().add(dirR);
            MapLocation posRR = rc.getLocation().add(dirRR);
            boolean diagonalMovesRemaining = diagonalMovesRemaining(from, to);
            double costLL = rc.canMove(dirLL) ? cooldownAtTile(posLL) : Double.MAX_VALUE;
            double costL = rc.canMove(dirL) ? (isDiagonal(dirL) && diagonalMovesRemaining ? .75 : 1) * cooldownAtTile(posL) : Double.MAX_VALUE;
            double costM = rc.canMove(dirM) ? (isDiagonal(dirM) && diagonalMovesRemaining ? .6 : 1) * cooldownAtTile(posM) : Double.MAX_VALUE;
            double costR = rc.canMove(dirR) ? (isDiagonal(dirR) && diagonalMovesRemaining ? .75 : 1) * cooldownAtTile(posR) : Double.MAX_VALUE;
            double costRR = rc.canMove(dirRR) ? cooldownAtTile(posRR) : Double.MAX_VALUE;

            rc.setIndicatorDot(posLL, 0, 0, 0);
            rc.setIndicatorDot(posL, 0, 0, 0);
            rc.setIndicatorDot(posM, 0, 0, 0);
            rc.setIndicatorDot(posR, 0, 0, 0);
            rc.setIndicatorDot(posRR, 0, 0, 0);

            if (costLL < tempCost && !dirLL.equals(oppLastDir) && notLastMoveOption(posLL)) {
                tempCost = costLL;
                tempDir = dirLL;
            }
            if (costRR < tempCost && !dirRR.equals(oppLastDir) && notLastMoveOption(posRR)) {
                tempCost = costRR;
                tempDir = dirRR;
            }
            if (costL <= tempCost && !dirL.equals(oppLastDir) && notLastMoveOption(posL)) {
                tempCost = costL;
                tempDir = dirL;
            }
            if (costR <= tempCost && !dirR.equals(oppLastDir) && notLastMoveOption(posR)) {
                tempCost = costR;
                tempDir = dirR;
            }
            if (costM <= tempCost && !dirM.equals(oppLastDir) && notLastMoveOption(posM)) {
                tempCost = costM;
                tempDir = dirM;
            }
            oldOpLL = posLL;
            oldOpL = posL;
            oldOpM = posM;
            oldOpR = posR;
            oldOpRR = posRR;
            lastDir = tempDir;
            return tempDir;
        } else {
            return dirM;
        }
    }

    static boolean notLastMoveOption(MapLocation pos) {
        if (pos.equals(oldOpLL)) {
            return false;
        } else if (pos.equals(oldOpL)) {
            return false;
        } else if (pos.equals(oldOpM)) {
            return false;
        } else if (pos.equals(oldOpR)) {
            return false;
        } else if (pos.equals(oldOpRR)) {
            return false;
        }
        return true;

    }

    static void best90() throws GameActionException {
        int startBytecode = Clock.getBytecodeNum();
        tryMove(bestDir90(rc.getLocation(), furthestSensibleTile(rc.getLocation(), pos)));
        int totalBytecode = Clock.getBytecodeNum() - startBytecode;
        mostBytecode = totalBytecode > mostBytecode ? totalBytecode : mostBytecode;
    }

    static void best90heur() throws GameActionException {
        int startBytecode = Clock.getBytecodeNum();
        tryMove(bestDir90Heur(rc.getLocation(), furthestSensibleTile(rc.getLocation(), pos)));
        int totalBytecode = Clock.getBytecodeNum() - startBytecode;
        mostBytecode = totalBytecode > mostBytecode ? totalBytecode : mostBytecode;
    }

    static Direction bestDir90(MapLocation from, MapLocation to) throws GameActionException {
        Direction heading = from.directionTo(to);
        if (!rc.getLocation().isAdjacentTo(to)) {
            Direction dirl = heading.rotateLeft();
            Direction dirr = heading.rotateRight();
            boolean diagonalMovesRemaining = diagonalMovesRemaining(from, to);
            double costl = (isDiagonal(dirl) && diagonalMovesRemaining ? 2 : 1)
                    * rc.sensePassability(rc.getLocation().add(dirl));
            double costm = (isDiagonal(heading) && diagonalMovesRemaining ? 2 : 1)
                    * rc.sensePassability(rc.getLocation().add(heading));
            double costr = (isDiagonal(dirr) && diagonalMovesRemaining ? 2 : 1)
                    * rc.sensePassability(rc.getLocation().add(dirr));
            if (costl > costm && costl > costr) {
                return (dirl);
            } else if (costr > costm) {
                return (heading);
            } else {
                return (heading);
            }
        } else {
            return heading;
        }
    }

    static Direction bestDir90Heur(MapLocation from, MapLocation to) throws GameActionException {
        Direction heading = from.directionTo(to);
        if (!rc.getLocation().isAdjacentTo(to)) {
            Direction dirl = heading.rotateLeft();
            Direction dirr = heading.rotateRight();
            MapLocation newPosL = rc.getLocation().add(dirl);
            MapLocation newPosM = rc.getLocation().add(heading);
            MapLocation newPosR = rc.getLocation().add(dirr);
            boolean diagonalMovesRemaining = diagonalMovesRemaining(from, to);
            double costl = (isDiagonal(dirl) && diagonalMovesRemaining ? .5 : 1) * cooldownAtTile(newPosL)
                    + (cooldownFromPassability(.1) * movesLeft(newPosL, to));
            double costm = (isDiagonal(heading) && diagonalMovesRemaining ? .5 : 1) * cooldownAtTile(newPosM)
                    + (cooldownFromPassability(.1) * movesLeft(newPosM, to));
            double costr = (isDiagonal(dirr) && diagonalMovesRemaining ? .5 : 1) * cooldownAtTile(newPosR)
                    + (cooldownFromPassability(.1) * movesLeft(newPosR, to));
            if (costl < costm && costl < costr) {
                return (dirl);
            } else if (costr < costm) {
                return (heading);
            } else {
                return (heading);
            }
        } else {
            return heading;
        }
    }

    static MapLocation furthestSensibleTile(MapLocation from, MapLocation to) {
        if (from.equals(to)) {
            return to;
        }
        MapLocation nextTile = from.add(from.directionTo(to));
        if (rc.canSenseLocation(nextTile)) {
            return furthestSensibleTile(nextTile, to);
        } else {
            return from;
        }

    }

    static void bestToFurthestWrapper() throws GameActionException {
        int startBytecode = Clock.getBytecodeNum();
        tryMove(bestToFurthestSensible());

        int totalBytecode = Clock.getBytecodeNum() - startBytecode;
        mostBytecode = totalBytecode < mostBytecode ? mostBytecode : totalBytecode;
    }

    static Direction bestToFurthestSensible() throws GameActionException {
        MapLocation from = rc.getLocation();
        MapLocation to = furthestSensibleTile(from, pos);
        rc.setIndicatorLine(from, to, 200, 200, 200);
        Direction header = from.directionTo(to);
        if (!from.isAdjacentTo(to)) {
            double leftCost = calcTurnDepth(from.add(header.rotateLeft()), to, 200, 0, 0);
            double middleCost = calcTurnDepth(from.add(header), to, 0, 200, 0);
            double rightCost = calcTurnDepth(from.add(header.rotateRight()), to, 0, 0, 200);
            if (leftCost < rightCost && leftCost < middleCost) {
                return header.rotateLeft();
            } else if (rightCost < middleCost) {
                return header.rotateRight();
            } else {
                return header;
            }
        } else {
            return header;
        }

    }

    static double calcTurnDepth(MapLocation from, MapLocation to, int r, int g, int b) throws GameActionException {
        rc.setIndicatorDot(from, r, g, b);
        if (from.equals(to)) {
            return 0;
        } else if (!rc.canSenseLocation(from)) {
            return 99999;
        }
        double currTileCost = cooldownAtTile(rc.getLocation());
        if (from.isAdjacentTo(to)) {
            return currTileCost;
        }
        Direction header = from.directionTo(to);
        double leftCost = currTileCost + calcTurnDepth(from.add(header.rotateLeft()), to, r, g, b);
        double middleCost = currTileCost + calcTurnDepth(from.add(header), to, r, g, b);
        double rightCost = currTileCost + calcTurnDepth(from.add(header.rotateRight()), to, r, g, b);
        rc.setIndicatorLine(rc.getLocation(), from.add(header.rotateLeft()), r, g, b);
        rc.setIndicatorLine(rc.getLocation(), from.add(header), r, g, b);
        rc.setIndicatorLine(rc.getLocation(), from.add(header.rotateRight()), r, g, b);
        if (leftCost < rightCost && leftCost < middleCost) {

            return leftCost;
        } else if (rightCost < middleCost) {

            return rightCost;
        } else {

            return middleCost;
        }
        /// return 0;
    }

    static double cooldownAtTile(MapLocation tile) throws GameActionException {
        return rc.getType().actionCooldown / rc.sensePassability(tile);
    }

    static double cooldownFromPassability(double speed) throws GameActionException {
        return rc.getType().actionCooldown / speed;
    }

    static boolean isDiagonal(Direction dir) {
        switch (dir) {
            case NORTHEAST:
            case SOUTHEAST:
            case SOUTHWEST:
            case NORTHWEST:
                return true;
            default:
                return false;
        }
    }

    static int movesLeft(MapLocation from, MapLocation to) {
        return Math.max(Math.abs(from.x - to.x), Math.abs(from.y - to.y));
    }

    static boolean diagonalMovesRemaining(MapLocation from, MapLocation to) {
        return Math.abs(from.x - to.x) != 0 && Math.abs(from.y - to.y) != 0;
    }

    static boolean tryMove(Direction dir) throws GameActionException {
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }
        return false;
    }

}
