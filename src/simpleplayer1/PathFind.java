package simpleplayer1;

import battlecode.common.*;

public class PathFind {
    public static Direction get_path_direction(RobotController rc, MapLocation ml) throws GameActionException {
        if(rc.getLocation().equals(ml)) return Direction.CENTER;
        for (int i = 0; i < 3; i++) {
            Direction left = rc.getLocation().directionTo(ml);
            Direction right = rc.getLocation().directionTo(ml);

            for (int j = 0; j < i; j++) {
                left = left.rotateLeft();
                right = right.rotateRight();
            }

            if (rc.sensePassability(rc.getLocation().add(left)) < rc.sensePassability(rc.getLocation().add(right))) {
                if (rc.canMove(left))
                    return left;
            } else {
                if (rc.canMove(right))
                    return right;
            }
        }
        return Direction.CENTER;

    }

    // static void path_to(RobotController rc, MapLocation ml) throws GameActionException {
    //     if (rc.canSenseLocation(ml)) {
    //         HashMap<MapLocation, MapLocation> parent = new HashMap<MapLocation, MapLocation>();
    //         Queue<MapLocation> queue = new ArrayDeque<MapLocation>();
    //         MapLocation closest_possible = new MapLocation(0, 0);

    //         int t = Clock.getBytecodeNum();

    //         int closest_distance = 1000;

    //         queue.add(rc.getLocation());
    //         visited.add(rc.getLocation());

    //         while (!queue.isEmpty()) {
    //             t = Clock.getBytecodeNum();
    //             MapLocation c = queue.poll();
    //             System.out.println(c);
    //             rc.setIndicatorDot(c, 0, 0, 255);

    //             for (Direction d : enumLists.directions) {
    //                 MapLocation nc = c.add(d);

    //                 if (rc.canSenseLocation(nc) && visited.contains(nc) == false) {
    //                     // if (rc.senseNearbyRobots(nc, 1, rc.getTeam()).length == 0
    //                     // && rc.senseNearbyRobots(nc, 1, rc.getTeam().opponent()).length == 0) {
    //                     t = Clock.getBytecodeNum();
    //                     parent.put(nc, c);

    //                     if (nc.distanceSquaredTo(ml) < closest_distance) {
    //                         closest_distance = nc.distanceSquaredTo(ml);
    //                         closest_possible = nc;
    //                     }
    //                     visited.add(nc);
    //                     t = Clock.getBytecodeNum();

    //                     queue.add(nc);

    //                     // }
    //                 }
    //             }

    //             if (Clock.getBytecodeNum() > 2000)
    //                 break;
    //             // System.out.println("Used:\t" + (Clock.getBytecodeNum() - t));
    //         }

    //         rc.setIndicatorDot(closest_possible, 255, 0, 0);

    //         MapLocation curr = closest_possible;
    //         while (parent.get(curr) != rc.getLocation()) {
    //             curr = parent.get(curr);
    //         }

    //         if (!tryMove(rc, rc.getLocation().directionTo(curr))) {
    //             System.out.println("Failed to move");
    //         } else {
    //             System.out.println("Moved!");
    //         }
    //     } else {
    //         Direction target_direction = rc.getLocation().directionTo(ml);
    //         MapLocation target = rc.getLocation();

    //         while (rc.canSenseLocation(target.add(target_direction))) {
    //             target = target.add(target_direction);
    //             target_direction = target.directionTo(ml);
    //         }
    //         rc.setIndicatorDot(target, 0, 255, 0);
    //         path_to(rc, target);
    //     }
    // }

    static boolean tryMove(RobotController rc, Direction dir) throws GameActionException {
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else
            return false;
    }
}
