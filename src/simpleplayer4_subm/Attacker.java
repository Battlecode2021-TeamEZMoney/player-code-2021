package simpleplayer4_subm;

import battlecode.common.*;

public abstract class Attacker extends Pawn {
    protected boolean withinAttackRange(RobotInfo enemy) throws GameActionException {
        return withinAttackRange(enemy.getLocation());
    }

    protected boolean withinAttackRange(MapLocation enemyLocation) throws GameActionException {
        return rc.getLocation().distanceSquaredTo(enemyLocation) <= rc.getType().actionRadiusSquared;
    }

    protected abstract boolean huntOrKill(RobotInfo enemy) throws GameActionException;
}
