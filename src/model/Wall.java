package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Wall {
    private final List<Point> walls = new ArrayList<>();
    private final Random random = new Random();

    public void clear() {
        walls.clear();
    }

    public List<Point> getWalls() {
        return walls;
    }

    public boolean contains(Point point) {
        return walls.contains(point);
    }

    public void generateRandomWalls(
            int count,
            int boardCols,
            int boardRows,
            List<Point> snakeBody
    ) {
        walls.clear();

        while (walls.size() < count) {
            int x = random.nextInt(boardCols);
            int y = random.nextInt(boardRows);

            Point wallPoint = new Point(x, y);

            boolean tooCloseToSpawn =
                    (x >= 16 && x <= 24) &&
                    (y >= 11 && y <= 19);

            if (tooCloseToSpawn) {
                continue;
            }

            if (snakeBody.contains(wallPoint)) {
                continue;
            }

            if (!walls.contains(wallPoint)) {
                walls.add(wallPoint);
            }
        }
    }
}