package test.java;

import model.Direction;
import model.Snake;

/**
 * Development Testing / Unit Testing cho DEV02 - UC02: Control Snake.
 * Người thực hiện: Lê Tuấn Anh - MSSV 23130008.
 *
 * Mục tiêu:
 * - Kiểm thử hướng mặc định sau khi reset.
 * - Kiểm thử cập nhật hướng hợp lệ.
 * - Kiểm thử chặn rắn quay ngược trực tiếp 180 độ.
 *
 * Cách chạy thủ công nếu project dùng cấu trúc src:
 * javac -encoding UTF-8 -cp src src/test/java/ControlSnakeSnakeUnitTest.java
 * java -cp src;src/test/java ControlSnakeSnakeUnitTest
 */
public class ControlSnakeSnakeUnitTest {

    public static void main(String[] args) {
        testDefaultDirectionAfterReset();
        testAcceptNonOppositeDirection();
        testRejectOppositeDirection();
        testNullDirectionIsIgnored();

        System.out.println("PASSED: ControlSnakeSnakeUnitTest");
    }

    private static void testDefaultDirectionAfterReset() {
        Snake snake = new Snake();
        snake.reset(5, 5);

        assertEquals(Direction.RIGHT, snake.getDirection(), "Sau reset, hướng mặc định của rắn phải là RIGHT");
    }

    private static void testAcceptNonOppositeDirection() {
        Snake snake = new Snake();
        snake.reset(5, 5);

        snake.setDirection(Direction.UP);

        assertEquals(Direction.UP, snake.getDirection(), "RIGHT đổi sang UP là hợp lệ");
    }

    private static void testRejectOppositeDirection() {
        Snake snake = new Snake();
        snake.reset(5, 5);

        assertTrue(snake.isOppositeDirection(Direction.LEFT), "RIGHT và LEFT là hai hướng đối lập");
        snake.setDirection(Direction.LEFT);
        assertEquals(Direction.RIGHT, snake.getDirection(), "Không được đổi trực tiếp từ RIGHT sang LEFT");

        snake.setDirection(Direction.UP);
        assertEquals(Direction.UP, snake.getDirection(), "RIGHT đổi sang UP là hợp lệ");

        assertTrue(snake.isOppositeDirection(Direction.DOWN), "UP và DOWN là hai hướng đối lập");
        snake.setDirection(Direction.DOWN);
        assertEquals(Direction.UP, snake.getDirection(), "Không được đổi trực tiếp từ UP sang DOWN");
    }

    private static void testNullDirectionIsIgnored() {
        Snake snake = new Snake();
        snake.reset(5, 5);

        snake.setDirection(null);

        assertEquals(Direction.RIGHT, snake.getDirection(), "newDirection null không được làm thay đổi hướng hiện tại");
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
}
