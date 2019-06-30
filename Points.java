package PongGame;

import java.awt.*;

public class Points {
    private int points;
    private int x;
    private int y;

    Points(int points, int x, int y) {
        this.points = points;
        this.x = x;
        this.y = y;
    }

    public void draw(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("TimesRoman", Font.PLAIN, 40));
        g.drawString("" + points, x, y);
    }

    void addPoint() {
        ++points;
    }
}
