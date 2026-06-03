package model;

import java.util.List;
import java.util.Random;

public class Food {
    private static final int BOARD_COLS = 40;
    private static final int BOARD_ROWS = 30;

    // [UI-03] Biến cờ lưu trạng thái: true nếu là mồi đặc biệt, false nếu là mồi thường
    private boolean isSpecial;

    // Đối tượng sinh số ngẫu nhiên dùng để chọn tọa độ và tỷ lệ xuất hiện mồi
    private final Random random = new Random();

    // Lưu tọa độ hiện tại của viên mồi trên bản đồ
    private Point position;

    /**
     * Chức năng: 3.5 Sinh vị trí ngẫu nhiên cho mồi mới và
     * 3.6 Kiểm tra tọa độ đảm bảo không trùng với thân rắn hoặc vật cản.
     * * @param snakeBody Danh sách các tọa độ hiện tại thuộc phần thân của con rắn.
     */
    public void spawn(List<Point> snakeBody) {
        // Tính tổng số ô có trên toàn bộ bản đồ game
        int totalCells = BOARD_COLS * BOARD_ROWS;

        // Trường hợp đặc biệt: Nếu chiều dài rắn đã chiếm hết toàn bộ bản đồ (Thắng game)
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

        // [UI-03] Thay đổi/Cập nhật tính năng: Xác suất ngẫu nhiên 20% sinh ra mồi đặc biệt (Special Food)
        // random.nextInt(100) trả về giá trị từ 0 đến 99. Nếu nhỏ hơn 20 (0 -> 19) thì gán true.
        this.isSpecial = random.nextInt(100) < 20;
    }

    /**
     * Lấy ra tọa độ vị trí hiện tại của viên mồi.
     * @return Đối tượng Point chứa tọa độ (x, y)
     */
    public Point getPosition() {
        return position;
    }

    /**
     * [UI-03] Chức năng kiểm tra loại mồi: Cung cấp trạng thái mồi để tầng View (Giao diện)
     * nhận biết và áp dụng các hiệu ứng đồ họa nhấp nháy/vàng hoàng gia đặc biệt.
     * * @return true nếu là mồi đặc biệt, false nếu là mồi thường.
     */
    public boolean isSpecial() {
        return isSpecial;
    }
}