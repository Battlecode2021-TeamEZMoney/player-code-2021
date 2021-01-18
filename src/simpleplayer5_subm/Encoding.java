package simpleplayer5_subm;

import battlecode.common.*;

class Encoding {
	private static final int LOC_BITS = 7;
	private static final int LOC_BITMASK = (1 << LOC_BITS) - 1;
	private static final int DIR_BITS = 3;
	private static final int DIR_BITMASK = (1 << DIR_BITS) - 1;
	private static final int EXP_BITS = 1;
	private static final int EXP_BITMASK = (1 << EXP_BITS) - 1;
	private static final int INFO_BITS = 5;
	private static final int INFO_BITMASK = (1 << INFO_BITS) - 1;

	static int encode(MapLocation loc, int info) throws GameActionException {
		return encode(loc, info, Direction.NORTH);
	}

	static int encode(MapLocation loc, int info, Direction dir) throws GameActionException {
		return encode(loc, info, Direction.NORTH, false);
	}

	static int encode(MapLocation loc, int info, Direction dir, boolean explorer) throws GameActionException {
		int x = loc.x, y = loc.y;
		int xEnc = x & LOC_BITMASK, yEnc = y & LOC_BITMASK;
		int dirEnc = 0;
		for (int i = 0; i < DirectionUtils.nonCenterDirections.length; i++) {
			if (dir.equals(DirectionUtils.nonCenterDirections[i])) {
				dirEnc = i;
				break;
			}
		}
		int expInc = explorer ? 1 : 0;
		return (xEnc << LOC_BITS) + yEnc + (dirEnc << (2 * LOC_BITS)) + (expInc << (2 * LOC_BITS + DIR_BITS))
				+ (info << (2 * LOC_BITS + DIR_BITS + EXP_BITS));
	}

	static MapLocation getLocationFromFlag(RobotController rc, int encoded) throws GameActionException {
		int[] enc = new int[] { (encoded >> LOC_BITS) & LOC_BITMASK, encoded & LOC_BITMASK };
		MapLocation curLoc = rc.getLocation();
		int[] cur = new int[] { curLoc.x, curLoc.y };
		int[] curOffset = new int[] { cur[0] & LOC_BITMASK, cur[1] & LOC_BITMASK };
		int[] locOffset = new int[2], loc = new int[2];
		for (int i = 0; i < 2; i++) {
			locOffset[i] = (Math.abs(enc[i] - curOffset[i]) < GameConstants.MAP_MAX_HEIGHT) ? enc[i]
					: enc[i] + (1 << LOC_BITS) * (enc[i] < curOffset[i] ? 1 : -1);
			loc[i] = (cur[i] & ~LOC_BITMASK) + locOffset[i];
		}
		return new MapLocation(loc[0], loc[1]);
	}

	static Direction getDirFromFlag(int encoded) throws GameActionException {
		int ind = (encoded >> (2 * LOC_BITS)) & DIR_BITMASK;
		return DirectionUtils.nonCenterDirections[ind];
	}

	static boolean getExplorerFromFlag(int encoded) throws GameActionException {
		return ((encoded >> (2 * LOC_BITS + DIR_BITS)) & EXP_BITMASK) == 1;
	}

	static int getInfoFromFlag(int encoded) throws GameActionException {
		return (encoded >> (2 * LOC_BITS + DIR_BITS + EXP_BITS)) & INFO_BITMASK;
	}
}
