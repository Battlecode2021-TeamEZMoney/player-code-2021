package simpleplayer3;

import battlecode.common.*;

class Bidding {
	private static final int MAX_ROUNDS = GameConstants.GAME_MAX_NUMBER_OF_ROUNDS;
	private static final int VOTES_TO_WIN = MAX_ROUNDS / 2 + 1, OFFSET = 100;
	private static final double GROWTH_RATE = 1.15, DECAY_RATE = 0.9;
	private static int prevBid = 1, prevTeamVotes = -1;
	private static double accum = 0;

	public static int bidAmount(RobotController rc) throws GameActionException {
		if (rc.getTeamVotes() >= 1501)
			return 0; // won bidding

		int bid = 1, curTeamVotes = rc.getTeamVotes();
		double curVoteValue = voteValue(rc);
		if (prevTeamVotes != -1) {
			if (curTeamVotes > prevTeamVotes) {
				// won previous round
				accum = DECAY_RATE * accum;
				bid = (int) (DECAY_RATE * prevBid + accum);
			} else {
				// lost previous round
				accum = DECAY_RATE * accum + curVoteValue;
				bid = (int) (GROWTH_RATE * prevBid + accum);
			}
		}
		bid = Math.max(bid, 1) + (int) (Math.random() * 2);
		bid = Math.min(bid, rc.getInfluence() / 20);
		prevBid = bid;
		prevTeamVotes = curTeamVotes;
		return bid;
	}

	public static double logLin(int x) throws GameActionException {
		if (x <= 0)
			return 0;
		return x * Math.log((double) x);
	}

	public static double voteValue(RobotController rc) throws GameActionException {
		int votes = rc.getTeamVotes(), rounds = rc.getRoundNum() - 1;
		int votesMin = VOTES_TO_WIN - (MAX_ROUNDS - rounds);
		double lgProbDiff = (double) (MAX_ROUNDS - rounds - 1) * Math.log(2) + logLin(VOTES_TO_WIN - votes - 1)
				+ logLin((MAX_ROUNDS - rounds) - (VOTES_TO_WIN - votes)) - logLin(MAX_ROUNDS - rounds - 1);
		// lgProbDiff: 0 (highest value) - maxRounds * ln(2) (lowest value)

		return (MAX_ROUNDS * Math.log(2) - lgProbDiff) / (votes - votesMin + OFFSET);
		// returns: 0.6 (lowest value) - 20.7 (highest value)
	}

	// alternate bidding, not used
	public static int bidAmount2(RobotController rc) throws GameActionException {
		if (rc.getTeamVotes() >= 1501)
			return 0; // won bidding

		int bid = 1, curTeamVotes = rc.getTeamVotes();
		double curVoteValue = voteValue(rc);
		if (prevTeamVotes != -1) {
			if (curTeamVotes > prevTeamVotes) {
				// won previous round
				accum = DECAY_RATE * accum;
				bid = Math.max(1, (int) ((DECAY_RATE + accum / 10) * prevBid));
			} else {
				// lost previous round
				accum = DECAY_RATE * accum + curVoteValue;
				// System.out.println("Accum: " + accum);
				bid = (int) ((GROWTH_RATE + accum / 10) * Math.max(1, prevBid));
			}
		}
		// System.out.println(bid + " " + rc.getInfluence());
		bid = Math.min(bid + (int) (Math.random() * 0), rc.getInfluence() / 20);

		prevBid = bid;
		prevTeamVotes = curTeamVotes;
		return bid;
	}
}