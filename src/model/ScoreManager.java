package model;

public class ScoreManager {
    private int currentScore;
    private int highScore;

    // [UI-03] Các thuộc tính mở rộng cho tính năng Combo Streak.
    private int comboCount = 0;
    private long lastEatTime = 0;

    // DEV04 - UC4.3 Save High Score:
    // Cờ này cho GamePanel biết ván hiện tại có phá kỷ lục để hiển thị badge KỶ LỤC MỚI.
    private boolean newHighScoreAchieved = false;

    // DEV04 nâng cấp phần cá nhân:
    // Các chỉ số này dùng cho bảng tổng kết Game Over để bài implement có dữ liệu rõ hơn.
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
     * Reset điểm hiện tại và thống kê phiên chơi mới nhưng không xóa thành tích lâu dài.
     * highScore được nạp lại từ file để bảo đảm Restart không làm mất điểm cao.
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

    // Hàm mặc định cũ: Giữ lại để đảm bảo các tính năng cũ không bị lỗi biên dịch.
    public void addScore() {
        addScore(10);
    }

    // [UI-03] Nạp chồng hàm addScore để nhận số điểm linh hoạt.
    public void addScore(int points) {
        currentScore += points;
        lastPointsEarned = points;
        updateHighScoreIfNeeded();
    }

    /**
     * DEV04 - UC4.3 Save High Score:
     * So sánh currentScore với highScore và ghi highscore.txt khi người chơi phá kỷ lục.
     * Việc ghi file chạy ở background thread để không block UI/Game Loop.
     */
    private void updateHighScoreIfNeeded() {
        if (currentScore > highScore) {
            highScore = currentScore;
            newHighScoreAchieved = true;

            // DEV04 - Tối ưu lưu điểm cao:
            // Chốt snapshot trước khi ghi background để Restart ván mới không làm sai dữ liệu cần lưu.
            final int scoreToSave = highScore;
            HighScoreRepository.saveHighScoreAsync(scoreToSave);
        }
    }

    /**
     * DEV04 - UC4.3 Save High Score tại thời điểm Game Over:
     * GameController gọi hàm này trong handleGameOver() để chốt điểm cuối ván
     * theo đúng Sequence Diagram, kể cả khi điểm đã được cập nhật trong lúc ăn mồi.
     */
    public void finalizeHighScoreOnGameOver() {
        updateHighScoreIfNeeded();
    }

    /**
     * UC03 kết hợp DEV04:
     * Mỗi lần ăn mồi, ScoreManager cập nhật điểm và đồng thời ghi nhận thống kê
     * để màn hình Game Over của UC04 có thể hiển thị tổng kết ván chơi.
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

    public int getHighScoreDelta() {
        return Math.max(0, highScore - highScoreBeforeSession);
    }
}
