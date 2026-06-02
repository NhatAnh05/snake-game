package model;

import java.util.List;

public class GameModel {
    private Snake snake;
    private Food food;
    private ScoreManager scoreManager;
    private GameState currentState;
    private GameMode currentMode = GameMode.CLASSIC;
    private Wall wall;

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

        scoreManager.resetScore();
        this.currentState = GameState.PLAYING;
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
