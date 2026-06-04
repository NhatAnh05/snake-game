package model;

public class ScoreManager {
    private int currentScore;
    private int highScore;
    // [UI-03] Các thuộc tính mở rộng cho tính năng Combo Streak
    private int comboCount = 0;
    private long lastEatTime = 0;

    public ScoreManager() {
        this.currentScore = 0;
        this.highScore = HighScoreRepository.loadHighScore();
    }

    public void resetScore() {
        this.currentScore = 0;
        this.highScore = HighScoreRepository.loadHighScore();
    }

    // Hàm mặc định cũ: Giữ lại để đảm bảo các tính năng cũ không bị lỗi biên dịch
    public void addScore() {
        addScore(10); // Tự động gọi hàm có tham số bên dưới với giá trị mặc định là 10
    }

    // [UI-03] NÂNG CẤP: Nạp chồng hàm addScore để nhận số điểm linh hoạt (10 hoặc 30)
    public void addScore(int points) {
        currentScore += points;

        // Cơ chế check kỷ lục và ghi file ngầm (Background Thread) giữ nguyên cực kỳ mượt mà
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
    public void processEatEvent(boolean isSpecialFood) {
        long currentTime = System.currentTimeMillis();
        int basePoints = isSpecialFood ? 30 : 10;

        // Kiểm tra xem lần ăn này có cách lần ăn trước dưới 5 giây (5000ms) không
        if (currentTime - lastEatTime <= 5000 && lastEatTime != 0) {
            comboCount++; // Tăng chuỗi combo
        } else {
            comboCount = 1; // Quá thời gian hoặc lần đầu ăn, thiết lập lại combo về 1
        }

        lastEatTime = currentTime; // Cập nhật mốc thời gian ăn mới nhất

        // Tính toán hệ số nhân điểm dựa trên số chuỗi ăn được
        double multiplier = 1.0;
        if (comboCount >= 4) {
            multiplier = 2.0; // Combo từ 4 phát trở lên: x2.0 điểm
        } else if (comboCount >= 2) {
            multiplier = 1.5; // Combo từ 2 đến 3 phát: x1.5 điểm
        }

        // Tính tổng điểm nhận được sau khi nhân hệ số
        int finalPoints = (int) (basePoints * multiplier);
        currentScore += finalPoints;

        if (currentScore > highScore) {
            highScore = currentScore;
            // HighScoreRepository.saveHighScore(highScore); // Lưu vật lý nếu có
        }
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
}