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
    public void addScore() {
        currentScore += 10;

        if (currentScore > highScore) {
            highScore = currentScore;

            // [Nâng cấp UC05] - Nhat Anh
            // Đẩy tiến trình ghi I/O ổ cứng sang luồng ngầm (Background Thread)
            // Giúp Game Loop (Timer) không bị chờ, chống hiện tượng khựng (lag) game.
            final int scoreToSave = highScore; // Tạo bản sao final để truyền vào Thread cho an toàn

            new Thread(() -> {
                HighScoreRepository.saveHighScore(scoreToSave);
            }).start();
        }
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public int getHighScore() {
        return highScore;
    }
}