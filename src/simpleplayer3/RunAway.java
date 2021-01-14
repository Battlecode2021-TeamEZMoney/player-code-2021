package simpleplayer3;

import java.util.ArrayList;

import battlecode.common.*;

class RunAway {
	static Direction runAwayDirection(RobotController rc, ArrayList<RobotInfo> robots) {
		MapLocation location = new MapLocation(0, 0);
		for (RobotInfo robot : robots) {
			location = location.translate(robot.location.x, robot.location.y);
		}
		MapLocation avgLocation = new MapLocation(location.x / robots.size(), location.y / robots.size());
		return rc.getLocation().directionTo(avgLocation).opposite();
	}
}
