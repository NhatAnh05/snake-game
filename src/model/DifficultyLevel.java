package model;

/**
 * UI-01: Enum biểu diễn độ khó của trò chơi.
 * EASY chạy chậm hơn, NORMAL là mặc định, HARD chạy nhanh hơn.
 */
public enum DifficultyLevel {
    EASY(180, "EASY"),
    NORMAL(140, "NORMAL"),
    HARD(95, "HARD");

    private final int delay;
    private final String label;

    DifficultyLevel(int delay, String label) {
        this.delay = delay;
        this.label = label;
    }

    public int getDelay() {
        return delay;
    }

    public String getLabel() {
        return label;
    }
}
