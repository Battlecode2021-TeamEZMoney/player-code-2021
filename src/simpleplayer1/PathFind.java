package simpleplayer1;
import java.util.*;

import battlecode.common.*;
import common.*;

public class PathFind {
    static void path_to(RobotController rc, MapLocation ml) throws GameActionException {
        if (rc.canSenseLocation(ml)) {
            HashMap<MapLocation, Boolean> visited = new HashMap<MapLocation, Boolean>();
            HashMap<MapLocation, MapLocation> parent = new HashMap<MapLocation, MapLocation>();
            Queue<MapLocation> queue = new ArrayDeque<MapLocation>();
            queue.add(rc.getLocation());

            visited.put(rc.getLocation(), true);
            while (!queue.isEmpty()) {
                MapLocation c = queue.poll();
                for (Direction d : enumLists.directions) {
                    MapLocation nc = c.add(d);

                    if (rc.canSenseLocation(nc) && visited.get(nc) == null) {
                        //if (rc.senseNearbyRobots(nc, 1, rc.getTeam()).length == 0
                        //        && rc.senseNearbyRobots(nc, 1, rc.getTeam().opponent()).length == 0) 
                            parent.put(nc, c);
                            visited.put(nc, true);
                            queue.add(nc);
                            
                    }
                }
            }

            MapLocation curr = ml;
            while (parent.get(curr) != rc.getLocation()) {
                curr = parent.get(curr);
            }

            if (!tryMove(rc, rc.getLocation().directionTo(curr))) {
                System.out.println("Failed to move");
            } else {
                System.out.println("Moved!");
            }
        } else {
            Direction target_direction = rc.getLocation().directionTo(ml);
            MapLocation target = rc.getLocation();

            while (rc.canSenseLocation(target.add(target_direction))) {
                target = target.add(target_direction);
            }
            path_to(rc, target);
        }
    }


    static boolean tryMove(RobotController rc, Direction dir) throws GameActionException {
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else
            return false;
    }
}
