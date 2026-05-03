import controller.GameController;
import model.GameModel;
import view.GameUI;

public class Main {
    public static void main(String[] args) {
        GameModel model = new GameModel();
        GameUI ui = new GameUI();
        GameController controller = new GameController(model, ui);
        controller.startGame();
    }
}