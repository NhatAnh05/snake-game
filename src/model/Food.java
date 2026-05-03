package model;
import java.util.Random;
import java.util.List;

public class Food {
    private Point position;

    public void spawn(List<Point> snakeBody) {
        Random r = new Random();

        this.position = new Point(r.nextInt(30), r.nextInt(30));
    }

    public Point getPosition() { return position; }
}