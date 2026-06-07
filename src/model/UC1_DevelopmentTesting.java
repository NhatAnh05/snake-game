
package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

    public class UC1_DevelopmentTesting {

        private GameModel gameModel;
        private Snake snake;
        private Food food;
        private ScoreManager scoreManager;
        private Wall wall;

        @BeforeEach
        public void setUp() {

            gameModel = new GameModel();


            snake = gameModel.getSnake();
            food = gameModel.getFood();
            scoreManager = gameModel.getScoreManager();
            wall = gameModel.getWall();

            gameModel.setCurrentState(GameState.MENU);
        }

        // =========================================================================
        // PHẦN 1: KIỂM THỬ LUỒNG THIẾT LẬP VÀ KHỞI TẠO DỮ LIỆU AN TOÀN
        // =========================================================================

        @Test
        public void testInitializeSecureNewGame_ResetBodyAndScore() {
            // [TC01] Kiểm tra dọn dẹp dữ liệu cũ, đặt lại độ dài rắn mặc định và xóa điểm ván cũ
            // Giả lập trạng thái ván chơi cũ: rắn ăn mồi lớn lên và tích lũy điểm số
            snake.grow();
            snake.grow();
            scoreManager.processEatEvent(false); // Cộng điểm và tăng combo
            scoreManager.processEatEvent(true);  // Ăn mồi đặc biệt

            assertTrue(snake.getBody().size() > 3, "Giả lập: Rắn phải dài hơn 3 đốt trước khi reset ván mới");
            assertTrue(scoreManager.getCurrentScore() > 0, "Giả lập: Điểm số phải lớn hơn 0 trước khi reset");

            // Hành động: Kích hoạt luồng vào trận an toàn
            gameModel.initializeSecureNewGame();

            // Xác thực kết quả: Các thực thể đưa về trạng thái mặc định an toàn
            assertEquals(3, snake.getBody().size(), "Luồng an toàn phải đưa số đốt rắn về chính xác 3 đốt mặc định");
            assertEquals(0, scoreManager.getCurrentScore(), "Điểm số hiện tại của ván chơi mới bắt buộc phải bằng 0");
            assertEquals("", gameModel.getGameOverReason(), "Lý do kết thúc ván (GameOverReason) phải được làm rỗng");
        }

        @Test
        public void testInitializeSecureNewGame_ForceDirectionRight() {
            // [TC02] Kiểm tra áp đặt cố định hướng di chuyển xuất phát ban đầu là RIGHT (Sang phải)
            // Giả lập ván chơi trước kết thúc khi con rắn đang di chuyển theo hướng DOWN hoặc LEFT
            snake.setDirection(Direction.DOWN);
            assertEquals(Direction.DOWN, snake.getDirection(), "Giả lập: Hướng rắn phải là DOWN trước khi reset");

            // Hành động: Khởi tạo trận đấu mới
            gameModel.initializeSecureNewGame();

            // Xác thực kết quả: Khắc phục triệt để lỗi hiển thị sai hướng đồ họa lúc vào trận
            assertEquals(Direction.RIGHT, snake.getDirection(), "Luồng an toàn phải ép hướng xuất phát của rắn luôn luôn sang phải (RIGHT)");
        }

        @Test
        public void testInitializeSecureNewGame_ResetComboStreak() {
            // [TC03] Kiểm tra làm sạch bộ đếm tương tác liên hoàn (Combo) để tránh sai lệch ván mới
            // Giả lập ván cũ người chơi đang có chuỗi ăn mồi liên tục sát thời gian (Combo > 0)
            scoreManager.processEatEvent(false);
            scoreManager.processEatEvent(false);
            assertTrue(scoreManager.getComboCount() > 1, "Giả lập: Bộ đếm combo phải lớn hơn 1 trước khi reset");

            // Hành động: Khởi tạo trận đấu mới
            gameModel.initializeSecureNewGame();

            // Xác thực kết quả: Bộ đếm combo cũ quay về 0, ăn mồi ở giây đầu tiên của ván mới tính điểm cơ bản
            assertEquals(0, scoreManager.getComboCount(), "Luồng an toàn phải gọi resetCombo() đưa số combo về 0");

            scoreManager.processEatEvent(false); // Ăn mồi thường ván mới (Cơ bản 10đ, combo = 1, hệ số nhân x1.0)
            assertEquals(10, scoreManager.getCurrentScore(), "Điểm ăn mồi đầu ván mới không được phép nhân theo hệ số combo của ván cũ");
        }

        @Test
        public void testInitializeSecureNewGame_FoodPositionValidation() {
            // [TC04] Kiểm tra vòng lặp kiểm tra an toàn tránh mồi sinh trùng vật cản/tường hoặc thân rắn
            // Giả lập môi trường chế độ sinh tồn SURVIVAL để sinh hệ thống tường chắn ngẫu nhiên
            gameModel.setCurrentMode(GameMode.SURVIVAL);

            // Hành động: Kích hoạt luồng khởi tạo ván đấu an toàn
            gameModel.initializeSecureNewGame();

            Point foodPosition = food.getPosition();
            List<Point> snakeBody = snake.getBody();

            // Xác thực kết quả: Vị trí mồi hợp lệ, nằm ngoài vùng chiếm dụng của tường và thân rắn
            assertNotNull(foodPosition, "Vị trí mồi mới sinh không được phép bị null");
            assertFalse(wall.contains(foodPosition), "Thuật toán kiểm tra an toàn phải đảm bảo tọa độ mồi không trùng với tường");
            assertFalse(snakeBody.contains(foodPosition), "Tọa độ mồi sinh ra không được phép nằm đè lên bất kỳ đốt thân nào của rắn");
        }

        // =========================================================================
        // PHẦN 2: KIỂM THỬ TRẠNG THÁI HỆ THỐNG VÀ ĐỒNG BỘ HIỆU ỨNG ĐẾM NGƯỢC
        // =========================================================================

        @Test
        public void testGameTransitionState() {
            // [TC05] Kiểm tra việc cập nhật biến quản lý trạng thái khi bắt đầu chơi
            assertEquals(GameState.MENU, gameModel.getCurrentState(), "Ban đầu trạng thái game phải là MENU");

            // Hành động: Kích hoạt chuỗi lệnh thiết lập game mới
            gameModel.initializeSecureNewGame();

            // Xác thực kết quả: Trạng thái hệ thống chuyển dịch chính xác sang PLAYING
            assertEquals(GameState.PLAYING, gameModel.getCurrentState(), "Hệ thống phải cập nhật currentState sang PLAYING sau khi chuẩn bị ván đấu");
        }

        @Test
        public void testSessionTimestampInitialization() {
            // [TC06] Kiểm tra việc thiết lập mốc thời gian (Timestamp) an toàn cho phiên chơi mới
            // Hành động: Khởi tạo ván mới
            gameModel.initializeSecureNewGame();

            // Xác thực kết quả: Thời gian bắt đầu ván chơi phải được ghi nhận thực tế, thời gian kết thúc bằng 0
            assertTrue(gameModel.getSessionDurationSeconds() >= 0, "Thời gian sống của phiên chơi phải tính toán hợp lệ từ mốc thời gian thực");
        }

        // =========================================================================
        // PHẦN 3: KIỂM THỬ ĐỒNG BỘ HƯỚNG HIỂN THỊ ĐỒ HỌA HUD (HUD DIRECTION SYNC)
        // =========================================================================

        @Test
        public void testHUDVisualDirectionSynchronization() {
            // [TC07] Kiểm tra luồng đồng bộ hướng logic của Snake sang thành phần hiển thị HUD mũi tên chỉ hướng
            gameModel.initializeSecureNewGame(); // Khởi tạo an toàn (Ép hướng về RIGHT)

            // Mô phỏng tầng View đọc hướng từ Model để vẽ mũi tên HUD: Lấy hướng trực tiếp từ snake.getDirection()
            Direction currentHUDDirection = snake.getDirection();
            assertEquals(Direction.RIGHT, currentHUDDirection, "Giao diện hiển thị HUD mũi tên chỉ hướng ban đầu phải đồng bộ là RIGHT");

            // Giả lập người chơi bấm nút điều hướng rẽ LÊN (UP)
            snake.setDirection(Direction.UP);
            currentHUDDirection = snake.getDirection();

            // Xác thực kết quả: HUD cập nhật hướng tức thì tại chu kỳ render kế tiếp, loại bỏ hoàn toàn độ trễ khung hình
            assertEquals(Direction.UP, currentHUDDirection, "Giao diện hiển thị HUD phải lập tức ghi nhận hướng UP để kết xuất mũi tên chính xác");
        }
    }