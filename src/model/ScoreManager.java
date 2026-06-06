package model;

public class ScoreManager {
    private int currentScore;
    private int highScore;

    // UI-03:
    // Thuộc tính mở rộng cho Combo Streak.
    private int comboCount = 0;
    private long lastEatTime = 0;

    // DEV04 - UC4.3 Save High Score:
    // Cho GamePanel biết ván hiện tại có phá kỷ lục không.
    private boolean newHighScoreAchieved = false;

    // DEV04 - UC04 Premium Summary:
    // Thống kê phiên chơi dùng để hiển thị ở màn hình Game Over.
    private int foodEaten = 0;
    private int specialFoodEaten = 0;
    private int maxComboCount = 0;
    private int lastPointsEarned = 0;
    private int highScoreBeforeSession = 0;

    public ScoreManager() {
        this.currentScore = 0;
        this.highScore = HighScoreRepository.loadHighScore();
        this.highScoreBeforeSession = this.highScore;
    }

    /**
     * DEV04 - UC4.4 Restart Game:
     * Reset điểm hiện tại và thống kê phiên chơi mới.
     * Không xóa high score vì high score là dữ liệu thành tích lâu dài của UC4.3.
     */
    public void resetScore() {
        this.currentScore = 0;
        this.highScore = HighScoreRepository.loadHighScore();
        this.highScoreBeforeSession = this.highScore;

        this.comboCount = 0;
        this.lastEatTime = 0;
        this.newHighScoreAchieved = false;

        this.foodEaten = 0;
        this.specialFoodEaten = 0;
        this.maxComboCount = 0;
        this.lastPointsEarned = 0;
    }

    /**
     * Hàm mặc định cũ:
     * Giữ lại để các phần code cũ gọi addScore() không bị lỗi.
     */
    public void addScore() {
        addScore(10);
    }

    /**
     * UI-03 + DEV04:
     * Cộng điểm linh hoạt và kiểm tra high score.
     */
    public void addScore(int points) {
        currentScore += points;
        lastPointsEarned = points;
        updateHighScoreIfNeeded();
    }

    /**
     * DEV04 - UC4.3 Save High Score:
     * So sánh currentScore với highScore.
     * Nếu điểm hiện tại cao hơn điểm cũ thì cập nhật highScore và lưu xuống highscore.txt.
     */
    private void updateHighScoreIfNeeded() {
        if (currentScore > highScore) {
            highScore = currentScore;
            newHighScoreAchieved = true;

            // DEV04 - Tối ưu lưu điểm cao:
            // Chốt snapshot trước khi ghi file nền để Restart không làm sai dữ liệu cần lưu.
            final int scoreToSave = highScore;

            new Thread(() -> {
                HighScoreRepository.saveHighScore(scoreToSave);
            }, "HighScore-Save-Thread").start();
        }
    }

    /**
     * DEV04 - UC4.3 Save High Score:
     * GameController gọi hàm này tại thời điểm Game Over để chốt điểm cuối ván
     * đúng với Sequence Diagram.
     */
    public void finalizeHighScoreOnGameOver() {
        updateHighScoreIfNeeded();
    }

    /**
     * UC03 kết hợp DEV04:
     * Khi rắn ăn mồi, hệ thống vừa cộng điểm vừa ghi nhận thống kê
     * để màn hình Game Over có bảng tổng kết ván chơi.
     */
    public void processEatEvent(boolean isSpecialFood) {
        long currentTime = System.currentTimeMillis();
        int basePoints = isSpecialFood ? 30 : 10;

        if (currentTime - lastEatTime <= 5000 && lastEatTime != 0) {
            comboCount++;
        } else {
            comboCount = 1;
        }

        lastEatTime = currentTime;

        maxComboCount = Math.max(maxComboCount, comboCount);

        foodEaten++;

        if (isSpecialFood) {
            specialFoodEaten++;
        }

        double multiplier = 1.0;

        if (comboCount >= 4) {
            multiplier = 2.0;
        } else if (comboCount >= 2) {
            multiplier = 1.5;
        }

        int finalPoints = (int) (basePoints * multiplier);
        lastPointsEarned = finalPoints;
        currentScore += finalPoints;

        updateHighScoreIfNeeded();
    }

    /**
     * DEV04:
     * Khi Game Over thì combo bị ngắt.
     */
    public void resetCombo() {
        this.comboCount = 0;
        this.lastEatTime = 0;
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public int getHighScore() {
        return highScore;
    }

    public boolean isNewHighScoreAchieved() {
        return newHighScoreAchieved;
    }

    public int getComboCount() {
        return comboCount;
    }

    public int getFoodEaten() {
        return foodEaten;
    }

    public int getSpecialFoodEaten() {
        return specialFoodEaten;
    }

    public int getMaxComboCount() {
        return maxComboCount;
    }

    public int getLastPointsEarned() {
        return lastPointsEarned;
    }

    public int getHighScoreBeforeSession() {
        return highScoreBeforeSession;
    }

    /**
     * DEV04 - UC4.3:
     * Trả về số điểm vượt kỷ lục trong phiên chơi hiện tại.
     * Ví dụ: highScore cũ = 100, highScore mới = 130 => delta = 30.
     */
    public int getHighScoreDelta() {
        return Math.max(0, highScore - highScoreBeforeSession);
    }
}