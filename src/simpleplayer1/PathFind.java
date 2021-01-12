package simpleplayer1;

import java.util.*;

import battlecode.common.*;
import common.DirectionUtils;
import common.DS.HashTable;

class Node implements Comparator<Node> {
    public MapLocation node;
    public Double cost;

    public Node() {
    }

    public Node(MapLocation node, Double cost) {
        this.node = node;
        this.cost = cost;
    }

    @Override
    public int compare(Node node1, Node node2) {
        if (node1.cost < node2.cost)
            return -1;
        if (node1.cost > node2.cost)
            return 1;
        return 0;
    }
}

public class PathFind {
    public static Direction pathTo(RobotController rc, MapLocation ml) throws GameActionException {
        if (rc.getLocation().equals(ml))
            return Direction.CENTER;

        HashMap<MapLocation, Double> dist = new HashMap<MapLocation, Double>();
        HashTable<MapLocation> visited = new HashTable<MapLocation>(100);
        PriorityQueue<Node> pq = new PriorityQueue<Node>(100, new Node());

        pq.add(new Node(ml, 0.0));
        dist.put(ml, 0.0);
        while (!pq.isEmpty()) {
            int tt = Clock.getBytecodeNum();
            Node c = pq.poll();
            int t = Clock.getBytecodeNum();
            Double current_dist = dist.get(c.node);
            //System.out.println("Getting cdist:\t" + (Clock.getBytecodeNum()-t));
            rc.setIndicatorDot(c.node, 255, 0, 0);
            //System.out.println(Clock.getBytecodeNum());

            for (int i = 0; i < 8; i++) {
                t = Clock.getBytecodeNum();
                Node nc = new Node(c.node.add(DirectionUtils.nonCenterDirections[i]), 0.0);
                if(nc.node.equals(rc.getLocation())) {
                    return rc.getLocation().directionTo(c.node);
                }

                //System.out.println("Init next node:\t" + (Clock.getBytecodeNum()-t));
                if (rc.canSenseLocation(nc.node)) {
                    nc.cost = 1/rc.sensePassability(nc.node);
                    t = Clock.getBytecodeNum();
                    if (!visited.contains(nc.node)) {
                        //System.out.println("Visited Check:\t" + (Clock.getBytecodeNum() - t));
                        visited.add(nc.node);
                        Double ndist = 999999999.0;

                        t = Clock.getBytecodeNum();
                        if (dist.containsKey(nc.node))
                            ndist = dist.get(nc.node);

                        if (ndist > current_dist + nc.cost) {
                            dist.put(nc.node, current_dist + nc.cost);
                        }
                        pq.add(new Node(nc.node, current_dist + nc.cost));
                        //System.out.println("PLAYER1 Update and Add:\t" + (Clock.getBytecodeNum() - t));
                    }
                }
            }
            //System.out.println("TOTAL LOOP:\t" + (Clock.getBytecodeNum()-tt));

        }

        Direction rtn = Direction.NORTH;
        double mindist = 999999999;
        for (int i = 0; i < 8; i++) {
            if (dist.get(rc.getLocation().add(DirectionUtils.nonCenterDirections[i])) < mindist) {
                mindist = dist.get(rc.getLocation().add(rtn));
                rtn = DirectionUtils.nonCenterDirections[i];
            }
        }

        //System.out.println(dist.get(rc.getLocation()));

        //System.out.println(Clock.getBytecodeNum());

        return rtn;
    }

    public static Direction get_path_direction(RobotController rc, MapLocation ml) throws GameActionException {
        if (rc.getLocation().equals(ml))
            return Direction.CENTER;
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

    // static void path_to(RobotController rc, MapLocation ml) throws
    // GameActionException {
    // if (rc.canSenseLocation(ml)) {
    // HashMap<MapLocation, MapLocation> parent = new HashMap<MapLocation,
    // MapLocation>();
    // Queue<MapLocation> queue = new ArrayDeque<MapLocation>();
    // MapLocation closest_possible = new MapLocation(0, 0);

    // int t = Clock.getBytecodeNum();

    // int closest_distance = 1000;

    // queue.add(rc.getLocation());
    // visited.add(rc.getLocation());

    // while (!queue.isEmpty()) {
    // t = Clock.getBytecodeNum();
    // MapLocation c = queue.poll();
    // System.out.println(c);
    // rc.setIndicatorDot(c, 0, 0, 255);

    // for (Direction d : enumLists.directions) {
    // MapLocation nc = c.add(d);

    // if (rc.canSenseLocation(nc) && visited.contains(nc) == false) {
    // // if (rc.senseNearbyRobots(nc, 1, rc.getTeam()).length == 0
    // // && rc.senseNearbyRobots(nc, 1, rc.getTeam().opponent()).length == 0) {
    // t = Clock.getBytecodeNum();
    // parent.put(nc, c);

    // if (nc.distanceSquaredTo(ml) < closest_distance) {
    // closest_distance = nc.distanceSquaredTo(ml);
    // closest_possible = nc;
    // }
    // visited.add(nc);
    // t = Clock.getBytecodeNum();

    // queue.add(nc);

    // // }
    // }
    // }

    // if (Clock.getBytecodeNum() > 2000)
    // break;
    // // System.out.println("Used:\t" + (Clock.getBytecodeNum() - t));
    // }

    // rc.setIndicatorDot(closest_possible, 255, 0, 0);

    // MapLocation curr = closest_possible;
    // while (parent.get(curr) != rc.getLocation()) {
    // curr = parent.get(curr);
    // }

    // if (!tryMove(rc, rc.getLocation().directionTo(curr))) {
    // System.out.println("Failed to move");
    // } else {
    // System.out.println("Moved!");
    // }
    // } else {
    // Direction target_direction = rc.getLocation().directionTo(ml);
    // MapLocation target = rc.getLocation();

    // while (rc.canSenseLocation(target.add(target_direction))) {
    // target = target.add(target_direction);
    // target_direction = target.directionTo(ml);
    // }
    // rc.setIndicatorDot(target, 0, 255, 0);
    // path_to(rc, target);
    // }
    // }

    static boolean tryMove(RobotController rc, Direction dir) throws GameActionException {
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else
            return false;
    }
}
