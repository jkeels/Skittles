package skittles.g7;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Preference {
	
	private Map<Integer, Double> rawPreference = new HashMap<Integer, Double>();
    	
	public int getCurrentFavorite(CandyBag bag){
		Set<Map.Entry<Integer, Double>> prefs = rawPreference.entrySet();
		int favColor = -1;
		Double payoff = Double.NEGATIVE_INFINITY;
		
		for(Map.Entry<Integer, Double> pref : prefs){
			double temp = Math.pow(bag.getCandy(pref.getKey()).getRemaining(), 2) * pref.getValue();
			if(temp > payoff){
				payoff = temp;
				favColor = pref.getKey();
			}
		}
		
		return favColor;
	}
	
	public void updatePreference(int color, double pref){
		rawPreference.put(color, pref);
	}
	
	public Double getRawPreference(int color){
		if(rawPreference.containsKey(color))return rawPreference.get(color);
		else return Double.NaN;
	}
	
	public Integer getThresholdToChangeFav(int color, CandyBag bag) throws IllegalArgumentException{
		if(!rawPreference.containsKey(color)){
			throw new IllegalArgumentException("Cannot compare colors for which preference is unknown");
		}
		
		if(rawPreference.get(color) <= 0){
			return Integer.MAX_VALUE;
		}
		
		if(color == getCurrentFavorite(bag)) return 0;
		
		return (int) Math.ceil(Math.sqrt(Math.pow(bag.getCandy(getCurrentFavorite(bag)).getRemaining(), 2)
				/rawPreference.get(color)));
	}


}
