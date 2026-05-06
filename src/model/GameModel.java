package model;

public class GameModel {
    private Snake snake;
    private Food food;
    private ScoreManager scoreManager;
    private GameState currentState;
    
    public GameModel() {
        this.snake = new Snake();
        this.food = new Food();
        this.scoreManager = new ScoreManager();
        this.currentState = GameState.MENU;
    }


    public void prepareNewGame() {
        snake.reset(15, 15);
        food.spawn(snake.getBody());
        scoreManager.resetScore();
        this.currentState = GameState.PLAYING;
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
}
