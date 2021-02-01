package final_usqualplayer3_subm;

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
    private int slandDistAway = 10;
    private int influence, maxInf;
    private int unitFunction;
    private int encoding = 0, nextEncoding = 0;
    private Map.Entry<MapLocation, Integer> minNeutral, minAllNeutral;
    private Map.Entry<MapLocation, Integer> minEnemy, minAllEnemy;
    private Set<MapLocation> friendlyHQs = new HashSet<MapLocation>();
    private Map<MapLocation, Integer> neutralHQs = new HashMap<MapLocation, Integer>();
    private Map<MapLocation, Integer> enemyHQs = new HashMap<MapLocation, Integer>();
    private Map<MapLocation, Integer> polSentID = new HashMap<MapLocation, Integer>();
    private ArrayList<Integer> units = new ArrayList<Integer>();
    private final Bidding bidController;
    private boolean bidLastRound;
    Queue<MapLocation> spawnLocs = new LinkedList<MapLocation>();

    EnlightenmentCenter(RobotController rcin) throws GameActionException {
        super(rcin);
        bidController = new Bidding();
    }

    void updateECs(int minBytecodeLeft) throws GameActionException {
        while (Clock.getBytecodesLeft() > minBytecodeLeft && !units.isEmpty()) {
            unitsIndex %= units.size();
            int unitID = units.get(unitsIndex);
            if (rc.canGetFlag(unitID)) {
                parseUnitFlag(rc.getFlag(unitID));
                unitsIndex++;
            } else {
                units.remove(unitsIndex);
            }
        }
    }

    void run() throws GameActionException {
        while (true) {
            turnCount++;
            // TODO: Implement smart handling of other units and other HQs
            // gatherIntel();

//             printStoredECs();

            encoding = nextEncoding;
            nextEncoding = 0;
            influence = 0;
            unitFunction = 0;
            updateECs(8000);

            // int start = Clock.getBytecodesLeft();
            minNeutral = minEntryHQ(neutralHQs);
            minEnemy = minEntryHQ(enemyHQs);
            minAllNeutral = entryWithMinVal(neutralHQs);
            minAllEnemy = entryWithMinVal(enemyHQs);
            maxInf = rc.getInfluence() - 30;
            slandCenter = slandCenter();

            if (rc.isReady() && !fullySurrounded()) {
                unitToBuild = getUnitToBuild();
                if (unitToBuild != RobotType.ENLIGHTENMENT_CENTER) {
                    // if (unitToBuild != RobotType.SLANDERER && rc.getInfluence() - 20 >
                    // Constants.minimumPolInf) unitToBuild = RobotType.POLITICIAN;
                    infToSpend = getNewUnitInfluence();
                    if (!unitToBuild.equals(RobotType.SLANDERER) && Constants.optimalSlandInfSet.contains(infToSpend)) {
                        // allows politicians to distinguish slanderers by their influence
                        infToSpend--;
                    }
                    if (unitToBuild == RobotType.POLITICIAN && !spawnLocs.isEmpty()) {
                        dirTarget = directionTo(spawnLocs.remove());
                    } else {
                        dirTarget = getPreferredDirection();
                    }
                    buildDirection = getBuildDirection(unitToBuild, dirTarget, infToSpend);
                    explorer = (unitToBuild == RobotType.MUCKRAKER && Math.random() < 0.5);
                    // || (unitToBuild == RobotType.POLITICIAN && Math.random() < 0.2);
                    if (rc.canBuildRobot(unitToBuild, buildDirection, infToSpend)) {
                        rc.buildRobot(unitToBuild, buildDirection, infToSpend);
                        unitsBuilt++;
                        RobotInfo robotBuilt = rc.senseRobotAtLocation(rc.getLocation().add(buildDirection));
                        int builtID = robotBuilt.getID();
                        units.add(builtID);
                        if (unitFunction == 4) {
                        	polSentID.put(minNeutral.getKey(), builtID);
                        } else if (unitFunction == 2) {
                        	//polSentID.put(minEnemy.getKey(), builtID);
                        }

                        // TODO: Implement flag based orders
                        // trySetFlag(getOrdersForUnit(unitToBuild));
                    }
                }
            }

            trySetFlag(getTarget());

            if (shouldBid()) {
                int bidAmount = (int) (bidController.getBidBaseAmount() * bidController.getBidMultiplier());
                bidLastRound = tryBid(Math.max(bidAmount - (bidAmount % 2), 2));
            } else {
                bidLastRound = false;
            }
            // int numBytecodes = Clock.getBytecodesLeft() - start;
            // if (numBytecodes < -8000) System.out.println(numBytecodes);
            // System.out.println(rc.getEmpowerFactor(allyTeam, 0));

            updateECs(1000);

            Clock.yield();
        }
    }
    
    private boolean fullySurrounded() throws GameActionException {
    	return rc.senseNearbyRobots(2).length == nbSquares();
    }
    
    private int nbSquares() throws GameActionException {
    	int nbs = 0;
    	for (Direction dir : DirectionUtils.nonCenterDirections) {
    		if (rc.onTheMap(rc.getLocation().add(dir))) {
    			nbs++;
    		}
    	}
    	return nbs;
    }
    
    Map.Entry<MapLocation, Integer> minEntryHQ(Map<MapLocation, Integer> HQs) {
    	Map<MapLocation, Integer> HQsToSpawn = new HashMap<MapLocation, Integer>();
    	for (MapLocation HQ : HQs.keySet()) {
    		if (!polSentID.containsKey(HQ) || !rc.canGetFlag(polSentID.get(HQ))) {
    			HQsToSpawn.put(HQ, HQs.get(HQ));
    		}
    	}
    	return entryWithMinVal(HQsToSpawn);
    }
    
    private Map.Entry<MapLocation, Integer> entryWithMinVal(Map<MapLocation, Integer> HQs) {
        if (HQs.isEmpty()) {
            return null;
        }
        return Collections.min(HQs.entrySet(), Map.Entry.comparingByValue());
    }

    private void parseUnitFlag(int flag) throws GameActionException {
        MapLocation tempLocation = Encoding.getLocationFromFlag(rc, flag);
        switch (Encoding.getTypeFromFlag(flag)) {
            case 2:
                enemyHQs.put(tempLocation, Encoding.getConvFromFlag(flag));
                friendlyHQs.remove(tempLocation);
                neutralHQs.remove(tempLocation);
                break;
            case 3:
                enemyHQs.remove(tempLocation);
                friendlyHQs.add(tempLocation);
                neutralHQs.remove(tempLocation);
                break;
            case 4:
                neutralHQs.put(tempLocation, Encoding.getConvFromFlag(flag));
                break;
            case 7:
            	System.out.println("EMPOWERING " + printLoc(tempLocation));
                spawnLocs.add(tempLocation);
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
//        return rc.getLocation();
         if (enemyHQs.size() == 0) {
        	 return rc.getLocation();
        	 //return Constants.origin;
         }
         MapLocation avgEnemyHQ = avgLoc(enemyHQs.keySet());
         Direction awayFromEnemyHQs = rc.getLocation().directionTo(avgEnemyHQ).opposite();
         MapLocation center = rc.getLocation().translate(slandDistAway * awayFromEnemyHQs.dx, slandDistAway * awayFromEnemyHQs.dy);
         return center;
    }

    private RobotType getUnitToBuild() throws GameActionException {
        double rand = Math.random();
        //int polsEmpowering = spawnLocs.isEmpty() ? 0 : 1;
        if (rc.getRoundNum() <= 2) {
            return RobotType.SLANDERER;
        }
//         else if (rand > Math.min(0.4, 0.2 + 0.2 * rc.getRoundNum() / 100)) {
//         return RobotType.POLITICIAN;
//         } else if (rand > 0) {
//         return RobotType.SLANDERER;
//         }
        else if (maxInf < Constants.minimumAttackPolInf) {
            return RobotType.MUCKRAKER;
        } else if (rc.getEmpowerFactor(allyTeam, 11) > 1.5 || crowdedByEnemy(rc.getLocation())) {
            return RobotType.POLITICIAN;
        }
//        else if (slandCenter != Constants.origin
//        		&& rand > Math.min(0.9, 0.9 + 0. * turnCount / 50) - 0.3 * polsEmpowering ) {
        else if (canSenseEnemyPolitician()) {
            return RobotType.MUCKRAKER;
        } else if ((slandCenter != Constants.origin && rand > 0.85) || !spawnLocs.isEmpty()) {
        	nextEncoding = Encoding.encode(slandCenter, FlagCodes.slandCenter, false);
            influence = rc.getInfluence() > 100 ? Constants.minimumPolInf * 2 : Constants.minimumPolInf;
            return RobotType.POLITICIAN;
        }
        else if (rand > 0.7 && ( (minNeutral != null && maxInf * rc.getEmpowerFactor(allyTeam, 20) >= minNeutral.getValue())
                || (minEnemy != null && maxInf * rc.getEmpowerFactor(allyTeam, 20) >= minEnemy.getValue() ) )) {
            //System.out.println("I AM SENT");
            return RobotType.POLITICIAN;
        } else if (rand > (0.4 + 0.2 * rc.getRoundNum() / Constants.MAX_ROUNDS)) {
        	if (Math.random() < 0.1) {
        		influence = maxInf / 4;
        	}
            return RobotType.MUCKRAKER;
        } else if (rand < Math.max(0.4, 0.5 - 0.1 * turnCount / 100) && !canSenseEnemyMuckraker()
                && rc.getEmpowerFactor(enemyTeam, 0) < 1.1) {
        	return RobotType.SLANDERER;
        } else {
            return RobotType.ENLIGHTENMENT_CENTER; // build no robot this round
        }
    }

    int getNewUnitInfluence() throws GameActionException {
        if (influence != 0) {
            return influence;
        }
        switch (unitToBuild) {
            case SLANDERER:
                Integer maxOptimalSlandInf = Constants.optimalSlandInfSet.floor(maxInf);
                if (slandCenter != Constants.origin) {
                    nextEncoding = Encoding.encode(slandCenter, FlagCodes.slandCenter, explorer);
                }
                return maxOptimalSlandInf != null ? maxOptimalSlandInf : 0;
            case POLITICIAN:
                if (rc.getEmpowerFactor(allyTeam, 11) > 1.5) {
                    // System.out.println(".a");
                	return Math.max(Constants.minimumAttackPolInf, maxInf / 2);
                } else if (minNeutral != null && maxInf * rc.getEmpowerFactor(allyTeam, 20) >= minNeutral.getValue()) {
                    // System.out.println(".b");
                    unitFunction = 4;
                    nextEncoding = Encoding.encode(minNeutral.getKey(), FlagCodes.neutralHQ, false);
                    return minNeutral.getValue();
                } else if (minEnemy != null && maxInf * rc.getEmpowerFactor(allyTeam, 20) >= minEnemy.getValue()) {
                    // System.out.println(".c");
                    unitFunction = 2;
                    nextEncoding = Encoding.encode(minEnemy.getKey(), FlagCodes.enemyHQ, false);
                    return (minEnemy.getValue() + 100 < maxInf) ? minEnemy.getValue() + 100: minEnemy.getValue();
                } else if (Math.random() < 1 && slandCenter != Constants.origin) {
                    // System.out.println(".b");
                    nextEncoding = Encoding.encode(slandCenter, FlagCodes.slandCenter, false);
                    return Constants.minimumPolInf;
                } else {
                    // System.out.println(".d");
                    return Math.max(Constants.minimumAttackPolInf, maxInf / 2);
                }
            case MUCKRAKER:
            		return 1;
            default:
                return 0;
        }
    }

    private int getTarget() throws GameActionException {
        if (encoding != 0) {
            return encoding;
        }
        
        if (canSenseEnemy() && Math.random() < 0.8) {
            return Encoding.encode(rc.getLocation(), FlagCodes.patrol, explorer);
        } else if (!neutralHQs.isEmpty()
                && ((unitToBuild.equals(RobotType.POLITICIAN) && Math.random() < 0.5) || Math.random() < 0.2)) {
            return Encoding.encode(minAllNeutral.getKey(), FlagCodes.neutralHQ, explorer);
        } else if (!enemyHQs.isEmpty()) {
            if (Math.random() < 0.2 && slandCenter != Constants.origin) {
                return Encoding.encode(slandCenter, FlagCodes.slandCenter, explorer);
            }
            return Encoding.encode(minAllEnemy.getKey(), FlagCodes.enemyHQ, explorer);
        } else {
            return Encoding.encode(rc.getLocation(), FlagCodes.simple, explorer);
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
                // System.out.println("Error, vote count out of expected bounds.... ????");
                // TODO: Not necessarily ^, the opponent often does not get all the votes we
                // didn't,
                // so we potentially could still win more votes with less than 751 total.
                return 1;
            }
            return ((1 + .5 * (.05 * (Math.log(rc.getTeamVotes() + 30 - lowerVote) / Math.log(1.5))))
                    / (1 + Math.exp(0.03 * (rc.getTeamVotes() - lowerVote)))) + 1 + (1 / (upperVote - lowerVote));
        }

    }

    void printStoredECs() throws GameActionException {
        System.out.println();
        for (Map.Entry<MapLocation, Integer> entry : enemyHQs.entrySet())
            System.out.println("Enemy EC: " + printLoc(entry.getKey()) + " with conv to spawn " + entry.getValue());
        System.out.println();
        for (MapLocation loc : friendlyHQs)
            System.out.println("Friendly EC: " + printLoc(loc));
        System.out.println();
        for (Map.Entry<MapLocation, Integer> entry : neutralHQs.entrySet())
            System.out.println("Neutral EC: " + printLoc(entry.getKey()) + " with conv to spawn " + entry.getValue());
        System.out.println();
        for (Integer unit : units)
            System.out.println("Unit ID: " + unit);
        System.out.println();
    }

}
