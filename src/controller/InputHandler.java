package controller;

import model.Direction;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class InputHandler extends KeyAdapter {
    private final GameController controller;
    private int currentKeyCode;

    public InputHandler(GameController controller) {
        this.controller = controller;
    }

    @Override
    public void keyPressed(KeyEvent event) {
        // Lấy mã số phím (KeyCode) và ký tự chữ thực tế (KeyChar - chuyển về chữ thường)
        currentKeyCode = event.getKeyCode();
        char keyChar = Character.toLowerCase(event.getKeyChar());

        System.out.println("DEBUG: KeyCode = " + currentKeyCode + " | KeyChar = " + keyChar);

        // =========================================================================
        // [UC05] - NHẬT ANH
        // PHẦN CHỈNH SỬA ĐỂ SỬA LỖI BÀN PHÍM VIE:
        // Đoạn code cũ "if (!isValidKey(currentKeyCode)) return;" đã được LOẠI BỎ hoàn toàn!
        // Lý do: Khi bật bộ gõ VIE, hệ thống trả về mã ẩn 229 (VK_PROCESSKEY) hoặc mã 0,
        // khiến hàm isValidKey() cũ coi là phím lỗi và chặn đứng sự kiện ngay lập tức.
        // =========================================================================

        // DEV04 - UC4.4 Restart Game:
        // ENTER là thao tác Player chọn Chơi lại theo Main Flow bước 2.
        // Controller sẽ tự kiểm tra state MENU/GAME_OVER trước khi reset ván chơi.
        if (currentKeyCode == KeyEvent.VK_ENTER) {
            controller.handleStartOrRestartRequest();
            return;
        }

        // DEV04 - UC4.4 Restart Game:
        // R là phím tắt bổ sung để Restart nhanh trên màn hình GAME_OVER.
        if (currentKeyCode == KeyEvent.VK_R || keyChar == 'r') {
            controller.handleStartOrRestartRequest();
            return;
        }

        // 3. P: Tạm dừng hoặc tiếp tục game.
        // 🌟 PHẦN CHỈNH SỬA: Chấp nhận cả mã phím VK_P HOẶC ký tự 'p' thực tế phát ra từ Unikey.
        if (currentKeyCode == KeyEvent.VK_P || keyChar == 'p') {
            controller.togglePause();
            return;
        }

        // DEV04 - UC4.4 Alternative Flow:
        // ESC cho phép Player không Restart mà quay về Menu chính sau Game Over.
        if (currentKeyCode == KeyEvent.VK_ESCAPE) {
            controller.backToMenu();
            return;
        }

        // =========================================================================
        // [UC05] - NHẬT ANH
        // PHẦN CHỈNH SỬA ĐỂ SỬA LỖI BÀN PHÍM VIE (XỬ LÝ DI CHUYỂN):
        // Thay vì chỉ dựa vào mã phím cơ học (W, A, S, D), hệ thống kiểm tra song song
        // cả mã phím lẫn ký tự chữ nhận được từ bộ gõ Telex tiếng Việt để gán hướng chính xác.
        // =========================================================================

        // =========================================================================
        // [DEV02 - UC02] - LÊ TUẤN ANH
        // PHẦN CẢI TIẾN CHỨC NĂNG CONTROL SNAKE:
        // Giữ nguyên toàn bộ comment và xử lý của các thành viên trước đó.
        // Phần cải tiến tập trung làm rõ trách nhiệm của InputHandler trong UC02:
        // 1. Nhận phím điều khiển từ người chơi.
        // 2. Ánh xạ phím mũi tên hoặc W/A/S/D thành hướng di chuyển tương ứng.
        // 3. Bỏ qua input không thuộc nhóm điều khiển.
        // 4. Chỉ gửi Direction hợp lệ sang GameController.
        //
        // Lưu ý:
        // InputHandler KHÔNG tự cập nhật hướng trực tiếp cho Snake.
        // GameController sẽ tiếp tục kiểm tra GameState == PLAYING và chống quay ngược 180 độ.
        // =========================================================================

        Direction newDirection = null;

        // Hướng LÊN: Chấp nhận phím mũi tên Lên, phím W, ký tự chữ 'w', hoặc chữ 'ư' (khi gõ Telex chữ W biến thành ư)
        if (currentKeyCode == KeyEvent.VK_UP
                || currentKeyCode == KeyEvent.VK_W
                || keyChar == 'w'
                || keyChar == 'ư') {
            newDirection = Direction.UP;
        }
        // Hướng XUỐNG: Chấp nhận phím mũi tên Xuống, phím S, hoặc ký tự chữ 's'
        else if (currentKeyCode == KeyEvent.VK_DOWN
                || currentKeyCode == KeyEvent.VK_S
                || keyChar == 's') {
            newDirection = Direction.DOWN;
        }
        // Hướng TRÁI: Chấp nhận phím mũi tên Trái, phím A, ký tự 'a', hoặc các chữ có dấu do gõ nhanh bộ gõ tự tạo ra ('á', 'à', 'ả', 'ã', 'ạ')
        else if (currentKeyCode == KeyEvent.VK_LEFT
                || currentKeyCode == KeyEvent.VK_A
                || keyChar == 'a'
                || keyChar == 'á'
                || keyChar == 'à'
                || keyChar == 'ả'
                || keyChar == 'ã'
                || keyChar == 'ạ') {
            newDirection = Direction.LEFT;
        }
        // Hướng PHẢI: Chấp nhận phím mũi tên Phải, phím D, ký tự 'd', hoặc chữ 'đ' (khi gõ Telex nhấn 2 lần D biến thành chữ đ)
        else if (currentKeyCode == KeyEvent.VK_RIGHT
                || currentKeyCode == KeyEvent.VK_D
                || keyChar == 'd'
                || keyChar == 'đ') {
            newDirection = Direction.RIGHT;
        }

        // DEV04 - State guard sau Game Over:
        // Khi state = GAME_OVER, GameController.requestChangeDirection() sẽ bỏ qua input điều hướng,
        // bảo đảm rắn không tiếp tục đổi hướng sau khi đã thua.

        // [DEV02 - UC02] - LÊ TUẤN ANH
        // Chỉ gửi yêu cầu đổi hướng khi hệ thống xác định được Direction hợp lệ.
        // Các phím không liên quan như X, B, Space... sẽ bị bỏ qua và không làm thay đổi hướng rắn.
        if (newDirection != null) {
            controller.requestChangeDirection(newDirection);
        }
    }

    // Giữ lại hàm mapKeyToDirection cũ để đảm bảo không lỗi biên dịch nếu các file khác gọi đến
    // [DEV02 - UC02] - LÊ TUẤN ANH
    // Hàm này vẫn phục vụ đúng nội dung trong Use Case/Sequence Diagram:
    // chuyển mã phím hợp lệ thành hướng di chuyển tương ứng.
    public Direction mapKeyToDirection(int keyCode) {
        return switch (keyCode) {
            case KeyEvent.VK_UP, KeyEvent.VK_W -> Direction.UP;
            case KeyEvent.VK_DOWN, KeyEvent.VK_S -> Direction.DOWN;
            case KeyEvent.VK_LEFT, KeyEvent.VK_A -> Direction.LEFT;
            case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> Direction.RIGHT;
            default -> null;
        };
    }

    // Giữ lại hàm isValidKey cũ để duy trì cấu trúc code ban đầu
    // [DEV02 - UC02] - LÊ TUẤN ANH
    // Hàm này dùng để kiểm tra nhóm phím hệ thống đang hỗ trợ:
    // điều hướng, bắt đầu/chơi lại, quay về menu và tạm dừng/tiếp tục.
    public boolean isValidKey(int keyCode) {
        return mapKeyToDirection(keyCode) != null
                || keyCode == KeyEvent.VK_ENTER
                || keyCode == KeyEvent.VK_ESCAPE
                || keyCode == KeyEvent.VK_R
                || keyCode == KeyEvent.VK_P;
    }

    public int getCurrentKeyCode() {
        return currentKeyCode;
    }

    public GameController getController() {
        return controller;
    }
}