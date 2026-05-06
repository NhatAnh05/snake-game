package controller;

import model.CollisionManager;
import model.Direction;
import model.GameModel;
import model.GameState;
import model.Point;
import model.Snake;
import view.GameUI;

import javax.swing.Timer;

public class GameController {
    private final GameModel model;
    private final GameUI view;
    private final Timer gameLoop;
    private final InputHandler inputHandler;
    private final CollisionManager collisionManager;

    public GameController(GameModel model, GameUI view) {
        this.model = model;
        this.view = view;
        this.inputHandler = new InputHandler(this);

        this.collisionManager = new CollisionManager(40, 30);

        this.gameLoop = new Timer(150, event -> updateGame());
        this.view.addKeyListener(inputHandler);
        this.view.getGamePanel().setOnStartAction(() -> {
            handleStartOrRestartRequest();
        });
    }

    public void startGame() {
        view.render(model);
        view.showGameScreen();
        view.requestFocusInWindow();
    }

    public void handleStartOrRestartRequest() {
        if (model.getCurrentState() == GameState.MENU
                || model.getCurrentState() == GameState.GAME_OVER) {
            model.prepareNewGame();
            gameLoop.start();
            view.render(model);
            view.requestFocusInWindow();
        }
    }

    public void requestChangeDirection(Direction newDirection) {
        if (!isGamePlaying()) {
            return;
        }

        if (validateDirection(newDirection)) {
            model.getSnake().setDirection(newDirection);
            view.render(model);
        }
    }

    public boolean validateDirection(Direction newDirection) {
        if (newDirection == null || model.getSnake() == null) {
            return false;
        }

        return !model.getSnake().isOppositeDirection(newDirection);
    }

    private void updateGame() {
       if (!isGamePlaying()) return;

        Snake snake = model.getSnake();
        Point head = snake.getHead();
        Point foodPos = model.getFood().getPosition();

        // 1. Kiểm tra xem bước tiếp theo có ăn mồi không
        boolean willEat = head.equals(foodPos); 

        // 2. Di chuyển rắn (nếu ăn mồi thì grow = true)
        snake.move(willEat); 

        // 3. Nếu ăn mồi, xử lý tăng điểm và tạo mồi mới
        if (willEat) {
            model.getScoreManager().increaseScore(); 
            model.getFood().spawn(snake.getBody()); 
        }

        // 4. Kiểm tra va chạm chết (tường/thân) sau khi di chuyển
        if (collisionManager.checkCollision(snake)) {
            handleGameOver();
            return;
        }

        view.render(model);
    }

    private void handleGameOver() {
        gameLoop.stop();
        model.setCurrentState(GameState.GAME_OVER);
        view.render(model);
    }

    public boolean isGamePlaying() {
        return model.getCurrentState() == GameState.PLAYING;
    }

    public InputHandler getInputHandler() {
        return inputHandler;
    }

    public void backToMenu() {
        if (model.getCurrentState() == GameState.GAME_OVER) {
            model.setCurrentState(GameState.MENU); // Chuyển trạng thái về MENU
            view.render(model); // Vẽ lại màn hình Menu
        }
    }
}
