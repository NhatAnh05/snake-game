package model;

import java.util.ArrayList;
import java.util.List;

public class Snake {
    private final List<Point> body = new ArrayList<>();
    private Direction direction;

    public void reset(int x, int y) {
        body.clear();

        // [DEV02 - UC02] - LÊ TUẤN ANH
        // Khi bắt đầu ván mới hoặc restart, hướng mặc định của rắn được đặt về RIGHT.
        // Việc xác định hướng ban đầu giúp GameController/InputHandler có cơ sở
        // để kiểm tra hướng mới và chặn quay ngược trực tiếp 180 độ.
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

    // UC02-MF-07: Cập nhật hướng di chuyển mới cho rắn khi hướng hợp lệ.
    // [DEV02 - UC02] - LÊ TUẤN ANH
    // Phương thức này là điểm cuối của luồng Control Snake:
    // InputHandler nhận phím -> GameController kiểm tra state/hướng -> Snake cập nhật direction.
    // Snake vẫn tự bảo vệ bằng cách kiểm tra null và chặn hướng đối lập,
    // giúp không bị lỗi nếu lớp khác gọi trực tiếp setDirection().
    public void setDirection(Direction newDirection) {
        if (newDirection == null) {
            return;
        }

        // UC02-AF03: Không cho rắn quay ngược trực tiếp 180 độ.
        // [DEV02 - UC02] - LÊ TUẤN ANH
        // Giữ lại lớp kiểm tra an toàn tại Model để bảo đảm quy tắc Use Case:
        // RIGHT không được đổi trực tiếp sang LEFT, UP không được đổi trực tiếp sang DOWN.
        if (isOppositeDirection(newDirection)) {
            return;
        }

        this.direction = newDirection;
    }

    // [DEV02 - UC02] - LÊ TUẤN ANH
    // Kiểm tra hướng mới có ngược trực tiếp 180 độ với hướng hiện tại hay không.
    // Phương thức này được dùng trong luồng Alternative Flow của UC02
    // và hỗ trợ GameController.validateDirection(newDirection).
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
