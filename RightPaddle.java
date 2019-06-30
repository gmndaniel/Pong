package PongGame;

class RightPaddle extends Paddle {
    RightPaddle(int FIELD_WIDTH, int FIELD_HEIGHT) {
        super(FIELD_WIDTH, FIELD_HEIGHT);
        side = Side.RIGHT;
        points = new Points(0, FIELD_WIDTH - 150, 40);
        x = FIELD_WIDTH - OFFSET - PADDLE_WIDTH;
    }
}
