package model;

public class ScoreManager {
    private int currentScore;
    private int highScore;

    public ScoreManager() {
        this.currentScore = 0;
        this.highScore = HighScoreRepository.loadHighScore();
    }

    public void resetScore() {
        this.currentScore = 0;
        this.highScore = HighScoreRepository.loadHighScore();
    }

    // 3.2 Tăng điểm tích lũy sau khi rắn ăn mồi
    public void addScore() {
        currentScore += 10;

        if (currentScore > highScore) {
            highScore = currentScore;
            HighScoreRepository.saveHighScore(highScore);
        }
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public int getHighScore() {
        return highScore;
    }
}