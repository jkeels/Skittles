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
				if(bid[color] != 0 || ask[color] != 0 )
						liquidity[player][color] = bid[color] - ask[color];
			}
		}
	}
	
	
	public int getTradeSize(Player me, int color){
		int numExchanged = random.nextInt(OFFER_SIZE)+1;
		int min=0;
		for(int player = 0; player < liquidity.length; player++){
			if(player != me.getPlayerIndex() && liquidity[player][color] > min){
				min = liquidity[player][color];
			}
		}
		if(min > 0){
			numExchanged = min;
		}
		return numExchanged;
	}
	
	public void getNextTradeOffer(Player me, Offer currentOffer, CandyBag bag, MarketKnowledge[] market){
		
		int numColors = bag.getNumColors();
		List<Candy> candies = bag.sortByGain();
		int[] bid = new int[numColors];
		int[] ask = new int[numColors];
		
		int fav = candies.get(0).getColor();
		int numExchanged = getTradeSize(me, fav);
		
		if(!tradesOfferedByMe.isEmpty()){
			Offer oldOffer = tradesOfferedByMe.get(tradesOfferedByMe.size()-1);
			if(!wasOfferAccepted(oldOffer) && oldOffer.getDesire()[fav] > 0 ){
				numExchanged = oldOffer.getDesire()[fav] - STEP;
				if(numExchanged <= 0 && candies.size() > 1){
					fav = candies.get(1).getColor();
					numExchanged = getTradeSize(me, fav);
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
