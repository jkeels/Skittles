package skittles.g7;

import java.util.Random;

import skittles.sim.Offer;
import skittles.sim.Player;

public class G7Player extends Player 
{
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

	
//	public DumpPlayer( int[] aintInHand )
//	{
//		this.aintInHand = aintInHand;
//		intColorNum = aintInHand.length;
//		dblHappiness = 0;
//	}

	@Override
	public void eat( int[] aintTempEat )
	{

		// Taste one of each for the first five turns
		while(turnNumber < intColorNum){
			if(aintInHand[turnNumber] > 0){

				break;
			}
			++turnNumber;
		}

		if(turnNumber < intColorNum){
			intLastEatIndex = turnNumber;
			intLastEatNum = 1;
			aintTempEat[intLastEatIndex] = intLastEatNum;
			isTasted[turnNumber] = true;
			aintInHand[turnNumber]--;
			++turnNumber;
			return;
		}
		
		// Now sort through the adblTaste array to find our happiest color and make that our color to hoard
		double currentBest = -2.0;
		for(int i = 0; i < intColorNum; ++i){
			if(adblTastes[i] > currentBest){
				indexToHoard = i;
				currentBest = adblTastes[i];
			}
		}
		
		// For the stupid iteration, pick a random skittle with happiness at least zero, thats not our hoarding color, and eat one of them
		intLastEatIndex = -1;
		intLastEatNum = -1;
		
		for(int i = 0; i < intColorNum; ++i){
			if(i != indexToHoard && adblTastes[i] >= 0 && aintInHand[i] > 0){
				intLastEatIndex = i;
				intLastEatNum = 1;
				break;
			}
		}
		
		// If we didnt find one to eat that gives us at least a happiness of zero, find the max of the rest
		if(intLastEatIndex < 0){
			currentBest = -2.0;
			for(int i = 0; i < intColorNum; ++i){
				if(i != indexToHoard && adblTastes[i] > currentBest && aintInHand[i] > 0){
					intLastEatIndex = i;
					intLastEatNum = 1;
					currentBest = adblTastes[i];
				}
			}
		}
		
		// If we still didnt find anything, then we're at the end of the game and should eat all of our hoard!
		if(intLastEatIndex < 0){
			intLastEatIndex = indexToHoard;
			intLastEatNum = aintInHand[intLastEatIndex];
		}
		
		aintTempEat[ intLastEatIndex ] = intLastEatNum;
		aintInHand[ intLastEatIndex ] -= intLastEatNum;
		
		System.out.println("After eating, skittles: ");
		for(int i=0; i < intColorNum; i++){
			System.out.print(aintInHand[i]+" ");
		}
	}
	
	@Override
	public void offer( Offer offTemp )
	{
		int intMaxColorIndex = 0;
		int intMaxColorNum = 0;
		int intMinColorIndex = 0;
		int intMinColorNum = Integer.MAX_VALUE;
		
		//print happiness logic
		for(int i = 0; i < adblTastes.length; i++)
			System.out.println("adblTastes["+i+"] = "+adblTastes[i]);
		
		for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
		{
			if ( aintInHand[ intColorIndex ] > intMaxColorNum )
			{
				intMaxColorNum = aintInHand[ intColorIndex ];
				intMaxColorIndex = intColorIndex;
			}
			if ( aintInHand[ intColorIndex ] > 0 && aintInHand[ intColorIndex ] < intMinColorNum )
			{
				intMinColorNum = aintInHand[ intColorIndex ];
				intMinColorIndex = intColorIndex;
		/**
		 * 
		 * Always ask for what you like the most and always offer what you hate the most
		 * If you dont know your tastes make empty offer 
		 * 
		 */
		
		int numExchanged = random.nextInt(5);
		int fav = -1;
		int hate = -1;
		double min = 1.0;
		double max = -1.0;
		for(int i=0; i<intColorNum; i++){
			if(isTasted[i]){
				if(adblTastes[i] < min){
					min = adblTastes[i];
					hate = i;
				}
				if(adblTastes[i] > max){
					max = adblTastes[i];
					fav =i;
				}
			}
		}
		
		int[] bid = new int[ intColorNum ];
		int[] ask = new int[ intColorNum ];
		
		if(fav != -1 && hate != -1){
			
			while(aintInHand[hate] < numExchanged ) numExchanged--;
			
			bid[hate] = numExchanged;
			ask[fav] = numExchanged;
		}
		
		System.out.println("In hand: ");
		for(int i=0; i < intColorNum; i++){
			System.out.print(aintInHand[i]+" ");
		}
		
		System.out.print("\nBid: ");
		for(int i=0; i < intColorNum; i++){
			System.out.print(bid[i]+" ");
		}
		System.out.println();
		System.out.print("Ask: ");
		for(int i=0; i < intColorNum; i++){
			System.out.print(ask[i]+" ");
		}
		System.out.println();
		}
		System.out.println();
		System.out.print("Ask: ");
		for(int i=0; i < intColorNum; i++){
			System.out.print(ask[i]+" ");
		}
		System.out.println();
		offTemp.setOffer( bid, ask );
	}

	@Override
	public void syncInHand(int[] aintInHand) 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void happier(double dblHappinessUp) 
	{
		double dblHappinessPerCandy = dblHappinessUp / Math.pow( intLastEatNum, 2 );
		if ( adblTastes[ intLastEatIndex ] == -1 )
		{
			adblTastes[ intLastEatIndex ] = dblHappinessPerCandy;
		}
		else
		{
			if ( adblTastes[ intLastEatIndex ] != dblHappinessPerCandy )
			{
				System.out.println( "Error: Inconsistent color happiness!" );
			}
		}
	}

	@Override
	public Offer pickOffer(Offer[] aoffCurrentOffers) 
	{
		Offer offReturn = null;
		for ( Offer offTemp : aoffCurrentOffers )
		{
			if ( offTemp.getOfferedByIndex() == intPlayerIndex || offTemp.getOfferLive() == false )
				continue;
			int[] aintDesire = offTemp.getDesire();
			if ( checkEnoughInHand( aintDesire ) )
			{
				//make sure that we dont trade too many of our liked candies
				offReturn = offTemp;
				aintDesire = offReturn.getDesire();
				int[] aintOffer = offReturn.getOffer();
				for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
				{
					aintInHand[ intColorIndex ] += aintOffer[ intColorIndex ] - aintDesire[ intColorIndex ];
					System.out.println("aintinhand["+intColorIndex+"] = " + aintInHand[intColorIndex]);
				}
				break;
			}
			
			System.out.println("Pick offer is: " + offTemp.toString());
		}
		

		return offReturn;
	}

	@Override
	public void offerExecuted(Offer offPicked) 
	{
		int[] aintOffer = offPicked.getOffer();
		int[] aintDesire = offPicked.getDesire();
		for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
		{
			aintInHand[ intColorIndex ] += aintDesire[ intColorIndex ] - aintOffer[ intColorIndex ];
		}
	}

	@Override
	public void updateOfferExe(Offer[] aoffCurrentOffers) 
	{
		// dumpplayer doesn't care
	}

	@Override
	public void initialize(int intPlayerIndex, String strClassName,	int[] aintInHand) 
	{
		this.intPlayerIndex = intPlayerIndex;
		this.strClassName = strClassName;
		this.aintInHand = aintInHand;
		intColorNum = aintInHand.length;
		dblHappiness = 0;
		indexToHoard = -1;
		adblTastes = new double[ intColorNum ];
		turnNumber = 0;
		isTasted = new boolean[intColorNum];
		for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
		{
			isTasted[intColorIndex] = false;
			adblTastes[ intColorIndex ] = -1;
		}
	}
	
	private boolean checkEnoughInHand( int[] aintTryToUse )
	{
		for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
		{
			if ( aintTryToUse[ intColorIndex ] > aintInHand[ intColorIndex ] )
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public String getClassName() 
	{
		return "DumpPlayer";
	}

	@Override
	public int getPlayerIndex() 
	{
		return intPlayerIndex;
	}

}
