package test.java;

import controller.InputHandler;
import model.Direction;

import java.awt.event.KeyEvent;

/**
 * Development Testing / Unit Testing cho DEV02 - UC02: Control Snake.
 * Người thực hiện: Lê Tuấn Anh - MSSV 23130008.
 *
 * Mục tiêu:
 * - Kiểm thử ánh xạ phím mũi tên và W/A/S/D thành Direction.
 * - Kiểm thử nhóm phím hợp lệ/không hợp lệ trong InputHandler.
 *
 * Cách chạy thủ công nếu project dùng cấu trúc src:
 * javac -encoding UTF-8 -cp src src/test/java/ControlSnakeInputHandlerUnitTest.java
 * java -cp src;src/test/java ControlSnakeInputHandlerUnitTest
 */
public class ControlSnakeInputHandlerUnitTest {

    public static void main(String[] args) {
        InputHandler handler = new InputHandler(null);

        assertEquals(Direction.UP, handler.mapKeyToDirection(KeyEvent.VK_UP), "VK_UP phải ánh xạ thành UP");
        assertEquals(Direction.DOWN, handler.mapKeyToDirection(KeyEvent.VK_DOWN), "VK_DOWN phải ánh xạ thành DOWN");
        assertEquals(Direction.LEFT, handler.mapKeyToDirection(KeyEvent.VK_LEFT), "VK_LEFT phải ánh xạ thành LEFT");
        assertEquals(Direction.RIGHT, handler.mapKeyToDirection(KeyEvent.VK_RIGHT), "VK_RIGHT phải ánh xạ thành RIGHT");

        assertEquals(Direction.UP, handler.mapKeyToDirection(KeyEvent.VK_W), "VK_W phải ánh xạ thành UP");
        assertEquals(Direction.DOWN, handler.mapKeyToDirection(KeyEvent.VK_S), "VK_S phải ánh xạ thành DOWN");
        assertEquals(Direction.LEFT, handler.mapKeyToDirection(KeyEvent.VK_A), "VK_A phải ánh xạ thành LEFT");
        assertEquals(Direction.RIGHT, handler.mapKeyToDirection(KeyEvent.VK_D), "VK_D phải ánh xạ thành RIGHT");

        assertTrue(handler.isValidKey(KeyEvent.VK_UP), "Phím mũi tên phải là phím hợp lệ");
        assertTrue(handler.isValidKey(KeyEvent.VK_W), "Phím W phải là phím hợp lệ");
        assertTrue(handler.isValidKey(KeyEvent.VK_ENTER), "ENTER là phím hệ thống hợp lệ để start/restart");
        assertTrue(handler.isValidKey(KeyEvent.VK_R), "R là phím hệ thống hợp lệ để restart");
        assertTrue(handler.isValidKey(KeyEvent.VK_P), "P là phím hệ thống hợp lệ để pause/resume");
        assertTrue(handler.isValidKey(KeyEvent.VK_ESCAPE), "ESC là phím hệ thống hợp lệ để quay về menu");

        assertEquals(null, handler.mapKeyToDirection(KeyEvent.VK_X), "VK_X không được ánh xạ thành Direction");
        assertFalse(handler.isValidKey(KeyEvent.VK_X), "VK_X không phải phím hợp lệ");
        assertFalse(handler.isValidKey(KeyEvent.VK_SPACE), "SPACE không phải phím hợp lệ trong UC02 Control Snake");

        System.out.println("PASSED: ControlSnakeInputHandlerUnitTest");
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null) {
            if (actual != null) {
                throw new AssertionError(message + " | expected=null, actual=" + actual);
            }
            return;
        }

        if (!expected.equals(actual)) {
            throw new AssertionError(message + " | expected=" + expected + ", actual=" + actual);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError(message);
        }
    }
}
