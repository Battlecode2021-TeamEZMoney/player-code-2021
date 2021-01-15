package simpleplayer3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import battlecode.common.*;
import common.*;

class EnlightenmentPlayer extends Robot{
    private int turnCount = 0;
    private int unitsBuilt = 0;
    private int unitsIndex = 0;
    private int spawnIndex = 0;
    private RobotType unitToBuild = RobotType.POLITICIAN;
    private Set<MapLocation> enemyHQs = new HashSet<MapLocation>();
    private Set<MapLocation> friendlyHQs = new HashSet<MapLocation>();
    private Set<MapLocation> neutralHQs = new HashSet<MapLocation>();
    private ArrayList<Integer> units = new ArrayList<Integer>();

    EnlightenmentPlayer(RobotController rcin){
        this.rc = rcin;
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
                    // TODO: Implement flag based orders
                    // trySetFlag(getOrdersForUnit(unitToBuild));
                }
            }

            if (shouldBid()) {
                tryBid(Bidding.bidAmount(rc));
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

    /*private void gatherIntel() {

    }*/

    private RobotType getUnitToBuild() {
        if (spawnIndex >= Constants.spawnOrder.length) {
            spawnIndex = 0;
        }
        if (rc.getInfluence() < Constants.minimumPolInf) {
            if (Constants.spawnOrder[spawnIndex].equals(RobotType.MUCKRAKER)) {
                spawnIndex++;
            }
            return RobotType.MUCKRAKER;
        } else if (rc.getInfluence() < Constants.optimalSlandInf[0]) {
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
                return Move.getTeamGoDir(rc);
            case POLITICIAN:
                return Move.getTeamGoDir(rc).opposite();
            case MUCKRAKER:
                return Move.getTeamGoDir(rc).opposite();
            default:
                return DirectionUtils.randomDirection();
        }
    }

    int getNewUnitInfluence() {
        switch (unitToBuild) {
            case SLANDERER:
                for (int i = 1; i < Constants.optimalSlandInf.length; i++) {
                    if (Constants.optimalSlandInf[i] > rc.getInfluence()) {
                        return Constants.optimalSlandInf[i - 1];
                    }
                }
                return Constants.optimalSlandInf[0];
            case POLITICIAN:
                return Math.min(100, Math.max(Constants.minimumPolInf, (int) rc.getInfluence() / 4));
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

    /*private int getOrdersForUnit(RobotType unit) {
        return 0; // TODO: Placeholder
    }*/

    private int getTarget() {
        if (enemyHQs.size() > 0) {
            return Encoding.encode((MapLocation) enemyHQs.toArray()[0], FlagCodes.enemyHQ);
        } else if (neutralHQs.size() > 0) {
            return Encoding.encode((MapLocation) neutralHQs.toArray()[0], FlagCodes.neutralHQ);
        } else if (rc.senseNearbyRobots(999, rc.getTeam().opponent()).length > 0) {
            return Encoding.encode(rc.getLocation(), FlagCodes.patrol);
        }
        return Encoding.encode(rc.getLocation(), FlagCodes.simple);
    }

    private boolean shouldBid() {
        return rc.getTeamVotes() < Constants.votesToWin
                && rc.getRoundNum() > (GameConstants.GAME_MAX_NUMBER_OF_ROUNDS / 6);
    }

    private int getBidAmount() {
        return (int) Math.max(4, rc.getInfluence() * .01); // TODO: Placeholder
    }

    private void tryBid(int bidAmount) throws GameActionException {
        if (rc.canBid(bidAmount)) {
            rc.bid(bidAmount);
        }
    }

    private void parseUnitFlag(int flag) {
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

}
