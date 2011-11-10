package skittles.g7;

import java.util.ArrayList;
import java.util.Random;

import skittles.sim.Offer;
import skittles.sim.Player;

public class G7Player extends Player {
	private int[] aintInHand;
	private int intColorNum;
	double dblHappiness;
	String strClassName;
	int intPlayerIndex;
	
	private static boolean DEBUG = false;

	private double[] adblTastes;
	private int intLastEatIndex;
	private int intLastEatNum;

	private Random random = new Random();

	private int turnNumber;
	private boolean[] isTasted;
	private boolean tasting;
	
	private marketKnowledge[] market;
	private hoardingObject indicesToHoard;

	private class hoardingObject {
		ArrayList<Integer> hoardingIndices = new ArrayList<Integer>();
		int maxSize;

		public hoardingObject() {
			maxSize = 1;
		}
		
		public hoardingObject(int size) {
			maxSize = size;
		}

		public int size() {
			return hoardingIndices.size();
		}

		public int get(int index) {
			return hoardingIndices.get(index);
		}

		public void add(int indexToAdd) {
			hoardingIndices.add(indexToAdd);
			this.reorder();
			if(hoardingIndices.size() > maxSize){
				hoardingIndices.subList(maxSize, hoardingIndices.size());
			}
		}
		
		public void update(){
			for(int i = 0; i < intColorNum; ++i){
				if(hoardingIndices.contains(i) || !isTasted[i] || aintInHand[i] == 0){
					continue;
				}
				this.add(i);
			}
		}
		
		public void reorder(){
			if(hoardingIndices.size() <= 1){
				return;
			}
			boolean modified = false;
			for(int i = 0; i < hoardingIndices.size() - 1; ++i){
				int firstIndex = hoardingIndices.get(i);
				int secondIndex = hoardingIndices.get(i + 1);
				
				if(aintInHand[firstIndex] == 0){
					hoardingIndices.remove(i);
					--i;
					continue;
				}
				if(aintInHand[secondIndex] == 0){
					hoardingIndices.remove(i + 1);
					--i;
					continue;
				}
				
				// I already had the indices, so why waste a function call that will just get them again?
				double firstValue = this.valueOfExternalIndex(firstIndex);
				double secondValue = this.valueOfExternalIndex(secondIndex);
				
				if(firstValue < secondValue){
					modified = true;
					hoardingIndices.set(i, secondIndex);
					hoardingIndices.set(i+1, firstIndex);
				}
			}
			
			if(modified){
				if(hoardingIndices.size() < maxSize){
					this.update();
				}
				this.reorder();
			}
		}
		
		public double valueOfInternalIndex(int index){
			int valueIndex = hoardingIndices.get(index);
			return valueOfExternalIndex(valueIndex);
		}
		
		public double valueOfExternalIndex(int index){
			return (adblTastes[index] * Math.pow(aintInHand[index], 2));
		}
		
		public boolean equals(int index){
			return hoardingIndices.contains(index);
		}
		
		public int topIndex(){
			return hoardingIndices.get(0);
		}
	}

	private class marketKnowledge {
		ArrayList<Double> colorKnowledge = new ArrayList<Double>();

		public marketKnowledge() {
			for (int i = 0; i < intColorNum; ++i) {
				colorKnowledge.add(0.0);
			}
		}

		public double getColorInfo(int index) {
			return colorKnowledge.get(index);
		}

		public void addColorInfo(int index, int delta) {
			colorKnowledge.set(index, colorKnowledge.get(index) + delta);
		}

		public void decay() {
			for (int i = 0; i < colorKnowledge.size(); ++i) {
				if (colorKnowledge.get(i) > 0) {
					colorKnowledge.set(i, colorKnowledge.get(i) * 0.8);
				}
			}
		}

		public int getMaxColorIndex() {
			double max = -2.0;
			int maxIndex = -1;
			for (int i = 0; i < colorKnowledge.size(); ++i) {
				if (colorKnowledge.get(i) > max) {
					max = colorKnowledge.get(i);
					maxIndex = i;
				}
			}
			return maxIndex;
		}

		public int getMinColorIndex() {
			double min = 2.0;
			int minIndex = -1;
			for (int i = 0; i < colorKnowledge.size(); ++i) {
				if (colorKnowledge.get(i) < min) {
					min = colorKnowledge.get(i);
					minIndex = i;
				}
			}
			return minIndex;
		}
	}

	// public DumpPlayer( int[] aintInHand )
	// {
	// this.aintInHand = aintInHand;
	// intColorNum = aintInHand.length;
	// dblHappiness = 0;
	// }

	@Override
	public void eat(int[] aintTempEat) {

		if (DEBUG) {
			System.out.println("Before eating, skittles: ");
			for (int i = 0; i < intColorNum; i++) {
				System.out.print(aintInHand[i] + " ");
			}
			System.out.println();
		}
		
		int tempIndex = 0;
		int inHand = 0;
		int indexToTaste = -1;
		// Taste one of each skittle that we have in our hand, prioritizing to the skittle with the highest stack size
		while (tempIndex < intColorNum) {
			if (!isTasted[tempIndex] && aintInHand[tempIndex] > inHand) {
				inHand = aintInHand[tempIndex];
				indexToTaste = tempIndex;
			}
			++tempIndex;
		}
		tempIndex = indexToTaste;
		if (tempIndex < intColorNum && tempIndex >= 0) {
			tasting = true;
			intLastEatIndex = tempIndex;
			intLastEatNum = 1;
			aintTempEat[intLastEatIndex] = intLastEatNum;
			isTasted[intLastEatIndex] = true;
			aintInHand[intLastEatIndex]--;
			++turnNumber;
			indicesToHoard.add(intLastEatIndex);
			return;
		}
		// After this point, we've tasted everything that we're going to taste

		// Update our indices that we are hoarding
		indicesToHoard.update();
		indicesToHoard.reorder();
		
		// tasting means that our last skittle that we ate was due to us tasting a skittle, not because we were just eating one
		if(tasting){
			tasting = false;
			intLastEatIndex = -1;
		}
		
		// If we just ate some skittles, reset intLastEatIndex to -1 if we finished off the pile
		
		if(intLastEatIndex >= 0 && aintInHand[intLastEatIndex] == 0){
			intLastEatIndex = -1;
		}
		
		// If there is still more of the last thing we tasted, lets taste some more if its negative, or consult the oracle if its positive
		
		if(intLastEatIndex >= 0){
			if(adblTastes[intLastEatIndex] < 0){
				intLastEatNum = 1;
			} else {
				// Use the oracle here to determine whether or not to eat one or eat all.  If the oracle returns true, we eat one.  Else we eat all.
				boolean oracle = false;
				if(oracle){
					intLastEatNum = 1;
				} else {
					intLastEatNum = aintInHand[intLastEatIndex];
				}
			}
		}
		
		
		// Find the skittle that will give us the least negative score (but still negative) and eat one of those.
			// This will give us trading time, and also cause us to not eat a lot of negative skittles in one go.
		double currentBest = -2.0;
		if (intLastEatIndex < 0) {
			for (int i = 0; i < intColorNum; ++i) {
				if (adblTastes[i] < 0 && adblTastes[i] > currentBest
						&& aintInHand[i] > 0) {
					intLastEatIndex = i;
					intLastEatNum = 1;
					currentBest = adblTastes[i];
				}
			}
		}
		// After this point, if intLastEatIndex == -1, then we have no more negative valued skittles.

		// Now find our smallest positive valued skittle that isnt one of our indicies to hoard
		currentBest = 2.0;
		if (intLastEatIndex < 0) {
			for (int i = 0; i < intColorNum; ++i) {
				if (!indicesToHoard.equals(i) && aintInHand[i] > 0
						&& adblTastes[i] < currentBest) {
					intLastEatIndex = i;
					intLastEatNum = 1;
					//intLastEatNum = aintInHand[i];
					currentBest = adblTastes[i];
				}
			}
		}

		// Now eat the hoard! (well, we eat the one that gives us less happiness first)
		if (intLastEatIndex < 0) {
			intLastEatIndex = indicesToHoard.get(indicesToHoard.size()-1);
			intLastEatNum = aintInHand[intLastEatIndex];
		}

		// Update the aintInHand array
		aintTempEat[intLastEatIndex] = intLastEatNum;
		aintInHand[intLastEatIndex] -= intLastEatNum;
		
		if (DEBUG) {
			System.out.println("After eating, skittles: ");
			for (int i = 0; i < intColorNum; i++) {
				System.out.print(aintInHand[i] + " ");
			}
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

		int numExchanged = random.nextInt(5) + 1;
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

		double highestFavColorValue = -1;
		int highestFavColorIndex = -1;
		for (marketKnowledge mk : market) {
			for (int i = 0; i < intColorNum; i++) {
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
			while (aintInHand[highestFavColorIndex] < numExchanged)
				numExchanged--;
			bid[highestFavColorIndex] = numExchanged;
			ask[fav] = numExchanged;

		}

		if (DEBUG) {
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
		}
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
		double highestPotentialScore = 0; // can change this in future phases
		for (Offer offTemp : aoffCurrentOffers) {
			if (offTemp.getOfferedByIndex() == intPlayerIndex
					|| offTemp.getOfferLive() == false)
				continue;
			int[] currentDesire = offTemp.getDesire();
			// int[] currentOffer = offTemp.getOffer();
			double currentPotentialScore = checkOffer(offTemp);
			// Check to see if we have enough to even go through with this trade
			// before we deliberate
			if (checkEnoughInHand(currentDesire)) {
				// check if offer is worth accepting. right now if its greater
				// than 0
				if (currentPotentialScore > highestPotentialScore) {
					// if here, than its the current best offer for us
					offReturn = offTemp;
					highestPotentialScore = currentPotentialScore;
				}
			}
		}

		if (highestPotentialScore > 0) {
			int[] tempDesire = offReturn.getDesire();
			tempDesire = offReturn.getDesire();
			int[] tempOffer = offReturn.getOffer();
			for (int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex++) {
				aintInHand[intColorIndex] += tempOffer[intColorIndex]
						- tempDesire[intColorIndex];
			}
		}
		return offReturn;
	}

	public double checkOffer(Offer offer) {

		double differenceInScore = 0;
		int[] tempDesire = offer.getDesire();
		int[] tempOffer = offer.getOffer();
		for (int i = 0; i < aintInHand.length; i++) {
			// if its a color we like, update potential score
			if (adblTastes[i] > 0) {
				differenceInScore += (adblTastes[i]
						* Math.pow((tempOffer[i] + aintInHand[i]), 2) - (adblTastes[i] * Math
						.pow(aintInHand[i], 2)));
				differenceInScore -= (adblTastes[i] * Math
						.pow(aintInHand[i], 2))
						- (adblTastes[i] * Math.pow(
								(aintInHand[i] - tempDesire[i]), 2));
			} else {
				differenceInScore += (adblTastes[i] * tempOffer[i]);
				differenceInScore -= (adblTastes[i] * tempDesire[i]);
			}

		}

		return differenceInScore;
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
		for (marketKnowledge mk : market) {
			mk.decay();
		}
		for (Offer off : aoffCurrentOffers) {
			int giverIndex = off.getOfferedByIndex();
			int[] givingUp = off.getOffer();
			int[] wants = off.getDesire();

			for (int i = 0; i < intColorNum; ++i) {
				market[giverIndex].addColorInfo(i, wants[i] - givingUp[i]);
			}
			if (off.getPickedByIndex() != -1) {
				givingUp = off.getDesire();
				wants = off.getOffer();
				for (int i = 0; i < intColorNum; ++i) {
					market[off.getPickedByIndex()].addColorInfo(i, givingUp[i]
							- wants[i]);
				}
			}
		}
	}

	@Override
	public void initialize(int PlayerNum, double wtfShen, int intPlayerIndex,
			String strClassName, int[] aintInHand) {
		this.intPlayerIndex = intPlayerIndex;
		this.strClassName = strClassName;
		this.aintInHand = aintInHand;
		intColorNum = aintInHand.length;
		dblHappiness = 0;
		adblTastes = new double[intColorNum];
		turnNumber = 0;
		isTasted = new boolean[intColorNum];
		tasting = false;
		
		// TODO: Come up with a heuristic and call new hoardingObject(int size)
		indicesToHoard = new hoardingObject();

		market = new marketKnowledge[PlayerNum];
		for (int i = 0; i < PlayerNum; ++i) {
			market[i] = new marketKnowledge();
		}

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
