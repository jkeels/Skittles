package skittles.sim;

public class Score implements Comparable<Score> {
	private int playerIndex;
	private double score;
	
	public Score(int index, double score)
	{
		this.playerIndex = index;
		this.score = score;
	}
	
	public int getPlayerIndex()
	{
		return playerIndex;
	}
	public double getScore()
	{
		return score;
	}

	@Override
	public int compareTo(Score o) {
		if (o.score < this.score)
		{
			return -1;
		}
		else if (o.score > this.score)
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}	
}
