package skittles.g7;

public class Candy {
	private Integer color;
	private int inHand;
	
	public Candy(int color, int inHand){
		this.color = color;
		this.inHand = inHand;
	}
	
	public void consume(int numConsumed) throws IllegalArgumentException{
		if(inHand < numConsumed){
			throw new IllegalArgumentException("Tried to remove/consume more candy than what we have");
		}
		inHand -= numConsumed;
	}
	
	public void addCandy(int candies){
		inHand += candies;
	}
	
	public int getRemaining(){
		return inHand;
	}
	
	public Integer getColor(){
		return color;
	}

	@Override
	public int hashCode() {
		return color;
	}
	
	
}
