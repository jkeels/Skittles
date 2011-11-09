package skittles.g7;

import java.util.LinkedList;
import java.util.List;

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
	
	private List<Offer> tradesOfferedByMe = new LinkedList<Offer>();
	
	public static boolean wasOfferAccepted(Offer offer){
		return offer.getPickedByIndex() != -1;
	}
	
	public void recordTradeOfferedByMe(Offer offer){
		tradesOfferedByMe.add(offer);
	}
	
	public Offer getNextTradeOffer(Offer currentOffer){
		//Offer newOffer = new Offer(offer.getOfferedByIndex(), 8);
		int n = tradesOfferedByMe.size();
		for(int i = n-1; i >= n-1-LOOKBACK; i--){
			Offer oldOffer = tradesOfferedByMe.get(i);
			if(!wasOfferAccepted(oldOffer)){
				int[] oldOfferedColors = oldOffer.getDesire();
				for(int color = 0; color < oldOfferedColors.length; color++){
					
				}
			}
			
		}
		return null;
	}

}
