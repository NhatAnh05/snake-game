import controller.GameController;
import model.GameModel;
import view.GameUI;

public class Main {
    public static void main(String[] args) {
        // Khởi tạo Model và View
        GameModel model = new GameModel();
        GameUI ui = new GameUI();

        // Nhồi Model và View vào Controller để điều phối
        GameController controller = new GameController(model, ui);

        // Bắt đầu game
        controller.startGame();
    }
}