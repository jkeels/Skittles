package skittles.g7;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import skittles.sim.Offer;

public class TradeHistory {
	
	/**
	 * 
	 * Maintain history of all offers that have been executed
	 * 
	 * Maintain history of all offers made by the other players
	 * 
	 * Recent trades for each color 
	 * 
	 * 
	 * 
	 */
	
	private static final int LOOKBACK = 3;
	private static final int STEP = 2;
	private static Random random = new Random();
	private static final boolean DEBUG = true;
	
	private List<Offer> tradesOfferedByMe = new LinkedList<Offer>();
	private List<Offer>[] tradesByOthers;
	
	public static boolean wasOfferAccepted(Offer offer){
		return offer.getPickedByIndex() != -1;
	}
	
	public void recordTradeOfferedByMe(Offer offer){
		tradesOfferedByMe.add(offer);
	}
	
	public void getNextTradeOffer(Offer currentOffer, CandyBag bag, MarketKnowledge[] market){
		
		int numColors = bag.getNumColors();
		List<Candy> candies = bag.sortByGain();
		int[] bid = new int[numColors];
		int[] ask = new int[numColors];
		
		int fav = candies.get(0).getColor();
		int numExchanged = 0;
		
		if(!tradesOfferedByMe.isEmpty()){
			Offer oldOffer = tradesOfferedByMe.get(tradesOfferedByMe.size()-1);
			if(!wasOfferAccepted(oldOffer) && oldOffer.getDesire()[fav] > 0 ){
				numExchanged = oldOffer.getDesire()[fav] - STEP;
				if(numExchanged <= 0){
					fav = candies.get(1).getColor();
				}
			}
		}
			
		double highestFavColorValue = -1;
		int highestFavColorIndex = -1;
		for (MarketKnowledge mk : market) {
			for (int i = 0; i < numColors; i++) {
				// retrieves the highest color value and makes sure its not our
				// favorite color
				if (mk.getColorInfo(i) > highestFavColorValue && i != fav) {
					highestFavColorValue = mk.getColorInfo(i);
					highestFavColorIndex = i;
				}
			}
			
			if (DEBUG) {
				System.out.println("highest color value: "
						+ highestFavColorValue + " with index:"
						+ highestFavColorIndex);
			}
		}

		if (highestFavColorValue != -1) {
			while (bag.getCandy(highestFavColorIndex).getRemaining() < numExchanged)
				numExchanged--;
			bid[highestFavColorIndex] = numExchanged;
			ask[fav] = numExchanged;

		}

		currentOffer.setOffer(bid, ask);

	}
	
	
	

}
