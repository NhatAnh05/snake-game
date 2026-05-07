package view;

import javax.swing.*;
import java.awt.*;
import model.GameModel;

public class GameUI extends JFrame {
    private GamePanel panel;

    public GameUI() {
        setTitle("SNAKE GAME");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        panel = new GamePanel();
        panel.setPreferredSize(new Dimension(1140, 600));

        add(panel);

        pack();
        setLocationRelativeTo(null);

        setFocusable(true);
    }

    public void render(GameModel model) {
        panel.updateModel(model);
        panel.repaint();
    }

    public void showGameScreen() {
        this.setVisible(true);

        this.requestFocusInWindow();
    }

    public GamePanel getGamePanel() {
        return panel;
    }
}