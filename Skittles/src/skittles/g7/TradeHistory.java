package skittles.g7;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import skittles.sim.Offer;
import skittles.sim.Player;

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
	private static final int OFFER_SIZE = 5;
	private static Random random = new Random();
	private static final boolean DEBUG = true;
	private int[][] liquidity;
	
	
	private List<Offer> tradesOfferedByMe = new LinkedList<Offer>();
	private Map<Integer, List<Offer>> tradesByOthers;
	
	public TradeHistory(int myID, int numPlayers, int numColors){
		tradesByOthers = new HashMap<Integer, List<Offer>>();
		
		liquidity = new int[numPlayers][];
		for(int i = 0; i < numPlayers; i++){
			if(i != myID) tradesByOthers.put(i, new ArrayList<Offer>());
			liquidity[i] = new int[numColors];
		}
	}
	
	public List<Offer> getTradesOfferedByMe() {
		return tradesOfferedByMe;
	}

	public static boolean wasOfferAccepted(Offer offer){
		return offer.getPickedByIndex() != -1;
	}
	
	public void recordTradeOffered(Player me, Offer offer){
		int player = offer.getOfferedByIndex(); 
		if(player == me.getPlayerIndex()){
			tradesOfferedByMe.add(offer);
		}else{
			tradesByOthers.get(offer.getOfferedByIndex()).add(offer);
			int[] bid = offer.getOffer();
			int[] ask = offer.getDesire();
			for(int color = 0; color < bid.length; color++){
				//if(bid[color] != 0 || ask[color] != 0 )
						liquidity[player][color] = bid[color] - ask[color];
			}
		}
	}
	
	
	public int getTradeSize(Player me, CandyBag bag, int color, Integer partner, int[] colorsToGiveUp){
		int numExchanged = random.nextInt(OFFER_SIZE)+1;
		int min=0;
		int prospectivePartner = -1;
		//Candy[] sortedCandies = bag.sortByGain().toArray(new Candy[bag.getNumColors()]);
		
		for(int player = 0; player < liquidity.length; player++){
			if(player != me.getPlayerIndex() && liquidity[player][color] > min){
				min = liquidity[player][color];
				prospectivePartner = player;
				for(int c = 0; c < liquidity[player].length; c++){
					if(c != color && liquidity[player][c] < 0){
						int toGiveUp = -liquidity[player][c];
						if(bag.getCandy(c).getRemaining() >= toGiveUp ){
							colorsToGiveUp[c] = toGiveUp;
						}else{
							colorsToGiveUp[c] = bag.getCandy(c).getRemaining();
						}
						
					}
				}
			}
		}
		if(min > 0){
			numExchanged = min;
			partner = prospectivePartner;
		}else{
			partner = -1;
		}
		return numExchanged;
	}
	
	public void getNextTradeOffer(Player me, Offer currentOffer, CandyBag bag, MarketKnowledge[] market){
		
		int numColors = bag.getNumColors();
		List<Candy> candies = bag.sortByGain();
		int[] bid = new int[numColors];
		int[] ask = new int[numColors];
		
		int fav = candies.get(0).getColor();
		Integer partner = new Integer(-1);
		int[] colorsToGiveUp = new int[bag.getNumColors()];
		int numExchanged = getTradeSize(me, bag, fav, partner, colorsToGiveUp);
		
		if(!tradesOfferedByMe.isEmpty()){
			Offer oldOffer = tradesOfferedByMe.get(tradesOfferedByMe.size()-1);
			if(!wasOfferAccepted(oldOffer) && oldOffer.getDesire()[fav] > 0 ){
				numExchanged = oldOffer.getDesire()[fav] - STEP;
				if(numExchanged <= 0 && candies.size() > 1){
					fav = candies.get(1).getColor();
					numExchanged = getTradeSize(me, bag, fav, partner, colorsToGiveUp);
				}
			}
		}
		
		int numToGiveUp = 0;
		for(int i=0; i < numColors; i++){
			numToGiveUp += colorsToGiveUp[i];
		}
		if(numExchanged > numToGiveUp){
			numExchanged = numToGiveUp;
		}
		
		// Now we need to check if we are giving up something that we hoard
		Offer tempOffer = new Offer(me.getPlayerIndex(), numColors);
		int[] tempAsk = new int[numColors];
		tempAsk[fav] = numExchanged;
		tempOffer.setOffer(tempAsk, colorsToGiveUp);
		double gain = Strategy.checkOffer(bag, tempOffer);
		
		if(gain > 0){
			currentOffer.setOffer(colorsToGiveUp, tempAsk);
		}else{
			
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
	
	
	

}
