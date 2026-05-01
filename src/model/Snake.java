package model;

import java.util.ArrayList;
import java.util.List;

public class Snake {
    private List<Point> body = new ArrayList<>();
    // Trong sơ đồ bạn để Direction là một thuộc tính
    private String direction;

    public void reset(int x, int y) {
        body.clear();
        direction = "RIGHT";
        body.add(new Point(x, y));
        body.add(new Point(x - 1, y));
        body.add(new Point(x - 2, y));
    }

    public List<Point> getBody() { return body; }
    public void move() {
        // 1. Lấy tọa độ cái đầu hiện tại
        Point head = body.get(0);
        Point newHead = new Point(head.x, head.y);

        // 2. Tính toán tọa độ đầu mới dựa vào hướng đi
        switch (direction) {
            case "UP":    newHead.y--; break;
            case "DOWN":  newHead.y++; break;
            case "LEFT":  newHead.x--; break;
            case "RIGHT": newHead.x++; break;
        }

        // 3. Gắn đầu mới vào đầu danh sách (Rắn dài ra 1 ô phía trước)
        body.add(0, newHead);

        // 4. Cắt cái đuôi cuối cùng đi (Để độ dài rắn không đổi)
        body.remove(body.size() - 1);
    }

    // Getter và Setter cho hướng đi
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }


}