package final_usqualplayer3_subm;

import battlecode.common.*;

import java.util.*;

class Politician extends Attacker {
	private MapLocation enemyHQ = null;
	private MapLocation neutralHQ = null;
	private int turnsWaited = 0;

	Politician(RobotController rcin) throws GameActionException {
		super(rcin);
	}

	Politician(Slanderer sland) throws GameActionException {
		super(sland.rc);
		this.turnCount = sland.turnCount;
		this.hqLocation = sland.hqLocation;
		this.hqID = sland.hqID;
		this.dirTarget = sland.dirTarget;
		this.slandCenter = sland.slandCenter;
		this.explorer = sland.explorer;
		this.defending = sland.defending;
	}

	void run() throws GameActionException {
		while (true) {
			turnCount++;
			if (slandCenter != null && rc.canGetFlag(hqID)) {
				parseHQFlagSland(rc.getFlag(hqID));
			}
			
			if (rc.isReady()) {
				updateHQs();
				if (Math.random() < 0.8 && neutralHQ == null && enemyHQ == null) {
					tryOptimalEmpower();
				}
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
			
			Clock.yield();
		}
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
				// includes Slanderers due to politicians not having "true sense"
				double angle = angleBetween(rc.getLocation(), slandCenter, robot.location);
				if (Constants.optimalSlandInfSet.contains(robot.influence)) {
					allySlands.add(robot);
				} else if (!Constants.optimalSlandInfSet.contains(robot.influence) && 60 <= angle && angle <= 120) {
					allyPols.add(robot);
				}

			} else if (robot.team == enemyTeam && robot.type == RobotType.MUCKRAKER) {
				enemyMucks.add(robot);
			}
		}
		tryOptimalEmpower(0.7, 3);
		empowerIfMuckNearby();
		empowerIfMuckNearSlanderer(enemyMucks, allySlands);
		if (distanceSquaredTo(slandCenter) < sensorRadiusSquared) {
			if (tryDirForward090180(directionTo(slandCenter).opposite())) {
				return;
			}
		}

		if (Math.random() < 0.4) {
			if (allySlands.isEmpty()) {
				tryDirForward090180(directionTo(slandCenter));
			} else if (allySlands.size() > 3) {
				tryDirForward090180(directionTo(slandCenter).opposite());
			}
		}
		tryDirForward090180(awayFromRobots(allyPols));
	}

	private void empowerIfMuckNearby() throws GameActionException {
		for (RobotInfo robot : rc.senseNearbyRobots(2, enemyTeam)) {
			if (robot.type.equals(RobotType.MUCKRAKER)) {
				tryEmpower(actionRadiusSquared);
			}
		}
	}

	private void empowerIfMuckNearSlanderer(ArrayList<RobotInfo> enemyMucks, ArrayList<RobotInfo> allySlands)
			throws GameActionException {
		if (enemyMucks.isEmpty() || allySlands.isEmpty()) {
			return;
		}
		int minDist = Integer.MAX_VALUE;
		RobotInfo minMuck = null;
		for (RobotInfo enemyMuck : enemyMucks) {
			RobotInfo closestSland = Collections.min(allySlands,
					Comparator.comparing(sland -> enemyMuck.location.distanceSquaredTo(sland.location)));
			int possDist = enemyMuck.location.distanceSquaredTo(closestSland.location);
			if (possDist < minDist) {
				minDist = possDist;
				minMuck = enemyMuck;
			}
		}
	
		if (minDist <= 25) {
			tryEmpower(distanceSquaredTo(minMuck));
		}
		tryDirForward090(directionTo(minMuck));
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
			for (RobotInfo robot : nearby) {
				if (distanceSquaredTo(robot) <= empRadius) {
					increase += Math.min((int) changeWithBuff(change, buff, robot), maxIncrease(robot));
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

	private void HQAttackRoutine(MapLocation locHQ, boolean aggressive) throws GameActionException {
		if (turnsWaited > 50) {
			aggressive = true;
		}
		int numSurrounding1 = numSurrounding(1);
		int numSurrounding2 = numSurrounding(2);
		int convHQ = rc.canSenseLocation(locHQ) ? rc.senseRobotAtLocation(locHQ).conviction : -1;
		if (rc.getLocation().isAdjacentTo(locHQ)) {
			if (DirectionUtils.isCardinal(directionTo(locHQ))) {
				if (damagePer(numSurrounding1) > convHQ || (aggressive && numSurrounding1 <= turnsWaited / 10 + 1)) {
					tryEmpower(1);
				}
			} else {
				if (!tryDirForward90(directionTo(locHQ))) {
					if (damagePer(numSurrounding2) > convHQ || (aggressive && numSurrounding2 <= turnsWaited / 10 + 1)) {
						tryEmpower(2);
					}
				} else {
					turnsWaited = 0;
				}
			}
			turnsWaited++;
		} else if (tryDirForward90(directionTo(locHQ))) {
			turnsWaited = 0;
		} else {
			if (withinAttackRange(locHQ)) {
				int numSurrounding = numSurrounding(distanceSquaredTo(locHQ));
				if (damagePer(numSurrounding) > convHQ || (aggressive && turnsWaited > 30)) {
					tryEmpower(distanceSquaredTo(locHQ));
				}
			}
			tryDirForward180(directionTo(locHQ));
			turnsWaited++;
		}
	}
	
	private int damagePer(int numSurrounding) {
		return (int) ((rc.getConviction() - 10) * rc.getEmpowerFactor(allyTeam, 0) / numSurrounding);
	}

	private void runNeutralCode() throws GameActionException {
		if (!rc.isReady() || neutralHQ == null) {
			return;
		}
		
		HQAttackRoutine(neutralHQ, false);
	}

	private void runAttackCode() throws GameActionException {
		if (!rc.isReady() || enemyHQ == null) {
			return;
		}

		HQAttackRoutine(enemyHQ, true);
	}

	private void runSimpleCode() throws GameActionException {
		if (!rc.isReady()) {
			return;
		}

		updateDirIfOnBorder();

		if (!tryDirForward90(dirTarget)) {
			tryDirForward90180(awayFromAllies());
		}
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
