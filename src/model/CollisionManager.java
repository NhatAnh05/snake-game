package model;

import java.util.List;

public class CollisionManager {
    private final int boardCols;
    private final int boardRows;

    public CollisionManager(int boardCols, int boardRows) {
        this.boardCols = boardCols;
        this.boardRows = boardRows;
    }
    // 3.1 Kiểm tra va chạm giữa đầu rắn và vị trí của mồi trên bàn chơi
    public boolean checkFoodCollision(Point head, Point foodPos) {
        if (head == null || foodPos == null) return false;
        return head.equals(foodPos);
    }
    public boolean checkWallCollision(Point head) {
        if (head == null) return false;

        return head.x < 0 || head.y < 0
                || head.x >= boardCols
                || head.y >= boardRows;
    }

    public boolean checkSelfCollision(List<Point> body) {
        if (body == null || body.size() < 2) return false;

        Point head = body.get(0);
        for (int i = 1; i < body.size(); i++) {
            if (head.equals(body.get(i))) {
                return true;
            }
        }
        return false;
    }
    
    public boolean checkObstacleCollision(Point head, List<Point> walls) {
        if (head == null || walls == null) {
            return false;
        }

        return walls.contains(head);
    }

    public boolean checkCollision(Snake snake, List<Point> walls) {
        if (snake == null || snake.getBody() == null || snake.getBody().isEmpty()) {
            return false;
        }

        return checkWallCollision(snake.getHead())
                || checkSelfCollision(snake.getBody())
                || checkObstacleCollision(snake.getHead(), walls);
    }
}
