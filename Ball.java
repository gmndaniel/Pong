package PongGame;

import java.awt.*;

public class Ball {
    private int HEIGHT;
    private double xSpeed;
    private double ySpeed;
    private double x;
    private double y;
    private int r;

    void setX(int x) {
        this.x = (double) x;
    }

    int getR() {
        return r;
    }

    double getXSpeed() {
        return xSpeed;
    }

    void setYSpeed(double ySpeed) {
        this.ySpeed = ySpeed;
    }

    double getYSpeed() {
        return ySpeed;
    }

    void setXSpeed(double xSpeed) {
        this.xSpeed = xSpeed;
    }

    Ball(int WIDTH, int HEIGHT) {
        r = 10;
        this.HEIGHT = HEIGHT;
        this.xSpeed = randomizeSpeed(0.2, 0.4);
        this.ySpeed = randomizeSpeed(0.3, 0.5);
        this.x = ((double) WIDTH) / 2;
        this.y = ((double) HEIGHT) / 2;
    }

    private double randomizeSpeed(double min, double max) {
        double symbol = Math.random() > 0.5 ? 1 : -1;
        return symbol * (Math.random() * (max) + min);
    }

    public void draw(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillOval((int) x - r, (int) y - r, 2 * r, 2 * r);
    }

    void move() {
        x += xSpeed;
        y += ySpeed;
    }

    int getX() {
        return (int) x;
    }

    int getY() {
        return (int) y;
    }

}
