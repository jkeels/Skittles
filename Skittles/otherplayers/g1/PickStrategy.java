package g1player;

import java.util.ArrayList;

import skittles.sim.Offer;

public class PickStrategy {

	public Offer pick(Offer[] aoffCurrentOffers, Infobase info) {
		/* array to keep track of the 'score' of offers.  Scores indicate how much the trade would help/hurt us */
		double[] offerScores = new double[aoffCurrentOffers.length];
		/* keeps track of which offers are giving us goal skittles */
		ArrayList<Integer> offersGainingGoalSkittles = new ArrayList<Integer>();
		
		/* make sure the tables in infobase are created */
		if (!info.tablesExist())
		{
			info.createTable(aoffCurrentOffers.length);
		}
		
		/* for each offer, analyze how much it's worth to us */
		for (int j = 0; j < aoffCurrentOffers.length; ++j)
		{
			Offer o = aoffCurrentOffers[j];
			if (o.getOfferLive())
			{
				double score = 0; // initialize score
				int[] weReceive = o.getOffer();
				int[] weGiveUp = o.getDesire();
				/* for each color, add up how our score would change if we accepted the offer */
				for (int i = 0; i < weReceive.length; ++i)
				{
					score += weReceive[i] * info.getColorHappiness(i);
					score -= weGiveUp[i] * info.getColorHappiness(i);
					/* if it's a skittle we'd be receiving and it's a goal skittle, keep track of it */
					if (weReceive[i] > 0 && info.getPriority().getHighestPriorityColor() == i)
					{
						offersGainingGoalSkittles.add(j);
					}
				}
			
				offerScores[j] = score; // set score for this offer
			}
			else
			{
				offerScores[j] = Double.NEGATIVE_INFINITY;
			}
		}
		
		/* based on analysis, choose best offer (or no offer if none are good) */
		if (offersGainingGoalSkittles.size() == 0)
		{
			// if there are no trades giving us our goal skittle, don't take any trades
			return null;
		}
		else
		{
			int bestIndex = -1; // initialize best offer to non-existent index
			double bestScore = -1000; // initialize best score to very low score
			for (int i : offersGainingGoalSkittles)
			{
				// if this offer has a better score than the best
				if (offerScores[i] > bestScore && canAffordTrade(aoffCurrentOffers[i].getDesire(), info))
				{
					bestIndex = i;
					bestScore = offerScores[i];
				}
			}
			
			if (bestIndex != -1)
			{
				/* update the skittles we have */
				int[] skittlesWeHave = info.getAintInHand();
				int[] getting = aoffCurrentOffers[bestIndex].getOffer();
				int[] giving = aoffCurrentOffers[bestIndex].getDesire();
				for (int j = 0; j < getting.length; ++j)
				{
					//this seems backward, but it's working
					skittlesWeHave[j] += giving[j];
					skittlesWeHave[j] -= getting[j];
				}	
				info.setAintInHand(skittlesWeHave);

				/* return chosen offer */
				return aoffCurrentOffers[bestIndex];
			}
			else
			{
				return null;
			}
		}		
	}
	
	private boolean canAffordTrade(int[] giving, Infobase info)
	{
		int[] skittlesWeHave = info.getAintInHand();
		for(int i = 0; i < giving.length; ++i)
		{
			if (!(skittlesWeHave[i] >= giving[i]))
				return false;
		}
		return true;
	}
}
