package model;

public class GameModel {
    private Snake snake;
    private Food food;
    private ScoreManager scoreManager;
    private GameState currentState;
    private GameMode currentMode = GameMode.CLASSIC;
    private Wall wall;

    // DEV04 - UC4.2 End Game / ERD SU_KIEN_GAME_OVER:
    // Lưu nguyên nhân kết thúc ván để GamePanel hiển thị trên overlay Game Over.
    private String gameOverReason = "";

    // DEV04 - UC04 Premium Summary:
    // Lưu mốc thời gian phiên chơi để Game Over hiển thị "thời gian sống".
    private long sessionStartTimeMillis = 0L;
    private long sessionEndTimeMillis = 0L;

    // UI-01:
    // Lưu độ khó mà người chơi chọn ở Main Menu.
    private DifficultyLevel difficultyLevel = DifficultyLevel.NORMAL;

    public GameModel() {
        this.snake = new Snake();
        this.food = new Food();
        this.scoreManager = new ScoreManager();
        this.currentState = GameState.MENU;
        this.wall = new Wall();
    }

    /**
     * DEV04 - UC4.4 Restart Game:
     * Reset dữ liệu phiên chơi mới gồm rắn, vật cản, mồi, điểm hiện tại và lý do Game Over.
     * High score không bị reset vì thuộc dữ liệu thành tích lâu dài của UC4.3.
     */
    public void prepareNewGame() {
        snake.reset(20, 15);

        if (currentMode == GameMode.SURVIVAL) {
            wall.generateRandomWalls(
                    25,
                    40,
                    30,
                    snake.getBody()
            );
        } else {
            wall.clear();
        }

        food.spawn(snake.getBody());

        while (wall.contains(food.getPosition())) {
            food.spawn(snake.getBody());
        }

        // DEV04 - UC4.4:
        // Reset điểm hiện tại và thống kê phiên chơi, nhưng giữ high score lâu dài.
        scoreManager.resetScore();

        this.gameOverReason = "";
        this.sessionStartTimeMillis = System.currentTimeMillis();
        this.sessionEndTimeMillis = 0L;
        this.currentState = GameState.PLAYING;
    }

    /**
     * DEV04 - UC4.2 End Game:
     * Đánh dấu thời điểm Game Over và khóa lý do thua để View đọc lại khi render overlay.
     */
    public void markGameOver(String reason) {
        setGameOverReason(reason);
        this.sessionEndTimeMillis = System.currentTimeMillis();
        this.currentState = GameState.GAME_OVER;
    }

    /**
     * DEV04 - UC4.2:
     * Trả về số giây người chơi sống trong phiên chơi hiện tại.
     */
    public long getSessionDurationSeconds() {
        if (sessionStartTimeMillis <= 0L) {
            return 0L;
        }

        long end = sessionEndTimeMillis > 0L
                ? sessionEndTimeMillis
                : System.currentTimeMillis();

        return Math.max(0L, (end - sessionStartTimeMillis) / 1000L);
    }

    public String getGameOverReason() {
        return gameOverReason;
    }

    /**
     * DEV04 - UC4.2:
     * Chuẩn hóa lý do thua để View không bị null/rỗng khi vẽ Game Over.
     */
    public void setGameOverReason(String gameOverReason) {
        if (gameOverReason == null || gameOverReason.isBlank()) {
            this.gameOverReason = "Không xác định";
        } else {
            this.gameOverReason = gameOverReason;
        }
    }

    public Wall getWall() {
        return wall;
    }

    public Snake getSnake() {
        return snake;
    }

    public Food getFood() {
        return food;
    }

    public ScoreManager getScoreManager() {
        return scoreManager;
    }

    public GameState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(GameState targetState) {
        this.currentState = targetState;
    }

    public GameMode getCurrentMode() {
        return currentMode;
    }

    public void setCurrentMode(GameMode currentMode) {
        if (currentMode != null) {
            this.currentMode = currentMode;
        }
    }

    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        if (difficultyLevel != null) {
            this.difficultyLevel = difficultyLevel;
        }
    }
    //UI01
    public void initializeSecureNewGame() {
        this.prepareNewGame();

        if (this.snake != null) {
            this.snake.setDirection(Direction.RIGHT);
        }

        if (this.scoreManager != null) {
            this.scoreManager.resetCombo();
        }

        if (this.food != null && this.wall != null && this.snake != null) {
            int maxAttempts = 100;
            int attempts = 0;

            while (this.wall.contains(this.food.getPosition()) && attempts < maxAttempts) {
                this.food.spawn(this.snake.getBody());
                attempts++;
            }
        }
    }
}