package controller;

import model.Direction;
import model.GameModel;
import model.GameState;
import view.GameUI;

import javax.swing.Timer;

public class GameController {
    private final GameModel model;
    private final GameUI view;
    private final Timer gameLoop;
    private final InputHandler inputHandler;

    public GameController(GameModel model, GameUI view) {
        this.model = model;
        this.view = view;
        this.inputHandler = new InputHandler(this);

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
        if (!isGamePlaying()) {
            return;
        }

        model.getSnake().move();
        view.render(model);
    }

    public boolean isGamePlaying() {
        return model.getCurrentState() == GameState.PLAYING;
    }

    public InputHandler getInputHandler() {
        return inputHandler;
    }
}
