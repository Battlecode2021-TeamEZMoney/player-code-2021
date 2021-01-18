package simpleplayer4;

import battlecode.common.*;
import common.*;
import java.util.*;

class EnlightenmentCenter extends Robot {
    private int turnCount = 0;
    private int unitsBuilt = 0;
    private int unitsIndex = 0;
    private int spawnIndex = 0;
    private RobotType unitToBuild;
    private int infToSpend;
    private Direction dirTarget, buildDirection;
    private boolean explorer;
    private int slandDistAway = 10;
    private Set<MapLocation> enemyHQs = new HashSet<MapLocation>();
    private Set<MapLocation> friendlyHQs = new HashSet<MapLocation>();
    private Set<MapLocation> neutralHQs = new HashSet<MapLocation>();
    private ArrayList<Integer> units = new ArrayList<Integer>();
    private final Bidding bidController;
    private boolean bidLastRound;

    EnlightenmentCenter(RobotController rcin) throws GameActionException {
        super(rcin); // Don't remove this.
        bidController = new Bidding();
    }

    void printStoredECs() throws GameActionException {
        System.out.println();
        for (MapLocation loc : enemyHQs)
            System.out.println("Enemy EC: " + printLoc(loc));
        System.out.println();
        for (MapLocation loc : friendlyHQs)
            System.out.println("Friendly EC: " + printLoc(loc));
        System.out.println();
        for (MapLocation loc : neutralHQs)
            System.out.println("Neutral EC: " + printLoc(loc));
        System.out.println();
        for (Integer unit : units)
            System.out.println("Unit ID: " + unit);
        System.out.println();
    }

    void run() throws GameActionException {
        while (true) {
            turnCount++;
            // TODO: Implement smart handling of other units and other HQs
            // gatherIntel();

            // printStoredECs();

            while (Clock.getBytecodesLeft() > 3000 && units.size() > 0) {
                unitsIndex %= units.size();
                int unitID = units.get(unitsIndex);
                if (rc.canGetFlag(unitID)) {
                    parseUnitFlag(rc.getFlag(unitID));
                    unitsIndex++;
                } else {
                    units.remove(unitsIndex);
                }
            }
            

            // int start = Clock.getBytecodesLeft();

            if (rc.isReady()) {
                unitToBuild = getUnitToBuild();
                infToSpend = getNewUnitInfluence();
                dirTarget = getPreferredDirection();
                buildDirection = getBuildDirection(unitToBuild, dirTarget, infToSpend);
                explorer = Math.random() < 0.8 && unitToBuild == RobotType.MUCKRAKER;
                if (rc.canBuildRobot(unitToBuild, buildDirection, infToSpend)) {
                    rc.buildRobot(unitToBuild, buildDirection, infToSpend);
                    unitsBuilt++;
                    RobotInfo robotBuilt = rc.senseRobotAtLocation(rc.getLocation().add(buildDirection));
                    units.add(robotBuilt.getID());

                    // TODO: Implement flag based orders
                    // trySetFlag(getOrdersForUnit(unitToBuild));
                }
            }

            trySetFlag(getTarget());

            if (shouldBid()) {
                int bidAmount = (int) (bidController.getBidBaseAmount() * bidController.getBidMultiplier());
                bidLastRound = tryBid(Math.max(bidAmount - (bidAmount % 2), 2));
            } else {
                bidLastRound = false;
            }
            // System.out.println(Clock.getBytecodesLeft() - start);
            // System.out.println(rc.getEmpowerFactor(allyTeam, 0));

            Clock.yield();
        }
    }

    /*
     * private void gatherIntel() {
     * 
     * }
     */

    private void parseUnitFlag(int flag) throws GameActionException {
        MapLocation tempLocation = Encoding.getLocationFromFlag(rc, flag);
        switch (Encoding.getInfoFromFlag(flag)) {
            case 2:
                enemyHQs.add(tempLocation);
                friendlyHQs.remove(tempLocation);
                neutralHQs.remove(tempLocation);
                return;
            case 3:
                enemyHQs.remove(tempLocation);
                friendlyHQs.add(tempLocation);
                neutralHQs.remove(tempLocation);
                return;
            case 4:
                neutralHQs.add(tempLocation);
                return;
        }
    }
    
    
    MapLocation avgLoc(Set<MapLocation> locs) {
    	MapLocation avg = Constants.origin;
    	if (locs.size() == 0) {
    		return avg;
    	}
    	for (MapLocation loc : locs) {
    		avg = avg.translate(loc.x, loc.y);
    	}
    	return new MapLocation(avg.x / locs.size(), avg.y / locs.size());
    }
    
    MapLocation slandCenter() {
    	if (enemyHQs.size() == 0) {
    		return rc.getLocation();
    	}
    	MapLocation avgEnemyHQ = avgLoc(enemyHQs);
		Direction awayFromEnemyHQs = rc.getLocation().directionTo(avgEnemyHQ).opposite();
		return rc.getLocation().translate(slandDistAway * awayFromEnemyHQs.dx, slandDistAway * awayFromEnemyHQs.dy);
    }
    
    private int getTarget() throws GameActionException {
        if (canSenseEnemy() && Math.random() < 0.8) {
            return Encoding.encode(rc.getLocation(), FlagCodes.patrol, dirTarget, explorer);
        } else if (neutralHQs.size() > 0 && (robotType.equals(RobotType.POLITICIAN) || Math.random() < 0.5)) {
            return Encoding.encode(neutralHQs.iterator().next(), FlagCodes.neutralHQ, dirTarget, explorer);
        } else if (enemyHQs.size() > 0) {
        	if (robotType.equals(RobotType.SLANDERER) || Math.random() < 0.1) {
        		return Encoding.encode(slandCenter(), FlagCodes.slandCenter, dirTarget, explorer);
        	}
            return Encoding.encode(enemyHQs.iterator().next(), FlagCodes.enemyHQ, dirTarget, explorer);
        } else {
            return Encoding.encode(rc.getLocation(), FlagCodes.simple, dirTarget, explorer);
        }
    }
    
    private RobotType getUnitToBuild() throws GameActionException {
        double rand = Math.random();
        if (rc.getRoundNum() <= 2) {
        	return RobotType.SLANDERER;
        } else if (rc.getInfluence() - 10 < Constants.minimumPolInf) {
            return RobotType.MUCKRAKER;
        } else if (rc.getEmpowerFactor(allyTeam, 11) > 4
        		|| crowdedByEnemy(rc.getLocation()) || crowded(rc.getLocation())) {
            return RobotType.POLITICIAN;
        } else if (rand > (0.4 + 0.2 * rc.getRoundNum() / Constants.MAX_ROUNDS) || canSenseEnemy()) {
            return RobotType.MUCKRAKER;
        } else if (rand > 0.05 * (1 - (double) rc.getRoundNum() / Constants.MAX_ROUNDS)
        		|| rc.getEmpowerFactor(enemyTeam, 0) > 1.1) {
            return RobotType.POLITICIAN;
        } else {
            return RobotType.SLANDERER;
        }
    }

    int getNewUnitInfluence() throws GameActionException {
        switch (unitToBuild) {
            case SLANDERER:
                Integer x = Constants.optimalSlandInfSet.floor(rc.getInfluence() - 10);
                return x != null ? x : 0;
            case POLITICIAN:
                return (rc.getEmpowerFactor(allyTeam, 11)) > 4 ? (rc.getInfluence() - 10) / 2
                        : Math.min(511, Math.max(Constants.minimumPolInf, (rc.getInfluence() - 10) / 2));
            case MUCKRAKER:
                return 1;
            default:
                return 1;
        }
    }

    Direction getPreferredDirection() throws GameActionException {
        return DirectionUtils.randomDirection();
    }

    private Direction getBuildDirection(RobotType type, Direction prefDir, int inf) throws GameActionException {
        if (rc.getInfluence() < inf || prefDir.equals(Direction.CENTER)) {
            return Direction.CENTER;
        }
        Direction[] dirs = { prefDir, prefDir.rotateRight(), prefDir.rotateLeft(),
                DirectionUtils.rotateRight90(prefDir), DirectionUtils.rotateLeft90(prefDir),
                prefDir.opposite().rotateLeft(), prefDir.opposite().rotateRight(), prefDir.opposite() };
        for (Direction dir : dirs) {
            if (rc.canBuildRobot(type, dir, inf)) {
                return dir;
            }
        }
        return Direction.CENTER;
    }

    private boolean buildableDir(Direction dir) throws GameActionException {
        return !dir.equals(Direction.CENTER);
    }

    private void addAllFriendlyBots(RobotInfo[] bots) throws GameActionException {
        for (RobotInfo bot : bots) {
            if (bot.getTeam().equals(allyTeam)) {
                units.add(bot.getID());
            }
        }
    }

    private boolean shouldBid() {
        return rc.getTeamVotes() < Constants.VOTES_TO_WIN && rc.getRoundNum() >= Bidding.START_BIDDING_ROUND;
    }

    private boolean tryBid(int bidAmount) throws GameActionException {
        if (rc.canBid(bidAmount)) {
            rc.bid(bidAmount);
            return true;
        }
        return false;
    }


    private class Bidding {
        private static final int MAX_ROUNDS = GameConstants.GAME_MAX_NUMBER_OF_ROUNDS;
        private static final int VOTES_TO_WIN = MAX_ROUNDS / 2 + 1;
        private static final int START_BIDDING_ROUND = MAX_ROUNDS / 8;
        private int prevBidBase = 2, prevTeamVotes = -1;
        private double linGrow = 0.002, multGrow = .02, multDecay = .01;
        private int decayAccum = 0, growAccum = 0;

        private int getBidBaseAmount() {
            if (rc.getTeamVotes() >= VOTES_TO_WIN)
                return 0; // won bidding

            int bidBase = prevBidBase, curTeamVotes = rc.getTeamVotes();
            if (prevTeamVotes != -1 && bidLastRound) {
                if (curTeamVotes > prevTeamVotes) {
                    growAccum = 0;
                    decayAccum += (int) Math.ceil(multDecay * bidBase);
                    bidBase -= decayAccum;
                } else {
                    decayAccum = 0;
                    bidBase += (int) (linGrow * rc.getInfluence());
                    growAccum += (int) Math.ceil(multGrow * bidBase);
                    bidBase += growAccum;
                }
            }
            bidBase = Math.max(bidBase + (bidBase % 2), 2);
            prevBidBase = bidBase;
            prevTeamVotes = curTeamVotes;
            return bidBase;
        }

        private double getBidMultiplier() {
            final int lowerVote = Math.max(VOTES_TO_WIN - MAX_ROUNDS + rc.getRoundNum(), 0);
            final int upperVote = Math.min(rc.getRoundNum(), VOTES_TO_WIN);
            if (rc.getTeamVotes() < lowerVote || rc.getTeamVotes() > upperVote) {
                System.out.println("Error, vote count out of expected bounds.... ????");
                return 1;
            }
            return ((1 + .5 * (.05 * (Math.log(rc.getTeamVotes() + 30 - lowerVote) / Math.log(1.5))))
                    / (1 + Math.exp(0.03 * (rc.getTeamVotes() - lowerVote)))) + 1 + (1 / (upperVote - lowerVote));
        }

    }

}
