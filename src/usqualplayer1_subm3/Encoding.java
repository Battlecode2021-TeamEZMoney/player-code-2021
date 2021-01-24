package usqualplayer1_subm3;

import battlecode.common.*;

class Encoding {
	private static final int LOC_BITS = 7;
	private static final int LOC_BITMASK = (1 << LOC_BITS) - 1;
	private static final int EXP_BITS = 1;
	private static final int EXP_BITMASK = (1 << EXP_BITS) - 1;
	private static final int TYPE_BITS = 3;
	private static final int TYPE_BITMASK = (1 << TYPE_BITS) - 1;
	private static final int CONV_BITS = 6;
	private static final int CONV_BITMASK = (1 << CONV_BITS) - 1;

	static int encode(MapLocation loc, int type) throws GameActionException {
		return encode(loc, type, false);
	}

	static int encode(MapLocation loc, int type, boolean explorer) throws GameActionException {
		return encode(loc, type, explorer, 0);
	}

	static int encode(MapLocation loc, int type, boolean explorer, int conv) throws GameActionException {
		int x = loc.x, y = loc.y;
		int xEnc = x & LOC_BITMASK, yEnc = y & LOC_BITMASK;
		int expInc = explorer ? 1 : 0;
		int typeEnc = type & TYPE_BITMASK;
		int convEnc = ((int) Math.round(Math.log(conv) / Math.log(1.5))) & CONV_BITMASK;
		return (xEnc << LOC_BITS) + yEnc + (expInc << (2 * LOC_BITS)) + (typeEnc << (2 * LOC_BITS + EXP_BITS))
				+ (convEnc << (2 * LOC_BITS + EXP_BITS + TYPE_BITS));
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

	static boolean getExplorerFromFlag(int encoded) throws GameActionException {
		return ((encoded >> (2 * LOC_BITS)) & EXP_BITMASK) == 1;
	}

	static int getTypeFromFlag(int encoded) throws GameActionException {
		return (encoded >> (2 * LOC_BITS + EXP_BITS)) & TYPE_BITMASK;
	}

	static int getConvFromFlag(int encoded) throws GameActionException {
		int convEnc = (encoded >> (2 * LOC_BITS + EXP_BITS + TYPE_BITS)) & CONV_BITMASK;
		double convClose = Math.exp(convEnc * Math.log(1.5));
		return (int) Math.max(convClose + 50, convClose * 1.3); // need a > 1.5 ^ 0.5. Currently a = 1.3
	}
}
