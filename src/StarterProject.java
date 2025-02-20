import edu.usu.graphics.Color;
import edu.usu.graphics.Graphics2D;

public class StarterProject {
    public static Color backgroundColor = new Color(46/255f, 64/255f, 69/255f);

    public static void main(String[] args) {
        try (Graphics2D graphics = new Graphics2D(1600, 1000, "The Grid")) {
            graphics.initialize(backgroundColor);
            Game game = new Game(graphics);
            game.initialize();
            game.run();
            game.shutdown();
        }
    }
}