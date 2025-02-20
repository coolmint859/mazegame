import edu.usu.graphics.*;

import java.util.ArrayList;
import java.util.LinkedList;

import static org.lwjgl.glfw.GLFW.*;

public class Game {
    private final Graphics2D graphics;
    private Maze maze;
    private MazeConfig.Difficulty difficulty;

    // color palette retrieved from https://www.color-hex.com/color-palette/1055758
    private final Color wallColor = new Color(211/255f, 211/255f, 211/255f);
    private final Color playerColor = new Color(255/255f, 244/255f, 230/255f);
    private final Color solutionColor = new Color(0/255f, 106/255f, 108/255f);
    private final Color breadcrumbColor = new Color(0/255f, 79/255f, 81/255f);;
    private final Color fontColor = new Color(211/255f, 211/255f, 211/255f);

    // background image retrieved from https://www.rawpixel.com/image/12226156/image-background-texture-aesthetic
    private final Texture backgroundImage = new Texture("./resources/images/background.jpg");
    private Rectangle backgroundRect;

    private Font textFont;

    private final KeyboardInput inputHandler;
    private final ArrayList<Rectangle> walls = new ArrayList<>();

    private Player player;
    private int startCell;
    private boolean gameWon;

    private Rectangle pauseBar;
    private boolean isPaused;

    private final LinkedList<Rectangle> solutionPath = new LinkedList<>();
    private Rectangle goal;
    private boolean displaySolution;

    private final ArrayList<Rectangle> breadcrumbs = new ArrayList<>();
    private boolean displayBreadcrumbs;

    private Rectangle hint;
    private boolean displayHint;

    private final int correctSquarePoints = 5;
    private final int foundGoalPoints = 10;
    private final int incorrectSquarePoints = -6;

    private int currentScore;
    private ArrayList<Integer> highScores;

    private double currentTime;
    private ArrayList<Double> bestTimes;

    private boolean displayHighScores;
    private boolean displayCredits;

    public Game(Graphics2D graphics) {
        this.graphics = graphics;
        this.inputHandler = new KeyboardInput(this.graphics.getWindow());
    }

    public void initialize() {
        this.maze = new Maze(MazeConfig.Difficulty.EASY);
        this.maze.enableShortestPathPrint();
        this.difficulty = MazeConfig.Difficulty.EASY;

        this.isPaused = false;
        this.displayBreadcrumbs = false;
        this.displaySolution = false;
        this.displayHint = false;

        this.pauseBar = new Rectangle(-0.3f, -0.2f, 0.6f, 0.2f, 0.8f);

        this.currentScore = 0;
        this.highScores = new ArrayList<>();

        this.currentTime = 0;
        this.bestTimes = new ArrayList<>();

        for (int i = 0; i < MazeConfig.Difficulty.values().length; i++) {
            this.highScores.add(0);
            this.bestTimes.add(0.0);
        }

        textFont = new Font("Arial", java.awt.Font.PLAIN, 42, false);

        this.startCell = MazeConfig.startCell;
        this.player = new Player(this.difficulty, this.startCell, this.maze.solve());

        this.goal = MazeConfig.createMazeRect(MazeConfig.goalLeft(difficulty), MazeConfig.goalTop(difficulty));

        updateSolutionPath();
        updateBreadcrumbs();

        this.registerInputCommands();
        this.makeMazeWalls();
        this.createMazeBackground();

        System.out.println(this.maze);
    }

    private void createMazeBackground() {
        int mazeSize = MazeConfig.mazeSize(difficulty);
        float wallWidth = MazeConfig.wallWidth(difficulty);
        float bgRenderValue = 0.4f;

        this.backgroundRect = new Rectangle(
                MazeConfig.startLeft(difficulty) + wallWidth/2,
                MazeConfig.startTop(difficulty) + wallWidth/2,
                MazeConfig.cellLength(difficulty) * mazeSize + wallWidth,
                MazeConfig.cellLength(difficulty) * mazeSize + wallWidth,
                bgRenderValue
        );
    }

    private void updateSolutionPath() {
        this.solutionPath.clear();
        for (int cell : this.player.getSolutionPath()) {
            float left = MazeConfig.cellLeft(this.maze.cellRow(cell), difficulty);
            float top = MazeConfig.cellTop(this.maze.cellCol(cell), difficulty);
            this.solutionPath.push(MazeConfig.createMazeRect(left, top));
        }
        // the hint square should be the second to last square on the solution path
        if (!gameWon)
            this.hint = solutionPath.get(solutionPath.size()-2);
    }

    private void updateBreadcrumbs() {
        this.breadcrumbs.clear();
        for (int cell : this.player.getBreadcrumbs()) {
            float left = MazeConfig.cellLeft(this.maze.cellRow(cell), difficulty);
            float top = MazeConfig.cellTop(this.maze.cellCol(cell), difficulty);
            this.breadcrumbs.add(MazeConfig.createMazeRect(left, top));
        }
    }

    private void registerInputCommands() {
        inputHandler.registerCommand(GLFW_KEY_ESCAPE, false, (double elapsedTime) -> {
            glfwSetWindowShouldClose(graphics.getWindow(), true);
        });
        inputHandler.registerCommand(GLFW_KEY_F1, true, (double elapsedTime) -> {
            if (!this.isPaused) {
                this.difficulty = MazeConfig.Difficulty.EASY;
                this.resetMaze();
            }
        });
        inputHandler.registerCommand(GLFW_KEY_F2, true, (double elapsedTime) -> {
            if (!this.isPaused) {
                this.difficulty = MazeConfig.Difficulty.MEDIUM;
                this.resetMaze();
            }
        });
        inputHandler.registerCommand(GLFW_KEY_F3, true, (double elapsedTime) -> {
            if (!this.isPaused) {
                this.difficulty = MazeConfig.Difficulty.HARD;
                this.resetMaze();
            }
        });
        inputHandler.registerCommand(GLFW_KEY_F4, true, (double elapsedTime) -> {
            if (!this.isPaused) {
                this.difficulty = MazeConfig.Difficulty.EXTREME;
                this.resetMaze();
            }
        });
        inputHandler.registerCommand(GLFW_KEY_F5, true, (double elapsedTime) -> {
            if (!this.isPaused)
                this.displayHighScores = !this.displayHighScores;
        });
        inputHandler.registerCommand(GLFW_KEY_F6, true, (double elapsedTime) -> {

            this.displayCredits = !this.displayCredits;
        });
        inputHandler.registerCommand(GLFW_KEY_SPACE, true, (double elapsedTime) -> {
            if (!this.gameWon)
                this.isPaused = !this.isPaused;
        });

        inputHandler.registerCommand(GLFW_KEY_W, false, this::moveUp);
        inputHandler.registerCommand(GLFW_KEY_A, false, this::moveLeft);
        inputHandler.registerCommand(GLFW_KEY_S, false, this::moveDown);
        inputHandler.registerCommand(GLFW_KEY_D, false, this::moveRight);

        inputHandler.registerCommand(GLFW_KEY_UP, false, this::moveUp);
        inputHandler.registerCommand(GLFW_KEY_LEFT, false, this::moveLeft);
        inputHandler.registerCommand(GLFW_KEY_DOWN, false, this::moveDown);
        inputHandler.registerCommand(GLFW_KEY_RIGHT, false, this::moveRight);

        inputHandler.registerCommand(GLFW_KEY_I, false, this::moveUp);
        inputHandler.registerCommand(GLFW_KEY_J, false, this::moveLeft);
        inputHandler.registerCommand(GLFW_KEY_K, false, this::moveDown);
        inputHandler.registerCommand(GLFW_KEY_L, false, this::moveRight);

        inputHandler.registerCommand(GLFW_KEY_P, true, (double elapsedTime) -> {
            if (!this.isPaused)
                this.displaySolution = !this.displaySolution;
        });
        inputHandler.registerCommand(GLFW_KEY_B, true, (double elapsedTime) -> {
            if (!this.isPaused)
                this.displayBreadcrumbs = !this.displayBreadcrumbs;
        });
        inputHandler.registerCommand(GLFW_KEY_H, true, (double elapsedTime) -> {
            if (!this.isPaused)
                this.displayHint = !this.displayHint;
        });


    }

    private void resetMaze() {
        this.maze = new Maze(this.difficulty);
        this.gameWon = false;
        this.currentTime = 0;
        this.displaySolution = false;
        this.displayBreadcrumbs = false;
        this.displayHint = false;

        this.goal = MazeConfig.createMazeRect(MazeConfig.goalLeft(difficulty), MazeConfig.goalTop(difficulty));
        this.player.resetPlayer(this.difficulty, this.maze.solve());

        this.currentScore = 0;

        this.updateSolutionPath();
        this.updateBreadcrumbs();
        this.createMazeBackground();
        this.makeMazeWalls();
    }

    private void makeMazeWalls() {
        this.walls.clear();

        float cellLength = MazeConfig.cellLength(difficulty);
        float left = MazeConfig.startLeft(difficulty);
        float top = MazeConfig.startTop(difficulty);

        for (int cell = 0; cell < this.maze.cellCount(); cell++) {
            // if cell is on the leftmost column
            if (cell % this.maze.size() == 0)
                this.walls.add(MazeConfig.createMazeWall(left, top, true, this.difficulty));
            // if wall between this cell and the cell to the right
            else if (this.maze.hasWallBetween(cell-1, cell))
                this.walls.add(MazeConfig.createMazeWall(left, top, true, this.difficulty));
            // if cell is on topmost row
            if (cell < this.maze.size())
                this.walls.add(MazeConfig.createMazeWall(left, top, false, this.difficulty));
            // if wall between this cell and the cell directly above
            else if (this.maze.hasWallBetween(cell-this.maze.size(), cell))
                this.walls.add(MazeConfig.createMazeWall(left, top, false, this.difficulty));

            // if cell is on bottommost row
            if (cell + this.maze.size() >= this.maze.cellCount())
                this.walls.add(MazeConfig.createMazeWall(left, top+cellLength, false, this.difficulty));

            // if cell is on rightmost columns
            if ((cell+1) % this.maze.size() == 0) {
                this.walls.add(MazeConfig.createMazeWall(left+cellLength, top, true, this.difficulty));
                left = -(cellLength * this.maze.size())/2;
                top += cellLength;
                continue;
            }
            left += cellLength;
        }
    }

    public void shutdown() {
    }

    public void run() {
        // Grab the first time
        double previousTime = glfwGetTime();

        while (!graphics.shouldClose()) {
            double currentTime = glfwGetTime();
            double elapsedTime = currentTime - previousTime;    // elapsed time is in seconds
            previousTime = currentTime;

            processInput(elapsedTime);
            update(elapsedTime);
            render(elapsedTime);
        }
    }

    private void processInput(double elapsedTime) {
        // Poll for window events: required in order for window, keyboard, etc events are captured.
        glfwPollEvents();
        inputHandler.update(elapsedTime);
    }

    private void moveUp(double elapsedTime) {
        if (this.gameWon || this.isPaused)
            return;

        this.player.moveUp(this.maze, elapsedTime);
    }

    private void moveDown(double elapsedTime) {
        if (this.gameWon || this.isPaused)
            return;

        this.player.moveDown(this.maze, elapsedTime);
    }

    private void moveLeft(double elapsedTime) {
        if (this.gameWon || this.isPaused)
            return;

        this.player.moveLeft(this.maze, elapsedTime);
    }

    private void moveRight(double elapsedTime) {
        if (this.gameWon || this.isPaused)
            return;

        this.player.moveRight(this.maze, elapsedTime);
    }

    private void updateScores() {
        if (!this.player.hasMoved())
            return;

        if (this.maze.isAtGoal(this.player.getCurrentCell())) {
            this.currentScore += this.foundGoalPoints;

            int difficultyIndex = MazeConfig.difficultyIndex(this.difficulty);
            int currentHighScore = this.highScores.get(difficultyIndex);
            if (this.currentScore > currentHighScore) {
                this.highScores.set(difficultyIndex, currentScore);
                this.bestTimes.set(difficultyIndex, currentTime);
            }

            this.gameWon = true;
        } else if (this.player.isOnSolutionPath()) {
            this.currentScore += this.correctSquarePoints;
        } else {
            this.currentScore += this.incorrectSquarePoints;
        }
        this.player.setMovedToFalse();
    }

    private void updateTimes(double elapsedTime) {
        if (!this.gameWon && !this.isPaused)
            this.currentTime += elapsedTime;
    }

    private void update(double elapsedTime) {
        this.updateScores();
        this.updateTimes(elapsedTime);
        this.updateSolutionPath();
        this.updateBreadcrumbs();
    }

    public void renderText() {
        float controlTextLeft = -0.9f;
        float textHeight = 0.04f;

        graphics.drawTextByHeight(textFont, "CONTROLS", controlTextLeft, -0.55f, textHeight, fontColor);
        graphics.drawTextByHeight(textFont, "New 5x5 Maze: F1", controlTextLeft, -0.5f, textHeight, fontColor);
        graphics.drawTextByHeight(textFont, "New 10x10 Maze: F2", controlTextLeft, -0.45f, textHeight, fontColor);
        graphics.drawTextByHeight(textFont, "New 15x15 Maze: F3", controlTextLeft, -0.4f, textHeight, fontColor);
        graphics.drawTextByHeight(textFont, "New 20x20 Maze: F4", controlTextLeft, -0.35f, textHeight, fontColor);

        graphics.drawTextByHeight(textFont, "Toggle Hint: H", controlTextLeft, -0.25f, textHeight, fontColor);
        graphics.drawTextByHeight(textFont, "Toggle Breadcrumbs: B", controlTextLeft, -0.20f, textHeight, fontColor);
        graphics.drawTextByHeight(textFont, "Toggle Solution: P", controlTextLeft, -0.15f, textHeight, fontColor);

        graphics.drawTextByHeight(textFont, "Move Up : W|I|UP", controlTextLeft, -0.05f, textHeight, fontColor);
        graphics.drawTextByHeight(textFont, "Move Down : S|K|DOWN", controlTextLeft, 0.0f, textHeight, fontColor);
        graphics.drawTextByHeight(textFont, "Move Left : A|J|LEFT", controlTextLeft, 0.05f, textHeight, fontColor);
        graphics.drawTextByHeight(textFont, "Move Right : D|L|RIGHT", controlTextLeft, 0.1f, textHeight, fontColor);

        graphics.drawTextByHeight(textFont, "Pause: SPACE", controlTextLeft, 0.2f, textHeight, fontColor);

        graphics.drawTextByHeight(textFont, "See HighScores: F5", controlTextLeft, 0.25f, textHeight, fontColor);
        graphics.drawTextByHeight(textFont, "See Credits: F6", controlTextLeft, 0.3f, textHeight, fontColor);

        graphics.drawTextByHeight(textFont, ("Score: " + this.currentScore), -0.5f, -0.6f, textHeight, fontColor);

        graphics.drawTextByHeight(textFont, String.format("Time: %.1f s", this.currentTime), 0.3f, -0.6f, textHeight, fontColor);

        if (gameWon) {
            graphics.drawTextByHeight(textFont, "Great Job!", 0.6f, 0.3f, textHeight, fontColor);
        }

        if (this.isPaused)
            graphics.drawTextByHeight(textFont, "PAUSED", -0.14f, -0.14f, 0.08f, 0.8f, breadcrumbColor);

        if (this.displayHighScores) {
            graphics.drawTextByHeight(textFont, "HIGH SCORES", 0.6f, -0.55f, textHeight, fontColor);
            for (int i = 0; i < this.highScores.size(); i++) {
                int mazeSize = MazeConfig.mazeSizeByIndex(i);
                graphics.drawTextByHeight(textFont,
                        String.format("%dx%d: %d @%.1f s",
                                mazeSize, mazeSize,
                                this.highScores.get(i),
                                this.bestTimes.get(i)
                                ),
                        0.6f, (-0.5f + 0.05f*i), textHeight, fontColor);
            }
        }

        if (displayCredits) {
            graphics.drawTextByHeight(textFont, "Designed by Preston Hall for CS5410", -0.3f, 0.52f, textHeight, fontColor);
            graphics.drawTextByHeight(textFont, "Background Image at rawpixel.com", -0.28f, 0.57f, textHeight, fontColor);
        }

    }

    private void render(double elapsedTime) {
        graphics.begin();

        graphics.draw(backgroundImage, backgroundRect, Color.WHITE);

        for (Rectangle wall : this.walls) {
            graphics.draw(wall, this.wallColor);
        }

        if (this.displayBreadcrumbs) {
            for (Rectangle breadcrumb : breadcrumbs) {
                graphics.draw(breadcrumb, this.breadcrumbColor);
            }
        }

        if (this.displayHint) {
            graphics.draw(hint, this.solutionColor);
        }

        if (this.displaySolution) {
            for (Rectangle solutionSquare : solutionPath) {
                graphics.draw(solutionSquare, this.solutionColor);
            }
        }

        graphics.draw(this.goal, solutionColor);
        graphics.draw(this.player.playerRect(), playerColor);

        if (isPaused)
            graphics.draw(pauseBar, wallColor);
        renderText();

        graphics.end();
    }
}
