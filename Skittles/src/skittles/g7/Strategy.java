package skittles.g7;

import java.util.ArrayList;
import java.util.List;

import skittles.sim.Offer;

public class Strategy {
	
	private int numPlayers;
	private List<Friend> friends;
	
	private CandyBag bag;
	private TradeHistory tradeHistory;
	
	private int numCandiesEatenOnLastTurn;
	private int colorEatenOnLastTurn;
	
	private double currentHappiness = 0;
	
	private boolean tasting = false;
	
	public Strategy(int numPlayers, CandyBag bag){
		this.numPlayers = numPlayers;
		friends = new ArrayList<Friend>(numPlayers);
		this.bag = bag;
		tradeHistory = new TradeHistory();
	}
	
	public void updateHappiness(double happiness){
		
		currentHappiness += happiness;

		double happinessPerCandy = happiness/ Math.pow(numCandiesEatenOnLastTurn, 2);
		Candy lastCandyEaten = bag.getCandy(colorEatenOnLastTurn); 
		if (!lastCandyEaten.isTasted()) {
			System.out.println("Setting pref for color: "+colorEatenOnLastTurn);
			lastCandyEaten.setPref(happinessPerCandy);
			lastCandyEaten.setTasted(true);
		} else {
			if (lastCandyEaten.getPref() != happinessPerCandy) {
				System.out.println("Error: Inconsistent color happiness!");
			}
		}
	
	}
	
	public void getNextSnack(int[] snack){
		int tempIndex = 0;
		int inHand = 0;
		int indexToTaste = -1;
		// Taste one of each skittle that we have in our hand, prioritizing to the skittle with the highest stack size
		while (tempIndex < bag.getNumColors()) {
			Candy tempCandy = bag.getCandy(tempIndex);
			if (!tempCandy.isTasted() && tempCandy.getRemaining() > inHand) {
				inHand = tempCandy.getRemaining();
				indexToTaste = tempIndex;
			}
			++tempIndex;
		}
		System.out.println("Index to taste: "+indexToTaste);
		tempIndex = indexToTaste;
		if (tempIndex < bag.getNumColors() && tempIndex >= 0) {
			tasting = true;
			colorEatenOnLastTurn = tempIndex;
			numCandiesEatenOnLastTurn = 1;
			snack[colorEatenOnLastTurn] = numCandiesEatenOnLastTurn;
			bag.removeCandy(tempIndex, 1);
			return;
		}
		// After this point, we've tasted everything that we're going to taste

		// Update our indices that we are hoarding
		
		// tasting means that our last skittle that we ate was due to us tasting a skittle, not because we were just eating one
		if(tasting){
			tasting = false;
			colorEatenOnLastTurn = -1;
		}
		
		// If we just ate some skittles, reset colorEatenOnLastTurn to -1 if we finished off the pile
		
		if(colorEatenOnLastTurn >= 0 && bag.getCandy(colorEatenOnLastTurn).getRemaining() == 0){
			colorEatenOnLastTurn = -1;
		}
		
		// If there is still more of the last thing we tasted, lets taste some more if its negative, or consult the oracle if its positive
		
		if(colorEatenOnLastTurn >= 0){
			Candy candy = bag.getLeastNegative();
			if (candy != null) {
				if (candy.compareTo(bag.getCandy(colorEatenOnLastTurn)) == 0) {
					numCandiesEatenOnLastTurn = 1;
				} else {
					// Use the oracle here to determine whether or not to eat one or eat all.  If the oracle returns true, we eat one.  Else we eat all.
					boolean oracle = false;
					if (oracle) {
						numCandiesEatenOnLastTurn = 1;
					} else {
						numCandiesEatenOnLastTurn = bag.getCandy(
								colorEatenOnLastTurn).getRemaining();
					}
				}
			}
		}
		
		
		// Find the skittle that will give us the least negative score (but still negative) and eat one of those.
			// This will give us trading time, and also cause us to not eat a lot of negative skittles in one go.
		if (colorEatenOnLastTurn < 0) {
			System.out.println("Retrieving least negative");
			Candy candy = bag.getLeastNegative();
			if(candy != null){
				colorEatenOnLastTurn = candy.getColor();
				numCandiesEatenOnLastTurn = 1;
			}
			
		}
		// After this point, if colorEatenOnLastTurn == -1, then we have no more negative valued skittles.

		// Now find our smallest positive valued skittle that isnt one of our indicies to hoard
		if (colorEatenOnLastTurn < 0) {
			System.out.println("Retrieving least positive");
			colorEatenOnLastTurn = bag.getLeastPositive().getColor();
			numCandiesEatenOnLastTurn = 1;
		}
		
		// Update the aintInHand array
		snack[colorEatenOnLastTurn] = numCandiesEatenOnLastTurn;
		System.out.println("Color to be eaten: "+colorEatenOnLastTurn);
		System.out.println("Num candies to be eaten: "+numCandiesEatenOnLastTurn);
		bag.removeCandy(colorEatenOnLastTurn, numCandiesEatenOnLastTurn);
	}
	
	public void getNextTradeOffer(Offer temp){
		// TODO: 
	}
	
	
}
