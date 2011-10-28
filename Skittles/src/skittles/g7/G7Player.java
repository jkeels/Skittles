package skittles.g7;

import skittles.sim.*;

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
		if(turnNumber < 5){
			intLastEatIndex = turnNumber;
			intLastEatNum = 1;
			aintTempEat[ intLastEatIndex ] = intLastEatNum;
			isTasted[turnNumber] = true;
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
	}
	
	@Override
	public void offer( Offer offTemp )
	{
		int intMaxColorIndex = 0;
		int intMaxColorNum = 0;
		int intMinColorIndex = 0;
		int intMinColorNum = Integer.MAX_VALUE;
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
			}
		}
		int[] aintOffer = new int[ intColorNum ];
		int[] aintDesire = new int[ intColorNum ];
		if ( intMinColorIndex != intMaxColorIndex )
		{
			aintOffer[ intMinColorIndex ] = intMinColorNum;
			aintDesire[ intMaxColorIndex ] = intMinColorNum;
		}
		offTemp.setOffer( aintOffer, aintDesire );
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
				offReturn = offTemp;
				aintDesire = offReturn.getDesire();
				int[] aintOffer = offReturn.getOffer();
				for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
				{
					aintInHand[ intColorIndex ] += aintOffer[ intColorIndex ] - aintDesire[ intColorIndex ];
				}
				break;
			}
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
