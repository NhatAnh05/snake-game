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
	private static final int CELL_SIZE = 20;
	private static final int BOARD_COLS = 40;
	private static final int BOARD_ROWS = 30;

	private static final int GAME_AREA_WIDTH = BOARD_COLS * CELL_SIZE; // 800
	private static final int GAME_AREA_HEIGHT = BOARD_ROWS * CELL_SIZE; // 600
	private static final int SIDEBAR_WIDTH = 340;

	private static final int PANEL_WIDTH = GAME_AREA_WIDTH + SIDEBAR_WIDTH; // 1140
	private static final int PANEL_HEIGHT = GAME_AREA_HEIGHT; // 600

	private GameModel currentModel;
	private BufferedImage imgHead, imgBody, imgFood;

	private boolean isSoundOn = true;
	private boolean isNeonTheme = true;

	private Timer transitionTimer;
	private int transitionAlpha = 0;
	private boolean isFadingOut = false;
	private boolean isTransitioning = false;
	private GameState nextState = null;

	private Runnable onStartAction;
	
	private String modeFeedbackText = null;
	private long modeFeedbackUntil = 0L;

	public GamePanel() {
		setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
	    setDoubleBuffered(true);

	    setFocusable(true);
	    requestFocusInWindow();

	    loadSprites();
	    setupMouseListener();
	}

	public void updateModel(GameModel model) {
		this.currentModel = model;
	}

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
				if (currentModel == null || isTransitioning) {
					return;
				}

				requestFocusInWindow();

				int mx = e.getX();
				int my = e.getY();
				int w = getWidth();
				int h = getHeight();

				// ================= MENU =================

				if (currentModel.getCurrentState() == GameState.MENU) {
					Rectangle btnStart = new Rectangle(w / 2 - 140, 340, 280, 65);

					int boxW = 200;
					int boxH = 55;
					int gap = 20;
					int startX = (w - (boxW * 3 + gap * 2)) / 2;
					int yBottom = h - 90;
					
					Rectangle btnMode = new Rectangle(startX, yBottom, boxW, boxH);

					int settingsX = startX + 2 * (boxW + gap);
					Rectangle btnSettings = new Rectangle(settingsX, yBottom, boxW, boxH);

					if (btnMode.contains(mx, my)) {
					    if (currentModel.getCurrentMode() == model.GameMode.CLASSIC) {
					        currentModel.setCurrentMode(model.GameMode.SURVIVAL);
					        showModeFeedback("Đã chọn: SURVIVAL");
					    } else {
					        currentModel.setCurrentMode(model.GameMode.CLASSIC);
					        showModeFeedback("Đã chọn: CLASSIC");
					    }

					    Toolkit.getDefaultToolkit().beep();
					    repaint();
					} else if (btnStart.contains(mx, my)) {
					    startTransition(GameState.PLAYING);
					} else if (btnSettings.contains(mx, my)) {
					    startTransition(GameState.SETTINGS);
					}
				}

				// ================= SETTINGS =================

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
		isFadingOut = true;
		transitionAlpha = 0;

		transitionTimer = new Timer(15, e -> {
			if (isFadingOut) {
				transitionAlpha += 20;

				if (transitionAlpha >= 255) {
					transitionAlpha = 255;
					isFadingOut = false;

					if (nextState == GameState.PLAYING && onStartAction != null) {
						onStartAction.run();
					}

					currentModel.setCurrentState(nextState);
				}
			} else {
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
		drawArcadeSilhouettes(g2d, w, h);
		drawTitleSection(g2d, w / 2, 230);
		drawButton(g2d, "BẮT ĐẦU", w / 2 - 140, 340, 280, 65);

		g2d.setFont(new Font("Consolas", Font.BOLD, 15));
		g2d.setColor(new Color(100, 200, 255, 180));

		String hint = "- NHẤN [ ENTER ] ĐỂ CHƠI -";
		FontMetrics fmHint = g2d.getFontMetrics();

		g2d.drawString(hint, (w - fmHint.stringWidth(hint)) / 2, 440);

		drawBottomPanels(g2d, w, h);
		drawModeFeedback(g2d, w, h);
	}

	// =====================================================
	// SETTINGS
	// =====================================================
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
		RadialGradientPaint rgp = new RadialGradientPaint(new Point2D.Float(w / 2, h / 2), w / 1.1f,
				new float[] { 0.0f, 1.0f }, new Color[] { new Color(15, 25, 45), new Color(2, 5, 15) });

		g2d.setPaint(rgp);
		g2d.fillRect(0, 0, w, h);

		g2d.setColor(new Color(0, 255, 255, 15));

		for (int i = 0; i < w; i += 35) {
			g2d.drawLine(i, 0, i, h);
		}

		for (int i = 0; i < h; i += 35) {
			g2d.drawLine(0, i, w, i);
		}
	}

	private void drawArcadeSilhouettes(Graphics2D g2d, int w, int h) {
		g2d.setColor(new Color(20, 20, 50, 60));

		int[] xL = { 0, 60, 60, 100, 100, 0 };
		int[] yL = { 180, 180, 280, 320, h, h };
		g2d.fillPolygon(xL, yL, 6);

		int[] xR = { w, w - 60, w - 60, w - 100, w - 100, w };
		int[] yR = { 180, 180, 280, 320, h, h };
		g2d.fillPolygon(xR, yR, 6);
	}

	private void drawTitleSection(Graphics2D g2d, int cx, int cy) {
		drawText(g2d, "SNAKE", cx - 160, cy, 75, new Color(50, 255, 100));
		drawText(g2d, "GAME", cx + 160, cy, 75, new Color(255, 30, 50));
	}

	private void drawBottomPanels(Graphics2D g2d, int w, int h) {

	    int boxW = 200;
	    int boxH = 55;
	    int gap = 20;

	    int startX = (w - (boxW * 3 + gap * 2)) / 2;
	    int y = h - 90;

	    String[] titles = {
	            "CHẾ ĐỘ CHƠI",
	            "ĐIỂM CAO",
	            "CÀI ĐẶT"
	    };

	    int highScore = 0;

	    if (currentModel != null &&
	            currentModel.getScoreManager() != null) {

	        highScore =
	                currentModel.getScoreManager().getHighScore();
	    }

	    // =========================
	    // MODE TEXT ĐỘNG
	    // =========================
	    String modeText = "CLASSIC";
	    Color modeColor = new Color(80, 255, 120);

	    if (currentModel != null &&
	            currentModel.getCurrentMode() != null) {

	        if (currentModel.getCurrentMode() == model.GameMode.SURVIVAL) {
	            modeText = "SURVIVAL";
	            modeColor = new Color(255, 80, 80);
	        }
	    }

	    String[] subs = {
	            modeText,
	            String.valueOf(highScore),
	            "ÂM THANH | GIAO DIỆN"
	    };

	    for (int i = 0; i < 3; i++) {
	        int bx = startX + i * (boxW + gap);
	        
	        if (i == 0) {
	            Color activeGlow = modeText.equals("SURVIVAL")
	                    ? new Color(255, 80, 80, 70)
	                    : new Color(80, 255, 120, 70);

	            g2d.setColor(activeGlow);
	            g2d.fillRoundRect(bx - 8, y - 8, boxW + 16, boxH + 16, 18, 18);
	        }
	        
	        // Glow
	        g2d.setColor(new Color(0, 150, 255, 40));
	        g2d.fillRoundRect(
	                bx - 3,
	                y - 3,
	                boxW + 6,
	                boxH + 6,
	                15,
	                15
	        );

	        // Box
	        g2d.setColor(new Color(0, 150, 255));
	        g2d.setStroke(new BasicStroke(2));

	        g2d.drawRoundRect(
	                bx,
	                y,
	                boxW,
	                boxH,
	                12,
	                12
	        );

	        // TITLE
	        g2d.setColor(Color.WHITE);
	        g2d.setFont(new Font("SansSerif", Font.BOLD, 13));

	        FontMetrics fmT = g2d.getFontMetrics();

	        g2d.drawString(
	                titles[i],
	                bx + (boxW - fmT.stringWidth(titles[i])) / 2,
	                y + 22
	        );

	        if (i == 0) {
	            g2d.setColor(modeColor);
	        } else {
	            g2d.setColor(new Color(150, 200, 255));
	        }

	        g2d.setFont(new Font("SansSerif", Font.PLAIN, 10));

	        FontMetrics fmS = g2d.getFontMetrics();

	        g2d.drawString(
	                subs[i],
	                bx + (boxW - fmS.stringWidth(subs[i])) / 2,
	                y + 40
	        );
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
		// Chọn theme
		if (isNeonTheme) {
			drawNeonTheme(g2d);
		} else {
			drawClassicTheme(g2d);
		}

		// Sidebar
		drawRightSidebar(g2d);

		// Overlay trạng thái
		if (currentModel.getCurrentState() == GameState.PAUSED) {
			drawPauseOverlay(g2d);
		} else if (currentModel.getCurrentState() == GameState.GAME_OVER) {
			drawGameOverOverlay(g2d);
		}
	}

	// ================================
	// THEME NEON
	// ================================
	private void drawNeonTheme(Graphics2D g2d) {
		// Background tổng
		g2d.setColor(new Color(8, 8, 12));
		g2d.fillRect(0, 0, getWidth(), getHeight());

		// Khu vực game
		g2d.setColor(new Color(10, 10, 15));
		g2d.fillRect(0, 0, GAME_AREA_WIDTH, GAME_AREA_HEIGHT);

		// Grid neon
		g2d.setColor(new Color(255, 255, 255, 10));

		for (int i = 0; i < GAME_AREA_WIDTH; i += CELL_SIZE) {
			g2d.drawLine(i, 0, i, GAME_AREA_HEIGHT);
		}

		for (int i = 0; i < GAME_AREA_HEIGHT; i += CELL_SIZE) {
			g2d.drawLine(0, i, GAME_AREA_WIDTH, i);
		}

		// Viền
		g2d.setColor(new Color(0, 255, 200, 90));
		g2d.setStroke(new BasicStroke(2));
		g2d.drawLine(GAME_AREA_WIDTH, 0, GAME_AREA_WIDTH, GAME_AREA_HEIGHT);
		
		// FOOD
		if (currentModel.getFood() != null && currentModel.getFood().getPosition() != null) {
			Point food = currentModel.getFood().getPosition();
			if (imgFood != null) {
				g2d.drawImage(imgFood, food.x * CELL_SIZE, food.y * CELL_SIZE, CELL_SIZE, CELL_SIZE, null);
			} else {
				g2d.setColor(new Color(255, 50, 100));
				g2d.fillOval(food.x * CELL_SIZE + 2, food.y * CELL_SIZE + 2, 16, 16);
			}
		}
		
		// SNAKE
		if (currentModel.getSnake() != null && currentModel.getSnake().getBody() != null) {
			java.util.List<Point> body = currentModel.getSnake().getBody();
			for (int i = 0; i < body.size(); i++) {
				Point p = body.get(i);
				if (i == 0) {
					// Head
					if (imgHead != null) {
						g2d.drawImage(imgHead, p.x * CELL_SIZE, p.y * CELL_SIZE, CELL_SIZE, CELL_SIZE, null);
					} else {
						g2d.setColor(new Color(0, 255, 150));
						g2d.fillRoundRect(p.x * CELL_SIZE, p.y * CELL_SIZE, CELL_SIZE, CELL_SIZE, 8, 8);
					}
				} else {
					// Body
					if (imgBody != null) {
						g2d.drawImage(imgBody, p.x * CELL_SIZE, p.y * CELL_SIZE, CELL_SIZE, CELL_SIZE, null);
					} else {
						g2d.setColor(new Color(0, 180, 120));
						g2d.fillRoundRect(p.x * CELL_SIZE + 2, p.y * CELL_SIZE + 2, 16, 16, 6, 6);
					}
				}
			}
		}
	}
	
	
	// ================================
	// THEME CỔ ĐIỂN
	// ================================
	private void drawClassicTheme(Graphics2D g2d) {
	    // Nền tổng
	    g2d.setColor(new Color(12, 12, 12));
	    g2d.fillRect(0, 0, getWidth(), getHeight());

	    // Khu vực game
	    g2d.setColor(new Color(0, 0, 0));
	    g2d.fillRect(0, 0, GAME_AREA_WIDTH, GAME_AREA_HEIGHT);

	    // Lưới rất nhẹ hoặc có thể bỏ hẳn
	    g2d.setColor(new Color(35, 35, 35));
	    for (int i = 0; i < GAME_AREA_WIDTH; i += CELL_SIZE) {
	        g2d.drawLine(i, 0, i, GAME_AREA_HEIGHT);
	    }
	    for (int i = 0; i < GAME_AREA_HEIGHT; i += CELL_SIZE) {
	        g2d.drawLine(0, i, GAME_AREA_WIDTH, i);
	    }

	    // Viền kiểu cổ điển
	    g2d.setColor(new Color(180, 180, 180));
	    g2d.setStroke(new BasicStroke(2));
	    g2d.drawRect(0, 0, GAME_AREA_WIDTH - 1, GAME_AREA_HEIGHT - 1);

	    // Food: đỏ đơn giản
	    if (currentModel.getFood() != null && currentModel.getFood().getPosition() != null) {
	        Point food = currentModel.getFood().getPosition();
	        g2d.setColor(new Color(220, 40, 40));
	        g2d.fillRect(food.x * CELL_SIZE + 4, food.y * CELL_SIZE + 4, 12, 12);
	    }

	    // Snake: vuông, retro
	    if (currentModel.getSnake() != null && currentModel.getSnake().getBody() != null) {
	        java.util.List<Point> body = currentModel.getSnake().getBody();

	        for (int i = 0; i < body.size(); i++) {
	            Point p = body.get(i);

	            if (i == 0) {
	                g2d.setColor(new Color(80, 255, 80));   // đầu sáng hơn
	            } else {
	                g2d.setColor(new Color(30, 170, 30));   // thân tối hơn
	            }

	            g2d.fillRect(
	                    p.x * CELL_SIZE,
	                    p.y * CELL_SIZE,
	                    CELL_SIZE,
	                    CELL_SIZE
	            );
	        }
	    }
	}

	private void drawRightSidebar(Graphics2D g2d) {
		int x = GAME_AREA_WIDTH;
		int w = SIDEBAR_WIDTH;
		int h = GAME_AREA_HEIGHT;

		// Nền sidebar
		g2d.setColor(new Color(16, 20, 28));
		g2d.fillRect(x, 0, w, h);

		// Viền trái
		g2d.setColor(new Color(0, 255, 200, 120));
		g2d.setStroke(new BasicStroke(2));
		g2d.drawLine(x, 0, x, h);

		int currentScore = 0;
		int highScore = 0;
		String stateText = "N/A";

		if (currentModel != null && currentModel.getScoreManager() != null) {
			currentScore = currentModel.getScoreManager().getCurrentScore();
			highScore = currentModel.getScoreManager().getHighScore();
		}

		if (currentModel != null) {
			switch (currentModel.getCurrentState()) {
			case PLAYING:
				stateText = "Đang chơi";
				break;
			case PAUSED:
				stateText = "Tạm dừng";
				break;
			case GAME_OVER:
				stateText = "Kết thúc";
				break;
			case MENU:
				stateText = "Menu";
				break;
			case SETTINGS:
				stateText = "Cài đặt";
				break;
			}
		}

		// Tiêu đề chính
		g2d.setFont(new Font("SansSerif", Font.BOLD, 24));
		g2d.setColor(new Color(0, 255, 200));
		drawCenteredString(g2d, "THÔNG TIN GAME", x + w / 2, 40);

		int cardX = x + 16;
		int cardW = w - 32;

		// =========================
		// CARD 1: THÔNG TIN
		// =========================
		int infoY = 68;
		int infoH = 145;
		drawSidebarSection(g2d, "THÔNG TIN", cardX, infoY, cardW, infoH);

		int valueRightX = cardX + cardW - 22;

		drawInfoRow(g2d, "Trạng thái", stateText, cardX + 18, infoY + 56, valueRightX, new Color(130, 230, 255));

		drawInfoRow(g2d, "Điểm cao", String.valueOf(highScore), cardX + 18, infoY + 91, valueRightX,
				new Color(255, 220, 80));

		drawInfoRow(g2d, "Điểm hiện tại", String.valueOf(currentScore), cardX + 18, infoY + 126, valueRightX,
				Color.WHITE);

		// =========================
		// CARD 2: ĐIỀU KHIỂN
		// =========================
		int controlY = 228;
		int controlH = 165;
		drawSidebarSection(g2d, "ĐIỀU KHIỂN", cardX, controlY, cardW, controlH);

		int keySize = 34;
		int gap = 10;
		int clusterWidth = keySize * 3 + gap * 2;
		int clusterX = x + (w - clusterWidth) / 2;
		int clusterY = controlY + 48;

		drawKeyBox(g2d, "↑", clusterX + keySize + gap, clusterY, keySize, keySize);
		drawKeyBox(g2d, "←", clusterX, clusterY + keySize + gap, keySize, keySize);
		drawKeyBox(g2d, "↓", clusterX + keySize + gap, clusterY + keySize + gap, keySize, keySize);
		drawKeyBox(g2d, "→", clusterX + (keySize + gap) * 2, clusterY + keySize + gap, keySize, keySize);

		// Đẩy dòng này xuống dưới cụm phím, tránh bị đè
		g2d.setFont(new Font("SansSerif", Font.PLAIN, 14));
		g2d.setColor(new Color(210, 220, 235));
		drawCenteredString(g2d, "Hoặc dùng W / A / S / D", x + w / 2, controlY + 145);

		// =========================
		// CARD 3: HƯỚNG DẪN
		// =========================
		int guideY = 410;
		int guideH = 180;
		drawSidebarSection(g2d, "HƯỚNG DẪN", cardX, guideY, cardW, guideH);

		g2d.setFont(new Font("SansSerif", Font.PLAIN, 14));
		g2d.setColor(Color.WHITE);

		// Dòng đầu tiên phải nằm dưới divider, không được trùng y + 38
		int lineX = cardX + 16;
		int lineY = guideY + 62;
		int lineGap = 28;

		g2d.drawString("• ENTER : Bắt đầu / chơi lại", lineX, lineY);
		g2d.drawString("• P      : Tạm dừng / tiếp tục", lineX, lineY + lineGap);
		g2d.drawString("• ESC    : Về menu chính", lineX, lineY + lineGap * 2);
		g2d.drawString("• Ăn thức ăn để tăng 10 điểm", lineX, lineY + lineGap * 3);
		g2d.drawString("• Tránh tường và thân rắn", lineX, lineY + lineGap * 4);
	}

	private void drawInfoRow(Graphics2D g2d, String label, String value, int x, int y, int valueRightX,
			Color valueColor) {
		g2d.setFont(new Font("SansSerif", Font.BOLD, 15));
		g2d.setColor(new Color(220, 225, 235));
		g2d.drawString(label, x, y);

		g2d.setFont(new Font("Consolas", Font.BOLD, 15));
		g2d.setColor(valueColor);

		FontMetrics fm = g2d.getFontMetrics();
		int valueX = valueRightX - fm.stringWidth(value);

		g2d.drawString(value, valueX, y);
	}

	private void drawSidebarSection(Graphics2D g2d, String title, int x, int y, int w, int h) {
		g2d.setColor(new Color(0, 0, 0, 90));
		g2d.fillRoundRect(x + 3, y + 4, w, h, 18, 18);

		g2d.setColor(new Color(24, 30, 40));
		g2d.fillRoundRect(x, y, w, h, 18, 18);

		g2d.setColor(new Color(0, 255, 200, 120));
		g2d.setStroke(new BasicStroke(2));
		g2d.drawRoundRect(x, y, w, h, 18, 18);

		g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
		g2d.setColor(new Color(0, 255, 200));
		g2d.drawString(title, x + 14, y + 24);

		g2d.setColor(new Color(255, 255, 255, 35));
		g2d.drawLine(x + 14, y + 38, x + w - 14, y + 38);
	}

	private void drawKeyBox(Graphics2D g2d, String text, int x, int y, int w, int h) {
		g2d.setColor(new Color(36, 44, 58));
		g2d.fillRoundRect(x, y, w, h, 10, 10);

		g2d.setColor(new Color(0, 255, 200, 140));
		g2d.setStroke(new BasicStroke(2));
		g2d.drawRoundRect(x, y, w, h, 10, 10);

		g2d.setFont(new Font("SansSerif", Font.BOLD, 18));
		g2d.setColor(Color.WHITE);

		FontMetrics fm = g2d.getFontMetrics();

		int tx = x + (w - fm.stringWidth(text)) / 2;
		int ty = y + ((h - fm.getHeight()) / 2) + fm.getAscent();

		g2d.drawString(text, tx, ty);
	}

	private void drawPauseOverlay(Graphics2D g2d) {
		g2d.setColor(new Color(0, 0, 0, 180));
		g2d.fillRect(0, 0, GAME_AREA_WIDTH, GAME_AREA_HEIGHT);

		drawText(g2d, "TẠM DỪNG", GAME_AREA_WIDTH / 2, GAME_AREA_HEIGHT / 2 - 80, 60, Color.ORANGE);

		g2d.setFont(new Font("SansSerif", Font.BOLD, 22));
		g2d.setColor(Color.WHITE);

		drawCenteredString(g2d, "Nhấn [ P ] để tiếp tục", GAME_AREA_WIDTH / 2, GAME_AREA_HEIGHT / 2 + 10);

		drawCenteredString(g2d, "Nhấn [ ESC ] để về Menu chính", GAME_AREA_WIDTH / 2, GAME_AREA_HEIGHT / 2 + 50);
	}

	private void drawGameOverOverlay(Graphics2D g2d) {
		g2d.setColor(new Color(0, 0, 0, 185));
		g2d.fillRect(0, 0, GAME_AREA_WIDTH, GAME_AREA_HEIGHT);

		drawText(g2d, "KẾT THÚC", GAME_AREA_WIDTH / 2, GAME_AREA_HEIGHT / 2 - 120, 60, Color.RED);

		int currentScore = 0;
		int highScore = 0;

		if (currentModel != null && currentModel.getScoreManager() != null) {
			currentScore = currentModel.getScoreManager().getCurrentScore();
			highScore = currentModel.getScoreManager().getHighScore();
		}

		g2d.setFont(new Font("Consolas", Font.BOLD, 24));

		g2d.setColor(Color.WHITE);
		drawCenteredString(g2d, "SCORE: " + currentScore, GAME_AREA_WIDTH / 2, GAME_AREA_HEIGHT / 2 - 35);

		g2d.setColor(new Color(255, 220, 80));
		drawCenteredString(g2d, "HIGH SCORE: " + highScore, GAME_AREA_WIDTH / 2, GAME_AREA_HEIGHT / 2);

		g2d.setFont(new Font("SansSerif", Font.PLAIN, 20));
		g2d.setColor(Color.WHITE);

		drawCenteredString(g2d, "Nhấn [ ENTER ] để chơi lại", GAME_AREA_WIDTH / 2, GAME_AREA_HEIGHT / 2 + 60);

		drawCenteredString(g2d, "Nhấn [ ESC ] để về Menu chính", GAME_AREA_WIDTH / 2, GAME_AREA_HEIGHT / 2 + 95);
	}

	private void drawCenteredString(Graphics2D g2d, String text, int centerX, int y) {
		FontMetrics fm = g2d.getFontMetrics();
		int x = centerX - fm.stringWidth(text) / 2;

		g2d.drawString(text, x, y);
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
	
	private void showModeFeedback(String text) {
	    modeFeedbackText = text;
	    modeFeedbackUntil = System.currentTimeMillis() + 1200;
	    repaint();
	}
	
	private void drawModeFeedback(Graphics2D g2d, int w, int h) {
	    if (modeFeedbackText == null || System.currentTimeMillis() > modeFeedbackUntil) {
	        return;
	    }

	    g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
	    FontMetrics fm = g2d.getFontMetrics();

	    int padX = 16;
	    int padY = 10;
	    int boxW = fm.stringWidth(modeFeedbackText) + padX * 2;
	    int boxH = 34;

	    int x = (w - boxW) / 2;
	    int y = 280;

	    g2d.setColor(new Color(0, 0, 0, 160));
	    g2d.fillRoundRect(x, y, boxW, boxH, 16, 16);

	    g2d.setColor(new Color(0, 255, 200));
	    g2d.setStroke(new BasicStroke(2));
	    g2d.drawRoundRect(x, y, boxW, boxH, 16, 16);

	    g2d.setColor(Color.WHITE);
	    g2d.drawString(modeFeedbackText, x + padX, y + 22);
	}
}
