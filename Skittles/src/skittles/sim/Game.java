package skittles.sim;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.parsers.*;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import skittles.manualplayer.ManualP;

public class Game 
{
	private Player[] aplyPlayers;
	private PlayerStatus[] aplsPlayerStatus;
	private int intPlayerNum;
	private int intColorNum;
	private boolean firstRound = true;
	
	private Offer[] aoffCurrentOffers = null;
	private int[][] aintCurrentEats = null;
	
	private int[] totalSkittlesInMarket = null;
	private int skittlesPerPlayer;
	
	public static Scanner scnInput = new Scanner( System.in );
	
	public Game( String strXMLPath )
	{
		DocumentBuilderFactory dbfGameConfig = DocumentBuilderFactory.newInstance();

		Document dcmGameConfig = null;
		try 
		{
			//Using factory get an instance of document builder
			DocumentBuilder dbdGameConfig = dbfGameConfig.newDocumentBuilder();
			//parse using builder to get DOM representation of the XML file
			dcmGameConfig = dbdGameConfig.parse( strXMLPath );
		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch(SAXException se) {
			se.printStackTrace();
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}
		//get the root element
		dcmGameConfig.getDocumentElement().normalize();
		//get a nodelist of elements
		NodeList ndlGame = dcmGameConfig.getElementsByTagName("Game");
		int intTotalNum = 0;
		if(ndlGame != null && ndlGame.getLength() > 0) 
		{
			for(int i = 0 ; i < ndlGame.getLength();i++) 
			{
				//get the employee element
				Element elmGame = (Element) ndlGame.item(i);
				//retrieve player information
				intColorNum = Integer.parseInt( getTagValue( elmGame, "ColorNum" ) );
				intTotalNum = Integer.parseInt( getTagValue( elmGame, "SkittleNum" ) );	
				skittlesPerPlayer = intTotalNum;
			}
		}
		// initialize players
		ArrayList< Player > alPlayers = new ArrayList< Player >();			// players
		ArrayList< PlayerStatus > alPlayerStatus = new ArrayList< PlayerStatus >();		// status of players for simulator's record
		//get a nodelist of elements
		NodeList ndlPlayers = dcmGameConfig.getElementsByTagName("Player");
		if(ndlPlayers != null && ndlPlayers.getLength() > 0) 
		{
			intPlayerNum = ndlPlayers.getLength();
			totalSkittlesInMarket = new int[intColorNum];
			for(int i = 0 ; i < ndlPlayers.getLength();i++) 
			{

				//get the employee element
				Element elmPlayer = (Element) ndlPlayers.item(i);
				//retrieve player information
				String strPlayerClass = getTagValue( elmPlayer, "Class" );
				String strTastes = getTagValue( elmPlayer, "Happiness" );
				String[] astrTastes = strTastes.split( "," );
				double[] adblTastes = new double[ intColorNum ];
				if ( !astrTastes[ 0 ].equals( "random" ) )
				{
					for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
					{
						adblTastes[ intColorIndex ] = Double.parseDouble( astrTastes[ intColorIndex ] );
					}
				}
				else
				{
					double dblMean = Double.parseDouble( astrTastes[ 1 ] );
					adblTastes = randomTastes( dblMean );
					boolean nl = false;
					if (Stats.happinessDistribution)
					{
						nl = true;
						System.out.println( "Random color happiness for player " + i + ":" );
						for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
						{
							System.out.print( adblTastes[ intColorIndex ] + "  ");
						}	
						System.out.println();
					}
					if (Stats.averageHappiness)
					{
						nl = true;
						double totalHappy = 0;
						for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
						{
							totalHappy += adblTastes[ intColorIndex ];
						}	
						double avgHappy = totalHappy / intColorNum;
						System.out.println( "Average color happiness for player " + i + ": " + avgHappy );
					}
					if (nl)
					{
						System.out.println();
						System.out.println("---------------------------------------------------");
					}
				}
				String strInHand = getTagValue( elmPlayer, "InHand" );
				int[] aintInHand = new int[ intColorNum ];
				int intTempSkittleCount = 0;
				if ( strInHand.equals( "random" ) )
				{
					aintInHand = randomInHand( intTotalNum );
					for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
					{
						intTempSkittleCount += aintInHand[ intColorIndex ];
					}
				}
				else
				{
					String[] astrInHand = strInHand.split( "," );
					for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
					{
						aintInHand[ intColorIndex ] = Integer.parseInt( astrInHand[ intColorIndex ] );
						intTempSkittleCount += aintInHand[ intColorIndex ];
					}
					if ( intTempSkittleCount != intTotalNum )
					{
						System.out.println( "Skittle number in hand is not consistent." );
					}
				}
				Player plyNew = null;
				try {
					plyNew = ( Player ) Class.forName( strPlayerClass ).newInstance();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				plyNew.initialize( intPlayerNum, i, strPlayerClass, aintInHand.clone() );
				alPlayers.add( plyNew );
				PlayerStatus plsTemp = new PlayerStatus( i, strPlayerClass, aintInHand.clone(), adblTastes.clone() );
				alPlayerStatus.add( plsTemp );
				
				for(int q = 0; q < intColorNum; ++q)
				{
					totalSkittlesInMarket[q] += aintInHand[q];
				}
			}
		}
		aplyPlayers = alPlayers.toArray( new Player[ 0 ] );
		aplsPlayerStatus = alPlayerStatus.toArray( new PlayerStatus[ 0 ] );	
	}
	
	public void runGame()
	{
		FileWriter[] afrtPortfolio = new FileWriter[ intPlayerNum ];
		BufferedWriter[] abfwPortfolio = new BufferedWriter[ intPlayerNum ];
		try {
			for ( int intPlayerIndex = 0; intPlayerIndex < intPlayerNum; intPlayerIndex ++ )
			{
				afrtPortfolio[ intPlayerIndex ] = new FileWriter( "P" + intPlayerIndex + ".txt" );
				abfwPortfolio[ intPlayerIndex ] = new BufferedWriter( afrtPortfolio[ intPlayerIndex ] );
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// check whether there is still at least one player has more skittles to eat
		logGame( abfwPortfolio, "E" );
		logGame( abfwPortfolio, "O" );
		logGame( abfwPortfolio, "P" );
		logGame( abfwPortfolio, "H" );
		logGame( abfwPortfolio, "N" );
		while ( !checkFinish() )
		{
			if (firstRound)
			{
				showEveryInHand();		
			}
			everyoneEatAndOffer();
			logGame( abfwPortfolio, "E" );
			int[] aintOrder = generateRandomOfferPickOrder();			// need code to log the order for repeated game
			pickOfferInOrder( aintOrder );
			broadcastOfferExcution();
			logGame( abfwPortfolio, "O" );
			logGame( abfwPortfolio, "P" );
			logGame( abfwPortfolio, "H" );
			logGame( abfwPortfolio, "N" );
		}
		
		if (Stats.netTrade)
		{
			for (PlayerStatus plsTemp : aplsPlayerStatus)
			{
				if (Stats.playerFilter == -1 || Stats.playerFilter == plsTemp.getPlayerIndex())
				{
					String netGive = intArrToString(plsTemp.getCumulativeTradeAway());
					String netGain = intArrToString(plsTemp.getCumulativeTradeGain());
					System.out.println("Player #" + plsTemp.getPlayerIndex() + "'s net trade: " + netGive + " <--> " + netGain);
				}
			}
		}
		
		double dblTotalScores = 0;
		for ( PlayerStatus plsTemp : aplsPlayerStatus )
		{
			dblTotalScores += plsTemp.getHappiness();
		}
		
		TreeSet<Score> rawScores = new TreeSet<Score>();
		TreeSet<Score> finalScores = new TreeSet<Score>();
		for ( PlayerStatus plsTemp : aplsPlayerStatus )
		{
			double dblTempHappy = plsTemp.getHappiness() + ((dblTotalScores - plsTemp.getHappiness()) / (intPlayerNum - 1));
			rawScores.add(new Score(plsTemp.getPlayerIndex(), plsTemp.getHappiness()));
			finalScores.add(new Score(plsTemp.getPlayerIndex(), dblTempHappy));
			boolean nl = true;
			if (Stats.rawScore && (Stats.playerFilter == -1 || Stats.playerFilter == plsTemp.getPlayerIndex()))
			{
				if (nl)
				{
					System.out.println();
				}
				nl = false;
				System.out.println( "Player #" + plsTemp.getPlayerIndex() + "'s RAW happiness is: " + plsTemp.getHappiness() );
			}
			if (Stats.othersAvgScore && (Stats.playerFilter == -1 || Stats.playerFilter == plsTemp.getPlayerIndex()))
			{
				if (nl)
				{
					System.out.println();
				}
				System.out.println( "Average of scores (minus Player#" + plsTemp.getPlayerIndex() + ") is: " + (dblTotalScores - plsTemp.getHappiness()) / (intPlayerNum - 1) );
				nl = false;
			}
			if (Stats.finalScore && (Stats.playerFilter == -1 || Stats.playerFilter == plsTemp.getPlayerIndex()))
			{
				if (nl)
				{
					System.out.println();
				}
				System.out.println( "Player #" + plsTemp.getPlayerIndex() + "'s TOTAL happiness is: " + dblTempHappy );
				nl = false;
			}
		}
		
		if (Stats.rawScoreRank)
		{
			System.out.println();
			System.out.println("---------------------------------------------------");
			System.out.println("Rank based on raw scores: ");
			Iterator<Score> i = rawScores.iterator();
			Score s = null;
			while (i.hasNext())
			{
				s = i.next();
				System.out.println(s.getPlayerIndex() + " with raw score " + s.getScore());
			}
			System.out.println();
			System.out.println("---------------------------------------------------");
		}
		if (Stats.finalScoreRank)
		{
			System.out.println();
			System.out.println("---------------------------------------------------");
			System.out.println("Rank based on final scores: ");
			Iterator<Score> i = finalScores.iterator();
			Score s = null;
			while (i.hasNext())
			{
				s = i.next();
				System.out.println(s.getPlayerIndex() + " with final score " + s.getScore());
			}
			System.out.println();
			System.out.println("---------------------------------------------------");
		}
		
		try {
			for ( int intPlayerIndex = 0; intPlayerIndex < intPlayerNum; intPlayerIndex ++ )
			{
				abfwPortfolio[ intPlayerIndex ].close();
				afrtPortfolio[ intPlayerIndex ].close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void logGame( BufferedWriter[] abfwPortfolio, String strLogWhat )
	{
		if ( strLogWhat.equals( "P" ) )
		{
			for ( int intPlayerIndex = 0; intPlayerIndex < intPlayerNum; intPlayerIndex ++ )
			{
				PlayerStatus plsTemp = aplsPlayerStatus[ intPlayerIndex ];
				int[] aintInHand = plsTemp.getInHand();
				for ( int intInHand : aintInHand )
				{
					try {
						abfwPortfolio[ intPlayerIndex ].write( intInHand + "\t" );
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		else if ( strLogWhat.equals( "E" ) )
		{
			for ( int intPlayerIndex = 0; intPlayerIndex < intPlayerNum; intPlayerIndex ++ )
			{
				int[] aintCurrentEat;
				if ( aintCurrentEats == null )
				{
					aintCurrentEat = new int[ intColorNum ];
				}
				else
				{
					aintCurrentEat = aintCurrentEats[ intPlayerIndex ];
				}
				for ( int intEat : aintCurrentEat )
				{
					try {
						abfwPortfolio[ intPlayerIndex ].write( intEat + "\t" );
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		else if ( strLogWhat.equals( "N" ) )
		{
			for ( int intPlayerIndex = 0; intPlayerIndex < intPlayerNum; intPlayerIndex ++ )
			{
				try {
					abfwPortfolio[ intPlayerIndex ].write( "\n" );
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		else if ( strLogWhat.equals( "O" ) )
		{
			int[][] aintSumOffExe = new int[ intPlayerNum ][ intColorNum ];
			int[][] aintOffs = new int[ intPlayerNum ][ intColorNum ];
			if ( aoffCurrentOffers != null )
			{
				for ( Offer offTemp : aoffCurrentOffers )
				{
					int[] aintOff = offTemp.getOffer();
					int[] aintDesire = offTemp.getDesire();
					int intOfferedByIndex = offTemp.getOfferedByIndex();
					int intPickedByIndex = offTemp.getPickedByIndex();
					for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
					{
						aintOffs[ intOfferedByIndex ][ intColorIndex ] = aintOff[ intColorIndex ] - aintDesire[ intColorIndex ];
					}
					if ( !offTemp.getOfferLive() && offTemp.getPickedByIndex() != -1 )
					{
						for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
						{
							aintSumOffExe[ intOfferedByIndex ][ intColorIndex ] += aintDesire[ intColorIndex ] - aintOff[ intColorIndex ];
							aintSumOffExe[ intPickedByIndex ][ intColorIndex ] += aintOff[ intColorIndex ] - aintDesire[ intColorIndex ];
						}
					}
				}
			}
			for ( int intPlayerIndex = 0; intPlayerIndex < intPlayerNum; intPlayerIndex ++ )
			{
				int[] aintSumOffE = aintSumOffExe[ intPlayerIndex ];
				int[] aintOff = aintOffs[ intPlayerIndex ];
				for ( int intOff : aintOff )
				{
					try {
						abfwPortfolio[ intPlayerIndex ].write( intOff + "\t" );
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				for ( int intSumOffE : aintSumOffE )
				{
					try {
						abfwPortfolio[ intPlayerIndex ].write( intSumOffE + "\t" );
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		else if ( strLogWhat.equals( "H" ) )
		{
			for ( int intPlayerIndex = 0; intPlayerIndex < intPlayerNum; intPlayerIndex ++ )
			{
				PlayerStatus plsTemp = aplsPlayerStatus[ intPlayerIndex ];
				double dblHappiness = plsTemp.getHappiness();
				try {
					abfwPortfolio[ intPlayerIndex ].write( dblHappiness + "\t" );
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private double[] randomTastes(double dblMean) 
	{
		double[] adblRandomTastes = new double[ intColorNum ];
		Random rdmTemp = new Random();
		for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
		{
			double dblTemp = -5;		// out of range [ -1, 1 ]
			while ( dblTemp < -1 || dblTemp > 1 )
			{
				dblTemp = rdmTemp.nextGaussian() + dblMean;
			}
			adblRandomTastes[ intColorIndex ] = dblTemp;
		}
		return adblRandomTastes;
	}

	private int[] randomInHand(int intTotalNum) 
	{
		int[] aintRandomInHand = new int[ intColorNum ];
//		Random rdmTemp = new Random();
//		int[] aintTemp = new int[ intColorNum + 1 ];
//		aintTemp[ intColorNum ] = intTotalNum;
//		for ( int intColorIndex = 1; intColorIndex < intColorNum; intColorIndex ++ )
//		{
//			aintTemp[ intColorIndex ] = rdmTemp.nextInt( intTotalNum + 1 );
//		}
//		Arrays.sort( aintTemp );
////		System.out.println( "RandomInHand: " );
//		for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
//		{
//			aintRandomInHand[ intColorIndex ] = aintTemp[ intColorIndex + 1 ] - aintTemp[ intColorIndex ];
////			System.out.print( aintRandomInHand[ intColorIndex ] + " " );
//		}
//		System.out.println();
		Random rdmTemp = new Random();
		for ( int intSkittleIndex = 0; intSkittleIndex < intTotalNum; intSkittleIndex ++ )
		{
			int intTemp = rdmTemp.nextInt( intColorNum );
			aintRandomInHand[ intTemp ] ++;
		}
		return aintRandomInHand;
	}

	private String getTagValue( Element elmPlayer, String strTagName )
	{
		String strValue = null;
		NodeList ndlPlayer = elmPlayer.getElementsByTagName( strTagName );
		if( ndlPlayer != null && ndlPlayer.getLength() > 0) {
			Element elmTag = (Element) ndlPlayer.item(0);
			strValue = elmTag.getFirstChild().getNodeValue();
		}
		return strValue;
	}
	
	private void showEveryInHand() 
	{
/*		System.out.println( "<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n" );
		System.out.println( "******************************************" );
		System.out.println( "------------------------------------------");*/
		firstRound = false;
		boolean nl = false;
		if (Stats.skittlesDistribution)
		{
			nl = true;
			System.out.println( "Skittles portfolio:" );
			for ( PlayerStatus plsTemp : aplsPlayerStatus )
			{
				System.out.println( plsTemp.toString() );
			}
			System.out.println();
		}
		
		if (Stats.noTradePotentialScore)
		{
			nl = true;
			for (PlayerStatus plsTemp : aplsPlayerStatus)
			{
				double potentialScore = 0;
				double[] taste = plsTemp.getAdblTaste();
				int[] skittlesInHand = plsTemp.getInHand();
				for (int color = 0; color < intColorNum; ++color)
				{
					if (taste[color] > 0)
					{
						potentialScore += taste[color] * (Math.pow(skittlesInHand[color], 2));
					}
					else
					{
						potentialScore += taste[color] * skittlesInHand[color];
					}
				}
				System.out.println("Player #" + plsTemp.getPlayerIndex() + "'s potential score without trading is " + potentialScore);
			}
		}
		if (Stats.maxHoardPotentialScore)
		{
			for (PlayerStatus plsTemp : aplsPlayerStatus)
			{
				double potentialScore = 0;
				int skittlesEaten = 0;
				int[] sortedIndicies = sortedHappyScore(plsTemp.getAdblTaste().clone());
				double[] tastes = plsTemp.getAdblTaste().clone();
				for (int i = 0; i < intColorNum && skittlesEaten < skittlesPerPlayer; ++i)
				{
					int happyColor = sortedIndicies[i];
					if (totalSkittlesInMarket[happyColor] >= skittlesPerPlayer)
					{
						if (plsTemp.getAdblTaste()[happyColor] >= 0)
						{
							potentialScore += tastes[happyColor] * (Math.pow(skittlesPerPlayer, 2));
						}
						else
						{
							potentialScore += tastes[happyColor] * skittlesPerPlayer;
						}
						skittlesEaten += skittlesPerPlayer;
					}
					else
					{
						if (tastes[happyColor] >= 0)
						{
							potentialScore += tastes[happyColor] * (Math.pow(totalSkittlesInMarket[happyColor], 2));
						}
						else
						{
							potentialScore += tastes[happyColor] * totalSkittlesInMarket[happyColor];
						}
						skittlesEaten += totalSkittlesInMarket[happyColor];
					}
				}
				System.out.println("Player #" + plsTemp.getPlayerIndex() + "'s max score with hoarding is " + potentialScore);
			}
		}
		if (nl)
		{
			System.out.println();
			System.out.println("---------------------------------------------------");
		}
		
/*		System.out.println( "------------------------------------------");
		System.out.println( "******************************************\n" );*/
	}

	private boolean checkFinish()
	{
		boolean blnFinish = true;
		for ( int intPlayerIndex = 0; intPlayerIndex < intPlayerNum; intPlayerIndex ++ )
		{
			if ( aplsPlayerStatus[ intPlayerIndex ].getTotalInHand() > 0 )
			{
				blnFinish = false;
				break;
			}
		}
		return blnFinish;
	}

	private void everyoneEatAndOffer()
	{
		ArrayList< Offer > alCurrentOffers = new ArrayList< Offer >();
		ArrayList< int[] > alEats = new ArrayList< int[] >();
		for ( int intPlayerIndex = 0; intPlayerIndex < intPlayerNum; intPlayerIndex ++ )
		{
			// skip the player who has eaten all the skittles
			int[] aintTempEat = new int[ intColorNum ];
			Offer offTemp = new Offer( intPlayerIndex, intColorNum );
			if ( aplsPlayerStatus[ intPlayerIndex ].getTotalInHand() == 0 )
			{
				alEats.add( aintTempEat );
				alCurrentOffers.add( offTemp );
				continue;
			}
			aplyPlayers[ intPlayerIndex ].eat( aintTempEat );
			// process eat
			if ( aplsPlayerStatus[ intPlayerIndex ].checkCanEat( aintTempEat ) )
			{
				double dblHappinessUp = aplsPlayerStatus[ intPlayerIndex ].eat( aintTempEat );
				alEats.add( aintTempEat );
				aplyPlayers[ intPlayerIndex ].happier( dblHappinessUp );
			}
			else
			{
				double dblHappinessUp = aplsPlayerStatus[ intPlayerIndex ].randEat( aintTempEat );
				alEats.add( aintTempEat );
				aplyPlayers[ intPlayerIndex ].happier( dblHappinessUp );
//				System.out.println( "Player #" + intPlayerIndex + ": You cannot eat these. Take them out of your mouth!" );
			}
			// process offer
			aplyPlayers[ intPlayerIndex ].offer( offTemp );
			if ( aplsPlayerStatus[ intPlayerIndex ].checkValidOffer( offTemp ) )
			{
				alCurrentOffers.add( offTemp );
			}
			else
			{
//				System.out.println( "Player #" + intPlayerIndex + ": Invalid offer. Shame on you :)" );
				Offer offEmpty = new Offer( intPlayerIndex, intColorNum );
				alCurrentOffers.add( offEmpty );
			}
		}
		aintCurrentEats = alEats.toArray( new int[ 0 ][] );
		aoffCurrentOffers = alCurrentOffers.toArray( new Offer[ 0 ] );
		
/*		System.out.println( "******************************************" );
		System.out.println( "------------------------------------------");*/
		if (Stats.consumption)
		{
			System.out.println();
			System.out.println( "Skittles consumption:" );
			for ( int intPlayerIndex = 0; intPlayerIndex < intPlayerNum; intPlayerIndex ++ )
			{
				if (Stats.playerFilter == -1 || Stats.playerFilter == intPlayerIndex)
				{
					System.out.print( "Player #" + intPlayerIndex + ": [ " );
					String strInHand = "";
					int[] aintInHand = alEats.get( intPlayerIndex );
					for ( int intInHand : aintInHand )
					{
						strInHand += intInHand + ", ";
					}
					System.out.println( strInHand.substring( 0, strInHand.length() - 2 ) + " ]" );
				}
			}
		}
		if (Stats.allOffers)
		{
			System.out.println( "All offers:" );
			for ( Offer offTemp : aoffCurrentOffers )
			{
				if (Stats.playerFilter == -1 || (Stats.playerFilter == offTemp.getOfferedByIndex() || Stats.playerFilter == offTemp.getPickedByIndex()))
				{
					System.out.println( offTemp.toString() );
				}
			}
			System.out.println( "------------------------------------------");
		}
//		System.out.println( "******************************************\n" );*/
	}
	
	private int[] generateRandomOfferPickOrder()
	{
		ArrayList< Integer > alPlayerIndices = new ArrayList< Integer >();
		for ( int intPlayerIndex = 0; intPlayerIndex < intPlayerNum; intPlayerIndex ++ )
		{
			alPlayerIndices.add( intPlayerIndex );
		}
		int[] aintOrder = new int[ intPlayerNum ];
		Random rdmGenerator = new Random();
//		System.out.println( "Random order is:" );
		for ( int intPlayerIndex = 0; intPlayerIndex < intPlayerNum; intPlayerIndex ++ )
		{
			int intRandom = rdmGenerator.nextInt( intPlayerNum - intPlayerIndex );
			aintOrder[ intPlayerIndex ] = alPlayerIndices.get( intRandom );
			alPlayerIndices.remove( intRandom );
//			System.out.print( aintOrder[ intPlayerIndex ] + " " );
		}
//		System.out.println( "\n" );
		return aintOrder;
	}
	
	private void pickOfferInOrder( int[] aintOrder )
	{
		for ( int intOrderIndex = 0; intOrderIndex < intPlayerNum; intOrderIndex ++ )
		{
			int intPlayerIndex = aintOrder[ intOrderIndex ];
			//skip the player who has eaten all the skittles
			if ( aplsPlayerStatus[ intPlayerIndex ].getTotalInHand() == 0 )
			{
				continue;
			}
			Offer offPicked = aplyPlayers[ intPlayerIndex ].pickOffer( aoffCurrentOffers );
			if ( offPicked != null )
			{
				if ( offPicked.getOfferLive() == false )
				{
//					System.out.println( "Offer has been picked, forget about it" );
				}
				else if ( !aplsPlayerStatus[ intPlayerIndex ].checkEnoughInHand( offPicked.getDesire() ) )
				{
//					System.out.println( "Player #" + intPlayerIndex + ": you don't have enough skittles to accept this offer. Don't even think about it!" );
				}
				else if ( intPlayerIndex == offPicked.getOfferedByIndex() )
				{
//					System.out.println( "Trade with yourself? Schizophrenia..." );
				}
				else
				{
					offPicked.setOfferLive( false );
					int intPickedByIndex = intPlayerIndex;
					int intOfferedByIndex = offPicked.getOfferedByIndex();
					offPicked.setPickedByIndex( intPickedByIndex );
					aplsPlayerStatus[ intOfferedByIndex ].offerExecuted( offPicked );
					aplyPlayers[ intOfferedByIndex ].offerExecuted( offPicked );
					aplsPlayerStatus[ intPickedByIndex ].pickedOffer( offPicked );
					// check after picking an offer, whether the offered offered by intPickedByIndex is still valid. if not, remove it
					Offer offOfferedByPicking = aoffCurrentOffers[ intPickedByIndex ];
					if ( offOfferedByPicking.getOfferLive() && !aplsPlayerStatus[ intPickedByIndex ].checkEnoughInHand( offOfferedByPicking.getOffer() ) )
					{
						offOfferedByPicking.setOfferLive( false );
					}
				}
			}
		}
	}

	private void broadcastOfferExcution()
	{
		for ( int intPlayerIndex = 0; intPlayerIndex < intPlayerNum; intPlayerIndex ++ )
		{
			aplyPlayers[ intPlayerIndex ].updateOfferExe( aoffCurrentOffers );
		}
		
/*		System.out.println( "\n******************************************" );
		System.out.println( "------------------------------------------");
		System.out.println( "Offer execution: " );*/
		
			for ( Offer offTemp : aoffCurrentOffers )
			{
				if ( offTemp.getPickedByIndex() != -1 )
				{
					aplsPlayerStatus[offTemp.getOfferedByIndex()].updateCumulativeTrades(offTemp);
					aplsPlayerStatus[offTemp.getPickedByIndex()].updateCumulativeTrades(offTemp);
					if (Stats.allAcceptedTrades)
					{
						if (Stats.playerFilter == -1 || (Stats.playerFilter == offTemp.getOfferedByIndex() || Stats.playerFilter == offTemp.getPickedByIndex()))
						{
							System.out.println( offTemp.toString() );
						}
					}
				}
			}
/*		System.out.println( "------------------------------------------");
		System.out.println( "******************************************\n" );*/
		
	}
	
	public static String arrayToString( int[] aintArray )
	{
		String strReturn = "[ ";
		for ( int intElement : aintArray )
		{
			strReturn += intElement + ", ";
		}
		strReturn = strReturn.substring( 0, strReturn.length() - 2 ) + " ]";
		return strReturn;
	}
	
	private String intArrToString(int[] arr)
	{
		String str = "[";
		
		for (int i : arr)
		{
			str = str + i + " ";
		}

		str.trim();
		str += "]";

		return str;
	}
	
	private int[] sortedHappyScore(double[] scores)
	{
		double[] sorted = scores.clone();
		int[] indicies = new int[scores.length];
		for (int i = 0; i < scores.length - 1; ++i)
		{
			int maxIndex = getMax(sorted, i);
			indicies[i] = indexOf(sorted[maxIndex], scores);
			swap(sorted, i, maxIndex);
		}
		return indicies;
	}
	
	private int indexOf(double s, double[] scores)
	{
		for (int i = 0; i < scores.length; ++i)
		{
			if (scores[i] == s)
			{
				return i;
			}
		}
		return -1;
	}
	
	private void swap(double[] scores, int index1, int index2)
	{
		double temp = scores[index1];
		scores[index1] = scores[index2];
		scores[index2] = temp;
	}
	
	private int getMax(double[] scores, int start)
	{
		int maxIndex = -1;
		double maxVal = Double.NEGATIVE_INFINITY;
		for (int i = start; i < scores.length; ++i)
		{
			if (scores[i] > maxVal)
			{
				maxVal = scores[i];
				maxIndex = i;
			}
		}
		return maxIndex;
	}
}
