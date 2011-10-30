package skittles.g7;

import java.util.Random;

import skittles.sim.Offer;
import skittles.sim.Player;

public class G7Player extends Player {
	private int[] aintInHand;
	private int intColorNum;
	double dblHappiness;
	String strClassName;
	int intPlayerIndex;

	private double[] adblTastes;
	private int intLastEatIndex;
	private int intLastEatNum;

	private Random random = new Random();

	private int indexToHoard;
	private int turnNumber;
	private boolean[] isTasted;

	// public DumpPlayer( int[] aintInHand )
	// {
	// this.aintInHand = aintInHand;
	// intColorNum = aintInHand.length;
	// dblHappiness = 0;
	// }

	@Override
	public void eat(int[] aintTempEat) {

		System.out.println("Before eating, skittles: ");
		for (int i = 0; i < intColorNum; i++) {
			System.out.print(aintInHand[i] + " ");
		}
		System.out.println();
		
		// Taste one of each for the first five turns
		while (turnNumber < intColorNum) {
			if (aintInHand[turnNumber] > 0) {

				break;
			}
			++turnNumber;
		}

		if (turnNumber < intColorNum) {
			intLastEatIndex = turnNumber;
			intLastEatNum = 1;
			aintTempEat[intLastEatIndex] = intLastEatNum;
			isTasted[intLastEatIndex] = true;
			aintInHand[intLastEatIndex]--;
			++turnNumber;
			return;
		}

		// Now sort through the adblTaste array to find our happiest color and
		// make that our color to hoard
		double currentBest = -2.0;
		for (int i = 0; i < intColorNum; ++i) {
			if (adblTastes[i] > currentBest) {
				indexToHoard = i;
				currentBest = adblTastes[i];
			}
		}

		// For the stupid iteration, find the skittle that will lose us the least happiness and eat one of those
		// This will give us some time to trade stuff
		intLastEatIndex = -1;
		intLastEatNum = -1;

		currentBest = -2.0;
		for (int i = 0; i < intColorNum; ++i) {
			if (adblTastes[i] < 0 && adblTastes[i] > currentBest && aintInHand[i] > 0) {
				intLastEatIndex = i;
				intLastEatNum = 1;
				currentBest = adblTastes[i];
			}
		}

		// Now that we're out of negative-score skittles, find the skittle that gives us the least positive happiness
		// and eat all of that color.  This will still give us a little bit of trading time to get more of our higher value
		// skittles

		currentBest = 2.0;
		if (intLastEatIndex < 0) {
			for (int i = 0; i < intColorNum; ++i) {
				if (i != indexToHoard && aintInHand[i] > 0 && adblTastes[i] < currentBest) {
					intLastEatIndex = i;
					intLastEatNum = aintInHand[i];
					currentBest = adblTastes[i];
				}
			}
		}

		// Now eat the hoard!
		if (intLastEatIndex < 0) {
			intLastEatIndex = indexToHoard;
			intLastEatNum = aintInHand[intLastEatIndex];
		}
		
		// Update the aintInHand array
		aintTempEat[intLastEatIndex] = intLastEatNum;
		aintInHand[intLastEatIndex] -= intLastEatNum;

		System.out.println("After eating, skittles: ");
		for (int i = 0; i < intColorNum; i++) {
			System.out.print(aintInHand[i] + " ");
		}
	}

	@Override
	public void offer(Offer offTemp) {
		/**
		 * 
		 * Always ask for what you like the most and always offer what you hate
		 * the most If you dont know your tastes make empty offer
		 * 
		 */

		int numExchanged = random.nextInt(5);
		int fav = -1;
		int hate = -1;
		double min = 1.0;
		double max = -1.0;
		for (int i = 0; i < intColorNum; i++) {
			if (isTasted[i]) {
				if (adblTastes[i] < min) {
					min = adblTastes[i];
					hate = i;
				}
				if (adblTastes[i] > max) {
					max = adblTastes[i];
					fav = i;
				}
			}
		}

		int[] bid = new int[intColorNum];
		int[] ask = new int[intColorNum];

		if (fav != -1 && hate != -1) {

			while (aintInHand[hate] < numExchanged)
				numExchanged--;

			bid[hate] = numExchanged;
			ask[fav] = numExchanged;
		}

		System.out.println("In hand: ");
		for (int i = 0; i < intColorNum; i++) {
			System.out.print(aintInHand[i] + " ");
		}

		System.out.print("\nBid: ");
		for (int i = 0; i < intColorNum; i++) {
			System.out.print(bid[i] + " ");
		}
		System.out.println();
		System.out.print("Ask: ");
		for (int i = 0; i < intColorNum; i++) {
			System.out.print(ask[i] + " ");
		}
		System.out.println();
		offTemp.setOffer(bid, ask);
	}

	@Override
	public void syncInHand(int[] aintInHand) {
		// TODO Auto-generated method stub

	}

	@Override
	public void happier(double dblHappinessUp) {
		double dblHappinessPerCandy = dblHappinessUp
				/ Math.pow(intLastEatNum, 2);
		if (adblTastes[intLastEatIndex] == -1) {
			adblTastes[intLastEatIndex] = dblHappinessPerCandy;
		} else {
			if (adblTastes[intLastEatIndex] != dblHappinessPerCandy) {
				System.out.println("Error: Inconsistent color happiness!");
			}
		}
	}

	@Override
	public Offer pickOffer(Offer[] aoffCurrentOffers) {
		Offer offReturn = null;
		for (Offer offTemp : aoffCurrentOffers) {
			if (offTemp.getOfferedByIndex() == intPlayerIndex
					|| offTemp.getOfferLive() == false)
				continue;
			int[] aintDesire = offTemp.getDesire();
			int[] aintGive = offTemp.getOffer();
			if (checkEnoughInHand(aintDesire)) {
				double receive = 0.0;
				double giveUp = 0.0;
				if(indexToHoard < 0 || aintDesire[indexToHoard] > 0){
					continue;
				}
				if(indexToHoard >= 0 && aintGive[indexToHoard] > 0){
					offReturn = offTemp;
				}
				if (offReturn == null) {
					for (int i = 0; i < intColorNum; ++i) {
						receive += adblTastes[i]*(Math.pow(aintDesire[i], 2));
						giveUp += adblTastes[i]*(Math.pow(aintGive[i], 2));
					}
					if(receive - giveUp > 0){
						offReturn = offTemp;
					}
				}
				if (offReturn != null) {
					for (int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex++) {
						aintInHand[intColorIndex] += aintGive[intColorIndex]
								- aintDesire[intColorIndex];
					}
					break;
				}
			}
		}
		return offReturn;
	}

	@Override
	public void offerExecuted(Offer offPicked) {
		int[] aintOffer = offPicked.getOffer();
		int[] aintDesire = offPicked.getDesire();
		for (int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex++) {
			aintInHand[intColorIndex] += aintDesire[intColorIndex]
					- aintOffer[intColorIndex];
		}
	}

	@Override
	public void updateOfferExe(Offer[] aoffCurrentOffers) {
		// dumpplayer doesn't care
	}

	@Override
	public void initialize(int intPlayerIndex, String strClassName,
			int[] aintInHand) {
		this.intPlayerIndex = intPlayerIndex;
		this.strClassName = strClassName;
		this.aintInHand = aintInHand;
		intColorNum = aintInHand.length;
		dblHappiness = 0;
		indexToHoard = -1;
		adblTastes = new double[intColorNum];
		turnNumber = 0;
		isTasted = new boolean[intColorNum];
		for (int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex++) {
			isTasted[intColorIndex] = false;
			adblTastes[intColorIndex] = -1;
		}
	}

	private boolean checkEnoughInHand(int[] aintTryToUse) {
		for (int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex++) {
			if (aintTryToUse[intColorIndex] > aintInHand[intColorIndex]) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String getClassName() {
		return "DumpPlayer";
	}

	@Override
	public int getPlayerIndex() {
		return intPlayerIndex;
	}

}
