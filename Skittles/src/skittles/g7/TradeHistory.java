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
	private static final boolean DEBUG = false;
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
		if(numExchanged == 0){
			if (DEBUG || true) {
				System.out
						.println("+_++++++++++++++++++++++*****************$$$$$$$$$$$$$$$$$$$$$@@@@@@@@@@@@@@@@@@@++++++++++++++++++_++++++++++++++++++++++*****************$$$$$$$$$$$$$$$$$$$$$@@@@@@@@@@@@@@@@@@@++++++++++++++++++_++++++++++++++++++++++*****************$$$$$$$$$$$$$$$$$$$$$@@@@@@@@@@@@@@@@@@@++++++++++++++++++_++++++++++++++++++++++*****************$$$$$$$$$$$$$$$$$$$$$@@@@@@@@@@@@@@@@@@@+++++++++++++++++");
			}
			List<Candy> prefList = bag.sortByPreference();
			List<Candy> gainList = bag.sortByGain();
			int index = 1;
			Candy wantColor = prefList.get(prefList.size() - index);
			while(wantColor.equals(gainList.get(index-1))){
				++index;
				try {
					wantColor = prefList.get(prefList.size() - index);
				} catch (IndexOutOfBoundsException e) {
					// TODO Auto-generated catch block
					wantColor = null;
				}
			}
			boolean skip = false;
			if(wantColor == null || wantColor.getPref() <= 0){
				skip = true;
			}
			if (!skip) {
				Candy giveColor = prefList.get(prefList.size() - index - 1);
				numExchanged = bag.switchThreshhold(giveColor, wantColor);
				int[] newBid = new int[bag.getNumColors()];
				int[] newAsk = new int[bag.getNumColors()];
				newBid[giveColor.getColor()] = numExchanged;
				newAsk[wantColor.getColor()] = numExchanged;
				currentOffer.setOffer(newBid, newAsk);
			}
		}
		
		/*if(numExchanged == 0){
			List<Candy> gainList = bag.sortByGain();
			int index = 1;
			Candy getRidOf = gainList.get(bag.getNumColors() - index);
			while (getRidOf.value() == 0){
				if(index >= bag.getNumColors() - 1){
					getRidOf = null;
					break;
				}
				++index;
				getRidOf = gainList.get(bag.getNumColors() - index);
			}
			boolean skip = false;
			if(getRidOf == null){
				skip = true;
			}
			if(!skip){
				 int min = 0;
				 int prospectivePartner = -1;
				 int color = getRidOf.getColor();
				 int [] colorsToGain = new int[numColors];
				 int toGain = 0;
				 
				 for(int player = 0; player < liquidity.length; player++){
						if(player != me.getPlayerIndex() && -liquidity[player][color] > min){
							min = -liquidity[player][color];
							prospectivePartner = player;
						}
					}
				 
				 
				 if(min != 0){
					 	toGain = 0;
						for(int c = 0; c < liquidity[prospectivePartner].length; c++){
							if(c != color && liquidity[prospectivePartner][c] > 0 &&
										goodToTrade(gainList, bag, color, bag.getNumColors() - index)){
								colorsToGain[c] = liquidity[prospectivePartner][c];
								toGain += colorsToGain[c];
							}
						}
					numExchanged = min > toGain ? toGain : min;
					int[] newBid = new int[bag.getNumColors()];
					newBid[color] = numExchanged;
					currentOffer.setOffer(newBid, colorsToGain);
				 }
			}
		}*/
	}
	
	
	private boolean goodToTrade(List<Candy> gainList, CandyBag bag, int color, int limit){
		int i;
		for(i=0; i<gainList.size(); i++){
			if(gainList.get(i).getColor() == color) break;
		}
		if(i < limit) return true;
		return false;
	}

}
