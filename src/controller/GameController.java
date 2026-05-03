package controller;

import model.GameModel;
import model.GameState;
import view.GameUI;

import javax.swing.Timer; // Nhớ import cái này của Swing nhé
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;


public class GameController {
    private GameModel model;
    private GameUI view;
    private Timer gameLoop; // Động cơ của game

    public GameController(GameModel model, GameUI view) {
        this.model = model;
        this.view = view;


        gameLoop = new Timer(150, e -> {
            if (model.getCurrentState() == GameState.PLAYING) {
                model.getSnake().move(); // Bắt rắn nhích 1 bước
                view.render(model);      // Vẽ lại màn hình ngay lập tức
            }
        });

        this.view.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleInput(e);
            }
        });
    }

    public void startGame() {
        view.render(model);
        view.showGameScreen();
    }

    private void handleInput(KeyEvent e) {
        int key = e.getKeyCode();


        if (model.getCurrentState() == GameState.MENU && key == KeyEvent.VK_ENTER) {
            model.prepareNewGame();
            gameLoop.start(); // Kích hoạt động cơ cho rắn chạy!
        }

        if (model.getCurrentState() == GameState.PLAYING) {
            String currentDir = model.getSnake().getDirection();


            if (key == KeyEvent.VK_UP && !currentDir.equals("DOWN")) {
                model.getSnake().setDirection("UP");
            }
            if (key == KeyEvent.VK_DOWN && !currentDir.equals("UP")) {
                model.getSnake().setDirection("DOWN");
            }
            if (key == KeyEvent.VK_LEFT && !currentDir.equals("RIGHT")) {
                model.getSnake().setDirection("LEFT");
            }
            if (key == KeyEvent.VK_RIGHT && !currentDir.equals("LEFT")) {
                model.getSnake().setDirection("RIGHT");
            }
        }

        view.render(model);
    }
}