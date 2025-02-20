import edu.usu.utils.Tuple2;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class Maze {
    private boolean[][] mazeGraph;
    private final int mazeSize;
    private final int numCells;

    private int start;
    private int goal;
    private boolean printShortestPath;

    public Maze(MazeConfig.Difficulty difficulty) {
        this.mazeSize = MazeConfig.mazeSize(difficulty);
        this.numCells = MazeConfig.numberOfCells(difficulty);

        this.start = 0;
        this.goal = this.numCells-1;
        this.printShortestPath = false;

        // initially make the maze full of walls
        this.mazeGraph = new boolean[this.numCells][this.numCells];
        for (int cell = 0; cell < this.numCells; cell++) {
            ArrayList<Integer> neighborCells = getNeighbors(cell);

            // all neighbors have walls separating them.
            for (int neighbor: neighborCells) {
                this.mazeGraph[cell][neighbor] = true;
                this.mazeGraph[neighbor][cell] = true;
            }
        }
        this.generateMaze();
    }

    public void enableShortestPathPrint() {
        this.printShortestPath = true;
    }

    public void disableShortestPathPrint() {
        this.printShortestPath = false;
    }

    public boolean isAtGoal(int cell) {
        return cell == this.goal;
    }

    public int size() {
        return this.mazeSize;
    }

    public int cellCount() {
        return this.numCells;
    }

    public int cellValue(int row, int col) {
        return this.mazeSize * row + col;
    }

    public int cellRow(int cell) {
        return cell % this.mazeSize;
    }

    public int cellCol(int cell) {
        return Math.floorDiv(cell, this.mazeSize);
    }

    /** returns true if this maze has a wall between the given cells, false otherwise */
    public boolean hasWallBetween(int cell1, int cell2) {
        return this.mazeGraph[cell1][cell2];
    }

    /**
     * generates a maze using Randomized Prim's algorithm
     * */
    private void generateMaze() {
        Random random = new Random();

        // add a random cell and it's neighbors to initialize lists
        int visitedCell = random.nextInt(this.numCells);
        ArrayList<Integer> mazeCells = new ArrayList<>();
        mazeCells.add(visitedCell);

        ArrayList<Integer> unvisitedCells = new ArrayList<>(this.getNeighbors(visitedCell));

        while (!unvisitedCells.isEmpty()) {
            // pick a random unvisited cell
            int unvisitedCellIndex = random.nextInt(unvisitedCells.size());
            int unvisitedCell = unvisitedCells.get(unvisitedCellIndex);

            // if the chosen cell is not already in the maze
            if (!mazeCells.contains(unvisitedCell)) {
                ArrayList<Integer> neighbors = this.getNeighbors(unvisitedCell);

                // pick a random visited cell that is a neighbor of the unvisited cell
                do {
                    int visitedCellIndex = random.nextInt(neighbors.size());
                    visitedCell = neighbors.get(visitedCellIndex);
                } while (!mazeCells.contains(visitedCell));

                // remove the wall separating the cells
                this.mazeGraph[visitedCell][unvisitedCell] = false;
                this.mazeGraph[unvisitedCell][visitedCell] = false;

                // add all neighbors of unvisited cell to unvisited cells (if not already in list)
                for (int neighbor: neighbors) {
                    if (!unvisitedCells.contains(neighbor))
                        unvisitedCells.add(neighbor);
                }

                // add the unvisited cell to the maze (making it visited)
                mazeCells.add(unvisitedCell);
            }
            // remove the unvisited cell from the list of unvisited cells
            unvisitedCells.remove(Integer.valueOf(unvisitedCell));
        }
    }

    /**
     * returns all neighbors of a cell
     * */
    private ArrayList<Integer> getNeighbors(int cell) {
        ArrayList<Integer> neighborCells = new ArrayList<>();
        // cell to right is neighbor if on same row
        if (cell+1 < this.numCells && (cell+1) % mazeSize != 0)
            neighborCells.add(cell+1);
        // cell below is neighbor
        if (cell+this.mazeSize < this.numCells)
            neighborCells.add(cell+this.mazeSize);
        // cell to left is neighbor if on same row
        if (cell-1 >= 0 && cell % mazeSize != 0)
            neighborCells.add(cell-1);
        // cell above is neighbor
        if (cell-this.mazeSize >= 0)
            neighborCells.add(cell-this.mazeSize);

        return neighborCells;
    }

    /**
    * returns all neighbors of a cell that are reachable by the cell
    * */
    public ArrayList<Integer> getReachableNeighbors(int cell) {
        ArrayList<Integer> neighbors = getNeighbors(cell);
        ArrayList<Integer> validNeighbors = new ArrayList<>();
        for (int neighbor : neighbors) {
            if (!this.mazeGraph[cell][neighbor])
                validNeighbors.add(neighbor);
        }
        return validNeighbors;
    }

    /**
     * Finds the shortest path of this maze using breadth first search.
     * Returns a stack of integers representing the cells (top cell is the start cell)
     * */
    public LinkedList<Integer> solve() {
        // solve maze using BFS
        LinkedList<Integer> nextCells = new LinkedList<>();
        ArrayList<Integer> visitedCells = new ArrayList<>();
        nextCells.add(this.start);
        while (!nextCells.isEmpty()) {
            int nextCell = nextCells.pop();
            visitedCells.add(nextCell);

            if (nextCell == this.goal) break;

            // append neighbors to the list and continue
            ArrayList<Integer> validNeighbors = getReachableNeighbors(nextCell);
            for (int neighbor : validNeighbors) {
                if (!visitedCells.contains(neighbor))
                    nextCells.add(neighbor);
            }
        }

        // reconstruct shortest path
        LinkedList<Integer> shortestPath = new LinkedList<>();
        int currCell = visitedCells.getLast();
        shortestPath.push(currCell);
        ArrayList<Integer> neighbors = getReachableNeighbors(currCell);
        for (int i = visitedCells.size()-1; i >= 0; i--) {
            int nextCell = visitedCells.get(i);
            if (neighbors.contains(nextCell)) {
                shortestPath.push(nextCell);
                currCell = nextCell;
                neighbors = getReachableNeighbors(currCell);
            }
        }

        return shortestPath;
    }

    public String toString() {
        StringBuilder mazeStr = new StringBuilder();
        mazeStr.append(String.format("MazeSize = %dx%d, # of Cells = %d\n", this.mazeSize, this.mazeSize, (int) Math.pow(this.mazeSize, 2)));
        mazeStr.append("|---".repeat(this.mazeSize));
        mazeStr.append("|\n");

        LinkedList<Integer> shortestPath = this.solve();

        for (int i = 0; i < this.mazeSize; i++) {
            mazeStr.append("|");
            for (int j = 0; j < this.mazeSize; j++) {
                int cell = cellValue(i, j);
                int neighbor = (cell+1 < this.numCells && (cell+1) % this.mazeSize != 0) ? cell+1 : -1;

                if (this.printShortestPath && shortestPath.contains(cell))
                    mazeStr.append(" X ");
                else
                    mazeStr.append("   ");

                if (neighbor == -1 || this.mazeGraph[cell][neighbor])
                    mazeStr.append("|");
                else
                    mazeStr.append(" ");
            }
            mazeStr.append("\n");

            for (int j = 0; j < this.mazeSize; j++) {
                mazeStr.append("|");
                int cell = cellValue(i, j);
                int neighbor = cell+this.mazeSize < this.numCells ? cell+this.mazeSize : -1;

                if (neighbor == -1 || this.mazeGraph[cell][neighbor])
                    mazeStr.append("---");
                else
                    mazeStr.append("   ");
            }
            mazeStr.append("|\n");
        }

        if (this.printShortestPath) {
            mazeStr.append("Cell on shortest path is marked with an 'X'.");
        }
        return mazeStr.toString();
    }

    public static void main(String[] args) {
        Maze maze = new Maze(MazeConfig.Difficulty.EASY);

        maze.enableShortestPathPrint();
        System.out.println(maze);
    }
}
