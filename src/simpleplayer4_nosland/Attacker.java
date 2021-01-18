package simpleplayer4_nosland;

import battlecode.common.*;

public abstract class Attacker extends Pawn {
    Attacker(RobotController rcin){
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
