package skittles.g7;

import java.util.HashMap;
import java.util.Map;

public class CandyBag {
	private Map<Integer, Candy> bag = new HashMap<Integer, Candy>();
	
	public CandyBag(int[] candies){
		for(int i=0; i < candies.length; i++){
			bag.put(i, new Candy(i,candies[i]));
		}
	}
	
	public void addCandy(int color, int candies){
		if(bag.containsKey(color)){
			bag.get(color).addCandy(candies);
		}else{
			bag.put(color, new Candy(color, candies));
		}
	}
	
	public void removeCandy(int color, int candies) throws IllegalArgumentException{
		if(!bag.containsKey(color)){
			throw new IllegalArgumentException("Tried to remove candy from a color that we don't have");
		}
		bag.get(color).consume(candies);
	}
	
	public Candy getCandy(int color){
		return bag.get(color);
	}
	
	public int getNumColors(){
		return bag.size();
	}
}
