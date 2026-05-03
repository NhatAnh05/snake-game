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


        panel = new GamePanel();
        panel.setPreferredSize(new Dimension(600, 600)); // Đặt kích thước ở đây rất chuẩn
        add(panel);

        pack();
        setLocationRelativeTo(null);
    }


    public void render(GameModel model) {
        this.currentModel = model;


        panel.updateModel(model);

        panel.repaint();
    }

    public void showGameScreen() {
        this.setVisible(true); }}