package simpleplayer5;

import battlecode.common.*;

public abstract class Attacker extends Pawn {
    protected MapLocation enemyHQ;
    protected MapLocation neutralTarget;
    
    Attacker(RobotController rcin) throws GameActionException {
        super(rcin); // Don't remove this.
    }
    
    protected boolean withinAttackRange(RobotInfo enemy) throws GameActionException {
        return withinAttackRange(enemy.getLocation());
    }

    protected boolean withinAttackRange(MapLocation enemyLocation) throws GameActionException {
        return rc.getLocation().distanceSquaredTo(enemyLocation) <= actionRadiusSquared;
    }

    protected abstract boolean huntOrKill(RobotInfo enemy) throws GameActionException;
}
