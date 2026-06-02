package model;

import java.util.List;
import java.util.Random;

public class Food {
    private static final int BOARD_COLS = 40;
    private static final int BOARD_ROWS = 30;

    private final Random random = new Random();
    private Point position;
    // 3.5 Sinh vị trí ngẫu nhiên cho mồi mới và
    // 3.6 kiểm tra tọa độ không trùng với thân rắn
    public void spawn(List<Point> snakeBody) {
        int totalCells = BOARD_COLS * BOARD_ROWS;

        if (snakeBody != null && snakeBody.size() >= totalCells) {
            position = null;
            return;
        }

        Point newPosition;

        do {
            newPosition = new Point(
                    random.nextInt(BOARD_COLS),
                    random.nextInt(BOARD_ROWS)
            );
        } while (snakeBody != null && snakeBody.contains(newPosition));

        this.position = newPosition;
    }

    public Point getPosition() {
        return position;
    }
}