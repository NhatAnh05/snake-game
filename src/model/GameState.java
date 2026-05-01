package model;

public enum GameState {
    MENU,       // Trạng thái chờ người chơi nhấn Start
    PLAYING,    // Trạng thái đang chơi (Rắn đang bò)
    PAUSED,     // Trạng thái tạm dừng (Nhiệm vụ Người 5)
    GAME_OVER   // Trạng thái chết (Nhiệm vụ Người 4)
}

