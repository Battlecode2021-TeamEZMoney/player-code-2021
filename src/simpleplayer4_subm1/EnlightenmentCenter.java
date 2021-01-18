package simpleplayer4_subm1;

import battlecode.common.*;

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
    private Set<MapLocation> enemyHQs = new HashSet<MapLocation>();
    private Set<MapLocation> friendlyHQs = new HashSet<MapLocation>();
    private Set<MapLocation> neutralHQs = new HashSet<MapLocation>();
    private ArrayList<Integer> units = new ArrayList<Integer>();
    private final Bidding bidController;

    EnlightenmentCenter(RobotController rcin) throws GameActionException {
        this.rc = rcin;
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
            
            //int start = Clock.getBytecodesLeft();
            
            if (rc.isReady()) {
                unitToBuild = getUnitToBuild();
                infToSpend = getNewUnitInfluence();
                dirTarget = getPreferredDirection();
                buildDirection = getBuildDirection(unitToBuild, dirTarget, infToSpend);
                explorer = Math.random() < 0.5 && unitToBuild == RobotType.MUCKRAKER;
                if (rc.canBuildRobot(unitToBuild, buildDirection, infToSpend)) {
                    rc.buildRobot(unitToBuild, buildDirection, infToSpend);
                    unitsBuilt++;
                    RobotInfo robotBuilt = rc.senseRobotAtLocation(rc.getLocation().add(buildDirection));
                    units.add(robotBuilt.getID());
                    //addAllFriendlyBots(rc.senseNearbyRobots(rc.getLocation().add(buildDirection), 0, rc.getTeam()));
                    
                    // TODO: Implement flag based orders
                    // trySetFlag(getOrdersForUnit(unitToBuild));
                }
            }
            
            trySetFlag(getTarget());

            if (shouldBid()) {
                tryBid(bidController.getBidAmount());
            }
            //System.out.println(Clock.getBytecodesLeft() - start);
            //System.out.println(rc.getEmpowerFactor(rc.getTeam(), 0));
            
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

    private int getTarget() throws GameActionException {
    	if (canSenseEnemy() && Math.random() < 0.8) {
            return Encoding.encode(rc.getLocation(), FlagCodes.patrol, dirTarget, explorer);
        } else if (neutralHQs.size() > 0) {
            return Encoding.encode(neutralHQs.iterator().next(), FlagCodes.neutralHQ, dirTarget, explorer);
        } else if (enemyHQs.size() > 0) {
            return Encoding.encode(enemyHQs.iterator().next(), FlagCodes.enemyHQ, dirTarget, explorer);
        } else {
        	return Encoding.encode(rc.getLocation(), FlagCodes.simple, dirTarget, explorer);
        }
    }

    private RobotType getUnitToBuild() throws GameActionException {
    	double rand = Math.random();
       	if ((crowdedByEnemy(rc.getLocation()) || rc.getEmpowerFactor(rc.getTeam(), 11) > 4) && rc.getInfluence()-10 >= Constants.minimumPolInf) {
    		return RobotType.POLITICIAN;
    	} else if (rand > 0.7 || rc.getInfluence()-10 < Constants.minimumPolInf) {
    		return RobotType.MUCKRAKER;
    	} else if (rand > 0 * (1 - rc.getRoundNum() / Constants.MAX_ROUNDS) || canSenseEnemy()){
    		return RobotType.POLITICIAN;
    	} else {
    		return RobotType.SLANDERER;
    	}
    }

    int getNewUnitInfluence() throws GameActionException {
        switch (unitToBuild) {
        case SLANDERER:
            Integer x = Constants.optimalSlandInfSet.floor(rc.getInfluence()-10);
            return x != null ? x : 0;
        case POLITICIAN:
            return (rc.getEmpowerFactor(rc.getTeam(), 11)) > 4 ? (rc.getInfluence()-10) / 2 : Math.min(300, Math.max(Constants.minimumPolInf, (rc.getInfluence()-10) / 4));
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
            if (bot.getTeam().equals(rc.getTeam())) {
                units.add(bot.getID());
            }
        }
    }

    /*
     * private int getOrdersForUnit(RobotType unit) { return 0; // TODO: Placeholder
     * }
     */

    private boolean shouldBid() throws GameActionException {
        return rc.getTeamVotes() < Constants.VOTES_TO_WIN
                && rc.getRoundNum() > (Constants.MAX_ROUNDS / 6);
    }

    private void tryBid(int bidAmount) throws GameActionException {
        if (rc.canBid(bidAmount)) {
            rc.bid(bidAmount);
        }
    }

    private class Bidding {
    	public static final int MAX_ROUNDS = Constants.MAX_ROUNDS;
        public static final int VOTES_TO_WIN = Constants.VOTES_TO_WIN, OFFSET = 100;
        private static final double GROWTH_RATE = 1.15, DECAY_RATE = 0.9;
        private int prevBid = 1, prevTeamVotes = -1;
        private double accum = 0;

        private int getBidAmount() throws GameActionException {
            if (rc.getTeamVotes() >= VOTES_TO_WIN)
                return 0; // won bidding

            int bid = 1, curTeamVotes = rc.getTeamVotes();
            double curVoteValue = voteValue();
            if (prevTeamVotes != -1) {
                if (curTeamVotes > prevTeamVotes) {
                    // won previous round
                    accum = DECAY_RATE * accum;
                    bid = (int) (DECAY_RATE * prevBid + accum);
                } else {
                    // lost previous round
                    accum = DECAY_RATE * accum + curVoteValue;
                    bid = (int) (GROWTH_RATE * prevBid + accum);
                }
            }
            bid = Math.max(bid, 1) + (int) (Math.random() * 2);
            bid = (int) Math.min(bid, rc.getInfluence() * (0.1 + 0.2 * rc.getRoundNum() / Constants.MAX_ROUNDS));
            prevBid = bid;
            prevTeamVotes = curTeamVotes;
            return bid;
        }

        private double logLin(int x) throws GameActionException {
            if (x <= 0)
                return 0;
            return x * Math.log((double) x);
        }

        public double voteValue() throws GameActionException {
            int votes = rc.getTeamVotes(), rounds = rc.getRoundNum() - 1;
            int votesMin = VOTES_TO_WIN - (MAX_ROUNDS - rounds);
            double lgProbDiff = (double) (MAX_ROUNDS - rounds - 1) * Math.log(2) + logLin(VOTES_TO_WIN - votes - 1)
                    + logLin((MAX_ROUNDS - rounds) - (VOTES_TO_WIN - votes)) - logLin(MAX_ROUNDS - rounds - 1);
            // lgProbDiff: 0 (highest value) - maxRounds * ln(2) (lowest value)

            return (MAX_ROUNDS * Math.log(2) - lgProbDiff) / (votes - votesMin + OFFSET);
            // returns: 0.6 (lowest value) - 20.7 (highest value)
        }
    }

}
