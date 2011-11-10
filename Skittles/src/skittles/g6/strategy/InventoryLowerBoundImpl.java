package skittles.g6.strategy;

import skittles.g6.CompulsiveEater;
import skittles.sim.Offer;

public class InventoryLowerBoundImpl implements InventoryLowerBound {

	private int[][] inventory;
	
	public void setPlayer(CompulsiveEater player) {
		int total = 0;
		
		inventory = new int[player.getPlayerNum()][player.getAIntInHand().length];
		
		for (int each : player.getAIntInHand()) {
			total+=each;
		}
		
		for (int i = 0; i < inventory.length; i++) {
			for (int j = 0; j < inventory[0].length; j++) {
				inventory[i][j] = 0;
			}
		}
	}

	public void decay() {
		for (int i = 0; i < inventory.length; i++) {
			for (int j = 0; j < inventory[0].length; j++) {
				inventory[i][j]--;
			}
		}
	}
	
	public void setCurrentOffers(Offer[] offers) {
		for (Offer offer : offers) {
			
			int giver = offer.getOfferedByIndex();
			int taker = offer.getPickedByIndex();
			
			int i = 0;
			for (int amt : offer.getOffer()) {
				if (amt>0 && amt>inventory[giver][i]) {
					inventory[giver][i] = amt;
				}
				i++;
			}
			
			if (taker < 0) continue;
			
			i = 0;
			for (int amt : offer.getDesire()) {
				if (amt>0 && amt>inventory[taker][i]) {
					inventory[taker][i] = amt;
				}
				i++;
			}
		}
	}

	// never query about our own player
	public int queryLowerBound(int playerId, int color) {
		return inventory[playerId][color];
	}

}
