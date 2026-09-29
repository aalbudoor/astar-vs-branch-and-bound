import java.util.ArrayList;

public class RamblerState extends SearchState {

	
	// Representation of our state.
	int posX;
	int posY;
	
	// These are globally constant variable so not putting in each state which is waste of memory
	static int rows;
	static int columns;	
	static int matrix[][];
	static RamblerState goal;
	
	
	// constructor for ramblers state
	
	public static void setRamblerMatrix(int row,int column,int[][]mat)
	{
		rows = row;
		columns = column;
		matrix = new int[rows][columns];
		for(int i=0;i<rows;i++)
		{
			for(int j=0;j<columns;j++)
			{
				matrix[i][j] = mat[i][j];
			}
		}	
		
	}
	
	public static void setGoalState(RamblerState goalState)
	{
		goal = goalState;
	}
	// constructor with all parameters.

	public RamblerState(int mat[][],int rows,int columns,int posX,int posY,RamblerState goal) {
		// TODO Auto-generated constructor stub
		setRamblerMatrix(rows,columns,mat);
		setGoalState(goal);
        this.posX = posX;
        this.posY = posY;
  
	}
	
	public RamblerState(int mat[][],int rows,int columns,int posX,int posY) {
		// TODO Auto-generated constructor stub
		setRamblerMatrix(rows,columns,mat);
        this.posX = posX;
        this.posY = posY;
  
	}
	
	
	// constructor with position on the grid
	
	public RamblerState(int posX,int posY)
	{
		this.posX = posX;
		this.posY = posY;
	}
	
	// constructor with another object
	public RamblerState(RamblerState s)
	{
		this.posX = s.getPosX();
		this.posY = s.getPosY();
	}
	
	
	// Getters
	public int getPosX()
	{
		return this.posX;
	}
	
	public int getPosY()
	{
		return this.posY;
	}

	@Override
	boolean goalPredicate(Search searcher) {
		if (this.isSameState((RamblerState)(((RamblersSearch)searcher).getGoal()).get_State()))
			return true;
		return false;
	}
	
	// for comparing 2 states if they are same or not.
	boolean isSameState(RamblerState s)
	{
		return posX==s.getX() && posY==s.getY();
	}

	int heightHeuristic()
	{
		return Math.abs(this.getHeight() - goal.getHeight());
	}
	
	int euclidianDistanceHeuristic()
	{
		return (int)Math.sqrt((this.getPosX()-goal.getPosX())*(this.getPosX()-goal.getPosX()) + (this.getPosY()-goal.getPosY())*(this.getPosX()-goal.getPosX()) + (this.getPosX()-goal.getHeight())*(this.getPosX()-goal.getHeight()));
	}
	
	int manhattanDistanceHeuristic()
	{
		return Math.abs(this.getPosX()-goal.getPosX()) + Math.abs(this.getPosY()-goal.getPosY());
	}

	@Override
	ArrayList<SearchState> getSuccessors(Search searcher) {
		
		ArrayList<SearchState> sol = new ArrayList<>();
		SearchState s;
		s = this.moveDown();
		
		if(s!=null)
		{
			s.localCost = this.cost((RamblerState)s);
			s.estRemCost = this.euclidianDistanceHeuristic();
			
			sol.add(s);
		}
		s = this.moveUp();
		if(s!=null)
		{
			s.localCost = this.cost((RamblerState) s);
			s.estRemCost = this.euclidianDistanceHeuristic();
			sol.add(s);
		}
		s = this.moveRight();
		
		if(s!=null)
		{
			s.localCost = this.cost((RamblerState) s);
			s.estRemCost = this.euclidianDistanceHeuristic();
			sol.add(s);
		}
		
		s = this.moveLeft();
		if(s!=null)
		{
			s.localCost = this.cost((RamblerState) s);
			s.estRemCost = this.euclidianDistanceHeuristic();
			sol.add(s);
		}
		
		return sol;
	}

	@Override
	boolean sameState(SearchState s2) {
		return ((RamblerState) s2).getX() == this.getX() && ((RamblerState) s2).getY() == this.getY();
	}

	int getRows() {
		return rows;
	}

	int getColumns() {
		return columns;
	}

	int getX() {
		return posX;
	}

	int getY() {
		return posY;
	}

	boolean setX(int x) {
		if (x >= 0 && x < rows) {
			posX = x;
			return true;
		}
		return false;
	}

	boolean setY(int y) {
		if (y >= 0 && y < columns) {
			posY = y;
			return true;
		}
		return false;
	}

	// getting hill of current state.
	int getHeight() {
		return matrix[posX][posY];
	}
	
	// cost of stepping from current state to state r2
	int cost(RamblerState r2) {
		if (this.getHeight() >= r2.getHeight())
			return 1;
		else
			return r2.getHeight() - this.getHeight() + 1;
	}
	
	// operations

	RamblerState moveUp() {
		RamblerState newState = new RamblerState(this);
		if (newState.setX(newState.getX() - 1))
			return newState;
		return null;
	}

	RamblerState moveDown() {
		RamblerState newState = new RamblerState(this);
		if (newState.setX(newState.getX() + 1))
			return newState;
		return null;
	}

	RamblerState moveRight() {
		RamblerState newState = new RamblerState(this);
		if (newState.setY(newState.getY() + 1))
			return newState;
		return null;
	}
	
	RamblerState moveLeft()
    {
		RamblerState newState = new RamblerState(this);
		if (newState.setY(newState.getY() - 1))
			return newState;
		return null;
    }
	
	public String toString()
	{
		String sol = Integer.toString(this.posX) + ", "+Integer.toString(this.posY);
		return sol;
		
	}

}
