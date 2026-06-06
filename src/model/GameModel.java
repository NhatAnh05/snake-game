package model;

import java.util.List;

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

    // UI-01: Lưu độ khó mà người chơi chọn ở Main Menu.
    // Giá trị này được GameController dùng để tính tốc độ Timer khi bắt đầu game.
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

        // DEV04 - UC4.4 bước 5: điểm hiện tại được đưa về 0,
        // nhưng highScore vẫn được đọc lại từ highscore.txt trong ScoreManager.
        scoreManager.resetScore();
        this.gameOverReason = "";
        this.currentState = GameState.PLAYING;
    }

    public String getGameOverReason() {
        return gameOverReason;
    }

    // DEV04 - UC4.2: Chuẩn hóa lý do thua để View không bị rỗng/null khi render Game Over.
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
    
    public Snake getSnake() { return snake; }
    public Food getFood() { return food; }
    public ScoreManager getScoreManager() { return scoreManager; }

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
        this.currentMode = currentMode;
    }

    // UI-01: Trả về độ khó hiện tại để hiển thị ở Main Menu và Sidebar.
    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    // UI-01: Cập nhật độ khó khi người chơi chọn EASY / NORMAL / HARD ở Main Menu.
    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        if (difficultyLevel != null) {
            this.difficultyLevel = difficultyLevel;
        }
    }
}
