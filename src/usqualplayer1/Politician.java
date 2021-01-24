package usqualplayer1;

import battlecode.common.*;
import common.*;
import java.util.*;
//import simpleplayer4.Robot.FlagCodes;

class Politician extends Attacker {
	private MapLocation enemyHQ = null;
	private MapLocation neutralHQ = null;
	private boolean waiting = Math.random() < 0.1;
	private MapLocation slandCenter = null;
	// private boolean inDefenseRing = false;
	// private MapLocation tempTarget = null;
	private int turnsWaited = 0;

	Politician(RobotController rcin) throws GameActionException {
		super(rcin);
	}

	Politician(Slanderer sland, MapLocation slandCenter) throws GameActionException {
		// TODO: Remember to update these when new common fields are added in the Pawn
		// and Robot classes.
		super(sland.rc);
		this.turnCount = sland.turnCount;
		this.hqLocation = sland.hqLocation;
		this.hqID = sland.hqID;
		this.dirTarget = sland.dirTarget;
		this.slandCenter = slandCenter;
	}

	void run() throws GameActionException {
		while (true) {
			turnCount++;
			// int start = 0;
			if (rc.isReady()) {
				updateHQs();
				if (Math.random() < 0.8 && neutralHQ == null && enemyHQ == null) {
					// int start = Clock.getBytecodesLeft();
					tryOptimalEmpower();
					// System.out.println(Clock.getBytecodesLeft()-start);
				}
				// start = Clock.getBytecodesLeft();
				endOfMatchEmpower();
				if (Clock.getBytecodesLeft() > 10000) {
					if (explorer) {
						runSimpleCode();
					} else if (slandCenter != null) {
						defendSlandCenter();
					} else if (neutralHQ != null) {
						runNeutralCode();
					} else if (enemyHQ != null) {
						runAttackCode();
					} else {
						if (rc.canGetFlag(hqID)) {
							parseHQFlag(rc.getFlag(hqID));
						} else {
							runSimpleCode();
						}
					}
				}
			} else {
				if (rc.canGetFlag(hqID) && slandCenter == null && enemyHQ == null && neutralHQ == null) {
					parseHQFlag(rc.getFlag(hqID));
				}
			}
			setNearbyHQFlag();
			/*
			 * if (Clock.getBytecodesLeft()-start < -10000) System.out.println("Used " +
			 * (Clock.getBytecodesLeft()-start));
			 * 
			 * System.out.println("N: " + (neutralHQ == null ? "null" :
			 * printLoc(neutralHQ))); System.out.println("E: " + (enemyHQ == null ? "null" :
			 * printLoc(enemyHQ))); System.out.println("S: " + (slandCenter == null ? "null"
			 * : printLoc(slandCenter))); System.out.println("LOC: " +
			 * printLoc(rc.getLocation()));
			 */
			Clock.yield();
		}

		// TODO: Implement differentiation for formerly slanderer units as well as
		// converted units.
	}

	private void updateHQs() throws GameActionException {
		if (enemyHQ != null && rc.canSenseLocation(enemyHQ)
				&& !rc.senseRobotAtLocation(enemyHQ).team.equals(enemyTeam)) {
			enemyHQ = null;
		}
		if (neutralHQ != null && rc.canSenseLocation(neutralHQ)
				&& !rc.senseRobotAtLocation(neutralHQ).team.equals(Team.NEUTRAL)) {
			neutralHQ = null;
		}

		RobotInfo[] nearby = rc.senseNearbyRobots();
		if (neutralHQ == null) {
			for (RobotInfo robot : nearby) {
				if (robot.type.equals(RobotType.ENLIGHTENMENT_CENTER) && robot.team.equals(Team.NEUTRAL)) {
					neutralHQ = robot.location;
					break;
				}
			}
		}
		if (enemyHQ == null) {
			for (RobotInfo robot : nearby) {
				if (robot.type.equals(RobotType.ENLIGHTENMENT_CENTER) && robot.team.equals(enemyTeam)) {
					enemyHQ = robot.location;
					break;
				}
			}
		}

	}

	private void parseHQFlag(int flag) throws GameActionException {
		MapLocation tempLocation = Encoding.getLocationFromFlag(rc, flag);
		switch (Encoding.getTypeFromFlag(flag)) {
			case 2:
				enemyHQ = tempLocation;
				runAttackCode();
				break;
			case 4:
				neutralHQ = tempLocation;
				runNeutralCode();
				break;
			case 6:
				slandCenter = tempLocation;
				defendSlandCenter();
				break;
			default:
				runSimpleCode();
				break;
		}
	}

	private void defendSlandCenter() throws GameActionException {
		if (!rc.isReady() || slandCenter == null) {
			return;
		}

		ArrayList<RobotInfo> allySlands = new ArrayList<RobotInfo>();
		ArrayList<RobotInfo> allyPols = new ArrayList<RobotInfo>();
		ArrayList<RobotInfo> enemyMucks = new ArrayList<RobotInfo>();
		for (RobotInfo robot : rc.senseNearbyRobots()) {
			if (robot.team == allyTeam && robot.type == RobotType.POLITICIAN) {
				// includes Slanderers
				double angle = angleBetween(rc.getLocation(), slandCenter, robot.location);
				if (Constants.optimalSlandInfSet.contains(robot.influence)) {
					allySlands.add(robot);
				} else if (!Constants.optimalSlandInfSet.contains(robot.influence) && 60 <= angle && angle <= 120) {
					allyPols.add(robot);
				} else {
					// System.out.println(printLoc(robot.location) + " " + angle);
				}

			} else if (robot.team == enemyTeam && robot.type == RobotType.MUCKRAKER) {
				enemyMucks.add(robot);
			}
		}
		// int start = Clock.getBytecodesLeft();
		tryOptimalEmpower(0.7, 3);
		// if (Clock.getBytecodesLeft()-start < -11000)
		// System.out.println(Clock.getBytecodesLeft()-start);
		empowerIfMuckNearby();
		empowerIfMuckNearSlanderer(enemyMucks, allySlands);
		if (distanceSquaredTo(slandCenter) < sensorRadiusSquared) {
			if (tryDirForward090180(directionTo(slandCenter).opposite())) {
				return;
			}
		}

		// System.out.println("SLANDS");
		// for (RobotInfo sland : slands) {
		// System.out.println(printLoc(sland.location));
		// }
		// System.out.println("POLS");
		// for (RobotInfo pol : pols) {
		// System.out.println(printLoc(pol.location));
		// }
		if (Math.random() < 0.2) {
			if (allySlands.isEmpty()) {
				tryDirForward090180(directionTo(slandCenter));
				// target = target.add(directionTo(slandCenter));
			} else if (allySlands.size() > 3) {
				tryDirForward090180(directionTo(slandCenter).opposite());
				// target = target.add(directionTo(slandCenter).opposite());
			}
		}
		tryDirForward090180(awayFromRobots(allyPols));

		// if (slands.size() > 4) {
		// inDefenseRing = false;
		// tryDirForward90180(directionTo(slandCenter).opposite());
		// } else if (slands.isEmpty()) {
		// inDefenseRing = true;
		// tryDirForward90180(directionTo(slandCenter));
		// }
		//
		// if (inDefenseRing) {
		// tryDirForward90(awayFromRobots(pols));
		// }
	}

	private void empowerIfMuckNearby() throws GameActionException {
		for (RobotInfo robot : rc.senseNearbyRobots(2, enemyTeam)) {
			if (robot.type.equals(RobotType.MUCKRAKER)) {
				// tryEmpower(distanceSquaredTo(robot));
				tryEmpower(actionRadiusSquared);
			}
		}
	}

	private void empowerIfMuckNearSlanderer(ArrayList<RobotInfo> enemyMucks, ArrayList<RobotInfo> allySlands)
			throws GameActionException {
		// System.out.println("MUCK");
		// for (RobotInfo enemyMuck : enemyMucks) {
		// System.out.println(printLoc(enemyMuck.location));
		// }
		// System.out.println("SLAND");
		// for (RobotInfo allySland : allySlands) {
		// System.out.println(printLoc(allySland.location));
		// }
		if (enemyMucks.isEmpty() || allySlands.isEmpty()) {
			return;
		}
		for (RobotInfo enemyMuck : enemyMucks) {
			RobotInfo closestSland = Collections.min(allySlands,
					Comparator.comparing(sland -> enemyMuck.location.distanceSquaredTo(sland.location)));
			if (enemyMuck.location.distanceSquaredTo(closestSland.location) <= 25) {
				tryEmpower(distanceSquaredTo(enemyMuck));
			}
		}
	}

	private int maxIncrease(RobotInfo robot) throws GameActionException {
		if (isEnemy(robot)) {
			switch (robot.type) {
				case ENLIGHTENMENT_CENTER:
					return Integer.MAX_VALUE;
				case POLITICIAN:
					return robot.conviction + robot.influence;
				case MUCKRAKER:
				case SLANDERER:
					return robot.conviction + 1;
				default:
					return 0;
			}
		} else {
			return initialConviction(robot) - robot.conviction;
		}
	}

	private double changeWithBuff(double conv, double buff, RobotInfo robot) {
		if (robot.type == RobotType.ENLIGHTENMENT_CENTER && robot.team == rc.getTeam()) {
			;
		} else if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
			double convNeededToConvert = robot.conviction / buff;
			if (conv <= convNeededToConvert) {
				conv *= buff;
			} else {
				conv = robot.conviction + (conv - convNeededToConvert);
			}
		} else {
			conv *= buff;
		}
		return conv;
	}

	private int[] optimalEmpowerRadiusAndInfo() throws GameActionException {
		// int start = Clock.getBytecodesLeft();
		if (rc.getConviction() <= 10) {
			return new int[] { 0, 0, 0, 0 };
		}
		int maxTotalIncrease = 0, optimalIncRadius = 0, optimalDestroyed = 0;
		RobotInfo[] nearby = rc.senseNearbyRobots(actionRadiusSquared);
		for (int empRadius = 1; empRadius <= actionRadiusSquared; empRadius++) {
			int increase = 0, numAffected = 0, numDestroyed = 0;
			for (RobotInfo robot : nearby) {
				if (distanceSquaredTo(robot) <= empRadius) {
					numAffected++;
				}
			}
			if (numAffected == 0) {
				continue;
			}
			double buff = rc.getEmpowerFactor(allyTeam, 0);
			double change = (int) (rc.getConviction() - 10) / numAffected;
			// TODO: factor in remaining undivided conviction, given to later creations
			for (RobotInfo robot : nearby) {
				if (distanceSquaredTo(robot) <= empRadius) {
					// int start = Clock.getBytecodesLeft();
					increase += Math.min((int) changeWithBuff(change, buff, robot), maxIncrease(robot));
					// System.out.println("a " + (Clock.getBytecodesLeft()-start));
					if (isEnemy(robot) && robot.conviction < change) {
						numDestroyed++;
					}
				}
			}
			if (increase > maxTotalIncrease) {
				maxTotalIncrease = increase;
				optimalIncRadius = empRadius;
				optimalDestroyed = numDestroyed;
			}
		}

		// if (Clock.getBytecodesLeft()-start < -8000) System.out.println("a " +
		// (Clock.getBytecodesLeft()-start));
		return new int[] { optimalIncRadius, maxTotalIncrease, optimalDestroyed };
	}

	private void tryOptimalEmpower() throws GameActionException {
		tryOptimalEmpower(0.7);
	}

	private void tryOptimalEmpower(double empowerThresh) throws GameActionException {
		tryOptimalEmpower(empowerThresh, 6);
	}

	private void tryOptimalEmpower(double empowerThresh, int destThresh) throws GameActionException {
		int[] radAndInfo = optimalEmpowerRadiusAndInfo();
		int rad = radAndInfo[0], inc = radAndInfo[1], numDest = radAndInfo[2];
		if ((rc.getConviction() > 10 && (double) inc / rc.getConviction() >= empowerThresh) || numDest >= destThresh) {
			tryEmpower(rad);
		}
	}

	private void HQAttackRoutine(MapLocation locHQ) throws GameActionException {
		int numSurrounding1 = numSurrounding(1);
		int numSurrounding2 = numSurrounding(2);
		if (rc.getLocation().isAdjacentTo(locHQ)) {
			if (DirectionUtils.isCardinal(directionTo(locHQ))) {
				if (numSurrounding1 <= turnsWaited / 10 + 1) {
					tryEmpower(1);
				}
			} else {
				if (!tryDirForward90(directionTo(locHQ))) {
					if (numSurrounding2 <= turnsWaited / 10 + 1) {
						tryEmpower(2);
					}
				} else {
					turnsWaited = 0;
				}
			}
			turnsWaited++;
		} else if (!tryDirForward90(directionTo(locHQ))) {
			if (withinAttackRange(locHQ) && turnsWaited > 20) {
				tryEmpower(distanceSquaredTo(locHQ));
			} else {
				tryDirForward180(directionTo(locHQ));
			}
			turnsWaited++;
		} else {
			turnsWaited = 0;
		}
	}

	private void runNeutralCode() throws GameActionException {
		if (!rc.isReady() || neutralHQ == null) {
			return;
		}

		if (!waiting || (rc.canSenseLocation(neutralHQ) && rc.senseRobotAtLocation(neutralHQ).conviction < 25)) {
			HQAttackRoutine(neutralHQ);
		} else {
			if (distanceSquaredTo(neutralHQ) > actionRadiusSquared && tryDirForward90(directionTo(neutralHQ))) {
				return;
			}
			tryDirForward90180(directionTo(neutralHQ).opposite());
		}
	}

	private void runAttackCode() throws GameActionException {
		if (!rc.isReady() || enemyHQ == null) {
			return;
		}

		HQAttackRoutine(enemyHQ);
	}

	private void runSimpleCode() throws GameActionException {
		if (!rc.isReady()) {
			return;
		}

		updateDirIfOnBorder();

		if (!tryDirForward90(dirTarget)) {
			tryDirForward90180(awayFromAllies());
		}

		// if (!tryDirForward90180(awayFromAllies())) {
		// tryDirForward90180(DirectionUtils.randomDirection());
		// }
	}

	private void tryEmpower(int radius) throws GameActionException {
		if (rc.canEmpower(radius)) {
			if (slandCenter != null) {
				encoded = Encoding.encode(rc.getLocation(), FlagCodes.empowering);
			}
			rc.empower(radius);
		}
	}

	private void endOfMatchEmpower() throws GameActionException {
		if (rc.getRoundNum() > GameConstants.GAME_MAX_NUMBER_OF_ROUNDS - 25) {
			tryEmpower(actionRadiusSquared);
		}
	}

}
