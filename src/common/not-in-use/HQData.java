

import battlecode.common.*;
import common.Constants;

public class HQData {
    private int id = -1;
    private final MapLocation pos;
    private Team team = null;

    HQData(MapLocation pos){
        this.pos = pos;
    }

    HQData(MapLocation pos, Team team){
        this.pos = pos;
        this.team = team;
    }

    HQData(int id, MapLocation pos){
        this.id = id;
        this.pos = pos;
    }

    HQData(int id, MapLocation pos, Team team){
        this.id = id;
        this.pos = pos;
        this.team = team;
    }

    public int getID(){
        return id;
    }

    public void setID(int id){
        this.id = id;
    }

    public boolean hasID(){
        return id > Constants.minID;
    }

    public boolean isIDCurrent(RobotController rc){
        if(rc.canGetFlag(id)){
            return true;
        } else {
            id = -1;
            team = null;
            return false;
        }
    }

    public int getFlag(RobotController rc) throws GameActionException{
        return rc.getFlag(id);
    }

    public MapLocation getLocation(){
        return pos;
    }

    public Team getTeam(){
        return team;
    }

    public void setTeam(Team team){
        this.team = team;
    }

    public boolean hasTeam(){
        return team != null;
    }

    public boolean isNeutral(){
        return team.equals(Team.NEUTRAL);
    }

    public boolean isFriendly(RobotController rc){
        return team.equals(rc.getTeam());
    }

    public boolean isEnemy(RobotController rc){
        return team.equals(rc.getTeam().opponent());
    }

}
