package skittles.sim;

import java.util.List;
import java.util.Vector;

public class Stats {
	/** The number of times to run the simulation with the given configurations	 */
	public static int rounds = 1;
	
	/** Whether the output should be written to files */
	public static boolean printToFiles = true;
	
	/** Whether the output should be written to the console */
	public static boolean printToConsole = true;
	
	/** This variable will filter the information printed based on player index.
	 * 		if the array contains -1, all information will be printed
	 * 		if the array contains one or more index values that are not -1,
	 * 			then for certain types of data, only the data concerning the specified
	 * 			index values will be printed (this way if you know your player's index value
	 * 			based on the GameConfig file you can only print information about how your 
	 * 			player is performing) */
	private final static int[] playerFilterArray = { 0 };
		
	/** The happiness values for all the skittles
	 * 		same as original simulator
	 * 
	 * 		not altered when filtering by player */
	public static boolean happinessDistribution = true;
	
	/** The average happiness distribution.  The happiness values for all skittles
	 * 	are averaged together.  This is useful for large numbers of skittles to get an idea
	 * 	of how 'lucky' a player is with their happiness values.
	 * 
	 * 		not altered when filtering by player */
	public static boolean averageHappiness = true;

	/** The original skittles portfolio for all players
	 * 		same as original simulator
	 * 
	 *  	not altered when filtering by player */
	public static boolean skittlesDistribution = true;

	/** Maximum achievable score if no additional trades are made and all
	 * 	positive value skittles are eaten at once, and all negative value skittles
	 * 	are eaten 1 at a time
	 * 
	 *  	not altered when filtering by player*/
	public static boolean noTradePotentialScore = true;

	/** Maximum score if player is successful in hoarding as many of their
	 * 	top value skittles as possible.  This also takes into account how many skittles
	 * 	are in existence this round.  For example, if each player has 50 skittles, and there are more
	 * 	colors than players, theoretically a player could hoard 50 of their best color.  If only 45 of
	 * 	that color are available, this score will reflect that 45 of the best skittle are hoarded, and 
	 * 	the remaining 5 skittles will be the second best color, and so on 
	 * 
	 * 		not altered when filtering by player*/
	public static boolean maxHoardPotentialScore = true;
	
	/** This prints out which skittles are eaten by each player every round
	 * 
	 *  Only consumption of players included in filter will be shown */
	public static boolean consumption = true;
	
	/** Prints out all the skittles in player's hands every round 
	 * 
	 * Only the skittles of players included in filter will be shown*/
	public static boolean skittlesPortfolio = true;
	
	/** Prints out all trades that are executed (but not all offers that are posed)
	 * 	
	 * 	Only trades that involve players in the filter will be shown */
	public static boolean allAcceptedTrades = true;
	
	/** Shows all offers that are proposed
	 * 
	 *  Only offers made by players in the filter will be shown */
	public static boolean allOffers = true;
	
	/** This is an aggregate of all trade data for each player.  The total number
	 * 	of skittles given away and gained during all trades involving the player
	 * 	will be shown.
	 * 
	 *  Only net trade data for players in the filter will be shown */
	public static boolean netTrade = true; 
	
	/** This is the raw score of each player, that does not take into account
	 * 	what any of the other player's scores were
	 * 
	 *  Only the raw score for players in the filter will be shown */
	public static boolean rawScore = true;
	
	/** The average of all other players scores.
	 * 
	 *  Only the averages excluding players in the filter will be shown */
	public static boolean othersAvgScore = true;
	
	/** The total score, which is (rawScore + othersAvgScore) / 2 
	 * 
	 * 	Only the total score for the players in the filter will be shown */
	public static boolean finalScore = true;
	
	/** This prints out all player index with their raw scores
	 * 	in order of their rank.
	 * 
	 *  	not altered when filtering by player */
	public static boolean rawScoreRank = true;
	
	/** This prints out all player index with their final scores
	 * 	in order of their rank
	 * 
	 *  	not altered when filtering by player */
	public static boolean finalScoreRank = true;
	

	
	
	
	
	
	
	/** This vector uses the playerFilterArray to populate it, this just simplifies 
	 * 	the internal code */
	public static Vector<Integer> playerFilter = new Vector<Integer>();
	
	/** Initialize method to convert int array to vector for simpler internal code */
	public static void init()
	{
		for(int i : playerFilterArray)
		{
			playerFilter.add(i);
		}
	}

}
