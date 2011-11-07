package skittles.g7;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class CandyBag {
	private Map<Integer, Candy> bag = new HashMap<Integer, Candy>();
	
	/**
	 * Ordered in ascending order
	 * 
	 */
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
	
	public List<Candy> sortByPreference(){
		List<Candy> sortedCandies = new ArrayList<Candy>();
		sortedCandies.addAll(bag.values());
		Collections.sort(sortedCandies, new Comparator<Candy>(){

			@Override
			public int compare(Candy c1, Candy c2) {
				return c1.getPref().compareTo(c2.getPref());
			}
			
		});
		
		return sortedCandies;
		
	}
	
	
	/**
	 * 
	 * Return the Candy that has a negative preference that's closest to zero
	 * 
	 * @return
	 */
	
	public Candy getLeastNegative(){
		// TODO: Complete this
		
		List<Candy> candies = sortByPreference();
		return null;
	}
	
	
	/**
	 * Return the Candy that will give you the least positive gain if you ate 
	 * all of that candy
	 * 
	 */
	public Candy getLeastPositive(){
		// TODO: Complete this
		return null;
	}
	
}
