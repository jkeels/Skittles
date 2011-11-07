package skittles.g7;

import java.util.ArrayList;
import java.util.List;

import skittles.sim.Offer;

public class Strategy {
	
	private int numPlayers;
	private List<Friend> friends;
	
	private CandyBag bag;
	private Preference pref;
	
	private int numCandiesEatenOnLastTurn;
	private int colorEatenOnLastTurn;
	
	private double currentHappiness = 0;
	
	public Strategy(int numPlayers, CandyBag bag){
		this.numPlayers = numPlayers;
		friends = new ArrayList<Friend>(numPlayers);
		this.bag = bag;
	}
	
	public void updateHappiness(double happiness){
		
		currentHappiness += happiness;

		double happinessPerCandy = happiness/ Math.pow(numCandiesEatenOnLastTurn, 2);
		Double rawPref = pref.getRawPreference(colorEatenOnLastTurn); 
		if (rawPref == Double.NaN) {
			pref.updatePreference(colorEatenOnLastTurn, happinessPerCandy);
		} else {
			if (rawPref != happinessPerCandy) {
				System.out.println("Error: Inconsistent color happiness!");
			}
		}
	
	}
	
	public void getNextSnack(int[] snack){
		//TODO: complete this method
	}
	
	public void getNextTradeOffer(Offer temp){
		// TODO: 
	}

}
