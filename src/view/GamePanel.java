package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import model.GameModel;
import model.GameState;
import model.Point;


public class GamePanel extends JPanel {
    private GameModel currentModel;
    private BufferedImage imgHead, imgBody, imgFood;

    private boolean isSoundOn = true;
    private boolean isNeonTheme = true;

    private Timer transitionTimer;
    private int transitionAlpha = 0;
    private boolean isFadingOut = false;
    private boolean isTransitioning = false;
    private GameState nextState = null;

    // Biến lưu trữ hành động khi bấm nút BẮT ĐẦU
    private Runnable onStartAction;

    public GamePanel() {
        setPreferredSize(new Dimension(800, 600));
        setDoubleBuffered(true);
        loadSprites();
        setupMouseListener();
    }

    public void updateModel(GameModel model) {
        this.currentModel = model;
    }

    // Hàm thiết lập hành động bắt đầu game từ Controller
    public void setOnStartAction(Runnable onStartAction) {
        this.onStartAction = onStartAction;
    }

    private void loadSprites() {
        try {
            imgHead = ImageIO.read(new File("head.png"));
            imgBody = ImageIO.read(new File("body.png"));
            imgFood = ImageIO.read(new File("food.png"));
        } catch (IOException e) {
            System.out.println("Không tìm thấy ảnh sprite.");
        }
    }

    private void setupMouseListener() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (currentModel == null || isTransitioning) return;

                int mx = e.getX();
                int my = e.getY();
                int w = getWidth();
                int h = getHeight();

                if (currentModel.getCurrentState() == GameState.MENU) {
                    Rectangle btnStart = new Rectangle(w / 2 - 140, 340, 280, 65);

                    int boxW = 200, boxH = 55, gap = 20;
                    int startX = (w - (boxW * 3 + gap * 2)) / 2;
                    int yBottom = h - 90;

                    int settingsX = startX + 2 * (boxW + gap);
                    Rectangle btnSettings = new Rectangle(settingsX, yBottom, boxW, boxH);

                    if (btnStart.contains(mx, my)) {
                        startTransition(GameState.PLAYING);
                    } else if (btnSettings.contains(mx, my)) {
                        startTransition(GameState.SETTINGS);
                    }
                }
                else if (currentModel.getCurrentState() == GameState.SETTINGS) {
                    Rectangle btnBack = new Rectangle(w / 2 - 100, h - 100, 200, 50);

                    int panelW = 400;
                    int panelX = (w - panelW) / 2;
                    int panelY = 180;
                    Rectangle btnSound = new Rectangle(panelX + 240, panelY + 45, 110, 35);
                    Rectangle btnTheme = new Rectangle(panelX + 240, panelY + 125, 110, 35);

                    if (btnBack.contains(mx, my)) {
                        startTransition(GameState.MENU);
                    } else if (btnSound.contains(mx, my)) {
                        isSoundOn = !isSoundOn;
                        repaint();
                    } else if (btnTheme.contains(mx, my)) {
                        isNeonTheme = !isNeonTheme;
                        repaint();
                    }
                }
            }
        });
    }
    private void startTransition(GameState targetState) {
        nextState = targetState;
        isTransitioning = true;
        isFadingOut = true; // Bắt đầu mờ dần sang đen
        transitionAlpha = 0;

        transitionTimer = new Timer(15, e -> {
            if (isFadingOut) {
                transitionAlpha += 20;
                if (transitionAlpha >= 255) {
                    transitionAlpha = 255;
                    isFadingOut = false; // Chuyển sang giai đoạn hiện rõ dần lên

                    // 1. KÍCH HOẠT GAME TRƯỚC (Khi trạng thái vẫn đang là MENU)
                    if (nextState == GameState.PLAYING && onStartAction != null) {
                        onStartAction.run();
                    }

                    // 2. SAU ĐÓ MỚI ĐỔI TRẠNG THÁI GIAO DIỆN
                    currentModel.setCurrentState(nextState);
                }
            } else {
                // Giai đoạn hiện rõ dần lên (Fade in)
                transitionAlpha -= 20;
                if (transitionAlpha <= 0) {
                    transitionAlpha = 0;
                    isTransitioning = false;
                    transitionTimer.stop();
                }
            }
            repaint();
        });
        transitionTimer.start();
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (currentModel == null) {
            drawLoading(g2d);
            return;
        }

        switch (currentModel.getCurrentState()) {
            case MENU:
                drawMenu(g2d);
                break;
            case SETTINGS:
                drawSettings(g2d);
                break;
            case PLAYING:
            case PAUSED:
            case GAME_OVER:
                drawGameWorld(g2d);
                break;
        }

        if (isTransitioning) {
            g2d.setColor(new Color(0, 0, 0, transitionAlpha));
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private void drawMenu(Graphics2D g2d) {
        int w = getWidth();
        int h = getHeight();

        drawBackground(g2d, w, h);
        drawTopBar(g2d, w);
        drawArcadeSilhouettes(g2d, w, h);
        drawTitleSection(g2d, w / 2, 230);
        drawButton(g2d, "BẮT ĐẦU", w / 2 - 140, 340, 280, 65);

        g2d.setFont(new Font("Consolas", Font.BOLD, 15));
        g2d.setColor(new Color(100, 200, 255, 180));
        String hint = "- NHẤN [ ENTER ] ĐỂ CHƠI -";
        FontMetrics fmHint = g2d.getFontMetrics();
        g2d.drawString(hint, (w - fmHint.stringWidth(hint)) / 2, 440);

        drawBottomPanels(g2d, w, h);
    }

    private void drawSettings(Graphics2D g2d) {
        int w = getWidth();
        int h = getHeight();

        drawBackground(g2d, w, h);
        drawText(g2d, "CÀI ĐẶT", w / 2, 120, 50, new Color(200, 100, 255));

        int panelW = 400;
        int panelH = 250;
        int panelX = (w - panelW) / 2;
        int panelY = 180;

        g2d.setColor(new Color(20, 30, 50, 200));
        g2d.fillRoundRect(panelX, panelY, panelW, panelH, 20, 20);
        g2d.setColor(new Color(200, 100, 255, 100));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(panelX, panelY, panelW, panelH, 20, 20);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 20));
        g2d.drawString("ÂM THANH", panelX + 40, panelY + 70);
        drawToggleButton(g2d, isSoundOn ? "BẬT" : "TẮT", panelX + 240, panelY + 45, isSoundOn);

        g2d.setColor(Color.WHITE);
        g2d.drawString("GIAO DIỆN", panelX + 40, panelY + 150);
        drawToggleButton(g2d, isNeonTheme ? "NEON" : "CỔ ĐIỂN", panelX + 240, panelY + 125, isNeonTheme);

        g2d.setColor(Color.WHITE);
        g2d.drawString("TỐC ĐỘ", panelX + 40, panelY + 220);
        g2d.setColor(new Color(100, 100, 100));
        g2d.fillRoundRect(panelX + 180, panelY + 210, 160, 10, 5, 5);
        g2d.setColor(new Color(0, 255, 200));
        g2d.fillRoundRect(panelX + 180, panelY + 210, 100, 10, 5, 5);
        g2d.fillOval(panelX + 180 + 90, panelY + 205, 20, 20);

        drawButton(g2d, "QUAY LẠI", w / 2 - 100, h - 100, 200, 50);
    }

    private void drawToggleButton(Graphics2D g2d, String text, int x, int y, boolean isActive) {
        Color mainColor = isActive ? new Color(0, 255, 150) : new Color(255, 50, 100);

        g2d.setColor(new Color(mainColor.getRed(), mainColor.getGreen(), mainColor.getBlue(), 50));
        g2d.fillRoundRect(x, y, 110, 35, 15, 15);
        g2d.setColor(mainColor);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(x, y, 110, 35, 15, 15);

        g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(text, x + (110 - fm.stringWidth(text)) / 2, y + 23);
    }

    private void drawBackground(Graphics2D g2d, int w, int h) {
        RadialGradientPaint rgp = new RadialGradientPaint(
                new Point2D.Float(w / 2, h / 2), w / 1.1f,
                new float[]{0.0f, 1.0f},
                new Color[]{new Color(15, 25, 45), new Color(2, 5, 15)}
        );
        g2d.setPaint(rgp);
        g2d.fillRect(0, 0, w, h);

        g2d.setColor(new Color(0, 255, 255, 15));
        for (int i = 0; i < w; i += 35) g2d.drawLine(i, 0, i, h);
        for (int i = 0; i < h; i += 35) g2d.drawLine(0, i, w, i);
    }

    private void drawTopBar(Graphics2D g2d, int w) {
        g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2d.setColor(new Color(0, 200, 255));
        g2d.drawString("MÀN 1", 30, 30);
        String score = "ĐIỂM CAO: 145,200";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(score, (w - fm.stringWidth(score)) / 2, 30);
        String coins = "XU: 1,500";
        g2d.drawString(coins, w - fm.stringWidth(coins) - 30, 30);
        g2d.setColor(new Color(0, 255, 255, 50));
        g2d.drawLine(0, 45, w, 45);
    }

    private void drawArcadeSilhouettes(Graphics2D g2d, int w, int h) {
        g2d.setColor(new Color(20, 20, 50, 60));
        int[] xL = {0, 60, 60, 100, 100, 0};
        int[] yL = {180, 180, 280, 320, h, h};
        g2d.fillPolygon(xL, yL, 6);
        int[] xR = {w, w - 60, w - 60, w - 100, w - 100, w};
        int[] yR = {180, 180, 280, 320, h, h};
        g2d.fillPolygon(xR, yR, 6);
    }

    private void drawTitleSection(Graphics2D g2d, int cx, int cy) {
        drawText(g2d, "SNAKE", cx - 160, cy, 75, new Color(50, 255, 100));
        drawText(g2d, "GAME", cx + 160, cy, 75, new Color(255, 30, 50));
    }

    private void drawBottomPanels(Graphics2D g2d, int w, int h) {
        int boxW = 200, boxH = 55, gap = 20;
        int startX = (w - (boxW * 3 + gap * 2)) / 2;
        int y = h - 90;
        String[] titles = {"CHẾ ĐỘ CHƠI", "ĐIỂM CAO", "CÀI ĐẶT"};
        String[] subs = {"CỔ ĐIỂN | SINH TỒN", "BẢNG XẾP HẠNG", "ÂM THANH | GIAO DIỆN"};

        for (int i = 0; i < 3; i++) {
            int bx = startX + i * (boxW + gap);
            g2d.setColor(new Color(0, 150, 255, 40));
            g2d.fillRoundRect(bx - 3, y - 3, boxW + 6, boxH + 6, 15, 15);
            g2d.setColor(new Color(0, 150, 255));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(bx, y, boxW, boxH, 12, 12);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 13));
            FontMetrics fmT = g2d.getFontMetrics();
            g2d.drawString(titles[i], bx + (boxW - fmT.stringWidth(titles[i])) / 2, y + 22);

            g2d.setColor(new Color(150, 200, 255));
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 10));
            FontMetrics fmS = g2d.getFontMetrics();
            g2d.drawString(subs[i], bx + (boxW - fmS.stringWidth(subs[i])) / 2, y + 40);
        }
    }

    private void drawText(Graphics2D g2d, String text, int cx, int y, int size, Color color) {
        g2d.setFont(new Font("Arial", Font.BOLD, size));
        FontMetrics fm = g2d.getFontMetrics();
        int tx = cx - fm.stringWidth(text) / 2;
        for (int i = 1; i <= 5; i++) {
            g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 40));
            g2d.drawString(text, tx - i, y);
            g2d.drawString(text, tx + i, y);
            g2d.drawString(text, tx, y - i);
            g2d.drawString(text, tx, y + i);
        }
        g2d.setColor(color);
        g2d.drawString(text, tx, y);
        g2d.setColor(new Color(255, 255, 255, 100));
        g2d.drawString(text, tx, y);
    }
    private void drawButton(Graphics2D g2d, String text, int x, int y, int w, int h) {
        g2d.setColor(new Color(0, 255, 200, 30));
        g2d.fillRoundRect(x - 6, y - 6, w + 12, h + 12, 25, 25);
        g2d.setColor(new Color(10, 30, 40));
        g2d.fillRoundRect(x, y, w, h, 20, 20);
        g2d.setColor(new Color(0, 255, 200));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(x, y, w, h, 20, 20);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(x + 5, y + 5, w - 10, h - 10, 15, 15);

        int fontSize = (h < 60) ? 22 : 30;
        g2d.setFont(new Font("SansSerif", Font.BOLD, fontSize));
        FontMetrics fm = g2d.getFontMetrics();
        int textX = x + (w - fm.stringWidth(text)) / 2;
        int textY = y + ((h - fm.getHeight()) / 2) + fm.getAscent();

        g2d.setColor(new Color(220, 255, 255));
        g2d.drawString(text, textX, textY);
    }

    private void drawGameWorld(Graphics2D g2d) {
        g2d.setColor(new Color(10, 10, 15));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.setColor(new Color(255, 255, 255, 10));
        for (int i = 0; i < getWidth(); i += 20) g2d.drawLine(i, 0, i, getHeight());
        for (int i = 0; i < getHeight(); i += 20) g2d.drawLine(0, i, getWidth(), i);

        if(currentModel.getFood() != null && currentModel.getFood().getPosition() != null) {
            Point food = currentModel.getFood().getPosition();
            if (imgFood != null) {
                g2d.drawImage(imgFood, food.x * 20, food.y * 20, 20, 20, null);
            } else {
                g2d.setColor(new Color(255, 50, 100));
                g2d.fillOval(food.x * 20 + 2, food.y * 20 + 2, 16, 16);
            }
        }

        if (currentModel.getSnake() != null && currentModel.getSnake().getBody() != null) {
            java.util.List<Point> body = currentModel.getSnake().getBody();
            for (int i = 0; i < body.size(); i++) {
                Point p = body.get(i);
                if (i == 0) {
                    g2d.setColor(new Color(0, 255, 150));
                    g2d.fillRoundRect(p.x * 20, p.y * 20, 20, 20, 8, 8);
                } else {
                    g2d.setColor(new Color(0, 180, 120));
                    g2d.fillRoundRect(p.x * 20 + 2, p.y * 20 + 2, 16, 16, 6, 6);
                }
            }
        }

        if (currentModel.getCurrentState() == GameState.PAUSED) {
            drawOverlay(g2d, "TẠM DỪNG", Color.ORANGE);
        } else if (currentModel.getCurrentState() == GameState.GAME_OVER) {
            drawOverlay(g2d, "KẾT THÚC", Color.RED);
        }
    }

    private void drawOverlay(Graphics2D g2d, String msg, Color color) {
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        drawText(g2d, msg, getWidth()/2, getHeight()/2, 60, color);
    }

    private void drawLoading(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Consolas", Font.BOLD, 20));
        FontMetrics fm = g2d.getFontMetrics();
        String msg = "ĐANG TẢI HỆ THỐNG...";
        g2d.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2);
    }
}