package controller;

import model.CollisionManager;
import model.DifficultyLevel;
import model.GameMode;
import model.Direction;
import model.GameModel;
import model.GameState;
import model.Point;
import model.Snake;
import view.GameUI;

import javax.swing.Timer;
import java.util.List;

public class GameController {
	private final GameModel model;
	private final GameUI view;
	private final Timer gameLoop;
	private final InputHandler inputHandler;
	private final CollisionManager collisionManager;
	private long lastPauseTime = 0;

	public GameController(GameModel model, GameUI view) {
		this.model = model;
		this.view = view;
		this.inputHandler = new InputHandler(this);

		this.collisionManager = new CollisionManager(40, 30);

		this.gameLoop = new Timer(getDelayByMode(), event -> updateGame());

		// =========================================================================
		// [UC05] - NHẬT ANH
		// PHẦN CHỈNH SỬA ĐỂ SỬA LỖI BÀN PHÍM VIE:
		// Ra lệnh cho Java vô hiệu hóa bộ gõ tiếng Việt hệ thống (IME) trên cửa sổ game.
		// Giúp ngăn chặn việc hệ điều hành tự ý chặn phím và sinh ra mã 229 (VK_PROCESSKEY).
		// =========================================================================
		this.view.enableInputMethods(false);
		this.view.getGamePanel().enableInputMethods(false);

		// Gắn bộ lắng nghe phím cho cả Frame chính và Panel để tránh mất tiêu điểm (Focus)
		this.view.addKeyListener(inputHandler);
		this.view.getGamePanel().addKeyListener(inputHandler);

		// Đồng bộ bộ xử lý phím vào GamePanel để giải quyết triệt để vấn đề Key Bindings chặn phím
		this.view.getGamePanel().setInputHandler(inputHandler);

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

			// ÉP FOCUS CỰC MẠNH: Buộc cả Frame và Panel phải tập trung đón bàn phím ngay khi vào trận
			// [UI-03] Chức năng thay đổi/Tối ưu điều hướng: Ép focus cửa sổ và Panel nhận diện phím điều khiển ngay khi chuyển trạng thái game
			view.requestFocusInWindow();
			view.getGamePanel().setFocusable(true);
			view.getGamePanel().requestFocusInWindow();
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

		// [UI-03] Cập nhật nâng cao: Kiểm tra và hủy trạng thái mồi đặc biệt nếu quá 7 giây không ăn
		model.getFood().updateExpiration();

		Snake snake = model.getSnake();
		Point foodPos = model.getFood().getPosition();

		Point nextHead = getNextHead(snake);
		boolean willEat = collisionManager.checkFoodCollision(nextHead, foodPos);

		// Di chuyển thực thể rắn
		snake.move();

		if (willEat) {
			snake.grow();

			// [UI-03] Kích hoạt bộ xử lý điểm nâng cao: Tính toán phân loại mồi kết hợp hệ số Combo chuỗi thời gian
			boolean isSpecial = model.getFood().isSpecial();
			model.getScoreManager().processEatEvent(isSpecial);

			// Tái tạo thực phẩm mới
			model.getFood().spawn(snake.getBody());

			while (model.getWall().contains(model.getFood().getPosition())) {
				model.getFood().spawn(snake.getBody());
			}

			// Đồng bộ hóa nhịp độ vận tốc cho vòng lặp game lập tức dựa trên điểm số mới
			gameLoop.setDelay(getDelayByMode());
		}

		List<Point> walls = model.getWall().getWalls();
		if (collisionManager.checkCollision(snake, walls)) {
			model.getScoreManager().resetCombo(); // Đứt chuỗi combo khi chết game
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
		long currentTime = System.currentTimeMillis();
		//  CHẶN ĐỨNG: Nếu 2 lần kích hoạt cách nhau dưới 200 mili-giây thì bỏ qua lần 2
		// [UI-03] Chức năng thay đổi/Điều khiển: Tích hợp biến chặn thời gian chống lỗi dính phím/nhấp nháy khi nhấn nút Pause quá nhanh
		if (currentTime - lastPauseTime < 200) {
			return;
		}
		lastPauseTime = currentTime;

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

	// UI-01: Tính tốc độ game dựa trên độ khó Easy / Normal / Hard.
	// Nếu ở chế độ Survival, tốc độ tiếp tục tăng dần theo điểm số.
	private int getDelayByMode() {
		DifficultyLevel difficulty = model.getDifficultyLevel();
		int baseDelay = difficulty != null ? difficulty.getDelay() : DifficultyLevel.NORMAL.getDelay();

		if (model.getCurrentMode() == GameMode.SURVIVAL) {
			int score = model.getScoreManager() != null
					? model.getScoreManager().getCurrentScore()
					: 0;

			int speedUp = (score / 3) * 8;
			return Math.max(45, baseDelay - speedUp);
		}

		return baseDelay;
	}
}