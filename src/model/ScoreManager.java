package model;

public class ScoreManager {
    private int currentScore;
    private int highScore;

    public void resetScore() {
        this.currentScore = 0;
        this.highScore = 0;
    }

    // Hàm tăng điểm khi ăn mồi
    public void increaseScore() {
        currentScore += 10; // Mỗi lần ăn tăng 10 điểm
        if (currentScore > highScore) {
            highScore = currentScore;
        }
    }

    public int getCurrentScore() { return currentScore; }
    public int getHighScore() { return highScore; }
}
