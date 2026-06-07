package model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HighScoreRepository {
    private static final String HIGH_SCORE_FILE = "highscore.txt";

    // DEV04 - UC4.3 Save High Score:
    // Dùng single-thread executor để ghi điểm cao tuần tự, tránh tạo nhiều Thread khi Restart liên tục.
    private static final ExecutorService HIGH_SCORE_EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "HighScore-Save-Worker");
        thread.setDaemon(true);
        return thread;
    });

    public static int loadHighScore() {
        Path path = Path.of(HIGH_SCORE_FILE);

        if (!Files.exists(path)) {
            return 0;
        }

        try {
            String content = Files.readString(path).trim();

            if (content.isEmpty()) {
                return 0;
            }

            return Integer.parseInt(content);
        } catch (IOException | NumberFormatException e) {
            return 0;
        }
    }

    public static void saveHighScore(int highScore) {
        writeHighScore(highScore);
    }

    // DEV04 - UC4.3 Save High Score:
    // Ghi file ở background để Game Over overlay hiển thị ngay, không bị khựng UI.
    public static void saveHighScoreAsync(int highScore) {
        int scoreSnapshot = Math.max(0, highScore);
        HIGH_SCORE_EXECUTOR.submit(() -> writeHighScore(scoreSnapshot));
    }

    private static void writeHighScore(int highScore) {
        Path path = Path.of(HIGH_SCORE_FILE);

        try {
            Files.writeString(path, String.valueOf(Math.max(0, highScore)));
        } catch (IOException e) {
            System.out.println("Không thể lưu điểm cao: " + e.getMessage());
        }
    }

    public static void shutdownAsyncSaver() {
        HIGH_SCORE_EXECUTOR.shutdown();
    }
}
