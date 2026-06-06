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
    // DEV04 - UC4.1 Check Collision:
    // Kiểm tra đầu rắn có vượt ra khỏi biên trái/phải/trên/dưới của bàn chơi hay không.
    public boolean checkWallCollision(Point head) {
        if (head == null) return false;

        return head.x < 0 || head.y < 0
                || head.x >= boardCols
                || head.y >= boardRows;
    }

    // DEV04 - UC4.1 Check Collision:
    // Kiểm tra đầu rắn có trùng với một phần thân rắn hay không.
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
    
    // DEV04 mở rộng UC4.1:
    // Bổ sung kiểm tra vật cản để lý do Game Over hiển thị rõ hơn khi chơi Survival.
    public boolean checkObstacleCollision(Point head, List<Point> walls) {
        if (head == null || walls == null) {
            return false;
        }

        return walls.contains(head);
    }

    // DEV04 - UC4.1 Main Flow bước 3-5:
    // Tổng hợp kết quả va chạm tường, thân và vật cản để GameController quyết định UC4.2 End Game.
    public boolean checkCollision(Snake snake, List<Point> walls) {
        if (snake == null || snake.getBody() == null || snake.getBody().isEmpty()) {
            return false;
        }

        return checkWallCollision(snake.getHead())
                || checkSelfCollision(snake.getBody())
                || checkObstacleCollision(snake.getHead(), walls);
    }
}
