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
        currentKeyCode = event.getKeyCode();

        // ENTER: bắt đầu game hoặc chơi lại sau khi Game Over
        if (currentKeyCode == KeyEvent.VK_ENTER) {
            controller.handleStartOrRestartRequest();
            return;
        }

        // P: tạm dừng hoặc tiếp tục game
        if (currentKeyCode == KeyEvent.VK_P) {
            controller.togglePause();
            return;
        }

        // ESC: quay về menu nếu đang Pause hoặc Game Over
        if (currentKeyCode == KeyEvent.VK_ESCAPE) {
            controller.backToMenu();
            return;
        }

        // Điều khiển hướng đi của rắn
        Direction newDirection = mapKeyToDirection(currentKeyCode);
        if (newDirection != null) {
            controller.requestChangeDirection(newDirection);
        }
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