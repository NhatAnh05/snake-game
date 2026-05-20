package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import model.DifficultyLevel;
import model.GameMode;
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

	// UI-01: Các lựa chọn trên Main Menu để hỗ trợ điều hướng bằng bàn phím.
	private static final int MENU_OPTION_START = 0;
	private static final int MENU_OPTION_MODE = 1;
	private static final int MENU_OPTION_DIFFICULTY = 2;
	private static final int MENU_OPTION_SETTINGS = 3;
	private static final int MENU_OPTION_COUNT = 4;

	// UI-01: Các lựa chọn trong màn hình Settings.
	private static final int SETTINGS_OPTION_SOUND = 0;
	private static final int SETTINGS_OPTION_THEME = 1;
	private static final int SETTINGS_OPTION_BACK = 2;
	private static final int SETTINGS_OPTION_COUNT = 3;

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

	// UI-01: Lưu vị trí đang được chọn để người chơi dùng phím điều hướng trong menu.
	private int selectedMenuOption = MENU_OPTION_START;
	private int selectedSettingsOption = SETTINGS_OPTION_SOUND;
	
	private String modeFeedbackText = null;
	private long modeFeedbackUntil = 0L;

	public GamePanel() {
		setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
	    setDoubleBuffered(true);

	    setFocusable(true);
	    requestFocusInWindow();

	    loadSprites();
	    setupMouseListener();
	    setupKeyboardActions();
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

	// UI-01: Thiết lập điều hướng menu bằng bàn phím cho phần giao diện.
	// Chỉ xử lý khi game đang ở MENU hoặc SETTINGS để không ảnh hưởng gameplay.
	private void setupKeyboardActions() {
		InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = getActionMap();

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "ui01-left");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "ui01-up");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "ui01-right");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "ui01-down");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "ui01-confirm");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ui01-escape");

		actionMap.put("ui01-left", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				handleUiNavigation(-1);
			}
		});

		actionMap.put("ui01-up", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				handleUiNavigation(-1);
			}
		});

		actionMap.put("ui01-right", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				handleUiNavigation(1);
			}
		});

		actionMap.put("ui01-down", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				handleUiNavigation(1);
			}
		});

		actionMap.put("ui01-confirm", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				handleUiConfirm();
			}
		});

		actionMap.put("ui01-escape", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentModel != null && currentModel.getCurrentState() == GameState.SETTINGS) {
					startTransition(GameState.MENU);
				}
			}
		});
	}

	// UI-01: Di chuyển vùng chọn trên Main Menu hoặc Settings.
	private void handleUiNavigation(int step) {
		if (currentModel == null || isTransitioning) {
			return;
		}

		if (currentModel.getCurrentState() == GameState.MENU) {
			selectedMenuOption = wrapSelection(selectedMenuOption + step, MENU_OPTION_COUNT);
			repaint();
		} else if (currentModel.getCurrentState() == GameState.SETTINGS) {
			selectedSettingsOption = wrapSelection(selectedSettingsOption + step, SETTINGS_OPTION_COUNT);
			repaint();
		}
	}

	// UI-01: Xác nhận lựa chọn bằng phím ENTER.
	private void handleUiConfirm() {
		if (currentModel == null || isTransitioning) {
			return;
		}

		if (currentModel.getCurrentState() == GameState.MENU) {
			activateMenuOption(selectedMenuOption);
		} else if (currentModel.getCurrentState() == GameState.SETTINGS) {
			activateSettingsOption(selectedSettingsOption);
		}
	}

	private int wrapSelection(int value, int count) {
		return (value % count + count) % count;
	}

	// UI-01: Thực thi lựa chọn trên Main Menu.
	private void activateMenuOption(int option) {
		switch (option) {
		case MENU_OPTION_START -> startTransition(GameState.PLAYING);
		case MENU_OPTION_MODE -> toggleGameMode();
		case MENU_OPTION_DIFFICULTY -> toggleDifficultyLevel();
		case MENU_OPTION_SETTINGS -> startTransition(GameState.SETTINGS);
		default -> {
		}
		}
	}

	// UI-01: Thực thi lựa chọn trong Settings.
	private void activateSettingsOption(int option) {
		switch (option) {
		case SETTINGS_OPTION_SOUND -> isSoundOn = !isSoundOn;
		case SETTINGS_OPTION_THEME -> isNeonTheme = !isNeonTheme;
		case SETTINGS_OPTION_BACK -> startTransition(GameState.MENU);
		default -> {
		}
		}

		repaint();
	}

	// UI-01: Chuyển chế độ chơi đang hiển thị trong Main Menu.
	private void toggleGameMode() {
		if (currentModel.getCurrentMode() == GameMode.CLASSIC) {
			currentModel.setCurrentMode(GameMode.SURVIVAL);
			showModeFeedback("Đã chọn chế độ: SURVIVAL");
		} else {
			currentModel.setCurrentMode(GameMode.CLASSIC);
			showModeFeedback("Đã chọn chế độ: CLASSIC");
		}

		if (isSoundOn) {
			Toolkit.getDefaultToolkit().beep();
		}

		repaint();
	}

	// UI-01: Chuyển độ khó theo vòng lặp EASY -> NORMAL -> HARD -> EASY.
	private void toggleDifficultyLevel() {
		DifficultyLevel currentDifficulty = currentModel.getDifficultyLevel();
		DifficultyLevel nextDifficulty;

		if (currentDifficulty == DifficultyLevel.EASY) {
			nextDifficulty = DifficultyLevel.NORMAL;
		} else if (currentDifficulty == DifficultyLevel.NORMAL) {
			nextDifficulty = DifficultyLevel.HARD;
		} else {
			nextDifficulty = DifficultyLevel.EASY;
		}

		currentModel.setDifficultyLevel(nextDifficulty);
		showModeFeedback("Đã chọn độ khó: " + nextDifficulty.getLabel());

		if (isSoundOn) {
			Toolkit.getDefaultToolkit().beep();
		}

		repaint();
	}

	// UI-01: Lắng nghe click chuột trên Main Menu và Settings.
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

					int boxW = 165;
					int boxH = 55;
					int gap = 16;
					int startX = (w - (boxW * 4 + gap * 3)) / 2;
					int yBottom = h - 90;
					
					Rectangle btnMode = new Rectangle(startX, yBottom, boxW, boxH);
					Rectangle btnDifficulty = new Rectangle(startX + boxW + gap, yBottom, boxW, boxH);

					int settingsX = startX + 3 * (boxW + gap);
					Rectangle btnSettings = new Rectangle(settingsX, yBottom, boxW, boxH);

					if (btnMode.contains(mx, my)) {
						selectedMenuOption = MENU_OPTION_MODE;
						activateMenuOption(MENU_OPTION_MODE);
					} else if (btnDifficulty.contains(mx, my)) {
						selectedMenuOption = MENU_OPTION_DIFFICULTY;
						activateMenuOption(MENU_OPTION_DIFFICULTY);
					} else if (btnStart.contains(mx, my)) {
						selectedMenuOption = MENU_OPTION_START;
						activateMenuOption(MENU_OPTION_START);
					} else if (btnSettings.contains(mx, my)) {
						selectedMenuOption = MENU_OPTION_SETTINGS;
						activateMenuOption(MENU_OPTION_SETTINGS);
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
						selectedSettingsOption = SETTINGS_OPTION_BACK;
						activateSettingsOption(SETTINGS_OPTION_BACK);
					} else if (btnSound.contains(mx, my)) {
						selectedSettingsOption = SETTINGS_OPTION_SOUND;
						activateSettingsOption(SETTINGS_OPTION_SOUND);
					} else if (btnTheme.contains(mx, my)) {
						selectedSettingsOption = SETTINGS_OPTION_THEME;
						activateSettingsOption(SETTINGS_OPTION_THEME);
					}
				}
			}
		});
	}

	// UI-01: Chuyển trạng thái màn hình có hiệu ứng fade.
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

	// UI-01: Vẽ Main Menu gồm Start, chọn chế độ chơi và Settings.
	private void drawMenu(Graphics2D g2d) {
		int w = getWidth();
		int h = getHeight();

		drawBackground(g2d, w, h);
		drawArcadeSilhouettes(g2d, w, h);
		drawTitleSection(g2d, w / 2, 230);
		drawButton(g2d, "BẮT ĐẦU", w / 2 - 140, 340, 280, 65);
		drawSelectionOutline(g2d, w / 2 - 140, 340, 280, 65, selectedMenuOption == MENU_OPTION_START);

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
	// UI-01: Vẽ màn hình Settings gồm âm thanh, giao diện và nút quay lại.
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
		drawSelectionOutline(g2d, panelX + 240, panelY + 45, 110, 35,
				selectedSettingsOption == SETTINGS_OPTION_SOUND);

		g2d.setColor(Color.WHITE);
		g2d.drawString("GIAO DIỆN", panelX + 40, panelY + 150);
		drawToggleButton(g2d, isNeonTheme ? "NEON" : "CỔ ĐIỂN", panelX + 240, panelY + 125, isNeonTheme);
		drawSelectionOutline(g2d, panelX + 240, panelY + 125, 110, 35,
				selectedSettingsOption == SETTINGS_OPTION_THEME);

		drawButton(g2d, "QUAY LẠI", w / 2 - 100, h - 100, 200, 50);
		drawSelectionOutline(g2d, w / 2 - 100, h - 100, 200, 50,
				selectedSettingsOption == SETTINGS_OPTION_BACK);
	}

	// UI-01: Vẽ viền sáng cho lựa chọn đang được focus bằng bàn phím.
	private void drawSelectionOutline(Graphics2D g2d, int x, int y, int w, int h, boolean selected) {
		if (!selected) {
			return;
		}

		Stroke oldStroke = g2d.getStroke();
		g2d.setStroke(new BasicStroke(3));
		g2d.setColor(new Color(255, 230, 90));
		g2d.drawRoundRect(x - 7, y - 7, w + 14, h + 14, 22, 22);
		g2d.setColor(new Color(255, 230, 90, 70));
		g2d.drawRoundRect(x - 12, y - 12, w + 24, h + 24, 28, 28);
		g2d.setStroke(oldStroke);
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

	// UI-01: Vẽ cụm chức năng phụ dưới Main Menu.
	// Gồm: Chế độ chơi, Độ khó, Điểm cao và Cài đặt.
	private void drawBottomPanels(Graphics2D g2d, int w, int h) {

	    int boxW = 165;
	    int boxH = 55;
	    int gap = 16;

	    int startX = (w - (boxW * 4 + gap * 3)) / 2;
	    int y = h - 90;

	    int highScore = 0;
	    if (currentModel != null && currentModel.getScoreManager() != null) {
	        highScore = currentModel.getScoreManager().getHighScore();
	    }

	    String modeText = "CLASSIC";
	    Color modeColor = new Color(80, 255, 120);
	    if (currentModel != null && currentModel.getCurrentMode() == GameMode.SURVIVAL) {
	        modeText = "SURVIVAL";
	        modeColor = new Color(255, 80, 80);
	    }

	    DifficultyLevel difficulty = DifficultyLevel.NORMAL;
	    if (currentModel != null && currentModel.getDifficultyLevel() != null) {
	        difficulty = currentModel.getDifficultyLevel();
	    }

	    String difficultyText = difficulty.getLabel();
	    Color difficultyColor = switch (difficulty) {
	    case EASY -> new Color(80, 255, 120);
	    case NORMAL -> new Color(80, 180, 255);
	    case HARD -> new Color(255, 90, 90);
	    };

	    String[] titles = {
	            "CHẾ ĐỘ",
	            "ĐỘ KHÓ",
	            "ĐIỂM CAO",
	            "CÀI ĐẶT"
	    };

	    String[] subs = {
	            modeText,
	            difficultyText,
	            String.valueOf(highScore),
	            "OPTIONS"
	    };

	    Color[] subColors = {
	            modeColor,
	            difficultyColor,
	            new Color(255, 220, 80),
	            new Color(200, 120, 255)
	    };

	    for (int i = 0; i < 4; i++) {
	        int bx = startX + i * (boxW + gap);

	        boolean isSelectedMenuPanel = (i == 0 && selectedMenuOption == MENU_OPTION_MODE)
	                || (i == 1 && selectedMenuOption == MENU_OPTION_DIFFICULTY)
	                || (i == 3 && selectedMenuOption == MENU_OPTION_SETTINGS);

	        if (i == 0 || i == 1) {
	            Color activeGlow = new Color(
	                    subColors[i].getRed(),
	                    subColors[i].getGreen(),
	                    subColors[i].getBlue(),
	                    65
	            );
	            g2d.setColor(activeGlow);
	            g2d.fillRoundRect(bx - 8, y - 8, boxW + 16, boxH + 16, 18, 18);
	        }

	        g2d.setColor(new Color(0, 150, 255, 40));
	        g2d.fillRoundRect(bx - 3, y - 3, boxW + 6, boxH + 6, 15, 15);

	        g2d.setColor(new Color(0, 150, 255));
	        g2d.setStroke(new BasicStroke(2));
	        g2d.drawRoundRect(bx, y, boxW, boxH, 15, 15);

	        g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
	        g2d.setColor(new Color(180, 220, 255));
	        drawCenteredString(g2d, titles[i], bx + boxW / 2, y + 20);

	        g2d.setFont(new Font("Consolas", Font.BOLD, 16));
	        g2d.setColor(subColors[i]);
	        drawCenteredString(g2d, subs[i], bx + boxW / 2, y + 43);

	        drawSelectionOutline(g2d, bx, y, boxW, boxH, isSelectedMenuPanel);
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
	// UI-01: Vẽ giao diện game theo phong cách Neon.
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
		drawWalls(g2d);
		
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
	// UI-01: Vẽ giao diện game theo phong cách cổ điển.
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
	    drawWalls(g2d);

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
	
	private void drawWalls(Graphics2D g2d) {

	    if (currentModel == null
	            || currentModel.getWall() == null
	            || currentModel.getWall().getWalls() == null) {
	        return;
	    }

	    java.util.List<Point> walls = currentModel.getWall().getWalls();

	    for (Point wall : walls) {

	        int x = wall.x * CELL_SIZE;
	        int y = wall.y * CELL_SIZE;

	        // Glow effect
	        g2d.setColor(new Color(0, 255, 255, 40));
	        g2d.fillRoundRect(
	                x - 2,
	                y - 2,
	                CELL_SIZE + 4,
	                CELL_SIZE + 4,
	                10,
	                10
	        );

	        // Main block
	        g2d.setColor(new Color(0, 255, 200));
	        g2d.fillRoundRect(
	                x,
	                y,
	                CELL_SIZE,
	                CELL_SIZE,
	                8,
	                8
	        );

	        // Border
	        g2d.setColor(Color.WHITE);
	        g2d.drawRoundRect(
	                x,
	                y,
	                CELL_SIZE,
	                CELL_SIZE,
	                8,
	                8
	        );
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
		String difficultyText = "NORMAL";

		if (currentModel != null && currentModel.getScoreManager() != null) {
			currentScore = currentModel.getScoreManager().getCurrentScore();
			highScore = currentModel.getScoreManager().getHighScore();
		}

		if (currentModel != null && currentModel.getDifficultyLevel() != null) {
			difficultyText = currentModel.getDifficultyLevel().getLabel();
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
		int infoH = 175;
		drawSidebarSection(g2d, "THÔNG TIN", cardX, infoY, cardW, infoH);

		int valueRightX = cardX + cardW - 22;

		drawInfoRow(g2d, "Trạng thái", stateText, cardX + 18, infoY + 56, valueRightX, new Color(130, 230, 255));

		drawInfoRow(g2d, "Điểm cao", String.valueOf(highScore), cardX + 18, infoY + 91, valueRightX,
				new Color(255, 220, 80));

		drawInfoRow(g2d, "Điểm hiện tại", String.valueOf(currentScore), cardX + 18, infoY + 126, valueRightX,
				Color.WHITE);

		drawInfoRow(g2d, "Độ khó", difficultyText, cardX + 18, infoY + 158, valueRightX,
				new Color(120, 220, 255));

		// =========================
		// CARD 2: ĐIỀU KHIỂN
		// =========================
		int controlY = 258;
		int controlH = 145;
		drawSidebarSection(g2d, "ĐIỀU KHIỂN", cardX, controlY, cardW, controlH);

		int keySize = 34;
		int gap = 10;
		int clusterWidth = keySize * 3 + gap * 2;
		int clusterX = x + (w - clusterWidth) / 2;
		int clusterY = controlY + 40;

		drawKeyBox(g2d, "↑", clusterX + keySize + gap, clusterY, keySize, keySize);
		drawKeyBox(g2d, "←", clusterX, clusterY + keySize + gap, keySize, keySize);
		drawKeyBox(g2d, "↓", clusterX + keySize + gap, clusterY + keySize + gap, keySize, keySize);
		drawKeyBox(g2d, "→", clusterX + (keySize + gap) * 2, clusterY + keySize + gap, keySize, keySize);

		// Đẩy dòng này xuống dưới cụm phím, tránh bị đè
		g2d.setFont(new Font("SansSerif", Font.PLAIN, 14));
		g2d.setColor(new Color(210, 220, 235));
		drawCenteredString(g2d, "Hoặc dùng W / A / S / D", x + w / 2, controlY + 128);

		// =========================
		// CARD 3: HƯỚNG DẪN
		// =========================
		int guideY = 418;
		int guideH = 172;
		drawSidebarSection(g2d, "HƯỚNG DẪN", cardX, guideY, cardW, guideH);

		g2d.setFont(new Font("SansSerif", Font.PLAIN, 14));
		g2d.setColor(Color.WHITE);

		// Dòng đầu tiên phải nằm dưới divider, không được trùng y + 38
		int lineX = cardX + 16;
		int lineY = guideY + 62;
		int lineGap = 25;

		g2d.drawString("• ENTER : Bắt đầu / chơi lại", lineX, lineY);
		g2d.drawString("• P      : Tạm dừng / tiếp tục", lineX, lineY + lineGap);
		g2d.drawString("• ESC    : Về menu chính", lineX, lineY + lineGap * 2);
		g2d.drawString("• Menu: chọn độ khó trước khi chơi", lineX, lineY + lineGap * 3);
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
