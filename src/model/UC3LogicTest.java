package model;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class UC3LogicTest {

    private ScoreManager scoreManager;
    private Snake snake;

    @BeforeEach
    public void setUp() {
        scoreManager = new ScoreManager();
        scoreManager.resetScore();

        snake = new Snake();
        snake.reset(10, 10);
    }

    // ==========================================
    // PHẦN 1: KIỂM THỬ LOGIC TÍNH ĐIỂM & COMBO
    // ==========================================

    @Test
    public void testProcessEatEvent_NormalFood() {
        // [TC01] Ăn mồi thường cộng 10 điểm
        scoreManager.processEatEvent(false);
        assertEquals(10, scoreManager.getCurrentScore(), "Điểm cơ bản của mồi thường phải là 10");
    }

    @Test
    public void testProcessEatEvent_SpecialFood() {
        // [TC06] Ăn mồi đặc biệt cộng 30 điểm
        scoreManager.processEatEvent(true);
        assertEquals(30, scoreManager.getCurrentScore(), "Điểm cơ bản của mồi đặc biệt phải là 30");
    }

    @Test
    public void testComboMultiplier() {
        // [TC08] Kiểm tra hệ số nhân Combo khi ăn liên tiếp dưới 5 giây
        scoreManager.processEatEvent(false); // Lần 1: 10đ (Tổng: 10)

        scoreManager.processEatEvent(false); // Lần 2 (Combo 2 -> x1.5): 10 * 1.5 = 15đ (Tổng: 25)
        assertEquals(25, scoreManager.getCurrentScore(), "Hệ số Combo x1.5 chưa hoạt động đúng");

        scoreManager.processEatEvent(false); // Lần 3 (Combo 3 -> x1.5): 10 * 1.5 = 15đ (Tổng: 40)
        scoreManager.processEatEvent(false); // Lần 4 (Combo 4 -> x2.0): 10 * 2.0 = 20đ (Tổng: 60)
        assertEquals(60, scoreManager.getCurrentScore(), "Hệ số Combo x2.0 chưa hoạt động đúng");
    }

    // ==========================================
    // PHẦN 2: KIỂM THỬ LOGIC DI CHUYỂN & LỚN LÊN CỦA RẮN
    // ==========================================

    @Test
    public void testGrow() {
        // [TC01] Kiểm tra độ dài thân rắn sau khi ăn mồi
        int initialSize = snake.getBody().size();
        snake.grow();
        int newSize = snake.getBody().size();

        assertEquals(initialSize + 1, newSize, "Rắn phải dài thêm 1 đốt sau khi gọi hàm grow()");
    }

    @Test
    public void testSetDirection_OppositeDirection() {
        // [UC02-AF03] Không cho phép rắn quay ngược 180 độ
        snake.setDirection(Direction.RIGHT);
        snake.setDirection(Direction.LEFT);

        assertEquals(Direction.RIGHT, snake.getDirection(), "Rắn không được phép quay ngược 180 độ");
    }

    // ==========================================
    // PHẦN 3: KIỂM THỬ LOGIC SINH MỒI (TRÁNH VẬT THỂ)
    // ==========================================

    @Test
    public void testSpawn_FoodNotInSnakeBody() {
        // [TC02] Kiểm tra mồi không xuất hiện đè lên thân rắn
        Food food = new Food();
        List<Point> snakeBody = new ArrayList<>();
        for(int i = 0; i < 5; i++) {
            for(int j = 0; j < 5; j++) {
                snakeBody.add(new Point(i, j));
            }
        }

        food.spawn(snakeBody);
        Point foodPos = food.getPosition();

        assertFalse(snakeBody.contains(foodPos), "Mồi không được sinh ra đè lên thân rắn");
    }
}