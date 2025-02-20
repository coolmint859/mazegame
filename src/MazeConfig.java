import edu.usu.graphics.Rectangle;

/**
 * Configuration class for the look of the maze in the window.
 * Prevents the data used for rendering from being stored all over the code.
 * */
public class MazeConfig {
    public static float mazeRectLength = 0.02f;
    public static float mazeRectRenderValue = 0.6f;

    public static int startCell = 0;

    public static float wallRenderValue = 0.6f;

    public static float wallWidth(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> 0.01f;
            case MEDIUM -> 0.008f;
            case HARD -> 0.006f;
            case EXTREME -> 0.005f;
        };
    }

    public static float cellLength(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> 0.1f;
            case MEDIUM -> 0.08f;
            case HARD -> 0.06f;
            case EXTREME -> 0.05f;
        };
    }

    public static Rectangle createMazeRect(float left, float top) {
        return new Rectangle(
                left, top,
                mazeRectLength,
                mazeRectLength,
                mazeRectRenderValue
        );
    }

    public static Rectangle createMazeWall(float left, float top, boolean isSideWall, Difficulty difficulty) {
        float wallWidth = wallWidth(difficulty);
        float trueLeft = left + wallWidth/2;
        float trueTop = top + wallWidth/2;

        if (isSideWall)
            return new Rectangle(trueLeft, trueTop, wallWidth, cellLength(difficulty), wallRenderValue);
        else
            return new Rectangle(trueLeft, trueTop, cellLength(difficulty), wallWidth, wallRenderValue);
    }

    /**
     * EASY: 5x5,
     * MEDIUM: 10x10,
     * HARD: 15x15,
     * EXTREME: 20x20
     * */
    public static int mazeSize(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> 5;
            case MEDIUM -> 10;
            case HARD -> 15;
            case EXTREME -> 20;
        };
    }

    public static int numberOfCells(Difficulty difficulty) {
        return (int) Math.pow(mazeSize(difficulty), 2);
    }

    public static float startLeft(Difficulty difficulty) {
        return -(cellLength(difficulty) * mazeSize(difficulty))/2;
    }

    public static float startTop(Difficulty difficulty) {
        return -(cellLength(difficulty) * mazeSize(difficulty))/2;
    }

    public static float playerStartLeft(Difficulty difficulty) {
        return (cellLength(difficulty)/ 2.0f) + startLeft(difficulty);
    }

    public static float playerStartTop(Difficulty difficulty) {
        return (cellLength(difficulty)/ 2.0f) + startTop(difficulty);
    }

    public static float goalLeft(Difficulty difficulty) {
        return playerStartLeft(difficulty) + cellLength(difficulty) * (mazeSize(difficulty)-1);
    }

    public static float goalTop(Difficulty difficulty) {
        return playerStartTop(difficulty) + cellLength(difficulty) * (mazeSize(difficulty)-1);
    }

    public static float cellLeft(int row, Difficulty difficulty) {
        if (row % mazeSize(difficulty) == 0)
            return playerStartLeft(difficulty);
        else
            return playerStartLeft(difficulty) + cellLength(difficulty) * row;
    }

    public static float cellTop(int col, Difficulty difficulty) {
        if (col + mazeSize(difficulty) > numberOfCells(difficulty))
            return playerStartTop(difficulty);
        else
            return playerStartTop(difficulty) + cellLength(difficulty) * col;
    }

    public static int difficultyIndex(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> 0;
            case MEDIUM -> 1;
            case HARD -> 2;
            case EXTREME -> 3;
        };
    }

    public static int mazeSizeByIndex(int i) {
        return switch (i) {
            case 0 -> mazeSize(Difficulty.EASY);
            case 1 -> mazeSize(Difficulty.MEDIUM);
            case 2 -> mazeSize(Difficulty.HARD);
            case 3 -> mazeSize(Difficulty.EXTREME);
            default -> mazeSize(Difficulty.EASY);
        };
    }

    public enum Difficulty {
        EASY, MEDIUM, HARD, EXTREME
    }
}
