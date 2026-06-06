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

	// DEV04 - UC4.4 Restart Game:
	// Lưu thời điểm lần bắt đầu/chơi lại gần nhất để chống dội phím ENTER/R.
	// Khớp với Chương 11: chỉ cho phép một thao tác Restart hợp lệ trong một khoảng ngắn.
	private long lastStartRestartTime = 0;

	private int countdownValue = 3;
	private Timer countdownTimer;

	// DEV04 nâng cấp phần cá nhân:
	// Timer riêng chỉ dùng để repaint hiệu ứng pulse/fade của overlay Game Over.
	private Timer gameOverUiTimer;

	// =========================================================================
	// [DEV02 - UC02] - LÊ TUẤN ANH
	// PHẦN CẢI TIẾN CONTROL SNAKE:
	// Lưu hướng điều khiển hợp lệ đang chờ áp dụng trong game tick tiếp theo.
	// Mục tiêu:
	// 1. Không cập nhật hướng rắn trực tiếp ngay tại sự kiện bàn phím.
	// 2. Chỉ áp dụng hướng khi game loop chạy, giúp đồng bộ với vòng lặp game.
	// 3. Chống lỗi người chơi nhấn nhiều phím quá nhanh trong cùng một tick.
	// 4. Không ảnh hưởng các phần DEV04 Restart/Game Over và UC05 Pause/Resume đã có.
	// =========================================================================
	private Direction pendingDirection;

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
	/**
	 * DEV04 - UC4.4 Restart Game / Sequence Diagram - Luồng chơi lại:
	 * 1. Player nhấn ENTER/R hoặc click nút CHƠI LẠI tại màn hình Game Over.
	 * 2. View/InputHandler chuyển yêu cầu sang GameController.
	 * 3. Controller kiểm tra trạng thái MENU/GAME_OVER trước khi khởi tạo ván mới.
	 * 4. Model reset rắn, mồi, điểm hiện tại và chuyển về PLAYING.
	 */
	public void handleStartOrRestartRequest() {
		long currentTime = System.currentTimeMillis();
		// DEV04 - Chống dội phím Restart:
		// Nếu KeyListener và Key Bindings cùng bắt ENTER/R thì chỉ xử lý lần đầu.
		if (currentTime - lastStartRestartTime < 250) {
			return;
		}
		lastStartRestartTime = currentTime;

		if (model.getCurrentState() == GameState.MENU || model.getCurrentState() == GameState.GAME_OVER) {
			// DEV04 - UC4.4 bước 4: dừng các Timer cũ trước khi prepareNewGame()
			// để tránh ván mới bị tick chồng từ ván Game Over trước đó.
			gameLoop.stop();
			if (countdownTimer != null && countdownTimer.isRunning()) {
				countdownTimer.stop();
			}
			stopGameOverUiAnimation();

			if (model.getCurrentState() == GameState.MENU) {
				model.initializeSecureNewGame();
			} else {
				model.prepareNewGame();
			}
			model.setCurrentState(GameState.PLAYING);

			// [DEV02 - UC02] - LÊ TUẤN ANH:
			// Reset hướng chờ khi bắt đầu/chơi lại để ván mới không bị ảnh hưởng
			// bởi input còn sót từ ván trước hoặc từ màn hình Game Over.
			pendingDirection = null;

			view.render(model);

			// ÉP FOCUS CỰC MẠNH: Buộc cả Frame và Panel phải tập trung đón bàn phím ngay khi vào trận
			// [UI-03] Chức năng thay đổi/Tối ưu điều hướng: Ép focus cửa sổ và Panel nhận diện phím điều khiển ngay khi chuyển trạng thái game
			view.requestFocusInWindow();
			view.getGamePanel().setFocusable(true);
			view.getGamePanel().requestFocusInWindow();

			startCountdown();
		}
	}
	private void startCountdown() {
		countdownValue = 3;
		if (countdownTimer != null && countdownTimer.isRunning()) {
			countdownTimer.stop();
		}


		countdownTimer = new Timer(1000, event -> {
			countdownValue--;
			if (countdownValue < 0) {
				countdownTimer.stop();

				gameLoop.setDelay(getDelayByMode());
				gameLoop.start();
			}
			view.render(model);
		});
		countdownTimer.start();
		view.render(model);
	}

	public int getCountdownValue() {
		return countdownValue;
	}

	public boolean isCountingDown() {
		return countdownTimer != null && countdownTimer.isRunning() && countdownValue >= 0;
	}
	public void requestChangeDirection(Direction newDirection) {
		// =========================================================================
		// [DEV02 - UC02] - LÊ TUẤN ANH
		// PHẦN CẢI TIẾN CONTROL SNAKE:
		// InputHandler chỉ gửi Direction hợp lệ vào đây.
		// GameController tiếp tục giữ trách nhiệm kiểm tra trạng thái game và hướng mới.
		// =========================================================================

		if (!isGamePlaying()) {
			// [DEV02 - UC02] - LÊ TUẤN ANH:
			// Không cho phép đổi hướng khi game đang MENU, PAUSED, GAME_OVER
			// hoặc đang countdown trước khi bắt đầu di chuyển.
			return;
		}

		if (pendingDirection != null) {
			// [DEV02 - UC02] - LÊ TUẤN ANH:
			// Nếu trong cùng một game tick đã có một hướng đang chờ xử lý,
			// bỏ qua input tiếp theo để tránh lỗi double-input làm rắn quay ngược 180 độ.
			return;
		}

		if (validateDirection(newDirection)) {
			// [DEV02 - UC02] - LÊ TUẤN ANH:
			// Không gọi Snake.setDirection() trực tiếp tại sự kiện bàn phím.
			// Hướng hợp lệ được lưu lại và sẽ áp dụng ở đầu vòng lặp updateGame().
			pendingDirection = newDirection;
		}
	}

	public boolean validateDirection(Direction newDirection) {
		if (newDirection == null || model.getSnake() == null) {
			return false;
		}

		// [DEV02 - UC02] - LÊ TUẤN ANH:
		// Ủy quyền cho Snake kiểm tra hướng đối lập.
		// Quy tắc: RIGHT không được đổi trực tiếp sang LEFT, UP không được đổi trực tiếp sang DOWN.
		return !model.getSnake().isOppositeDirection(newDirection);
	}

	private void applyPendingDirection() {
		// [DEV02 - UC02] - LÊ TUẤN ANH:
		// Áp dụng hướng chờ đúng thời điểm game tick.
		// Điều này đồng bộ input bàn phím với vòng lặp game và giúp chuyển động ổn định hơn.
		if (pendingDirection == null || model.getSnake() == null) {
			return;
		}

		if (validateDirection(pendingDirection)) {
			model.getSnake().setDirection(pendingDirection);
		}

		pendingDirection = null;
	}

	private void updateGame() {
		if (!isGamePlaying()) {
			return;
		}

		// [DEV02 - UC02] - LÊ TUẤN ANH:
		// Áp dụng hướng điều khiển hợp lệ trước khi tính vị trí đầu rắn tiếp theo.
		applyPendingDirection();

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

		// DEV04 - UC4.1 Check Collision:
		// Sau khi Snake di chuyển trong mỗi game tick, Controller lấy danh sách vật cản
		// và gọi CollisionManager để kiểm tra va chạm tường, thân rắn hoặc obstacle.
		List<Point> walls = model.getWall().getWalls();
		if (collisionManager.checkCollision(snake, walls)) {
			model.getScoreManager().resetCombo(); // Đứt chuỗi combo khi chết game
			handleGameOver(resolveGameOverReason(snake, walls));
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

	/**
	 * DEV04 - UC4.1 + ERD GameOverEvent:
	 * Xác định nguyên nhân Game Over để View hiển thị rõ ràng
	 * và mô phỏng thuộc tính collision_type trong sự kiện Game Over.
	 */
	private String resolveGameOverReason(Snake snake, List<Point> walls) {
		if (snake == null || snake.getHead() == null) {
			return "Rắn không còn vị trí hợp lệ";
		}

		Point head = snake.getHead();
		if (collisionManager.checkWallCollision(head)) {
			return "Đâm vào biên bản đồ";
		}
		if (collisionManager.checkObstacleCollision(head, walls)) {
			return "Đâm vào vật cản";
		}
		if (collisionManager.checkSelfCollision(snake.getBody())) {
			return "Cắn vào thân rắn";
		}
		return "Va chạm không xác định";
	}

	/**
	 * DEV04 - UC4.2 End Game + UC4.3 Save High Score:
	 * - Dừng game loop/countdown để rắn không tiếp tục di chuyển.
	 * - Lưu lý do thua vào GameModel.
	 * - Chốt/cập nhật high score trước khi render overlay Game Over.
	 * - Chuyển state sang GAME_OVER đúng như Sequence Diagram.
	 */
	private void handleGameOver(String reason) {
		gameLoop.stop();
		if (countdownTimer != null && countdownTimer.isRunning()) {
			countdownTimer.stop();
		}
		countdownValue = -1;

		// [DEV02 - UC02] - LÊ TUẤN ANH:
		// Xóa hướng chờ khi game over để người chơi không thể tiếp tục đổi hướng sau khi đã thua.
		pendingDirection = null;

		// DEV04 - UC4.2 End Game:
		// Ghi nhận lý do thua + thời điểm kết thúc để overlay Game Over hiển thị tổng kết phiên chơi.
		model.markGameOver(reason);

		// DEV04 - UC4.3 Save High Score:
		// Chốt điểm cuối ván và bảo đảm highscore.txt được cập nhật nếu phá kỷ lục.
		model.getScoreManager().finalizeHighScoreOnGameOver();

		view.render(model);
		startGameOverUiAnimation();
	}

	/**
	 * DEV04 nâng cấp phần cá nhân - Game Over Overlay Animation:
	 * Khi game đã GAME_OVER, logic game dừng nhưng View vẫn được repaint nhẹ để tạo hiệu ứng pulse.
	 * Timer này không cập nhật vị trí rắn nên không ảnh hưởng UC02/UC03.
	 */
	private void startGameOverUiAnimation() {
		stopGameOverUiAnimation();
		gameOverUiTimer = new Timer(80, event -> {
			if (model.getCurrentState() == GameState.GAME_OVER) {
				view.render(model);
			} else {
				stopGameOverUiAnimation();
			}
		});
		gameOverUiTimer.start();
	}

	private void stopGameOverUiAnimation() {
		if (gameOverUiTimer != null && gameOverUiTimer.isRunning()) {
			gameOverUiTimer.stop();
		}
	}

	public void pauseGame() {
		if (model.getCurrentState() == GameState.PLAYING) {
			// [DEV02 - UC02] - LÊ TUẤN ANH:
			// Khi tạm dừng game, xóa hướng đang chờ để tránh áp dụng input cũ lúc resume.
			pendingDirection = null;

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
		return model.getCurrentState() == GameState.PLAYING && countdownValue < 0;
	}
	public boolean isGamePaused() {
		return model.getCurrentState() == GameState.PAUSED;
	}

	public InputHandler getInputHandler() {
		return inputHandler;
	}

	/**
	 * DEV04 - Alternative Flow UC4.4-AF1:
	 * Khi người chơi không chọn chơi lại mà chọn ESC/Menu, hệ thống giữ an toàn trạng thái,
	 * dừng Timer hiện tại và đưa giao diện về Main Menu.
	 */
	public void backToMenu() {
		if (model.getCurrentState() == GameState.GAME_OVER || model.getCurrentState() == GameState.PAUSED) {
			gameLoop.stop();
			if (countdownTimer != null) countdownTimer.stop();
			stopGameOverUiAnimation();

			// [DEV02 - UC02] - LÊ TUẤN ANH:
			// Xóa hướng chờ khi quay về menu để input điều hướng không còn tác dụng ngoài trạng thái PLAYING.
			pendingDirection = null;

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