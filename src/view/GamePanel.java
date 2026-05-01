package view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import model.GameModel;
import model.Point;

public class GamePanel extends JPanel {
    private GameModel currentModel;

    // Biến lưu trữ ảnh thật
    private BufferedImage imgHead;
    private BufferedImage imgBody;
    private BufferedImage imgFood;

    // 1. Hàm khởi tạo (Constructor) bị thiếu đã được thêm vào
    public GamePanel() {
        setPreferredSize(new Dimension(600, 600));
        loadSprites(); // Tải ảnh ngay khi mở game
    }

    // 2. Hàm cập nhật dữ liệu từ GameUI
    public void updateModel(GameModel model) {
        this.currentModel = model;
    }

    // 3. Hàm tải ảnh từ ổ cứng
    private void loadSprites() {
        try {
            imgHead = ImageIO.read(new File("head.png"));
            imgBody = ImageIO.read(new File("body.png"));
            imgFood = ImageIO.read(new File("food.png"));
        } catch (IOException e) {
            System.out.println("Chưa có file ảnh. Hệ thống sẽ tự động dùng đồ họa Neon dự phòng!");
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Khử răng cưa cho hình mượt mà
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Vẽ nền không gian sâu (Gradient tối)
        GradientPaint gp = new GradientPaint(0, 0, new Color(20, 20, 20), 0, getHeight(), new Color(40, 40, 40));
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Vẽ lưới Grid mờ (20 pixels)
        g2d.setColor(new Color(255, 255, 255, 20));
        for (int i = 0; i < getWidth(); i += 20) {
            g2d.drawLine(i, 0, i, getHeight());
            g2d.drawLine(0, i, getWidth(), i);
        }

        // Nếu Model chưa được truyền vào thì không vẽ tiếp
        if (currentModel == null) return;

        // Dùng GameState để quyết định vẽ cái gì
        switch (currentModel.getCurrentState()) {
            case MENU:
                drawMenu(g2d);
                break;
            case PLAYING:
                drawGameObjects(g2d);
                break;
            case PAUSED:
                drawGameObjects(g2d);
                drawOverlayText(g2d, "PAUSED", "Press P to Resume", Color.ORANGE);
                break;
            case GAME_OVER:
                drawGameObjects(g2d); // Vẫn vẽ xác rắn mờ ở dưới
                drawOverlayText(g2d, "GAME OVER", "Press ENTER to Restart", Color.RED);
                break;
        }
    }

    private void drawMenu(Graphics2D g2d) {
        g2d.setColor(new Color(0, 255, 150)); // Màu Xanh Neon
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 50));
        g2d.drawString("SNAKE GAME", 135, 280);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI", Font.ITALIC, 18));
        g2d.drawString("Press [ ENTER ] to Start", 195, 330);
    }

    private void drawGameObjects(Graphics2D g2d) {
        // --- VẼ MỒI (FOOD) ---
        if (currentModel.getFood() != null && currentModel.getFood().getPosition() != null) {
            Point f = currentModel.getFood().getPosition();
            if (imgFood != null) {
                // Nếu có ảnh thì vẽ ảnh (ép về kích thước 20x20)
                g2d.drawImage(imgFood, f.x * 20, f.y * 20, 20, 20, null);
            } else {
                // Nếu không có ảnh thì vẽ Neon
                g2d.setColor(new Color(255, 50, 50));
                g2d.fillOval(f.x * 20 + 2, f.y * 20 + 2, 16, 16);
            }
        }

        // --- VẼ RẮN (SNAKE) ---
        if (currentModel.getSnake() != null && currentModel.getSnake().getBody() != null) {
            java.util.List<Point> body = currentModel.getSnake().getBody();
            if (!body.isEmpty()) {
                // 1. Vẽ Thân rắn (Từ đốt thứ 2 trở đi)
                for (int i = 1; i < body.size(); i++) {
                    Point p = body.get(i);
                    if (imgBody != null) {
                        g2d.drawImage(imgBody, p.x * 20, p.y * 20, 20, 20, null);
                    } else {
                        g2d.setColor(new Color(0, 200, 100)); // Màu thân Neon
                        g2d.fillRoundRect(p.x * 20 + 1, p.y * 20 + 1, 18, 18, 6, 6);
                    }
                }

                // 2. Vẽ Đầu rắn (Đốt số 0)
                Point head = body.get(0);
                if (imgHead != null) {
                    // Vẽ ảnh và xoay đầu
                    drawRotatedHead(g2d, head, currentModel.getSnake().getDirection());
                } else {
                    g2d.setColor(new Color(0, 255, 150)); // Màu đầu Neon sáng
                    g2d.fillRoundRect(head.x * 20 + 1, head.y * 20 + 1, 18, 18, 6, 6);
                }
            }
        }

        // --- VẼ ĐIỂM SỐ (SCORE) ---
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.setFont(new Font("Consolas", Font.BOLD, 18));
        if (currentModel.getScoreManager() != null) {
            g2d.drawString("SCORE: " + currentModel.getScoreManager().getCurrentScore(), 20, 30);
        }
    }

    // Logic xoay ảnh đầu rắn siêu mượt
    private void drawRotatedHead(Graphics2D g2d, Point headPoint, String direction) {
        AffineTransform oldAT = g2d.getTransform();
        g2d.translate(headPoint.x * 20 + 10, headPoint.y * 20 + 10); // Đưa tâm về giữa ô

        double angle = 0;
        // Tránh lỗi NullPointerException nếu direction chưa được gán
        if (direction != null) {
            switch (direction) {
                case "UP":    angle = -Math.PI / 2; break;
                case "DOWN":  angle = Math.PI / 2; break;
                case "LEFT":  angle = Math.PI; break;
                case "RIGHT": angle = 0; break;
            }
        }

        g2d.rotate(angle);
        g2d.drawImage(imgHead, -10, -10, 20, 20, null); // Vẽ từ tâm lệch 10px
        g2d.setTransform(oldAT); // Reset trục tọa độ
    }

    private void drawOverlayText(Graphics2D g2d, String title, String subtitle, Color titleColor) {
        // Lớp phủ đen mờ (Opacity)
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Chữ tiêu đề lớn (Căn giữa)
        g2d.setColor(titleColor);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 50));
        FontMetrics fm = g2d.getFontMetrics();
        int titleX = (getWidth() - fm.stringWidth(title)) / 2;
        g2d.drawString(title, titleX, 280);

        // Chữ phụ đề nhỏ (Căn giữa)
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI", Font.ITALIC, 20));
        FontMetrics fmSub = g2d.getFontMetrics();
        int subX = (getWidth() - fmSub.stringWidth(subtitle)) / 2;
        g2d.drawString(subtitle, subX, 330);
    }
}