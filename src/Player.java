import edu.usu.graphics.Rectangle;

import java.util.ArrayList;
import java.util.LinkedList;

public class Player {
    private int currentCell;
    private float cellLength;
    private int mazeSize;

    private final Rectangle playerRect;

    // move rate in seconds
    private final double moveRate = 0.07f;
    // amount of time since last moved in seconds
    private double timeSinceLastMove = 0;

    private ArrayList<Integer> breadcrumbs;
    private LinkedList<Integer> solutionPath;

    private boolean playerHasMoved = false;
    private boolean playerMovedOffPath = true;

    public Player(MazeConfig.Difficulty difficulty, int startCell, LinkedList<Integer> solutionPath) {
        this.playerRect = new Rectangle(
                MazeConfig.playerStartLeft(difficulty),
                MazeConfig.playerStartTop(difficulty),
                MazeConfig.mazeRectLength,
                MazeConfig.mazeRectLength,
                MazeConfig.mazeRectRenderValue
        );

        this.currentCell = startCell;
        this.mazeSize = MazeConfig.mazeSize(difficulty);
        this.cellLength = MazeConfig.cellLength(difficulty);

        this.breadcrumbs = new ArrayList<>();
        this.breadcrumbs.add(startCell);
        this.solutionPath = solutionPath;
    }

    public Rectangle playerRect() {
        return this.playerRect;
    }

    public LinkedList<Integer> getSolutionPath() {
        return this.solutionPath;
    }

    public ArrayList<Integer> getBreadcrumbs() {
        return this.breadcrumbs;
    }

    public int getCurrentCell() {
        return this.currentCell;
    }

    public boolean isOnSolutionPath() {
        return !playerMovedOffPath;
    }

    public boolean hasMoved() {
        return this.playerHasMoved;
    }

    public void setMovedToFalse() {
        this.playerHasMoved = false;
    }

    private void updateSolutionPath() {
        if (this.solutionPath.contains(this.currentCell)){
            this.solutionPath.pop();
            this.playerMovedOffPath = false;
        } else {
            this.solutionPath.push(currentCell);
            this.playerMovedOffPath = true;
        }
    }

    public void resetPlayer(MazeConfig.Difficulty difficulty, LinkedList<Integer> solutionPath) {
        this.currentCell = 0;
        this.cellLength = MazeConfig.cellLength(difficulty);
        this.mazeSize = MazeConfig.mazeSize(difficulty);

        this.playerRect.left = MazeConfig.playerStartLeft(difficulty);
        this.playerRect.top = MazeConfig.playerStartTop(difficulty);
        this.solutionPath = solutionPath;

        this.breadcrumbs = new ArrayList<>();
        this.breadcrumbs.add(currentCell);

        System.out.println(currentCell);
    }

    public void moveRight(Maze maze, double elapsedTime) {
        // don't move if the last move happened less than "moveRate" seconds ago
        if (this.timeSinceLastMove < this.moveRate) {
            this.timeSinceLastMove += elapsedTime;
            return;
        }
        // can't move right if already at rightmost column
        if ((this.currentCell+1) % maze.size() == 0) {
            return;
        }
        // can't move right if wall blocking
        if (maze.hasWallBetween(this.currentCell, this.currentCell+1)) {
            return;
        }

        this.timeSinceLastMove -= this.moveRate;
        this.playerRect.left += this.cellLength;
        this.currentCell += 1;
        this.breadcrumbs.add(this.currentCell);
        this.playerHasMoved = true;

        this.updateSolutionPath();

        System.out.println(currentCell);
    }

    public void moveDown(Maze maze, double elapsedTime) {
        // don't move if the last move happened less than "moveRate" seconds ago
        if (this.timeSinceLastMove < this.moveRate) {
            this.timeSinceLastMove += elapsedTime;
            return;
        }
        // can't move down if already at bottom row
        if ((this.currentCell + maze.size()) >= maze.cellCount()) {
            return;
        }
        // can't move down if wall blocking
        if (maze.hasWallBetween(this.currentCell, this.currentCell+maze.size())) {
            return;
        }

        this.timeSinceLastMove -= this.moveRate;
        this.playerRect.top += cellLength;
        this.currentCell += this.mazeSize;
        this.breadcrumbs.add(this.currentCell);
        this.playerHasMoved = true;

        this.updateSolutionPath();

        System.out.println(currentCell);
    }

    public void moveLeft(Maze maze, double elapsedTime) {
        // don't move if the last move happened less than "moveRate" seconds ago
        if (this.timeSinceLastMove < this.moveRate) {
            this.timeSinceLastMove += elapsedTime;
            return;
        }
        // can't move left if already at leftmost column
        if (this.currentCell % maze.size() == 0) {
            return;
        }
        // can't move left if wall blocking
        if (maze.hasWallBetween(this.currentCell, this.currentCell-1)) {
            return;
        }

        this.timeSinceLastMove -= this.moveRate;

        this.playerRect.left -= cellLength;
        this.currentCell -= 1;
        this.breadcrumbs.add(this.currentCell);
        this.playerHasMoved = true;

        this.updateSolutionPath();

        System.out.println(currentCell);
    }

    public void moveUp(Maze maze, double elapsedTime) {
        // don't move if the last move happened less than "moveRate" seconds ago
        if (this.timeSinceLastMove < this.moveRate) {
            this.timeSinceLastMove += elapsedTime;
            return;
        }
        // can't move up if already at top
        if (this.currentCell < maze.size()) {
            return;
        }
        // can't move up if wall blocking
        if (maze.hasWallBetween(this.currentCell, this.currentCell - maze.size())) {
            return;
        }

        this.timeSinceLastMove -= this.moveRate;
        this.playerRect.top -= cellLength;
        this.currentCell -= this.mazeSize;
        this.breadcrumbs.add(this.currentCell);

        this.updateSolutionPath();

        this.playerHasMoved = true;
    }
}
