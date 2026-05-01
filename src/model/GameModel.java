package model;

public class GameModel {
    private Snake snake;
    private Food food;
    private ScoreManager scoreManager;
    private GameState currentState; // Đổi tên biến thành currentState cho rõ nghĩa

    public GameModel() {
        // Khởi tạo các thành phần logic
        this.snake = new Snake();
        this.food = new Food();
        this.scoreManager = new ScoreManager();
        this.currentState = GameState.MENU; // Mặc định vào là ở màn hình Menu
    }

    // Hàm Reset để Người 1 thực hiện Start Game
    public void prepareNewGame() {
        snake.reset(15, 15);           // Đưa rắn về giữa màn hình (lưới 30x30)
        food.spawn(snake.getBody());   // Sinh mồi lần đầu (tránh thân rắn)
        scoreManager.resetScore();     // Đưa điểm về 0
        this.currentState = GameState.PLAYING; // Chuyển trạng thái sang đang chơi
    }

    // --- Getters ---
    public Snake getSnake() { return snake; }
    public Food getFood() { return food; }
    public ScoreManager getScoreManager() { return scoreManager; }

    // Sửa lại chỗ này: Trả về kiểu GameState thay vì int
    public GameState getCurrentState() {
        return currentState;
    }

    // --- Setters ---
    public void setCurrentState(GameState state) {
        this.currentState = state;
    }
}