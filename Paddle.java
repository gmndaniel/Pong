package PongGame;

import java.awt.*;

class Paddle {
    private static int FIELD_HEIGHT;
    protected static int PADDLE_WIDTH = 20;
    private static int PADDLE_HEIGHT = 80;
    protected static int OFFSET = 40;

    protected Points points;

    void addPoint() {
        points.addPoint();
    }

    enum PaddleType {AI, PLAYER}
    enum Side {LEFT, RIGHT}
    Side side;

    private boolean upAccel;
    private boolean downAccel;
    private double y;

    public void setY(double y) {
        this.y = y;
    }


    double getYSpeed() {
        return ySpeed;
    }

    protected double ySpeed;
    protected int x;

    Paddle(int FIELD_WIDTH, int FIELD_HEIGHT) {
        Paddle.FIELD_HEIGHT = FIELD_HEIGHT;
        y = (float)(FIELD_HEIGHT / 2);
        ySpeed = 0;
    }

    int getX() {
        return x;
    }

    static int getPaddleWidth() {
        return PADDLE_WIDTH;
    }

    static int getPaddleHeight() {
        return PADDLE_HEIGHT;
    }

    void setUpAccel(boolean upAccel) {
        this.upAccel = upAccel;
    }

    void setDownAccel(boolean downAccel) {
        this.downAccel = downAccel;
    }

    public void draw(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(x, (int) y, PADDLE_WIDTH, PADDLE_HEIGHT);
        points.draw(g);
    }

    void move() {
        if (upAccel) {
            resetIfOppositeDirection(ySpeed > 0);
            ySpeed -= 0.02;
        } else if (downAccel) {
            resetIfOppositeDirection(ySpeed < 0);
            ySpeed += 0.02;
        } else {
            ySpeed *= 0.990;
        }
        limitSpeed();
        y += ySpeed;
        limitYToBoundaries();
    }

    private void resetIfOppositeDirection(boolean opDir) {
        if (opDir) {
            ySpeed = 0;
        }
    }

    private void limitYToBoundaries() {
        if (y < 0) {
            y = 0;
        } else if (y + PADDLE_HEIGHT > FIELD_HEIGHT) {
            y = FIELD_HEIGHT - PADDLE_HEIGHT;
        }
    }

    private void limitSpeed() {
        if (ySpeed >= 0.5) {
            ySpeed = 0.5;
        } else if (ySpeed <= -0.5) {
            ySpeed = -0.5;
        }
    }

    int getY() {
        return (int) y;
    }
}
