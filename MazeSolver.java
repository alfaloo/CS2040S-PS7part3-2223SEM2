import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Function;

public class MazeSolver implements IMazeSolver {
	public class TreeNode implements Comparable<TreeNode>{
		int[] coords;
		int scariness;
		boolean done = false;
		public TreeNode(int[] coords, int scariness) {
			this.coords = coords;
			this.scariness = scariness;
		}

		@Override
		public int compareTo(TreeNode node) {
			return this.scariness - node.scariness;
		}

		@Override
		public String toString() {
			return String.valueOf(this.scariness);
		}
	}
	private static final int NORTH = 0, SOUTH = 1, EAST = 2, WEST = 3;
	private static final int TRUE_WALL = Integer.MAX_VALUE;
	private static final int EMPTY_SPACE = 0;
	private static final List<Function<Room, Integer>> WALL_FUNCTIONS = Arrays.asList(
			Room::getNorthWall,
			Room::getEastWall,
			Room::getWestWall,
			Room::getSouthWall
	);
	private static final int[][] DELTAS = new int[][] {
			{ -1, 0 }, // North
			{ 0, 1 }, // East
			{ 0, -1 }, // West
			{ 1, 0 } // South
	};
	private Maze maze;
	private int startRow, startCol;
	private int endRow, endCol;
	private int scariness;
	private TreeNode root;
	TreeNode[][] been;
	private PriorityQueue<TreeNode> queue = new PriorityQueue<>();

	public MazeSolver() {}

	@Override
	public void initialize(Maze maze) {
		this.maze = maze;
		this.been = new TreeNode[this.maze.getRows()][this.maze.getColumns()];
	}

	@Override
	public Integer pathSearch(int startRow, int startCol, int endRow, int endCol) throws Exception {
		if (maze == null) {
			throw new Exception("Oh no! You cannot call me without initializing the maze!");
		}

		if (startRow < 0 || startCol < 0 || startRow >= maze.getRows() || startCol >= maze.getColumns() ||
				endRow < 0 || endCol < 0 || endRow >= maze.getRows() || endCol >= maze.getColumns()) {
			throw new IllegalArgumentException("Invalid start/end coordinate");
		}

		this.startRow = startRow;
		this.startCol = startCol;
		this.endRow = endRow;
		this.endCol = endCol;
		this.queue.clear();

		for (int i = 0; i < maze.getRows(); ++i) {
			for (int j = 0; j < maze.getColumns(); ++j) {
				this.been[i][j] = null;
			}
		}

		this.been[startRow][startCol] = new TreeNode(new int[] {startRow, startCol}, 0);
		this.queue.add(this.been[startRow][startCol]);

		solve();

		return this.been[endRow][endCol] == null
				? null
				: this.been[endRow][endCol].scariness;
	}

	private boolean canGo(TreeNode node, int dir) {
		int row = node.coords[0];
		int col = node.coords[1];
		// not needed since our maze has a surrounding block of wall
		// but Joe the Average Coder is a defensive coder!
		if (row + DELTAS[dir][0] < 0 || row + DELTAS[dir][0] >= maze.getRows()) return false;
		if (col + DELTAS[dir][1] < 0 || col + DELTAS[dir][1] >= maze.getColumns()) return false;

		return WALL_FUNCTIONS.get(dir).apply(maze.getRoom(row, col)) != TRUE_WALL;
	}

	private int[] directionCoord(TreeNode node, int direction) {
		int row = node.coords[0];
		int col = node.coords[1];
		return new int[] {row + DELTAS[direction][0], col + DELTAS[direction][1]};
	}

	private int getScariness(TreeNode node, int dir) {
		int row = node.coords[0];
		int col = node.coords[1];
		return WALL_FUNCTIONS.get(dir).apply(maze.getRoom(row, col)) == 0
				? 1
				: WALL_FUNCTIONS.get(dir).apply(maze.getRoom(row, col));
	}

	private void solve() {
		while (!this.queue.isEmpty()) {
			TreeNode curr = this.queue.poll();
			int row = curr.coords[0];
			int col = curr.coords[1];
			for (int direction = 0; direction < 4; ++direction) {
				if (canGo(curr, direction)) {
					TreeNode dirNode = this.been[curr.coords[0] + DELTAS[direction][0]][curr.coords[1] + DELTAS[direction][1]];
					if (dirNode == null) {
						this.been[curr.coords[0] + DELTAS[direction][0]][curr.coords[1] + DELTAS[direction][1]]
								= new TreeNode(directionCoord(curr, direction),
								curr.scariness + getScariness(curr, direction));
						this.queue.add(this.been[curr.coords[0] + DELTAS[direction][0]][curr.coords[1] + DELTAS[direction][1]]);
					} else if (!dirNode.done) {
						dirNode.scariness = Math.min(curr.scariness + getScariness(curr, direction), dirNode.scariness);
						this.queue.remove(dirNode);
						this.queue.add(dirNode);
					}
				}
			}
			curr.done = true;
		}
	}

	@Override
	public Integer bonusSearch(int startRow, int startCol, int endRow, int endCol) throws Exception {
		// TODO: Find minimum fear level given new rules.
		return null;
	}

	@Override
	public Integer bonusSearch(int startRow, int startCol, int endRow, int endCol, int sRow, int sCol) throws Exception {
		// TODO: Find minimum fear level given new rules and special room.
		return null;
	}

	public static void main(String[] args) {
		try {
			Maze maze = Maze.readMaze("maze-dense.txt");
			MazeSolver solver = new MazeSolver();
			solver.initialize(maze);

			System.out.println(solver.pathSearch(0, 0, 3, 3));
			for (int i = 0; i < 4; i ++) {
				System.out.println(Arrays.toString(solver.been[i]));
			}
//			System.out.println(solver.pathSearch(0, 0, 0, 3));
//			for (int i = 0; i < 1; i ++) {
//				System.out.println(Arrays.toString(solver.been[i]));
//			}
//			System.out.println(solver.pathSearch(0, 0, 0, 5));
//			for (int i = 0; i < 1; i ++) {
//				System.out.println(Arrays.toString(solver.been[i]));
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
