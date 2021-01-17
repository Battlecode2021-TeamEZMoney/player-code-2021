package simpleplayer4_alt2;

import battlecode.common.*;
//import java.util.*;

class Slanderer extends Pawn {
    Politician successor;

    Slanderer(RobotController rcin) throws GameActionException {
        this.rc = rcin;
        getHomeHQ();
    }

    void run() throws GameActionException {
        while (rc.getType().equals(RobotType.SLANDERER)) {
            turnCount++;
            if (rc.isReady()) {
                // Handle detecting enemies
            	tryDirForward180(awayFromEnemyMuckrakers());
            }
            Clock.yield();
        }
        if (successor == null) {
            successor = new Politician(this);
        }
        successor.run();

    }
}
