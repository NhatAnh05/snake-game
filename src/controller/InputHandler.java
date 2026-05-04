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

        if (currentKeyCode == KeyEvent.VK_ENTER) {
            controller.handleStartOrRestartRequest();
            return;
        }

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
        return mapKeyToDirection(keyCode) != null;
    }

    public int getCurrentKeyCode() {
        return currentKeyCode;
    }
}
