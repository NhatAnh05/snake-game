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
        // UC02-MF-02: Hệ thống tiếp nhận sự kiện bàn phím từ người chơi.
        currentKeyCode = event.getKeyCode();

        // UC02-AF01: Nếu phím không thuộc nhóm điều khiển hợp lệ thì bỏ qua input.
        if (!isValidKey(currentKeyCode)) {
            return;
        }

        // ENTER: bắt đầu game từ Menu hoặc chơi lại sau khi Game Over.
        if (currentKeyCode == KeyEvent.VK_ENTER) {
            controller.handleStartOrRestartRequest();
            return;
        }

        // P: tạm dừng hoặc tiếp tục game.
        if (currentKeyCode == KeyEvent.VK_P) {
            controller.togglePause();
            return;
        }

        // ESC: quay về Menu khi game đang Pause hoặc Game Over.
        if (currentKeyCode == KeyEvent.VK_ESCAPE) {
            controller.backToMenu();
            return;
        }

        // UC02-MF-04, UC02-MF-05: Chuyển phím mũi tên/WASD thành hướng di chuyển tương ứng.
        Direction newDirection = mapKeyToDirection(currentKeyCode);

        // UC02-MF-06 -> UC02-MF-08 được xử lý trong GameController và Snake.
        controller.requestChangeDirection(newDirection);
    }

    public Direction mapKeyToDirection(int keyCode) {
        return switch (keyCode) {
            case KeyEvent.VK_UP, KeyEvent.VK_W -> Direction.UP;
            case KeyEvent.VK_DOWN, KeyEvent.VK_S -> Direction.DOWN;
            case KeyEvent.VK_LEFT, KeyEvent.VK_A -> Direction.LEFT;
            case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> Direction.RIGHT;
            default -> null;
        };
    }

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
