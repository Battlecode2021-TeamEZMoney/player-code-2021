package usqualplayer1_alt;

import battlecode.common.*;

public abstract class Attacker extends Pawn {
    Attacker(RobotController rcin) throws GameActionException {
        super(rcin);
    }

    protected boolean withinAttackRange(RobotInfo enemy) throws GameActionException {
        return withinAttackRange(enemy.getLocation());
    }

    protected boolean withinAttackRange(MapLocation enemyLocation) throws GameActionException {
        return rc.getLocation().distanceSquaredTo(enemyLocation) <= actionRadiusSquared;
    }
}
