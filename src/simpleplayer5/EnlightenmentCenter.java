package simpleplayer5;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import battlecode.common.*;
import common.*;

class EnlightenmentCenter extends Robot {
    private int turnCount = 0;
    private int unitsBuilt = 0;
    private int unitsIndex = 0;
    private int spawnIndex = 0;
    private RobotType unitToBuild = RobotType.POLITICIAN;
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

    void run() throws GameActionException {
        while (true) {
            turnCount++;
            // TODO: Implement smart handling of other units and other HQs
            // gatherIntel();

            if (rc.isReady()) {
                unitToBuild = getUnitToBuild();
                int infToSpend = getNewUnitInfluence();
                Direction buildDirection = getBuildDirection(unitToBuild, getPreferredDirection(), infToSpend);
                if (buildableDir(buildDirection)) {
                    rc.buildRobot(unitToBuild, buildDirection, infToSpend);
                    unitsBuilt++;
                    addAllFriendlyBots(rc.senseNearbyRobots(rc.getLocation().add(buildDirection), 0, rc.getTeam()));
                }
            }

            if (shouldBid()) {
                int bidAmount = (int) (bidController.getBidBaseAmount() * bidController.getBidMultiplier());
                bidLastRound = tryBid(Math.max(bidAmount - (bidAmount % 2), 2));
                System.out.println(Math.max(bidAmount - (bidAmount % 2), 2));
            } else {
                bidLastRound = false;
            }

            while (Clock.getBytecodesLeft() > 1000 && units.size() > 0) {
                int unitID = units.get(unitsIndex);
                if (rc.canGetFlag(unitID)) {
                    parseUnitFlag(rc.getFlag(unitID));
                    if (++unitsIndex >= units.size()) {
                        unitsIndex = 0;
                    }
                } else {
                    units.remove(unitsIndex);
                }
                if (unitsIndex >= units.size()) {
                    unitsIndex = 0;
                }
            }
            trySetFlag(getTarget());
            Clock.yield();
        }
    }

    /*
     * private void gatherIntel() {
     * 
     * }
     */

    private RobotType getUnitToBuild() {
        if (spawnIndex >= Constants.spawnOrder.length) {
            spawnIndex = 0;
        }
        if (rc.getInfluence() < Constants.minimumPolInf) {
            if (Constants.spawnOrder[spawnIndex].equals(RobotType.MUCKRAKER)) {
                spawnIndex++;
            }
            return RobotType.MUCKRAKER;
        } else if (rc.getInfluence() < Constants.optimalSlandInfArray[0]) {
            if (Constants.spawnOrder[spawnIndex].equals(RobotType.POLITICIAN)) {
                spawnIndex++;
            }
            return RobotType.POLITICIAN;
        } else if (rc.getRoundNum() <= 2) {
            return RobotType.SLANDERER;
        } else {
            return Constants.spawnOrder[spawnIndex++];
        }
    }

    Direction getPreferredDirection() {
        switch (unitToBuild) {
            case SLANDERER:
                return getTeamGoDir();
            case POLITICIAN:
            case MUCKRAKER:
                return getTeamGoDir().opposite();
            default:
                return DirectionUtils.randomDirection();
        }
    }

    int getNewUnitInfluence() {
        switch (unitToBuild) {
            case SLANDERER:
                for (int i = 1; i < Constants.optimalSlandInfArray.length; i++) {
                    if (Constants.optimalSlandInfArray[i] > rc.getInfluence()) {
                        return Constants.optimalSlandInfArray[i - 1];
                    }
                }
                return Constants.optimalSlandInfArray[0];
            case POLITICIAN:
                return Math.max(Constants.minimumPolInf, (int) rc.getInfluence() / 200);
            case MUCKRAKER:
                return 1;
            default:
                return 1;
        }
    }

    private Direction getBuildDirection(RobotType type, Direction prefDir, int inf) {
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

    private boolean buildableDir(Direction dir) {
        return !dir.equals(Direction.CENTER);
    }

    private void addAllFriendlyBots(RobotInfo[] bots) {
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

    private int getTarget() throws GameActionException {
        if (enemyHQs.size() > 0) {
            return Encoding.encode((MapLocation) enemyHQs.toArray()[0], FlagCodes.enemyHQ);
        } else if (neutralHQs.size() > 0) {
            return Encoding.encode((MapLocation) neutralHQs.toArray()[0], FlagCodes.neutralHQ);
        } else if (rc.senseNearbyRobots(999, rc.getTeam().opponent()).length > 0) {
            return Encoding.encode(rc.getLocation(), FlagCodes.patrol);
        }
        return Encoding.encode(rc.getLocation(), FlagCodes.simple);
    }

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
