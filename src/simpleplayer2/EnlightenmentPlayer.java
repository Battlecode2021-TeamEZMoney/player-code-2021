package simpleplayer2;

import java.util.ArrayList;

import battlecode.common.*;
import common.Constants;
import common.DirectionUtils;

public class EnlightenmentPlayer {
    private static RobotController rc;
    private static int turnCount = 0;
    private static int unitCount = 0;
    private static int lastVoteCount = 0;
    private static ArrayList<HQData> enemyHQs = new ArrayList<HQData>();
    private static ArrayList<HQData> friendlyHQs = new ArrayList<HQData>();
    private static ArrayList<HQData> neutralHQs = new ArrayList<HQData>();
    private static ArrayList<Integer> units = new ArrayList<Integer>();
    static void runEnlightenmentCenter(RobotController rc) throws GameActionException {
        EnlightenmentPlayer.rc = rc;
        while (true){
            turnCount++;
            checkHQs();
            gatherIntel();
            
            RobotType unitToBuild = getUnitType();
            int infToSpend = getNewUnitInfluence();
            Direction buildDirection = getBuildDirection(unitToBuild, getPreferredDirection(), infToSpend);
            if (canBuild(buildDirection)){
                rc.buildRobot(unitToBuild, buildDirection, infToSpend);
                addAllFriendlyBots(rc.senseNearbyRobots(rc.getLocation().add(buildDirection), 0, rc.getTeam()));
                trySetFlag(getOrdersForUnit(unitToBuild));
            }
            
            
            if (shouldBid()) {
                tryBid(getBidAmount());
            }

            lastVoteCount = rc.getTeamVotes();
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

        for(HQData hq : neutralHQs){
            //TODO: Handle Neutral HQs
        }
    }

    private static void gatherIntel(){

    }


    private static RobotType getUnitType(){
        return RobotType.POLITICIAN; //TODO: Placeholder
    }

    static Direction getPreferredDirection(){
        return Direction.NORTH; //TODO: Placeholder
    }

    static int getNewUnitInfluence(){
        return 0; //TODO: Placeholder
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

    private static boolean canBuild(Direction dir){
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
        return rc.getTeamVotes() < Constants.votesToWin;
    }
    
    private static int getBidAmount(){
        return 0; //TODO: Placeholder
    }

    private static void tryBid(int bidAmount) throws GameActionException{
        if (rc.canBid(bidAmount)){
            rc.bid(bidAmount);
        }
    }
    
}
