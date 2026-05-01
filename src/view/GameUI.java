package view;

import javax.swing.*;
import java.awt.*;
import model.GameModel;

public class GameUI extends JFrame {
    private GamePanel panel;
    private GameModel currentModel; // Biến lưu trữ Model (có thể giữ lại hoặc bỏ đi đều được)

    public GameUI() {
        setTitle("SNAKE ADVENTURE - MVC");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        // Khởi tạo bảng vẽ
        panel = new GamePanel();
        panel.setPreferredSize(new Dimension(600, 600)); // Đặt kích thước ở đây rất chuẩn
        add(panel);

        pack();
        setLocationRelativeTo(null); // Giữa màn hình
    }

    // Hàm này được Controller gọi mỗi khi có thay đổi dữ liệu (Rắn bò, ăn mồi...)
    public void render(GameModel model) {
        this.currentModel = model;

        // DÒNG QUAN TRỌNG NHẤT: Bơm dữ liệu từ UI xuống Panel
        panel.updateModel(model);

        panel.repaint(); // Yêu cầu Panel vẽ lại màn hình
    }

    public void showGameScreen() {
        this.setVisible(true); // Lệnh này giúp hiển thị cửa sổ JFrame lên màn hình
    }
}