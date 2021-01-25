package usqualplayer2;

import battlecode.common.*;
import common.*;

import java.util.*;

abstract class Pawn extends Robot {
    protected MapLocation hqLocation = null;
    protected int hqID;
    protected Direction dirTarget = Direction.CENTER;
    protected boolean explorer = false;
    protected boolean defending = false;
    protected Pathfinding pathingController;
    protected int mode = -1;

    Pawn(RobotController rcin) throws GameActionException {
        super(rcin);
        getHomeHQ();
        this.pathingController = new Pathfinding();
    }

    static Pawn unitFromRobotController(RobotController rc) throws Exception {
        switch (rc.getType()) {
            case POLITICIAN:
                return new Politician(rc);
            case MUCKRAKER:
                return new Muckraker(rc);
            case SLANDERER:
                return new Slanderer(rc);
            default:
                throw new Exception(rc.getType() + "is not a valid pawn type.");
        }
    }

    protected boolean getHomeHQ() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots(2, allyTeam);
        for (RobotInfo robot : robots) {
            if (robot.type.equals(RobotType.ENLIGHTENMENT_CENTER)) {
                hqLocation = robot.getLocation();
                hqID = robot.getID();
                dirTarget = directionTo(hqLocation).opposite();
                explorer = rcType.equals(RobotType.MUCKRAKER) && Math.random() > .5;
                return true;
            }
        }
        return false;
    }

    protected boolean hasOrCanGetHomeHQ() throws GameActionException {
        if (rc.canGetFlag(hqID)) {
            return true;
        } else if (getHomeHQ()) {
            return true;
        }
        return false;
    }

    void setNearbyHQFlag() throws GameActionException {
        if (encoded == 0) {
            RobotInfo[] nearby = rc.senseNearbyRobots();
            int tempDist = Integer.MAX_VALUE;
            Team tempTeam = null;
            RobotInfo tempBot = null;
            for (RobotInfo robot : nearby) {
                int distFromHQ = minMovesLeft(robot.getLocation(), hqLocation);
                if (robot.getType().equals(RobotType.ENLIGHTENMENT_CENTER)) {
                    if (distFromHQ < 15 && isEnemy(robot) && (!tempTeam.equals(enemyTeam) || tempDist > distFromHQ)) {
                        tempTeam = robot.getTeam();
                        tempDist = distFromHQ;
                        tempBot = robot;
                    }

                    if (tempDist < 15 && tempTeam.equals(enemyTeam)) {
                        continue;
                    } else {
                        if (tempTeam.equals(allyTeam) && distFromHQ < tempDist) {
                        } else if (tempTeam.equals(enemyTeam) && (robot.getTeam().equals(Team.NEUTRAL)
                                || (distFromHQ < tempDist && robot.getTeam().equals(enemyTeam)))) {
                        } else if (tempTeam.equals(Team.NEUTRAL) && distFromHQ < tempDist
                                && robot.getTeam().equals(Team.NEUTRAL)) {
                        } else if (Math.random() < .1) {
                        } else {
                            continue;
                        }
                        tempTeam = robot.getTeam();
                        tempDist = distFromHQ;
                        tempBot = robot;
                    }
                }
            }
            encoded = Encoding.encode(tempBot.getLocation(), flagCodeFromHQTeam(tempTeam), tempBot.conviction);
        }
        trySetFlag(encoded);
        encoded = 0;
    }

    protected boolean tryMove(Direction dir) throws GameActionException {
        if (!dir.equals(Direction.CENTER) && rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }
        return false;
    }

    protected Direction dirForward90(Direction dir) throws GameActionException {
        if (dir.equals(Direction.CENTER)) {
            return dir;
        }
        Direction[] possDirs = { dir, dir.rotateLeft(), dir.rotateRight() };
        if (Math.random() < 0.5) {
            swap(possDirs, 1, 2);
        }
        return maxPassabilityDir(possDirs);
    }

    protected boolean tryDirForward90(Direction dir) throws GameActionException {
        return tryMove(dirForward90(dir));
    }

    protected Direction dirForward180(Direction dir) throws GameActionException {
        if (dir.equals(Direction.CENTER)) {
            return dir;
        }
        Direction[] possDirs = { dir, dir.rotateLeft(), dir.rotateRight(), DirectionUtils.rotateLeft90(dir),
                DirectionUtils.rotateRight90(dir) };
        if (Math.random() < 0.5) {
            swap(possDirs, 1, 2);
        }
        if (Math.random() < 0.5) {
            swap(possDirs, 3, 4);
        }
        return maxPassabilityDir(possDirs);
    }

    protected Direction maxPassabilityDir(Direction[] possDirs) throws GameActionException {
        Direction bestDir = possDirs[0];
        double maxPassability = 0;
        for (Direction curDir : possDirs) {
            if (rc.canMove(curDir)) {
                double curPassability = rc.sensePassability(rc.getLocation().add(curDir));
                if (curPassability > maxPassability) {
                    maxPassability = curPassability;
                    bestDir = curDir;
                }
            }
        }
        return bestDir;
    }

    protected boolean tryDirForward180(Direction dir) throws GameActionException {
        return tryMove(dirForward180(dir));
    }

    protected boolean tryDirForward90180(Direction dir) throws GameActionException {
        return tryDirForward90(dir) || tryDirForward180(dir);
    }

    protected boolean tryDirForward090180(Direction dir) throws GameActionException {
        return tryMove(dir) || tryDirForward90180(dir);
    }

    protected Direction awayFromRobots(List<RobotInfo> robots) throws GameActionException {
        MapLocation location = new MapLocation(0, 0);
        if (robots.size() == 0) {
            return Direction.CENTER; // return dirTarget;
        }
        for (RobotInfo robot : robots) {
            location = location.translate(robot.location.x, robot.location.y);
        }

        location = new MapLocation(location.x / robots.size(), location.y / robots.size());
        return directionTo(location).opposite();
        // return awayFromRobots1AndTowardsRobots2(robots, Collections.emptyList());
    }

    // protected Direction awayFromRobots1AndTowardsRobots2(List<RobotInfo> robots1,
    // List<RobotInfo> robots2) throws GameActionException {
    // MapLocation location = new MapLocation(0, 0);
    // if (robots1.isEmpty() && robots2.isEmpty()) {
    // return Direction.CENTER; //return dirTarget;
    // }
    // for (RobotInfo robot : robots1) {
    // location = location.translate(robot.location.x, robot.location.y);
    // }
    // for (RobotInfo robot : robots2) {
    // MapLocation oppositeLoc = oppositePoint(rc.getLocation(), robot.location);
    // location = location.translate(oppositeLoc.x, oppositeLoc.y);
    // }
    //
    // int size = robots1.size() + robots2.size();
    // location = new MapLocation(location.x / size, location.y / size);
    // return directionTo(location).opposite();
    // }

    protected Direction awayFromAllies() throws GameActionException {
        return awayFromRobots(Arrays.asList(rc.senseNearbyRobots(sensorRadiusSquared, allyTeam)));
    }

    protected Direction towardsRobots(List<RobotInfo> robots) throws GameActionException {
        return awayFromRobots(robots).opposite();
        // return awayFromRobots1AndTowardsRobots2(Collections.emptyList(), robots);
    }

    protected void updateDirIfOnBorder() throws GameActionException {
        for (Direction dir : DirectionUtils.cardinalDirections) {
            if (!rc.onTheMap(rc.getLocation().add(dir))) {
                dir = dir.opposite();
                switch ((int) (Math.random() * 3)) {
                    case 0:
                        dirTarget = dir.rotateLeft();
                        return;
                    case 1:
                        dirTarget = dir.rotateRight();
                        return;
                    case 2:
                    default:
                        dirTarget = dir;
                }
            }
        }
    }

    protected class Pathfinding {
        private Direction lastDir = null;
        private MapLocation pos = null;
        private MapLocation oldOpLL = null;
        private MapLocation oldOpL = null;
        private MapLocation oldOpM = null;
        private MapLocation oldOpR = null;
        private MapLocation oldOpRR = null;

        void resetPrevMovesAndDir() {
            lastDir = null;
            oldOpLL = null;
            oldOpL = null;
            oldOpM = null;
            oldOpR = null;
            oldOpRR = null;
        }

        private void setTarget(MapLocation target) {
            if (pos.equals(target)) {
                return;
            } else {
                resetPrevMovesAndDir();
                pos = target;
            }
        }

        MapLocation getTarget() {
            return pos;
        }

        Direction dirToTarget(MapLocation target) throws GameActionException {
            setTarget(target);
            if (pos == null) {
                return Direction.CENTER;
            }
            return bestDir180(rc.getLocation(), furthestSensibleTile(rc.getLocation(), pos));
        }

        private Direction bestDir180(MapLocation from, MapLocation to) throws GameActionException {
            Direction dirM = from.directionTo(to);
            boolean rcLocIsNotEqFrom = rc.getLocation().equals(from);
            if (from.isAdjacentTo(to)) {
                Direction oppLastDir = lastDir.opposite();
                double tempCost = Double.MAX_VALUE - 1;
                Direction tempDir = dirM;
                Direction dirL = dirM.rotateLeft();
                Direction dirLL = dirL.rotateLeft();
                Direction dirR = dirM.rotateRight();
                Direction dirRR = dirR.rotateRight();
                MapLocation posLL = from.add(dirLL);
                MapLocation posL = from.add(dirL);
                MapLocation posM = from.add(dirM);
                MapLocation posR = from.add(dirR);
                MapLocation posRR = from.add(dirRR);
                boolean diagonalMovesRemaining = diagonalMovesRemaining(from, to);
                double costLL = rcLocIsNotEqFrom || rc.canMove(dirLL) ? cooldownAtTile(posLL) : Double.MAX_VALUE;
                double costL = rcLocIsNotEqFrom || rc.canMove(dirL)
                        ? (DirectionUtils.isDiagonal(dirL) && diagonalMovesRemaining ? .75 : 1) * cooldownAtTile(posL)
                        : Double.MAX_VALUE;
                double costM = rcLocIsNotEqFrom || rc.canMove(dirM)
                        ? (DirectionUtils.isDiagonal(dirM) && diagonalMovesRemaining ? .6 : 1) * cooldownAtTile(posM)
                        : Double.MAX_VALUE;
                double costR = rcLocIsNotEqFrom || rc.canMove(dirR)
                        ? (DirectionUtils.isDiagonal(dirR) && diagonalMovesRemaining ? .75 : 1) * cooldownAtTile(posR)
                        : Double.MAX_VALUE;
                double costRR = rcLocIsNotEqFrom || rc.canMove(dirRR) ? cooldownAtTile(posRR) : Double.MAX_VALUE;

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

        private boolean notLastMoveOption(MapLocation pos) {
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

        private MapLocation furthestSensibleTile(MapLocation from, MapLocation to) {
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

        private boolean diagonalMovesRemaining(MapLocation from, MapLocation to) {
            return Math.abs(from.x - to.x) != 0 && Math.abs(from.y - to.y) != 0;
        }

        private double cooldownAtTile(MapLocation tile) throws GameActionException {
            return cooldownFromPassability(rc.sensePassability(tile));
        }

        private double cooldownFromPassability(double speed) {
            return baseActionCooldown / speed;
        }
    }
}