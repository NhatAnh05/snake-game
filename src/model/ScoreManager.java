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

    public int getCurrentScore() {
        return currentScore;
    }

    public int getHighScore() {
        return highScore;
    }
}