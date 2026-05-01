package model;

public class ScoreManager {
    private int currentScore;

    public void resetScore() {
        this.currentScore = 0;
    }

    public int getCurrentScore() { return currentScore; }
}