package simpleplayer3;

import java.util.ArrayList;

import battlecode.common.*;

public class EnlightenmentPlayer {
    private static RobotController rc;
    private static int turnCount = 0;
    private static int unitsBuilt = 0;
    private static int lastVoteCount = 0;
    private static int unitsIndex = 0;
    private static int spawnIndex = 0;
    private static RobotType unitToBuild = RobotType.POLITICIAN;
    private static ArrayList<HQData> enemyHQs = new ArrayList<HQData>();
    private static ArrayList<HQData> friendlyHQs = new ArrayList<HQData>();
    //private static ArrayList<HQData> neutralHQs = new ArrayList<HQData>();
    private static ArrayList<Integer> units = new ArrayList<Integer>();
    static void runEnlightenmentCenter(RobotController rcin) throws GameActionException {
        EnlightenmentPlayer.rc = rcin;
        while (true){
            turnCount++;
            //TODO: Implement smart handling of other units and other HQ
            //checkHQs();
            //gatherIntel();
            
            if(rc.isReady()){
                unitToBuild = getUnitToBuild();
                int infToSpend = getNewUnitInfluence();
                Direction buildDirection = getBuildDirection(unitToBuild, getPreferredDirection(), infToSpend);
                if (buildableDir(buildDirection)){
                    rc.buildRobot(unitToBuild, buildDirection, infToSpend);
                    unitsBuilt++;
                    //TODO: Implement uses for friendly bot tracking
                    //addAllFriendlyBots(rc.senseNearbyRobots(rc.getLocation().add(buildDirection), 0, rc.getTeam()));
                    //TODO: Implement flag based orders
                    //trySetFlag(getOrdersForUnit(unitToBuild));
                }
            }
            
            if (shouldBid()) {
                tryBid(getBidAmount());
            }

            lastVoteCount = rc.getTeamVotes();

            //TODO: Implement flag handling
            /*while(Clock.getBytecodesLeft() > 500){
                int unitID = units.get(unitsIndex);
                if(rc.canGetFlag(unitID)){
                    parseFlag(rc.getFlag(unitID));
                } else {
                    units.remove(units.get(unitID));
                }
                if(++unitsIndex >= units.size()){
                    unitsIndex = 0;
                }
            }*/

            Clock.yield();
        }
    }

    private static boolean IDOutdated(HQData hq, ArrayList<HQData> moveTo){
        if(hq.hasID() && !hq.isIDCurrent(rc)){
            if(hq.hasTeam()){
                hq.setTeam(newTeam(hq.getTeam()));
            }
            moveTo.add(hq);
            return true;
        }
        return false;
    }

    private static Team newTeam(Team team){
        switch(team){
            case A: return Team.B;
            case B: return Team.A;
            default: return null;
        }
    }

    private static void checkHQs(){
        enemyHQs.removeIf(hq -> IDOutdated(hq, friendlyHQs));

        friendlyHQs.removeIf(hq -> IDOutdated(hq, enemyHQs));

        //TODO: Handle Neutral HQs
        //for(HQData hq : neutralHQs){}
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

    private static boolean trySetFlag(int newFlag) throws GameActionException{
        if (rc.canSetFlag(newFlag)){
            rc.setFlag(newFlag);
            return true;
        } else if (rc.canSetFlag(0)){
            rc.setFlag(0);
        } 
        return false;
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

    private static void parseFlag(int flag){
        //TODO: Flag parsing
    }
    
}
