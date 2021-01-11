package simpleplayer2;
import battlecode.common.*;

public class RunAway {
	static Direction runAwayDirection(RobotController rc, RobotInfo[] robots) {
		MapLocation location = new MapLocation(0,0);
		int numRobots = 0;
		for (RobotInfo robot : robots) {
			if (robot.team == rc.getTeam().opponent() && robot.type == RobotType.MUCKRAKER) {
				location = location.translate(robot.location.x, robot.location.y);
				numRobots++;
			}
		}
		MapLocation avgLocation = new MapLocation(location.x/numRobots, location.y/numRobots);
		return rc.getLocation().directionTo(avgLocation).opposite();
	}
}
