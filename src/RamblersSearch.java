public class RamblersSearch extends Search {

	RamblerState init;
	RamblerState goal;
	SearchNode initNode;
	SearchNode goalNode;
	
	public RamblersSearch()
	{
		TerrainMap tmap = new TerrainMap("/Users/aalbudoor/git/Ramblers/Ramblers/tmc.pgm");
		init = new RamblerState(tmap.getTmap(),tmap.getWidth(),tmap.getDepth(),0,0);
		goal = new RamblerState(tmap.getTmap(),tmap.getWidth(),tmap.getDepth(),tmap.getWidth()-1,tmap.getDepth()-1);
		initNode = new SearchNode(init,0,0);
		goalNode = new SearchNode(goal,0,0);
		
		
	}
	
	public RamblersSearch(RamblerState init,RamblerState goal)
	{
		
		this.init = init;
		this.goal = goal;
		initNode = new SearchNode(init,0,0);
		goalNode = new SearchNode(goal,0,0);
	}
	
	public void runSearch(String strat)
	{
		super.runSearch(init,strat);
	}

	
	public SearchNode getGoal()
	{
		return this.goalNode;
	}
	
//	public static void main(String args[])
//	{
//		RamblersSearch search = new RamblersSearch();
//		search.runSearch("branchAndBound");
//	}
//	
}
