package view;

import javax.swing.*;

import controller.InputHandler;

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

	// [DEV02 - UC02] - LÊ TUẤN ANH
	// GamePanel giữ tham chiếu InputHandler để chuyển tiếp phím điều hướng
	// từ Key Bindings về đúng luồng Control Snake khi game đang PLAYING.
	// Không xử lý trực tiếp logic đổi hướng tại View để tránh phá vỡ MVC.
	private InputHandler inputHandler;

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
	// =========================================================================
	// [UC05] - NHẬT ANH
	// PHẦN CHỈNH SỬA ĐỂ SỬA LỖI BÀN PHÍM VIE:
	// Ra lệnh cho Java vô hiệu hóa bộ gõ tiếng Việt hệ thống (IME) trên cửa sổ game.
	// Giúp ngăn chặn việc hệ điều hành tự ý chặn phím và sinh ra mã 229 (VK_PROCESSKEY).
	// =========================================================================
	private void setupKeyboardActions() {
		InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = getActionMap();

		// Đăng ký các phím điều khiển hệ thống bằng mũi tên và Enter
		// [DEV02 - UC02] - LÊ TUẤN ANH
		// Giữ lại các Key Bindings có sẵn để bảo đảm GamePanel vẫn nhận được phím
		// khi JPanel đang focus. Các phím điều hướng sẽ được chuyển tiếp về InputHandler
		// thay vì cập nhật hướng trực tiếp trong View.
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "ui01-left");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "ui01-up");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "ui01-right");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "ui01-down");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "ui01-confirm");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ui01-escape");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), "ui01-pause");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "ui01-restart");

		// --- BỔ SUNG BẢO VỆ: ĐĂNG KÝ THEO KÝ TỰ CHỮ (Dành riêng để đè bẹp Unikey VIE Telex) ---
		inputMap.put(KeyStroke.getKeyStroke('p'), "ui01-pause");
		inputMap.put(KeyStroke.getKeyStroke('P'), "ui01-pause");
		inputMap.put(KeyStroke.getKeyStroke('r'), "ui01-restart");
		inputMap.put(KeyStroke.getKeyStroke('R'), "ui01-restart");

		// [DEV02 - UC02] - LÊ TUẤN ANH
		// Bổ sung/giữ ánh xạ W/A/S/D ở tầng View để khớp với Use Case Control Snake:
		// Player có thể điều khiển rắn bằng cả phím mũi tên và W/A/S/D.
		inputMap.put(KeyStroke.getKeyStroke('w'), "ui01-up");
		inputMap.put(KeyStroke.getKeyStroke('W'), "ui01-up");
		inputMap.put(KeyStroke.getKeyStroke('ư'), "ui01-up"); // Chặn Telex gõ chữ W ra chữ Ư
		inputMap.put(KeyStroke.getKeyStroke('Ư'), "ui01-up");

		inputMap.put(KeyStroke.getKeyStroke('s'), "ui01-down");
		inputMap.put(KeyStroke.getKeyStroke('S'), "ui01-down");

		inputMap.put(KeyStroke.getKeyStroke('a'), "ui01-left");
		inputMap.put(KeyStroke.getKeyStroke('A'), "ui01-left");

		inputMap.put(KeyStroke.getKeyStroke('d'), "ui01-right");
		inputMap.put(KeyStroke.getKeyStroke('D'), "ui01-right");

		// [DEV02 - UC02] - LÊ TUẤN ANH
		// Khi đang ở MENU/SETTINGS, phím trái dùng cho điều hướng UI.
		// Khi đang chơi game, phím trái được chuyển tiếp sang InputHandler
		// để GameController kiểm tra GameState và chống quay ngược 180 độ.
		actionMap.put("ui01-left", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("[KeyBindings] -> Bắt trúng phím TRÁI / A");
				if (currentModel != null && (currentModel.getCurrentState() == GameState.MENU || currentModel.getCurrentState() == GameState.SETTINGS)) {
					handleUiNavigation(-1);
				} else if (inputHandler != null) {
					inputHandler.keyPressed(new KeyEvent(GamePanel.this, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_LEFT, KeyEvent.CHAR_UNDEFINED));
				}
			}
		});

		// [DEV02 - UC02] - LÊ TUẤN ANH
		// Chuyển tiếp phím lên/W về InputHandler trong gameplay,
		// giữ đúng trách nhiệm View chỉ nhận input và không tự đổi hướng rắn.
		actionMap.put("ui01-up", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("[KeyBindings] -> Bắt trúng phím LÊN / W");
				if (currentModel != null && (currentModel.getCurrentState() == GameState.MENU || currentModel.getCurrentState() == GameState.SETTINGS)) {
					handleUiNavigation(-1);
				} else if (inputHandler != null) {
					inputHandler.keyPressed(new KeyEvent(GamePanel.this, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_UP, KeyEvent.CHAR_UNDEFINED));
				}
			}
		});

		// [DEV02 - UC02] - LÊ TUẤN ANH
		// Chuyển tiếp phím phải/D về InputHandler để thống nhất luồng Control Snake.
		actionMap.put("ui01-right", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("[KeyBindings] -> Bắt trúng phím PHẢI / D");
				if (currentModel != null && (currentModel.getCurrentState() == GameState.MENU || currentModel.getCurrentState() == GameState.SETTINGS)) {
					handleUiNavigation(1);
				} else if (inputHandler != null) {
					inputHandler.keyPressed(new KeyEvent(GamePanel.this, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_RIGHT, KeyEvent.CHAR_UNDEFINED));
				}
			}
		});

		// [DEV02 - UC02] - LÊ TUẤN ANH
		// Chuyển tiếp phím xuống/S về InputHandler để controller xử lý hợp lệ hướng.
		actionMap.put("ui01-down", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("[KeyBindings] -> Bắt trúng phím XUỐNG / S");
				if (currentModel != null && (currentModel.getCurrentState() == GameState.MENU || currentModel.getCurrentState() == GameState.SETTINGS)) {
					handleUiNavigation(1);
				} else if (inputHandler != null) {
					inputHandler.keyPressed(new KeyEvent(GamePanel.this, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_DOWN, KeyEvent.CHAR_UNDEFINED));
				}
			}
		});

		actionMap.put("ui01-confirm", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("[KeyBindings] -> Bắt trúng phím ENTER");
				if (currentModel != null && (currentModel.getCurrentState() == GameState.MENU || currentModel.getCurrentState() == GameState.SETTINGS)) {
					handleUiConfirm();
				} else if (inputHandler != null) {
					inputHandler.keyPressed(new KeyEvent(GamePanel.this, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED));
				}
			}
		});

		actionMap.put("ui01-escape", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("[KeyBindings] -> Bắt trúng phím ESC");
				if (currentModel != null) {
					if (currentModel.getCurrentState() == GameState.SETTINGS) {
						startTransition(GameState.MENU);
					} else if (inputHandler != null) {
						inputHandler.keyPressed(new KeyEvent(GamePanel.this, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED));
					}
				}
			}
		});

		// 🌟 BỔ SUNG: Xử lý hành động ép chuyển tiếp phím P sang InputHandler bất chấp việc mất Focus
		actionMap.put("ui01-pause", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("[KeyBindings] -> Bắt trúng phím P từ luồng bảo vệ.");
				if (inputHandler != null) {
					inputHandler.keyPressed(new KeyEvent(GamePanel.this, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_P, 'P'));
				}
			}
		});

		actionMap.put("ui01-restart", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("[KeyBindings] -> Bắt trúng phím R / Restart");
				if (inputHandler != null) {
					inputHandler.keyPressed(new KeyEvent(GamePanel.this, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_R, 'R'));
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
				// ================= DEV04 - UC4.4 RESTART GAME =================
				// Player click nút CHƠI LẠI/MENU trên overlay Game Over.
				// View chỉ gửi yêu cầu, GameController mới quyết định reset state theo Sequence Diagram.
				else if (currentModel.getCurrentState() == GameState.GAME_OVER) {
					Rectangle btnRestart = getGameOverRestartButtonBounds();
					Rectangle btnMenu = getGameOverMenuButtonBounds();

					if (btnRestart.contains(mx, my)) {
						if (onStartAction != null) {
							onStartAction.run();
						}
					} else if (btnMenu.contains(mx, my) && inputHandler != null) {
						inputHandler.keyPressed(new KeyEvent(GamePanel.this, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED));
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
			case MENU -> drawMenu(g2d);
			case SETTINGS -> drawSettings(g2d);
			case PLAYING, PAUSED, GAME_OVER -> drawGameWorld(g2d);
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
		drawSelectionOutline(g2d, panelX + 240, panelY + 45, 110, 35, selectedSettingsOption == SETTINGS_OPTION_SOUND);

		g2d.setColor(Color.WHITE);
		g2d.drawString("GIAO DIỆN", panelX + 40, panelY + 150);
		drawToggleButton(g2d, isNeonTheme ? "NEON" : "CỔ ĐIỂN", panelX + 240, panelY + 125, isNeonTheme);
		drawSelectionOutline(g2d, panelX + 240, panelY + 125, 110, 35, selectedSettingsOption == SETTINGS_OPTION_THEME);

		drawButton(g2d, "QUAY LẠI", w / 2 - 100, h - 100, 200, 50);
		drawSelectionOutline(g2d, w / 2 - 100, h - 100, 200, 50, selectedSettingsOption == SETTINGS_OPTION_BACK);
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
		RadialGradientPaint rgp = new RadialGradientPaint(new java.awt.geom.Point2D.Float(w / 2, h / 2), w / 1.1f,
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

		String[] titles = { "CHẾ ĐỘ", "ĐỘ KHÓ", "ĐIỂM CAO", "CÀI ĐẶT" };
		String[] subs = { modeText, difficultyText, String.valueOf(highScore), "OPTIONS" };
		Color[] subColors = { modeColor, difficultyColor, new Color(255, 220, 80), new Color(200, 120, 255) };

		for (int i = 0; i < 4; i++) {
			int bx = startX + i * (boxW + gap);

			boolean isSelectedMenuPanel = (i == 0 && selectedMenuOption == MENU_OPTION_MODE)
					|| (i == 1 && selectedMenuOption == MENU_OPTION_DIFFICULTY)
					|| (i == 3 && selectedMenuOption == MENU_OPTION_SETTINGS);

			if (i == 0 || i == 1) {
				Color activeGlow = new Color(subColors[i].getRed(), subColors[i].getGreen(), subColors[i].getBlue(), 65);
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

		// Viền Neon khu vực chơi
		g2d.setColor(new Color(0, 255, 200, 90));
		g2d.setStroke(new BasicStroke(2));
		g2d.drawLine(GAME_AREA_WIDTH, 0, GAME_AREA_WIDTH, GAME_AREA_HEIGHT);

		drawWalls(g2d);

		// RẮN (NEON THEME)
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
						g2d.fillRoundRect(p.x * CELL_SIZE + 2, p.y * CELL_SIZE + 2, CELL_SIZE - 4, CELL_SIZE - 4, 6, 6);
					}
				}
			}
		}
		// [UI-03] Tái cấu trúc Render: Loại bỏ phần mã vẽ mồi (Food) dư thừa trực tiếp trong hàm này để dồn quản lý về hàm drawFood tập trung.
	}

	// ================================
	// THEME CỔ ĐIỂN
	// ================================
	// UI-01: Vẽ giao diện game theo phong cách cổ điển (Retro Arcade).
	private void drawClassicTheme(Graphics2D g2d) {
		// Nền tổng
		g2d.setColor(new Color(12, 12, 12));
		g2d.fillRect(0, 0, getWidth(), getHeight());

		// Khu vực game
		g2d.setColor(new Color(0, 0, 0));
		g2d.fillRect(0, 0, GAME_AREA_WIDTH, GAME_AREA_HEIGHT);

		// Lưới rất nhẹ cổ điển
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

		// RẮN (CLASSIC THEME)
		if (currentModel.getSnake() != null && currentModel.getSnake().getBody() != null) {
			java.util.List<Point> body = currentModel.getSnake().getBody();
			for (int i = 0; i < body.size(); i++) {
				Point p = body.get(i);
				if (i == 0) {
					g2d.setColor(new Color(80, 255, 80)); // đầu sáng hơn
				} else {
					g2d.setColor(new Color(30, 170, 30)); // thân tối hơn
				}
				g2d.fillRect(p.x * CELL_SIZE, p.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
			}
		}
		// [UI-03] Tái cấu trúc Render: Loại bỏ phần mã vẽ mồi (Food) cũ tại đây để sửa lỗi xung đột layer hiển thị thực thể.
	}

	// =====================================================
// QUẢN LÝ ĐỒ HỌA THẾ GIỚI GAME (CORE GAME WORLD)
// =====================================================
	private void drawGameWorld(Graphics2D g2d) {
		// 1. Vẽ nền map và rắn theo phong cách được chọn
		if (isNeonTheme) {
			drawNeonTheme(g2d);
		} else {
			drawClassicTheme(g2d);
		}

		// [UI-03] Quản lý luồng hiển thị: Thêm lời gọi hàm drawFood(g2d) tường minh vào luồng paint chính để hiển thị mồi chuẩn xác lên màn hình.
		drawFood(g2d);

		// 3. Vẽ hướng chỉ định đầu rắn để bổ trợ điều hướng
		drawSnakeHeadDirection(g2d);

		// 4. Vẽ Sidebar thông tin bên phải ngoài rìa map chơi
		drawRightSidebar(g2d);

		// [UI-03] NÂNG CẤP NÂNG CAO: VẼ THANH ĐẾM NGƯỢC THỜI GIAN MỒI ĐẶC BIỆT
		// Phục vụ cơ chế Tự động hủy trạng thái mồi (Food Expiry) trực quan trên giao diện
		if (currentModel.getFood() != null && currentModel.getFood().isSpecial()) {
			long timeLeft = currentModel.getFood().getTimeLeft();

			if (timeLeft > 0) {
				int maxWidth = 200;
				int barWidth = (int) ((timeLeft / 7000.0) * maxWidth);
				int x = 25;
				int y = 50;
				int height = 10;
				g2d.setColor(new Color(40, 40, 40, 180));
				g2d.fillRoundRect(x, y, maxWidth, height, 6, 6);
				if (timeLeft > 2500) {

					g2d.setColor(new Color(255, 215, 0));
				} else {
					long systemTime = System.currentTimeMillis();
					if ((systemTime / 150) % 2 == 0) {
						g2d.setColor(new Color(255, 30, 30));
					} else {
						g2d.setColor(new Color(255, 215, 0));
					}
				}
				g2d.fillRoundRect(x, y, barWidth, height, 6, 6);
				g2d.setColor(Color.WHITE);
				g2d.setFont(new Font("SansSerif", Font.BOLD, 11));
				g2d.drawString("BONUS TIME: " + (timeLeft / 1000 + 1) + "s", x + maxWidth + 12, y + 9);
			}
		}
		if (inputHandler != null && inputHandler.getController() != null) {
			var controller = inputHandler.getController();

			if (controller.isCountingDown()) {
				int countdown = controller.getCountdownValue();
				String text = countdown == 0 ? "GO!" : String.valueOf(countdown);


				g2d.setFont(new Font("Impact", Font.BOLD, 90));
				FontMetrics fm = g2d.getFontMetrics();


				int textX = (GAME_AREA_WIDTH - fm.stringWidth(text)) / 2;
				int textY = (GAME_AREA_HEIGHT - fm.getHeight()) / 2 + fm.getAscent();

				g2d.setColor(new Color(0, 0, 0, 100));
				g2d.fillRect(0, 0, GAME_AREA_WIDTH, GAME_AREA_HEIGHT);


				g2d.setColor(new Color(255, 50, 100, 150));
				g2d.drawString(text, textX + 4, textY + 4);


				g2d.setColor(new Color(255, 230, 50));
				g2d.drawString(text, textX, textY);
			}
		}
		if (currentModel.getCurrentState() == GameState.PAUSED) {
			drawPauseOverlay(g2d);
		} else if (currentModel.getCurrentState() == GameState.GAME_OVER) {
			// DEV04 - UC4.2 End Game:
			// Khi state = GAME_OVER, View vẽ overlay kết thúc thay vì thay đổi logic game.
			drawGameOverOverlay(g2d);
		}
	}
	private void drawWalls(Graphics2D g2d) {
		if (currentModel == null || currentModel.getWall() == null || currentModel.getWall().getWalls() == null) {
			return;
		}

		java.util.List<Point> walls = currentModel.getWall().getWalls();
		for (Point wall : walls) {
			int x = wall.x * CELL_SIZE;
			int y = wall.y * CELL_SIZE;

			if (isNeonTheme) {
				// Vật cản kiểu phát sáng tương thích Neon
				g2d.setColor(new Color(0, 255, 255, 40));
				g2d.fillRoundRect(x - 2, y - 2, CELL_SIZE + 4, CELL_SIZE + 4, 10, 10);

				g2d.setColor(new Color(0, 255, 200));
				g2d.fillRoundRect(x, y, CELL_SIZE, CELL_SIZE, 8, 8);

				g2d.setColor(Color.WHITE);
				g2d.drawRoundRect(x, y, CELL_SIZE, CELL_SIZE, 8, 8);
			} else {
				// Vật cản Retro khối đặc kiểu gạch thô cổ điển
				g2d.setColor(Color.DARK_GRAY);
				g2d.fillRect(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2);
				g2d.setColor(Color.GRAY);
				g2d.drawRect(x, y, CELL_SIZE - 1, CELL_SIZE - 1);
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

		// Viền trái sidebar ngăn cách vùng chơi
		g2d.setColor(new Color(0, 255, 200, 120));
		g2d.setStroke(new BasicStroke(2));
		g2d.drawLine(x, 0, x, h);

		// DEV04 - UC4.3 Save High Score:
		// Lấy điểm hiện tại, điểm cao và cờ phá kỷ lục từ ScoreManager để render kết quả cuối.
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
				case PLAYING -> stateText = "Đang chơi";
				case PAUSED -> stateText = "Tạm dừng";
				case GAME_OVER -> stateText = "Kết thúc";
				case MENU -> stateText = "Menu";
				case SETTINGS -> stateText = "Cài đặt";
			}
		}

		// Tiêu đề chính sidebar
		g2d.setFont(new Font("SansSerif", Font.BOLD, 24));
		g2d.setColor(new Color(0, 255, 200));
		drawCenteredString(g2d, "THÔNG TIN GAME", x + w / 2, 40);

		int cardX = x + 16;
		int cardW = w - 32;

		// =========================
		// CARD 1: THÔNG TIN CHỈ SỐ
		// =========================
		int infoY = 68;
		int infoH = 175;
		drawSidebarSection(g2d, "THÔNG TIN", cardX, infoY, cardW, infoH);

		int valueRightX = cardX + cardW - 22;
		drawInfoRow(g2d, "Trạng thái", stateText, cardX + 18, infoY + 56, valueRightX, new Color(130, 230, 255));
		drawInfoRow(g2d, "Điểm cao", String.valueOf(highScore), cardX + 18, infoY + 91, valueRightX, new Color(255, 220, 80));
		drawInfoRow(g2d, "Điểm số", String.valueOf(currentScore), cardX + 18, infoY + 126, valueRightX, Color.WHITE);
		drawInfoRow(g2d, "Độ khó", difficultyText, cardX + 18, infoY + 158, valueRightX, new Color(120, 220, 255));

		// =========================
		// CARD 2: CỤM PHÍM ĐIỀU KHIỂN D-PAD
		// =========================
		int controlY = 258;
		int controlH = 145;
		drawSidebarSection(g2d, "ĐIỀU KHIỂN", cardX, controlY, cardW, controlH);

		int keySize = 34;
		int gap = 10;
		int clusterWidth = keySize * 3 + gap * 2;
		int clusterX = x + (w - clusterWidth) / 2;
		int clusterY = controlY + 40;

		// [DEV02 - UC02] - LÊ TUẤN ANH
		// Hiển thị cụm phím điều khiển trong sidebar để người chơi nhận biết
		// các input hợp lệ của chức năng Control Snake.
		drawKeyBox(g2d, "↑", clusterX + keySize + gap, clusterY, keySize, keySize);
		drawKeyBox(g2d, "←", clusterX, clusterY + keySize + gap, keySize, keySize);
		drawKeyBox(g2d, "↓", clusterX + keySize + gap, clusterY + keySize + gap, keySize, keySize);
		drawKeyBox(g2d, "→", clusterX + (keySize + gap) * 2, clusterY + keySize + gap, keySize, keySize);

		g2d.setFont(new Font("SansSerif", Font.PLAIN, 14));
		g2d.setColor(new Color(210, 220, 235));
		drawCenteredString(g2d, "Hoặc dùng W / A / S / D", x + w / 2, controlY + 128);

		// =========================
		// CARD 3: HƯỚNG DẪN LUẬT CHƠI KHÁC
		// =========================
		int guideY = 418;
		int guideH = 172;
		drawSidebarSection(g2d, "HƯỚNG DẪN", cardX, guideY, cardW, guideH);

		g2d.setFont(new Font("SansSerif", Font.PLAIN, 14));
		g2d.setColor(Color.WHITE);

		int lineX = cardX + 16;
		int lineY = guideY + 62;
		int lineGap = 25;

		g2d.drawString("• ENTER/R : Bắt đầu / chơi lại", lineX, lineY);
		g2d.drawString("• P       : Tạm dừng / tiếp tục", lineX, lineY + lineGap);
		g2d.drawString("• ESC     : Về menu chính", lineX, lineY + lineGap * 2);
		g2d.drawString("• Menu: chọn độ khó trước khi chơi", lineX, lineY + lineGap * 3);
		g2d.drawString("• Tránh tường, vật cản và thân rắn", lineX, lineY + lineGap * 4);
	}

	private void drawInfoRow(Graphics2D g2d, String label, String value, int x, int y, int valueRightX, Color valueColor) {
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

	/**
	 * DEV04 - UC4.2 + UC4.3 + UC4.4:
	 * Vẽ màn hình Game Over theo hướng hiện đại, có dấu tiếng Việt đầy đủ.
	 * - UC4.2: hiển thị trạng thái kết thúc và lý do thua.
	 * - UC4.3: hiển thị điểm hiện tại, điểm cao và kỷ lục mới nếu có.
	 * - UC4.4: cung cấp nút Chơi lại và Menu chính.
	 *
	 * Lưu ý:
	 * - Đã bỏ dòng footer "UC04..." để giao diện sạch hơn.
	 * - Bố cục được tách rõ để tránh đè chữ, đè nút.
	 */
	private void drawGameOverOverlay(Graphics2D g2d) {
		long now = System.currentTimeMillis();
		float pulse = (float) ((Math.sin(now / 260.0) + 1.0) / 2.0);
		int pulseAlpha = 24 + (int) (pulse * 34);

		// Nền tối vừa đủ để làm nổi card Game Over.
		g2d.setColor(new Color(0, 0, 0, 210));
		g2d.fillRect(0, 0, GAME_AREA_WIDTH, GAME_AREA_HEIGHT);

		// Scanline nhẹ, giữ phong cách neon nhưng không gây rối mắt.
		g2d.setColor(new Color(255, 255, 255, 6));
		for (int y = 0; y < GAME_AREA_HEIGHT; y += 9) {
			g2d.drawLine(0, y, GAME_AREA_WIDTH, y);
		}

		// DEV04 - UC4.3 Save High Score:
		// Lấy điểm hiện tại, điểm cao và thống kê phiên chơi từ ScoreManager/GameModel.
		int currentScore = 0;
		int highScore = 0;
		int highScoreDelta = 0;
		int foodEaten = 0;
		int specialFoodEaten = 0;
		int maxCombo = 0;
		boolean isNewRecord = false;
		long survivalSeconds = 0;
		String reason = "Không xác định";
		String modeText = "CLASSIC";
		String difficultyText = "NORMAL";

		if (currentModel != null) {
			survivalSeconds = currentModel.getSessionDurationSeconds();
			modeText = currentModel.getCurrentMode() == null ? "CLASSIC" : currentModel.getCurrentMode().name();

			if (currentModel.getDifficultyLevel() != null) {
				difficultyText = currentModel.getDifficultyLevel().getLabel();
			}

			if (currentModel.getScoreManager() != null) {
				currentScore = currentModel.getScoreManager().getCurrentScore();
				highScore = currentModel.getScoreManager().getHighScore();
				highScoreDelta = currentModel.getScoreManager().getHighScoreDelta();
				foodEaten = currentModel.getScoreManager().getFoodEaten();
				specialFoodEaten = currentModel.getScoreManager().getSpecialFoodEaten();
				maxCombo = currentModel.getScoreManager().getMaxComboCount();
				isNewRecord = currentModel.getScoreManager().isNewHighScoreAchieved();
			}

			if (currentModel.getGameOverReason() != null && !currentModel.getGameOverReason().isBlank()) {
				reason = currentModel.getGameOverReason();
			}
		}

		int cardW = 680;
		int cardH = 510;
		int cardX = (GAME_AREA_WIDTH - cardW) / 2;
		int cardY = 42;

		// Glow ngoài card.
		g2d.setColor(new Color(255, 55, 120, pulseAlpha));
		g2d.fillRoundRect(cardX - 12, cardY - 12, cardW + 24, cardH + 24, 34, 34);

		g2d.setColor(new Color(0, 255, 210, 14 + pulseAlpha / 3));
		g2d.fillRoundRect(cardX - 4, cardY - 4, cardW + 8, cardH + 8, 28, 28);

		GradientPaint panelPaint = new GradientPaint(
				cardX, cardY,
				new Color(18, 26, 48, 248),
				cardX, cardY + cardH,
				new Color(7, 11, 25, 248)
		);
		g2d.setPaint(panelPaint);
		g2d.fillRoundRect(cardX, cardY, cardW, cardH, 28, 28);

		g2d.setColor(new Color(255, 80, 125, 220));
		g2d.setStroke(new BasicStroke(3f));
		g2d.drawRoundRect(cardX, cardY, cardW, cardH, 28, 28);

		g2d.setColor(new Color(255, 255, 255, 40));
		g2d.setStroke(new BasicStroke(1.2f));
		g2d.drawRoundRect(cardX + 10, cardY + 10, cardW - 20, cardH - 20, 22, 22);

		// Vùng 1: Tiêu đề + lý do thua.
		drawText(g2d, "GAME OVER", GAME_AREA_WIDTH / 2, cardY + 74, 48, new Color(255, 95, 130));
		drawGameOverReasonPill(g2d, "Lý do: " + reason, GAME_AREA_WIDTH / 2, cardY + 106);

		if (isNewRecord) {
			String badgeText = highScoreDelta > 0 ? "KỶ LỤC MỚI  +" + highScoreDelta : "KỶ LỤC MỚI!";
			drawNewRecordBadge(g2d, badgeText, GAME_AREA_WIDTH / 2, cardY + 136, pulseAlpha);
		}

		// Vùng 2: Điểm hiện tại + điểm cao.
		int statY = cardY + 160;
		int statW = 240;
		int statH = 82;
		int statGap = 34;
		int leftStatX = GAME_AREA_WIDTH / 2 - statW - statGap / 2;
		int rightStatX = GAME_AREA_WIDTH / 2 + statGap / 2;

		drawGameOverStatBox(g2d, "ĐIỂM CỦA BẠN", String.valueOf(currentScore), leftStatX, statY, statW, statH, new Color(0, 255, 210));
		drawGameOverStatBox(g2d, "ĐIỂM CAO", String.valueOf(highScore), rightStatX, statY, statW, statH, new Color(255, 220, 80));

		// Vùng 3: Thống kê phiên chơi.
		int summaryX = cardX + 52;
		int summaryY = cardY + 262;
		int summaryW = cardW - 104;
		int summaryH = 136;
		drawGameOverSummaryPanel(g2d, summaryX, summaryY, summaryW, summaryH);

		drawGameOverSummaryRow(g2d, "Thời gian sống", formatDuration(survivalSeconds), summaryX + 24, summaryY + 34, summaryX + summaryW / 2 + 18, summaryY + 34);
		drawGameOverSummaryRow(g2d, "Mồi đã ăn", foodEaten + "  (đặc biệt " + specialFoodEaten + ")", summaryX + 24, summaryY + 64, summaryX + summaryW / 2 + 18, summaryY + 64);
		drawGameOverSummaryRow(g2d, "Combo cao nhất", "x" + Math.max(maxCombo, 1), summaryX + 24, summaryY + 94, summaryX + summaryW / 2 + 18, summaryY + 94);
		drawGameOverSummaryRow(g2d, "Chế độ / Độ khó", modeText + " / " + difficultyText, summaryX + 24, summaryY + 124, summaryX + summaryW / 2 + 18, summaryY + 124);

		// Vùng 4: Đánh giá, tách riêng khỏi nút để không bị đè.
		String rating = buildPerformanceRating(currentScore, survivalSeconds, foodEaten, maxCombo);
		Color ratingColor = getPerformanceRatingColor(currentScore, maxCombo);
		drawGameOverRatingBadge(g2d, rating, GAME_AREA_WIDTH / 2, cardY + 420, ratingColor);

		// Vùng 5: Hành động theo UC4.4 Restart Game.
		Rectangle restartButton = getGameOverRestartButtonBounds();
		Rectangle menuButton = getGameOverMenuButtonBounds();

		drawGameOverButton(g2d, "CHƠI LẠI", "ENTER / R", restartButton, new Color(0, 255, 180));
		drawGameOverButton(g2d, "MENU CHÍNH", "ESC", menuButton, new Color(120, 190, 255));
	}

	private Rectangle getGameOverRestartButtonBounds() {
		int cardW = 680;
		int cardX = (GAME_AREA_WIDTH - cardW) / 2;
		int cardY = 42;
		return new Rectangle(cardX + 92, cardY + 440, 220, 58);
	}

	private Rectangle getGameOverMenuButtonBounds() {
		int cardW = 680;
		int cardX = (GAME_AREA_WIDTH - cardW) / 2;
		int cardY = 42;
		return new Rectangle(cardX + 368, cardY + 440, 220, 58);
	}

	private void drawGameOverReasonPill(Graphics2D g2d, String text, int centerX, int y) {
		g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
		FontMetrics fm = g2d.getFontMetrics();
		int pillW = Math.max(280, fm.stringWidth(text) + 44);
		int pillH = 34;
		int pillX = centerX - pillW / 2;

		g2d.setColor(new Color(255, 255, 255, 14));
		g2d.fillRoundRect(pillX, y - 24, pillW, pillH, 18, 18);

		g2d.setColor(new Color(255, 105, 135, 100));
		g2d.setStroke(new BasicStroke(1.4f));
		g2d.drawRoundRect(pillX, y - 24, pillW, pillH, 18, 18);

		g2d.setColor(new Color(238, 243, 255));
		drawCenteredString(g2d, text, centerX, y - 2);
	}

	private void drawNewRecordBadge(Graphics2D g2d, String text, int centerX, int y, int pulseAlpha) {
		g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
		FontMetrics fm = g2d.getFontMetrics();
		int badgeW = fm.stringWidth(text) + 48;
		int badgeH = 30;
		int badgeX = centerX - badgeW / 2;

		g2d.setColor(new Color(255, 220, 80, 32 + pulseAlpha));
		g2d.fillRoundRect(badgeX - 4, y - 21, badgeW + 8, badgeH + 8, 18, 18);

		g2d.setColor(new Color(255, 220, 80));
		g2d.fillRoundRect(badgeX, y - 17, badgeW, badgeH, 16, 16);

		g2d.setColor(new Color(35, 25, 6));
		drawCenteredString(g2d, text, centerX, y + 4);
	}

	private void drawGameOverStatBox(Graphics2D g2d, String label, String value, int x, int y, int w, int h, Color accent) {
		g2d.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 28));
		g2d.fillRoundRect(x - 4, y - 4, w + 8, h + 8, 18, 18);

		g2d.setColor(new Color(10, 18, 32, 235));
		g2d.fillRoundRect(x, y, w, h, 18, 18);

		g2d.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 170));
		g2d.setStroke(new BasicStroke(2f));
		g2d.drawRoundRect(x, y, w, h, 18, 18);

		g2d.setFont(new Font("SansSerif", Font.BOLD, 13));
		g2d.setColor(new Color(210, 220, 235));
		drawCenteredString(g2d, label, x + w / 2, y + 25);

		g2d.setFont(new Font("Consolas", Font.BOLD, 34));
		g2d.setColor(accent);
		drawCenteredString(g2d, value, x + w / 2, y + 61);
	}

	private void drawGameOverSummaryPanel(Graphics2D g2d, int x, int y, int w, int h) {
		g2d.setColor(new Color(0, 0, 0, 85));
		g2d.fillRoundRect(x + 3, y + 4, w, h, 20, 20);

		g2d.setColor(new Color(12, 20, 34, 232));
		g2d.fillRoundRect(x, y, w, h, 20, 20);

		g2d.setColor(new Color(0, 255, 200, 110));
		g2d.setStroke(new BasicStroke(1.8f));
		g2d.drawRoundRect(x, y, w, h, 20, 20);

		g2d.setColor(new Color(255, 255, 255, 28));
		g2d.drawLine(x + 18, y + 18, x + w - 18, y + 18);
	}

	private void drawGameOverSummaryRow(Graphics2D g2d, String leftLabel, String leftValue, int leftX, int leftY, int rightX, int rightY) {
		g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
		g2d.setColor(new Color(175, 190, 210));
		g2d.drawString(leftLabel, leftX, leftY);

		g2d.setFont(new Font("Consolas", Font.BOLD, 15));
		g2d.setColor(new Color(238, 246, 255));
		g2d.drawString(leftValue, rightX, rightY);
	}

	private void drawGameOverRatingBadge(Graphics2D g2d, String text, int centerX, int y, Color accent) {
		g2d.setFont(new Font("SansSerif", Font.BOLD, 15));
		FontMetrics fm = g2d.getFontMetrics();
		int badgeW = fm.stringWidth(text) + 44;
		int badgeH = 34;
		int x = centerX - badgeW / 2;

		g2d.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 40));
		g2d.fillRoundRect(x - 5, y - 23, badgeW + 10, badgeH + 10, 18, 18);

		g2d.setColor(new Color(12, 18, 30, 236));
		g2d.fillRoundRect(x, y - 18, badgeW, badgeH, 16, 16);

		g2d.setColor(accent);
		g2d.setStroke(new BasicStroke(2));
		g2d.drawRoundRect(x, y - 18, badgeW, badgeH, 16, 16);

		g2d.setColor(accent);
		drawCenteredString(g2d, text, centerX, y + 4);
	}

	private String buildPerformanceRating(int score, long survivalSeconds, int foodEaten, int maxCombo) {
		int ratingPoint = score + foodEaten * 5 + maxCombo * 8 + (int) Math.min(60, survivalSeconds);
		if (ratingPoint >= 260) return "ĐÁNH GIÁ: HUYỀN THOẠI";
		if (ratingPoint >= 170) return "ĐÁNH GIÁ: XUẤT SẮC";
		if (ratingPoint >= 90) return "ĐÁNH GIÁ: ỔN ĐỊNH";
		return "ĐÁNH GIÁ: CẦN TẬP THÊM";
	}

	private Color getPerformanceRatingColor(int score, int maxCombo) {
		if (score >= 200 || maxCombo >= 6) return new Color(255, 220, 80);
		if (score >= 100 || maxCombo >= 3) return new Color(0, 255, 200);
		return new Color(120, 190, 255);
	}

	private String formatDuration(long seconds) {
		long minutes = seconds / 60;
		long remainingSeconds = seconds % 60;
		return String.format("%02d:%02d", minutes, remainingSeconds);
	}

	private void drawGameOverButton(Graphics2D g2d, String title, String hint, Rectangle rect, Color accent) {
		long now = System.currentTimeMillis();
		int glowAlpha = 28 + (int) (((Math.sin(now / 180.0) + 1.0) / 2.0) * 35);

		g2d.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), glowAlpha));
		g2d.fillRoundRect(rect.x - 5, rect.y - 5, rect.width + 10, rect.height + 10, 20, 20);

		g2d.setColor(new Color(14, 24, 34, 238));
		g2d.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 18, 18);

		g2d.setColor(accent);
		g2d.setStroke(new BasicStroke(2.2f));
		g2d.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 18, 18);

		g2d.setFont(new Font("SansSerif", Font.BOLD, 19));
		g2d.setColor(Color.WHITE);
		drawCenteredString(g2d, title, rect.x + rect.width / 2, rect.y + 24);

		g2d.setFont(new Font("Consolas", Font.BOLD, 12));
		g2d.setColor(new Color(205, 218, 235));
		drawCenteredString(g2d, hint, rect.x + rect.width / 2, rect.y + 43);
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

	// [DEV02 - UC02] - LÊ TUẤN ANH
	// GameController gọi hàm này để đồng bộ InputHandler vào GamePanel.
	// Nhờ đó Key Bindings của View có thể chuyển tiếp phím về đúng controller,
	// tránh việc GamePanel tự xử lý logic đổi hướng.
	public void setInputHandler(InputHandler inputHandler) {
		this.inputHandler = inputHandler;
	}

	// UI-01: Vẽ mũi tên chỉ hướng đi ngay phía trước để bổ trợ cho người chơi dễ nhìn hướng đi của rắn.
	private void drawSnakeHeadDirection(Graphics2D g2d) {
		if (currentModel == null || currentModel.getCurrentState() != GameState.PLAYING) return;
		if (currentModel.getSnake() == null || currentModel.getSnake().getBody() == null) return;

		java.util.List<Point> body = currentModel.getSnake().getBody();
		if (body.isEmpty()) return;

		model.Direction currentDir = currentModel.getSnake().getDirection();
		if (currentDir == null) return;

		Point head = body.get(0);
		int headPixelX = head.x * CELL_SIZE;
		int headPixelY = head.y * CELL_SIZE;
		int centerX = headPixelX + (CELL_SIZE / 2);
		int centerY = headPixelY + (CELL_SIZE / 2);

		int offset = (CELL_SIZE / 2) + 22;
		switch (currentDir) {
			case UP -> centerY -= offset;
			case DOWN -> centerY+= offset;
			case LEFT -> centerX -= offset;
			case RIGHT -> centerX += offset;
		}

		Stroke oldStroke = g2d.getStroke();
		Color arrowColor = new Color(255, 15, 60);

		int headLength = 8;
		int headWidth = 7;
		int stemLength = 14;
		int stemWidth = 3;

		Polygon arrow = new Polygon();
		switch (currentDir) {
			case UP -> {
				arrow.addPoint(centerX, centerY - headLength);
				arrow.addPoint(centerX + headWidth, centerY);
				arrow.addPoint(centerX + stemWidth, centerY);
				arrow.addPoint(centerX + stemWidth, centerY + stemLength);
				arrow.addPoint(centerX - stemWidth, centerY + stemLength);
				arrow.addPoint(centerX - stemWidth, centerY);
				arrow.addPoint(centerX - headWidth, centerY);
			}
			case DOWN -> {
				arrow.addPoint(centerX, centerY + headLength);
				arrow.addPoint(centerX + headWidth, centerY);
				arrow.addPoint(centerX + stemWidth, centerY);
				arrow.addPoint(centerX + stemWidth, centerY - stemLength);
				arrow.addPoint(centerX - stemWidth, centerY - stemLength);
				arrow.addPoint(centerX - stemWidth, centerY);
				arrow.addPoint(centerX - headWidth, centerY);
			}
			case LEFT -> {
				arrow.addPoint(centerX - headLength, centerY);
				arrow.addPoint(centerX, centerY - headWidth);
				arrow.addPoint(centerX, centerY - stemWidth);
				arrow.addPoint(centerX + stemLength, centerY - stemWidth);
				arrow.addPoint(centerX + stemLength, centerY + stemWidth);
				arrow.addPoint(centerX, centerY + stemWidth);
				arrow.addPoint(centerX, centerY + headWidth);
			}
			case RIGHT -> {
				arrow.addPoint(centerX + headLength, centerY);
				arrow.addPoint(centerX, centerY - headWidth);
				arrow.addPoint(centerX, centerY - stemWidth);
				arrow.addPoint(centerX - stemLength, centerY - stemWidth);
				arrow.addPoint(centerX - stemLength, centerY + stemWidth);
				arrow.addPoint(centerX, centerY + stemWidth);
				arrow.addPoint(centerX, centerY + headWidth);
			}
		}

		g2d.setColor(arrowColor);
		g2d.fillPolygon(arrow);

		g2d.setColor(new Color(255, 255, 255, 220));
		g2d.setStroke(new BasicStroke(1.2f));
		g2d.drawPolygon(arrow);

		g2d.setStroke(oldStroke);
	}

	// CHECKLIST: Hàm render hình ảnh/màu sắc riêng biệt cho Food tập trung dựa trên Theme hiện tại
	// [UI-03] Chức năng hiển thị: Kích hoạt toàn diện hàm vẽ mồi drawFood() tách biệt, tích hợp logic xử lý phân cấp theme (Neon/Classic) và mồi đặc biệt (Special Pulse) giúp gom cụm mã nguồn tối ưu.
	private void drawFood(Graphics2D g2d) {
		if (currentModel == null || currentModel.getFood() == null) return;

		Point f = currentModel.getFood().getPosition();
		if (f == null) return;

		int pixelX = f.x * CELL_SIZE;
		int pixelY = f.y * CELL_SIZE;

		if (currentModel.getFood().isSpecial()) {
			// --- ĐỒ HỌA MỒI ĐẶC BIỆT ---
			if (isNeonTheme) {
				long time = System.currentTimeMillis();
				int alpha = 140 + (int) (115 * Math.sin(time / 120.0));

				g2d.setColor(new Color(255, 215, 0, alpha));
				int[] xPoints = { pixelX + CELL_SIZE / 2, pixelX + CELL_SIZE - 2, pixelX + CELL_SIZE / 2, pixelX + 2 };
				int[] yPoints = { pixelY + 2, pixelY + CELL_SIZE / 2, pixelY + CELL_SIZE - 2, pixelY + CELL_SIZE / 2 };
				g2d.fillPolygon(xPoints, yPoints, 4);

				g2d.setColor(new Color(255, 255, 255, alpha));
				g2d.drawPolygon(xPoints, yPoints, 4);
			} else {
				if ((System.currentTimeMillis() / 150) % 2 == 0) {
					g2d.setColor(Color.YELLOW);
				} else {
					g2d.setColor(new Color(140, 140, 0));
				}
				g2d.fillRect(pixelX + 3, pixelY + 3, CELL_SIZE - 6, CELL_SIZE - 6);
			}
		} else {
			// --- ĐỒ HỌA MỒI THƯỜNG ---
			if (isNeonTheme) {
				if (imgFood != null) {
					g2d.drawImage(imgFood, pixelX, pixelY, CELL_SIZE, CELL_SIZE, null);
				} else {
					g2d.setColor(new Color(255, 50, 100));
					g2d.fillOval(pixelX + 2, pixelY + 2, CELL_SIZE - 4, CELL_SIZE - 4);
				}
			} else {
				// Mồi thường cổ điển: hình ô vuông đỏ retro đặc ruột
				g2d.setColor(new Color(220, 40, 40));
				g2d.fillRect(pixelX + 4, pixelY + 4, CELL_SIZE - 8, CELL_SIZE - 8);
			}
		}
	}
}