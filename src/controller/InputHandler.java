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

        // 1. ENTER: bắt đầu game từ Menu hoặc chơi lại sau khi Game Over.
        if (currentKeyCode == KeyEvent.VK_ENTER) {
            controller.handleStartOrRestartRequest();
            return;
        }

        // 2. P: Tạm dừng hoặc tiếp tục game.
        // 🌟 PHẦN CHỈNH SỬA: Chấp nhận cả mã phím VK_P HOẶC ký tự 'p' thực tế phát ra từ Unikey.
        if (currentKeyCode == KeyEvent.VK_P || keyChar == 'p') {
            controller.togglePause();
            return; 
        }     
        
        // 3. ESC: quay về Menu khi game đang Pause hoặc Game Over.
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
        Direction newDirection = null;

        // Hướng LÊN: Chấp nhận phím mũi tên Lên, phím W, ký tự chữ 'w', hoặc chữ 'ư' (khi gõ Telex chữ W biến thành ư)
        if (currentKeyCode == KeyEvent.VK_UP || currentKeyCode == KeyEvent.VK_W || keyChar == 'w' || keyChar == 'ư') {
            newDirection = Direction.UP;
        } 
        // Hướng XUỐNG: Chấp nhận phím mũi tên Xuống, phím S, hoặc ký tự chữ 's'
        else if (currentKeyCode == KeyEvent.VK_DOWN || currentKeyCode == KeyEvent.VK_S || keyChar == 's') {
            newDirection = Direction.DOWN;
        } 
        // Hướng TRÁI: Chấp nhận phím mũi tên Trái, phím A, ký tự 'a', hoặc các chữ có dấu do gõ nhanh bộ gõ tự tạo ra ('á', 'à', 'ả', 'ã', 'ạ')
        else if (currentKeyCode == KeyEvent.VK_LEFT || currentKeyCode == KeyEvent.VK_A || keyChar == 'a' 
                || keyChar == 'á' || keyChar == 'à' || keyChar == 'ả' || keyChar == 'ã' || keyChar == 'ạ') {
            newDirection = Direction.LEFT;
        } 
        // Hướng PHẢI: Chấp nhận phím mũi tên Phải, phím D, ký tự 'd', hoặc chữ 'đ' (khi gõ Telex nhấn 2 lần D biến thành chữ đ)
        else if (currentKeyCode == KeyEvent.VK_RIGHT || currentKeyCode == KeyEvent.VK_D || keyChar == 'd' || keyChar == 'đ') {
            newDirection = Direction.RIGHT;
        }

        // Nếu bắt được hướng đi hợp lệ thì gửi yêu cầu đổi hướng sang Controller để điều khiển rắn
        if (newDirection != null) {
            controller.requestChangeDirection(newDirection);
        }
    }

    // Giữ lại hàm mapKeyToDirection cũ để đảm bảo không lỗi biên dịch nếu các file khác gọi đến
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
    public boolean isValidKey(int keyCode) {
        return mapKeyToDirection(keyCode) != null
                || keyCode == KeyEvent.VK_ENTER
                || keyCode == KeyEvent.VK_ESCAPE
                || keyCode == KeyEvent.VK_P;
    }

    public int getCurrentKeyCode() {
        return currentKeyCode;
    }
}
