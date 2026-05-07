package model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class HighScoreRepository {
    private static final String HIGH_SCORE_FILE = "highscore.txt";

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
        Path path = Path.of(HIGH_SCORE_FILE);

        try {
            Files.writeString(path, String.valueOf(highScore));
        } catch (IOException e) {
            System.out.println("Không thể lưu điểm cao: " + e.getMessage());
        }
    }
}