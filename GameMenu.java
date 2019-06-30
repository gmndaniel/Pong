package PongGame;

import java.awt.*;

public class GameMenu {
    public void draw(Graphics g) {
        g.setColor(Color.ORANGE);
        g.setFont(new Font("Helvetica", Font.PLAIN, 70));
        g.drawString("PONG", 400, 100);
        g.setColor(Color.WHITE);
        g.setFont(new Font("TimesRoman", Font.PLAIN, 50));
        g.drawString("1. Player vs AI", 300, 200);
        g.drawString("2. Player vs Player", 300, 300);
        g.drawString("3. AI vs AI", 300, 400);
    }
}