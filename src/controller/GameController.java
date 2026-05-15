package controller;

import model.CollisionManager;

import model.GameMode;
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

		this.gameLoop = new Timer(getDelayByMode(), event -> updateGame());

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
		if (model.getCurrentState() == GameState.MENU || model.getCurrentState() == GameState.GAME_OVER) {

			model.prepareNewGame();
			gameLoop.setDelay(getDelayByMode()); 
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

		Snake snake = model.getSnake();
		Point foodPos = model.getFood().getPosition();

		/*
		 * Kiểm tra ô tiếp theo của đầu rắn. Nếu ô tiếp theo trùng với food thì rắn sẽ
		 * ăn mồi.
		 */
		Point nextHead = getNextHead(snake);
        boolean willEat = collisionManager.checkFoodCollision(nextHead, foodPos);

		// Di chuyển rắn
		snake.move();

		// Nếu ăn mồi thì tăng điểm và sinh food mới
		if (willEat) {
            snake.grow();
			model.getScoreManager().addScore();
			model.getFood().spawn(snake.getBody());
		}

		// Kiểm tra va chạm sau khi rắn đã di chuyển
		if (collisionManager.checkCollision(snake)) {
			handleGameOver();
			return;
		}

		view.render(model);
	}

	private Point getNextHead(Snake snake) {
		Point head = snake.getBody().get(0);
		Point nextHead = new Point(head.x, head.y);

		Direction direction = snake.getDirection();

		switch (direction) {
		case UP -> nextHead.y--;
		case DOWN -> nextHead.y++;
		case LEFT -> nextHead.x--;
		case RIGHT -> nextHead.x++;
		}

		return nextHead;
	}

	private void handleGameOver() {
		gameLoop.stop();
		model.setCurrentState(GameState.GAME_OVER);
		view.render(model);
	}

	public void pauseGame() {
		if (model.getCurrentState() == GameState.PLAYING) {
			model.setCurrentState(GameState.PAUSED);
			gameLoop.stop();
			view.render(model);
			view.requestFocusInWindow();
		}
	}

	public void resumeGame() {
		if (model.getCurrentState() == GameState.PAUSED) {
			model.setCurrentState(GameState.PLAYING);
			gameLoop.setDelay(getDelayByMode()); 
			gameLoop.start();
			view.render(model);
			view.requestFocusInWindow();
		}
	}

	public void togglePause() {
		if (model.getCurrentState() == GameState.PLAYING) {
			pauseGame();
		} else if (model.getCurrentState() == GameState.PAUSED) {
			resumeGame();
		}
	}

	public boolean isGamePlaying() {
		return model.getCurrentState() == GameState.PLAYING;
	}

	public boolean isGamePaused() {
		return model.getCurrentState() == GameState.PAUSED;
	}

	public InputHandler getInputHandler() {
		return inputHandler;
	}

	public void backToMenu() {
		if (model.getCurrentState() == GameState.GAME_OVER || model.getCurrentState() == GameState.PAUSED) {

			gameLoop.stop();
			model.setCurrentState(GameState.MENU);
			view.render(model);
			view.requestFocusInWindow();
		}
	}

	private int getDelayByMode() {
	    if (model.getCurrentMode() == GameMode.SURVIVAL) {

	        int score = model.getScoreManager() != null
	                ? model.getScoreManager().getCurrentScore()
	                : 0;

	        // tăng tốc rõ ràng hơn
	        int base = 140;
	        int speedUp = (score / 3) * 8;

	        return Math.max(55, base - speedUp);
	    }

	    return 140;
	}
}
