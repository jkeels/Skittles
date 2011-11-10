package g1player;

import skittles.sim.Offer;

public class Infobase {
	
	/**
	 * made Infobase as a singleton class.
	 */
	public static Infobase INFO_BASE = null;
	
	int[][] playerPreferences = null;
	int[][] estimatedSkittles = null;
	int numPlayers;
	
	//private Priority priority;
	private Priority priority;
	private int desiredColorCount = 0; //'c' as per discussion terminology
	private double[] colorHappinessArray;//happiness matrix
	private int[] aintInHand;
	private int initialSkittlesPerPlayer;
	
	private int intColorNum;
	double dblHappiness;
	String strClassName;
	int intPlayerIndex;
	public boolean roundComplete = false;
	
	private double[] adblTastes;
	private int intLastEatIndex;
	private int intLastEatNum;


	/**
	 * get the infobase object using this static method.
	 * @return
	 */
	public static Infobase getInfoBase(){
		if(INFO_BASE == null){
			INFO_BASE = new Infobase();
		}
		return INFO_BASE;
	}
	
	private Infobase() {
		priority = new Priority();
	}
	
	public Priority getPriority() {
		return priority;
	}
	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	public boolean tablesExist()
	{
		if (playerPreferences == null || estimatedSkittles == null)
			return false;
		else
			return true;
	}
		
	public void createTable(int numPlayers)
	{
		playerPreferences = new int[numPlayers][intColorNum];
		estimatedSkittles = new int[numPlayers][intColorNum];
		this.numPlayers = numPlayers;
		
		int estSkittlesPerColor = initialSkittlesPerPlayer/intColorNum;
		
		for (int i = 0; i < numPlayers; ++i)
		{
			for (int j = 0; j <intColorNum ; ++j)
			{
				estimatedSkittles[i][j] = estSkittlesPerColor;
			}
			// calculate desired number of colors for this round
			if (numPlayers >= intColorNum) {
				desiredColorCount = 1;
			} else {
				desiredColorCount = Math.round(intColorNum / numPlayers);
			}
		}

	}
	
	public void updatePlayerPreferences(int playerIndex, Offer playerOffer)
	{
		
	}
	
	public void updateEstSkittles(int playerIndex, Offer playerOffer)
	{
		
	}
	public void updateHappiness(double dblHappinessUp, int intLastEatIndex, int intLastEatNum) {
		if (intLastEatNum == 1)
		{
			INFO_BASE.colorHappinessArray[intLastEatIndex] = dblHappinessUp;
		}
	}

	public void updateOfferExecute(Offer offPicked) {
		
		int[] skittlesWeHave = INFO_BASE.getAintInHand();
		int[] giving = offPicked.getOffer();
		int[] getting= offPicked.getDesire();
		for (int j = 0; j < getting.length; ++j)
		{
			skittlesWeHave[j] -= giving[j];
			skittlesWeHave[j] += getting[j];
		}	
		INFO_BASE.setAintInHand(skittlesWeHave);		
	}

	public void updateOfferExe(Offer[] aoffCurrentOffers) {
		for (Offer o : aoffCurrentOffers)
		{
			int offeredBy = o.getOfferedByIndex();
			int tookOffer = o.getPickedByIndex();
			if(tookOffer == -1 || offeredBy == 1 || offeredBy == intPlayerIndex ){ //dhaval, dont update for our player
				continue; // dhaval array exception
			}
			int[] desired = o.getDesire();
			int[] offered = o.getOffer();
			if (!o.getOfferLive())
			{
				for (int i = 0; i < desired.length; ++i)
				{
					INFO_BASE.playerPreferences[offeredBy][i] += desired[i];
					INFO_BASE.playerPreferences[offeredBy][i] -= offered[i];
					INFO_BASE.playerPreferences[tookOffer][i] -= desired[i];
					INFO_BASE.playerPreferences[tookOffer][i] += offered[i];
				}	
			}
		}
	}
	public int getDesiredColorCount() {
		return desiredColorCount;
	}

	public void setDesiredColorCount(int desiredColorCount) {
		this.desiredColorCount = desiredColorCount;
	}

	public double[] getColorHappinessArray() {
		return colorHappinessArray;
	}

	public void setColorHappinessArray(double[] colorHappiness) {
		this.colorHappinessArray = colorHappiness;
	}

	public double getColorHappiness(int index) {
		return colorHappinessArray[index];
	}

	public int[] getAintInHand() {
		return aintInHand;
	}

	public void setAintInHand(int[] aintInHand) {
		this.aintInHand = aintInHand;
	}

	public int getIntColorNum() {
		return intColorNum;
	}

	public void setIntColorNum(int intColorNum) {
		this.intColorNum = intColorNum;
	}

	public double getDblHappiness() {
		return dblHappiness;
	}

	public void setDblHappiness(double dblHappiness) {
		this.dblHappiness = dblHappiness;
	}

	public String getStrClassName() {
		return strClassName;
	}

	public void setStrClassName(String strClassName) {
		this.strClassName = strClassName;
	}

	public int getIntPlayerIndex() {
		return intPlayerIndex;
	}

	public void setIntPlayerIndex(int intPlayerIndex) {
		this.intPlayerIndex = intPlayerIndex;
	}

	public double[] getAdblTastes() {
		return adblTastes;
	}

	public void setAdblTastes(double[] adblTastes) {
		this.adblTastes = adblTastes;
	}
	
	public void setAdblTasteElement(int index , double  value){
		this.adblTastes[index] = value;
	}

	public int getIntLastEatIndex() {
		return intLastEatIndex;
	}

	public void setIntLastEatIndex(int intLastEatIndex) {
		this.intLastEatIndex = intLastEatIndex;
	}

	public int getIntLastEatNum() {
		return intLastEatNum;
	}

	public void setIntLastEatNum(int intLastEatNum) {
		this.intLastEatNum = intLastEatNum;
	}

	public int getInitialSkittlesPerPlayer() {
		return initialSkittlesPerPlayer;
	}

	public void setInitialSkittlesPerPlayer(int initialSkittlesPerPlayer) {
		this.initialSkittlesPerPlayer = initialSkittlesPerPlayer;
	}


}
