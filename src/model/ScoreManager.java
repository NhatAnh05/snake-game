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

    // Hàm tăng điểm khi ăn mồi
    public void increaseScore() {
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