package model;

import java.util.ArrayList;
import java.util.List;

public class Snake {
    private final List<Point> body = new ArrayList<>();
    private Direction direction;

    public void reset(int x, int y) {
        body.clear();
        direction = Direction.RIGHT;

        body.add(new Point(x, y));
        body.add(new Point(x - 1, y));
        body.add(new Point(x - 2, y));
    }
    
    public Point getHead() {
        return body.isEmpty() ? null : body.get(0);
    }

    public List<Point> getBody() {
        return body;
    }

    public void move() { // Bỏ tham số grow
        if (body.isEmpty() || direction == null) return;

        Point head = body.get(0);
        Point newHead = new Point(head.x, head.y);

        switch (direction) {
            case UP -> newHead.y--;
            case DOWN -> newHead.y++;
            case LEFT -> newHead.x--;
            case RIGHT -> newHead.x++;
        }

        body.add(0, newHead);
        body.remove(body.size() - 1);
    }
    // 3.3 Bổ sung thêm một đốt mới vào cuối thân rắn sau khi ăn mồi
    public void grow() {
        if (body.isEmpty()) return;
        Point tail = body.get(body.size() - 1);
        body.add(new Point(tail.x, tail.y));
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction newDirection) {
        if (newDirection == null) {
            return;
        }

        if (isOppositeDirection(newDirection)) {
            return;
        }

        this.direction = newDirection;
    }

    public boolean isOppositeDirection(Direction newDirection) {
        if (direction == null || newDirection == null) {
            return false;
        }

        return (direction == Direction.UP && newDirection == Direction.DOWN)
                || (direction == Direction.DOWN && newDirection == Direction.UP)
                || (direction == Direction.LEFT && newDirection == Direction.RIGHT)
                || (direction == Direction.RIGHT && newDirection == Direction.LEFT);
    }
    
    
}
