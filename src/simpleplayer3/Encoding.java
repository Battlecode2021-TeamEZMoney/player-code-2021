package simpleplayer3;

import battlecode.common.*;

class Encoding {
	private static final int LOC_BITS = 7;
	private static final int LOC_BITMASK = (1 << LOC_BITS) - 1;
	private static final int INFO_BITS = 5;
	private static final int INFO_BITMASK = (1 << INFO_BITS) - 1;

	static int encode(MapLocation loc, int info) {
		int x = loc.x, y = loc.y;
		int xEnc = x & LOC_BITMASK, yEnc = y & LOC_BITMASK;
		return (info << (2 * LOC_BITS)) + (xEnc << LOC_BITS) + yEnc;
	}

	static MapLocation decodeLocation(RobotController rc, int encoded) {
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

	static int decodeInfo(int encoded) {
		return (encoded >> 2 * LOC_BITS) & INFO_BITMASK;
	}

	public static boolean trySetFlag(RobotController rc, int newFlag) throws GameActionException {
		if (rc.canSetFlag(newFlag)) {
			rc.setFlag(newFlag);
			return true;
		} else if (rc.canSetFlag(0)) {
			rc.setFlag(0);
		}
		return false;
	}
}
