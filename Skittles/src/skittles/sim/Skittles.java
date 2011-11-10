package skittles.sim;

public class Skittles 
{
	public static void main( String[] args )
	{		
		Stats.init();
		for (int i = 0; i < Stats.rounds; ++i)
		{
			Game gamNew = new Game( "GameConfig.xml" );
			gamNew.runGame();
		}
	}
}
