package sprintplayer1;

import battlecode.common.*;

public abstract class Attacker extends Pawn {
    protected boolean withinAttackRange(RobotInfo enemy) {
        return withinAttackRange(enemy.getLocation());
    }

    protected boolean withinAttackRange(MapLocation enemyLocation) {
        return enemyLocation.distanceSquaredTo(rc.getLocation()) < rc.getType().actionRadiusSquared;
    }

    protected abstract boolean huntOrKill(RobotInfo enemy) throws GameActionException;
}
