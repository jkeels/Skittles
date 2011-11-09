package skittles.g6.strategy;

public class Parameters {
	public static final double PRIMARY_THRESHOLD = 0.75;
	public static final double SECONDARY_THRESHOLD = 0.5;
	public static final double UNKNOWN_TASTE = -.000000121; //must remain < 0
	
	public static final int BIG_AMOUNT_DIVISOR = 4; 
		/* seems like the smaller we make it, the
		 * riskier - if people happen to accept the big offer, we do good. but if not, we do
		 * terrible 
		 */
	
	public static final int GIVE_UP_TURNS = 3;
}
