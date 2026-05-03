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

    public GamePanel() {
        setPreferredSize(new Dimension(600, 600));
        loadSprites(); // Tải ảnh ngay khi mở game
    }

    public void updateModel(GameModel model) {
        this.currentModel = model;
    }

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

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


        GradientPaint gp = new GradientPaint(0, 0, new Color(20, 20, 20), 0, getHeight(), new Color(40, 40, 40));
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());


        g2d.setColor(new Color(255, 255, 255, 20));
        for (int i = 0; i < getWidth(); i += 20) {
            g2d.drawLine(i, 0, i, getHeight());
            g2d.drawLine(0, i, getWidth(), i);
        }


        if (currentModel == null) return;


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

    // ---------------------------------------------------------
    // 1. MÀN HÌNH START GAME (MENU)
    // ---------------------------------------------------------
    private void drawMenu(Graphics2D g2d) {
        int width = getWidth();
        int height = getHeight();

        // Vẽ Khung viền Neon nhấp nháy mờ
        g2d.setColor(new Color(0, 255, 150, 50));
        g2d.setStroke(new BasicStroke(8));
        g2d.drawRoundRect(40, 40, width - 80, height - 80, 20, 20);

        g2d.setColor(new Color(0, 255, 150, 200));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(45, 45, width - 90, height - 90, 15, 15);

        // Vẽ Tiêu đề Game Glow
        String title1 = "SNAKE";
        String title2 = "ARCADE";
        g2d.setFont(new Font("Monospaced", Font.BOLD, 75));
        FontMetrics fmTitle = g2d.getFontMetrics();

        int xTitle1 = (width - fmTitle.stringWidth(title1)) / 2;
        int xTitle2 = (width - fmTitle.stringWidth(title2)) / 2;

        drawGlowText(g2d, title1, xTitle1, 160, new Color(57, 255, 20));
        drawGlowText(g2d, title2, xTitle2, 240, new Color(57, 255, 20));

        // Vẽ rắn trang trí
        drawDecorativeSnake(g2d, width / 2 - 80, 310);

        // Text hướng dẫn
        String startText = "- PRESS [ ENTER ] TO START -";
        g2d.setFont(new Font("Consolas", Font.BOLD, 22));
        FontMetrics fmStart = g2d.getFontMetrics();
        g2d.setColor(Color.WHITE);
        g2d.drawString(startText, (width - fmStart.stringWidth(startText)) / 2, 420);

        String controlsText = "CONTROLS: [W A S D] OR [ARROWS]";
        g2d.setFont(new Font("Consolas", Font.PLAIN, 16));
        FontMetrics fmControls = g2d.getFontMetrics();
        g2d.setColor(new Color(150, 150, 150));
        g2d.drawString(controlsText, (width - fmControls.stringWidth(controlsText)) / 2, 480);
    }

    private void drawGlowText(Graphics2D g2d, String text, int x, int y, Color neonColor) {
        g2d.setColor(new Color(neonColor.getRed(), neonColor.getGreen(), neonColor.getBlue(), 40));
        g2d.drawString(text, x - 3, y - 3);
        g2d.drawString(text, x + 3, y + 3);

        g2d.setColor(new Color(neonColor.getRed(), neonColor.getGreen(), neonColor.getBlue(), 80));
        g2d.drawString(text, x - 1, y - 1);
        g2d.drawString(text, x + 1, y + 1);

        g2d.setColor(new Color(200, 255, 200));
        g2d.drawString(text, x, y);
    }

    private void drawDecorativeSnake(Graphics2D g2d, int startX, int startY) {
        int size = 18;
        int gap = 2;

        g2d.setColor(new Color(0, 200, 100));
        for (int i = 0; i < 4; i++) {
            g2d.fillRect(startX + (size + gap) * i, startY, size, size);
        }

        int headX = startX + (size + gap) * 4;
        g2d.setColor(new Color(220, 255, 220));
        g2d.fillRect(headX, startY, size, size);

        g2d.setColor(Color.RED);
        g2d.fillRect(headX + 10, startY + 4, 4, 4);

        int foodX = startX + (size + gap) * 6 + 5;
        g2d.setColor(new Color(255, 50, 50));
        g2d.fillOval(foodX, startY, size, size);

        g2d.setColor(new Color(139, 69, 19));
        g2d.fillRect(foodX + size/2 - 1, startY - 4, 2, 6);
    }

    // ---------------------------------------------------------
    // 2. VẼ ĐỐI TƯỢNG GAME (PLAYING)
    // ---------------------------------------------------------
    private void drawGameObjects(Graphics2D g2d) {
        // Vẽ mồi
        if (currentModel.getFood() != null && currentModel.getFood().getPosition() != null) {
            Point f = currentModel.getFood().getPosition();
            if (imgFood != null) {
                g2d.drawImage(imgFood, f.x * 20, f.y * 20, 20, 20, null);
            } else {
                g2d.setColor(new Color(255, 50, 50));
                g2d.fillOval(f.x * 20 + 2, f.y * 20 + 2, 16, 16);
            }
        }

        // Vẽ rắn
        if (currentModel.getSnake() != null && currentModel.getSnake().getBody() != null) {
            java.util.List<Point> body = currentModel.getSnake().getBody();
            if (!body.isEmpty()) {
                for (int i = 1; i < body.size(); i++) {
                    Point p = body.get(i);
                    if (imgBody != null) {
                        g2d.drawImage(imgBody, p.x * 20, p.y * 20, 20, 20, null);
                    } else {
                        g2d.setColor(new Color(0, 200, 100));
                        g2d.fillRoundRect(p.x * 20 + 1, p.y * 20 + 1, 18, 18, 6, 6);
                    }
                }

                Point head = body.get(0);
                if (imgHead != null) {
                    drawRotatedHead(g2d, head, currentModel.getSnake().getDirection());
                } else {
                    g2d.setColor(new Color(0, 255, 150));
                    g2d.fillRoundRect(head.x * 20 + 1, head.y * 20 + 1, 18, 18, 6, 6);
                }
            }
        }

        // Vẽ điểm số
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.setFont(new Font("Consolas", Font.BOLD, 18));
        if (currentModel.getScoreManager() != null) {
            g2d.drawString("SCORE: " + currentModel.getScoreManager().getCurrentScore(), 20, 30);
        }
    }

    private void drawRotatedHead(Graphics2D g2d, Point headPoint, String direction) {
        AffineTransform oldAT = g2d.getTransform();
        g2d.translate(headPoint.x * 20 + 10, headPoint.y * 20 + 10);

        double angle = 0;
        if (direction != null) {
            switch (direction) {
                case "UP":    angle = -Math.PI / 2; break;
                case "DOWN":  angle = Math.PI / 2; break;
                case "LEFT":  angle = Math.PI; break;
                case "RIGHT": angle = 0; break;
            }
        }

        g2d.rotate(angle);
        g2d.drawImage(imgHead, -10, -10, 20, 20, null);
        g2d.setTransform(oldAT);
    }

    // ---------------------------------------------------------
    // 3. VẼ CHỮ PHỦ LÊN KHI PAUSE / GAME OVER
    // ---------------------------------------------------------
    private void drawOverlayText(Graphics2D g2d, String title, String subtitle, Color titleColor) {
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setColor(titleColor);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 50));
        FontMetrics fm = g2d.getFontMetrics();
        int titleX = (getWidth() - fm.stringWidth(title)) / 2;
        g2d.drawString(title, titleX, 280);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI", Font.ITALIC, 20));
        FontMetrics fmSub = g2d.getFontMetrics();
        int subX = (getWidth() - fmSub.stringWidth(subtitle)) / 2;
        g2d.drawString(subtitle, subX, 330);
    }
}