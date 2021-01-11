package simpleplayer2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import battlecode.common.*;
import common.*;

public class EnlightenmentPlayer {
    private static RobotController rc;
    private static int turnCount = 0;
    private static int unitsBuilt = 0;
    private static int lastVoteCount = 0;
    private static int unitsIndex = 0;
    private static int spawnIndex = 0;
    private static RobotType unitToBuild = RobotType.POLITICIAN;
    private static Set<MapLocation> enemyHQs = new HashSet<MapLocation>();
    private static Set<MapLocation> friendlyHQs = new HashSet<MapLocation>();
    private static Set<MapLocation> neutralHQs = new HashSet<MapLocation>();
    private static ArrayList<Integer> units = new ArrayList<Integer>();
    static void runEnlightenmentCenter(RobotController rcin) throws GameActionException {
        EnlightenmentPlayer.rc = rcin;
        while (true){
            turnCount++;
            //TODO: Implement smart handling of other units and other HQ
            //gatherIntel();
            
            if(rc.isReady()){
                unitToBuild = getUnitToBuild();
                int infToSpend = getNewUnitInfluence();
                Direction buildDirection = getBuildDirection(unitToBuild, getPreferredDirection(), infToSpend);
                if (buildableDir(buildDirection)){
                    rc.buildRobot(unitToBuild, buildDirection, infToSpend);
                    unitsBuilt++;
                    addAllFriendlyBots(rc.senseNearbyRobots(rc.getLocation().add(buildDirection), 0, rc.getTeam()));
                    //TODO: Implement flag based orders
                    //trySetFlag(getOrdersForUnit(unitToBuild));
                }
            }
            
            int bidAmount = Bidding.bidAmount(rc);
            if (rc.canBid(bidAmount)) rc.bid(bidAmount);

            lastVoteCount = rc.getTeamVotes();

            while(Clock.getBytecodesLeft() > 500){
                int unitID = units.get(unitsIndex);
                if(rc.canGetFlag(unitID)){
                    parseUnitFlag(rc.getFlag(unitID));
                    if(++unitsIndex >= units.size()){
                        unitsIndex = 0;
                    }
                } else {
                    units.remove(units.get(unitID));
                }
                if(unitsIndex >= units.size()){
                    unitsIndex = 0;
                }
            }
            Encoding.trySetFlag(rc, getTarget());
            Clock.yield();
        }
    }


    private static void gatherIntel(){

    }

    private static RobotType getUnitToBuild(){
        if (rc.getInfluence() < Constants.minimumPolInf){
            if(Constants.spawnOrder[spawnIndex].equals(RobotType.MUCKRAKER)){
                spawnIndex++;
            }
            return RobotType.MUCKRAKER;
        } else if (rc.getInfluence() < Constants.optimalSlandInf[0]){
            if(Constants.spawnOrder[spawnIndex].equals(RobotType.POLITICIAN)){
                spawnIndex++;
            }
            return RobotType.POLITICIAN;
        } else if (rc.getRoundNum() <= 2) {
            return RobotType.SLANDERER;
        } else {
            if(spawnIndex >= Constants.spawnOrder.length){
                spawnIndex = 0;
            }
            return Constants.spawnOrder[spawnIndex++];
        }
    }

    static Direction getPreferredDirection(){
        switch(unitToBuild){
            case SLANDERER: return Move.getTeamGoDir(rc);
            case POLITICIAN: return Move.getTeamGoDir(rc).opposite();
            case MUCKRAKER: return Move.getTeamGoDir(rc).opposite();
            default: return DirectionUtils.randomDirection();
        }
    }

    static int getNewUnitInfluence(){
        switch(unitToBuild){
            case SLANDERER: 
            for(int i = 1; i < Constants.optimalSlandInf.length; i++){
                if(Constants.optimalSlandInf[i] > rc.getInfluence()){
                    return Constants.optimalSlandInf[i-1];
                }
            } 
            return Constants.optimalSlandInf[0];
            case POLITICIAN: return Math.min(100, Math.max(Constants.minimumPolInf, (int) rc.getInfluence()/4));
            case MUCKRAKER: return 1;
            default: return 1;
        }
    }

    private static Direction getBuildDirection(RobotType type, Direction prefDir, int inf){
        if(rc.getInfluence() < inf || prefDir.equals(Direction.CENTER)){
            return Direction.CENTER;
        }
        Direction[] dirs = {prefDir, prefDir.rotateRight(), prefDir.rotateLeft(), DirectionUtils.rotateRight90(prefDir), DirectionUtils.rotateLeft90(prefDir), prefDir.opposite().rotateLeft(), prefDir.opposite().rotateRight(), prefDir.opposite()};
        for (Direction dir : dirs) {
            if (rc.canBuildRobot(type, dir, inf)){
                return dir;
            }
        }
        return Direction.CENTER;
    }

    private static boolean buildableDir(Direction dir){
        return !dir.equals(Direction.CENTER);
    }

    private static void addAllFriendlyBots(RobotInfo[] bots){
        for (RobotInfo bot: bots){
            if(bot.getTeam().equals(rc.getTeam())){
                units.add(bot.getID());
            }
        }
    }

    private static int getOrdersForUnit(RobotType unit){
        return 0; //TODO: Placeholder
    }

    private static int getTarget(){
        if(enemyHQs.size() > 0){
            return Encoding.encode((MapLocation) enemyHQs.toArray()[0], Codes.enemyHQ);
        } else if (neutralHQs.size() > 0) {
            return Encoding.encode((MapLocation) neutralHQs.toArray()[0], Codes.neutralHQ);
        } else if (rc.senseNearbyRobots(999, rc.getTeam().opponent()).length > 1){
            return Encoding.encode(rc.getLocation(), Codes.patrol);
        }
        return Encoding.encode(rc.getLocation(), Codes.simple);
    }

    private static boolean shouldBid(){
        return rc.getTeamVotes() < Constants.votesToWin && rc.getRoundNum() > 500;
    }
    
    private static int getBidAmount(){
        return (int) Math.max(4, rc.getInfluence()*.01); //TODO: Placeholder
    }

    private static void tryBid(int bidAmount) throws GameActionException{
        if (rc.canBid(bidAmount)){
            rc.bid(bidAmount);
        }
    }

    private static void parseUnitFlag(int flag){
        MapLocation tempLocation = Encoding.decodeLocation(rc, flag);
        switch(Encoding.decodeInfo(flag)){
            case 2:enemyHQs.add(tempLocation); 
                   friendlyHQs.remove(tempLocation);
                   neutralHQs.remove(tempLocation);
                   return;
            case 3:enemyHQs.remove(tempLocation); 
                   friendlyHQs.add(tempLocation);
                   neutralHQs.remove(tempLocation);
                   return;
            case 4:neutralHQs.add(tempLocation);
                   return;
        }
    }
    
}
