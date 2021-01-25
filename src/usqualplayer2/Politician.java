package usqualplayer2;

import battlecode.common.*;
import common.*;

import java.util.*;

class Politician extends Attacker {
	private int turnsWaited = 0;
	private boolean spawnedAsAttacker = true;
	Politician(RobotController rcin) throws GameActionException {
		super(rcin);
		setSpawnMode();
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
		setSpawnMode();
	}

	void setSpawnMode() throws GameActionException {
		if(hasOrCanGetHomeHQ()){
			int tempType = Encoding.getTypeFromFlag(rc.getFlag(hqID));
			spawnedAsAttacker = tempType == 2 || tempType == 4;
		} else {
			spawnedAsAttacker = true;
		}
	}

	void run() throws GameActionException {
		while (true) {
			turnCount++;
			if (neutralHQ == null && enemyHQ == null) {
				tryOptimalEmpower();
			}

			endOfMatchEmpower();
			if(spawnedAsAttacker){
				updateHQs();
			}

			if (hasOrCanGetHomeHQ()) {
				parseHQFlag(rc.getFlag(hqID));
				if (mode == 4 || (neutralHQ != null && neutralHQIsCurrent())) {
					runNeutralCode();
				} else if (mode == 2 || (enemyHQ != null && enemyHQIsCurrent())) {
					runAttackCode();
				} else if (slandCenter != null) {
					defendSlandCenter();
				} else {
					runSimpleCode();
				}
			} else {
				if (neutralHQ != null && neutralHQIsCurrent()) {
					runNeutralCode();
				} else if (enemyHQ != null && enemyHQIsCurrent()) {
					runAttackCode();
				} else {
					runSimpleCode();
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
		if (neutralHQ == null || enemyHQ == null) {
			for (RobotInfo robot : nearby) {
				if (robot.getType().equals(RobotType.ENLIGHTENMENT_CENTER)) {
					if (neutralHQ == null && robot.getTeam().equals(Team.NEUTRAL)){
						neutralHQ = robot.getLocation();
						break;
					} else if (enemyHQ == null && robot.getTeam().equals(enemyTeam)){
						enemyHQ = robot.getLocation();
						if(neutralHQ != null){
							break;
						}
					}
				}
			}
		}
	}

	private void parseHQFlag(int flag) throws GameActionException {
		MapLocation tempLocation = Encoding.getLocationFromFlag(rc, flag);
		switch (Encoding.getTypeFromFlag(flag)) {
			case 2:
				if (enemyHQ == null && spawnedAsAttacker) {
					enemyHQ = tempLocation;
					mode = 2;
				}
				break;
			case 4:
				if (neutralHQ == null && spawnedAsAttacker) {
					neutralHQ = tempLocation;
					mode = 4;
				}
				break;
			case 6:
				slandCenter = tempLocation;
				break;
			default:
				break;
		}

		if (enemyHQ == null && mode == 2){
			mode = -1;
		}

		if (neutralHQ == null && mode == 4){
			mode = -1;
		}
	}

	private void defendSlandCenter() throws GameActionException {
		if (!rc.isReady()) {
			return;
		} else if (slandCenter == null) {
			runSimpleCode();
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
				}

			} else if (robot.team == enemyTeam && robot.type == RobotType.MUCKRAKER) {
				enemyMucks.add(robot);
			}
		}

		tryOptimalEmpower(0.7, 3);

		empowerIfMuckNearSlanderer(enemyMucks, allySlands);
		if (distanceSquaredTo(slandCenter) < sensorRadiusSquared) {
			if (tryDirForward090180(directionTo(slandCenter).opposite())) {
				return;
			}
		}


		if (Math.random() < 0.4) {
			if (allySlands.isEmpty()) {
				tryMove(pathingController.dirToTarget(slandCenter));
				pathingController.resetPrevMovesAndDir();
			} else if (allySlands.size() > 3) {
				tryDirForward090180(directionTo(slandCenter).opposite());
			}
		}
		tryDirForward090180(awayFromRobots(allyPols));
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
		int convHQ = rc.canSenseLocation(locHQ) ? rc.senseRobotAtLocation(locHQ).conviction : -1;
		if (rc.getLocation().isAdjacentTo(locHQ)) {
			if (DirectionUtils.isCardinal(directionTo(locHQ))) {
				if (damagePer(numSurrounding1) > convHQ || numSurrounding1 <= turnsWaited / 10 + 1) {
					tryEmpower(1);
				}
			} else {
				if (!tryDirForward90(directionTo(locHQ))) {
					if (damagePer(numSurrounding2) > convHQ || numSurrounding2 <= turnsWaited / 10 + 1) {
						tryEmpower(2);
					}
				} else {
					turnsWaited = 0;
				}
			}
			turnsWaited++;
		} else if (!tryDirForward90(directionTo(locHQ))) {
			if (withinAttackRange(locHQ) && turnsWaited > 30) {
				tryEmpower(distanceSquaredTo(locHQ));
			} else {
				tryMove(pathingController.dirToTarget(locHQ));
			}
			turnsWaited++;
		} else {
			turnsWaited = 0;
		}
	}
	
	private int damagePer(int numSurrounding) {
		return (int) ((rc.getConviction() - 10) * rc.getEmpowerFactor(allyTeam, 0) / numSurrounding);
	}

	private void runNeutralCode() throws GameActionException {
		if (rc.canSenseLocation(neutralHQ) && !rc.senseRobotAtLocation(neutralHQ).getTeam().equals(Team.NEUTRAL)){
			neutralHQ = null;
		}
		if (!rc.isReady()) {
			return;
		}
		if (neutralHQ == null) {
			defendSlandCenter();
			return;
		}
		
		HQAttackRoutine(neutralHQ);
	}

	private void runAttackCode() throws GameActionException {
		if (rc.canSenseLocation(enemyHQ) && !rc.senseRobotAtLocation(enemyHQ).getTeam().equals(enemyTeam)){
			enemyHQ = null;
		}
		if (!rc.isReady()) {
			return;
		}
		if (enemyHQ == null) {
			defendSlandCenter();
			return;
		}
		HQAttackRoutine(enemyHQ);
	}

	private void runSimpleCode() throws GameActionException {
		if (!rc.isReady()) {
			return;
		}

		pathingController.resetPrevMovesAndDir();
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

	protected void parseHQFlagSland(int flag) throws GameActionException {
		MapLocation tempLocation = Encoding.getLocationFromFlag(rc, flag);
		switch (Encoding.getTypeFromFlag(flag)) {
			case 6:
				slandCenter = tempLocation;
				break;
			default:
				break;
		}
	}

}
