package g1player;

import g1player.main.DummyMain;

import java.util.Random;

public class OfferStrategy {
	private Infobase info;
	private int c = 2 ; 	//how many colors we are going to have last round.N/k or 1.

	private int count = 0;
	private Random rand = new Random();
	private int colorNum;
	

	/*
	 * We look for the most number of skittles we can get this term, in {C}, we don't care what kind of 
	 * skittles we are going to give away as long as it is not in C.
	 */
	public void getOffer(int[] aintOffer, int[] aintDesire, Infobase infoUpdate) {
		this.info = infoUpdate;
//		this.c=info.getDesiredColorCount();
		count++;

				
		int[] priorityArray = info.getPriority().getPriorityArray(); 
		DummyMain.printArray(priorityArray, "OfferStrategy priorityArray:");
		colorNum=priorityArray.length;
		
		int[] maxOffers = new int[c]; //# of skittles others able to give us
		int[] colorOffers = new int[c]; //what should we offer
		int colorOffer=0;
		int colorGet=0;
		
		if(count==1){
			/*
			 * at the beginning of the game, we have little info about what other's like of dislike
			 * so the beginning rounds will have special strategy
			 */
			int tempLeast=colorNum;
			int leastLike = priorityArray[tempLeast-1];
			//System.out.println("leastLike<<<<   ".concat(String.valueOf(leastLike)));
			while(info.getAintInHand()[leastLike]==0){
				leastLike=priorityArray[--tempLeast];
				//System.out.println("tempLeast<<<<   ".concat(String.valueOf(leastLike)));
			}
			int quantity = info.getAintInHand()[leastLike];
			quantity=Math.min(info.getAintInHand()[leastLike], 1);

			aintOffer[leastLike] = quantity;
			aintDesire[priorityArray[rand.nextInt(c)]] = quantity;
			return;
		}
		
		for (int i = 0; i < c; i++){
			maxOffers[i] = 0;
			for (int j = c;j < priorityArray.length;j++) 
			{
				int tempOffer = this.calculateOffer(priorityArray[i],priorityArray[j]);
				if (tempOffer>info.getAintInHand()[priorityArray[j]]) //check if we have that many of skittles?
				{
					tempOffer = info.getAintInHand()[priorityArray[j]];
				}
				if (tempOffer> maxOffers[i] ){ 
					maxOffers[i] = tempOffer;
					colorOffers[i]=priorityArray[j];
				}
			}
		}
		int maxQuantity = 0;
		for(int i=0;i<c;i++){
			if(maxQuantity<maxOffers[i]){
				maxQuantity=maxOffers[i];
				colorGet = priorityArray[i];
				colorOffer = colorOffers[i];
			}			
		}
		aintOffer[colorOffer]=maxQuantity;
		aintDesire[colorGet]=maxQuantity;
		
		//if we can't find perfect trade, propose some other trade.
		if(maxQuantity==0){   
			//TODO: take other's like/dislike into consideration
			int tempLeast=colorNum;
			int leastLike = priorityArray[--tempLeast]; //dhaval modified for array index out of bound exception
			while(info.getAintInHand()[leastLike]==0 && !info.roundComplete){
				leastLike=priorityArray[--tempLeast];
				//System.out.println("tempLeast<<<<   ".concat(String.valueOf(tempLeast)));
			}
			int quantity = info.getAintInHand()[leastLike];

			aintOffer[leastLike] = quantity;
			aintDesire[priorityArray[rand.nextInt(c)]] = quantity;
		}
	}

	// this functions calculates the max number of colorGet we can get from trading colorOffer
	private int calculateOffer(int colorGet, int colorOffer) {
		int max = 0;
		for(int i = 0; i<info.numPlayers; i++){
			if(info.playerPreferences[i][colorOffer]>0 && info.playerPreferences[i][colorGet]<0)
			{
				if(max<info.estimatedSkittles[i][colorGet])
				{
					max=info.estimatedSkittles[i][colorGet];
				}
			}
		}
		return max;
	}

	public int getC() {
		return c;
	}
	
	public void setC(int c) {
		this.c = c;
	}
	
	public int getCount() {
		return count;
	}
	
	public void setCount(int count) {
		this.count = count;
	}
}

