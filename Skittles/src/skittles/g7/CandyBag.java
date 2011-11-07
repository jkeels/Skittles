package skittles.g7;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class CandyBag {
	private Map<Integer, Candy> bag = new HashMap<Integer, Candy>();
	private SortedSet<Candy> orderedBag = new TreeSet<Candy>();
	
	public CandyBag(int[] candies){
		for(int i=0; i < candies.length; i++){
			Candy candy = new Candy(i,candies[i]);
			bag.put(i, candy);
			orderedBag.add(candy);
		}
	}
	
	public void addCandy(int color, int candies){
		if(bag.containsKey(color)){
			bag.get(color).addCandy(candies);
		}else{
			Candy candy = new Candy(color, candies);
			bag.put(color, candy);
			orderedBag.add(candy);
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
	
	public Candy getNthCandy(int n){
		return orderedBag.toArray(new Candy[orderedBag.size()])[n];
	}
}
