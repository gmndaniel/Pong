package PongGame;

class LeftPaddle extends Paddle {
    LeftPaddle(int FIELD_WIDTH, int FIELD_HEIGHT) {
        super(FIELD_WIDTH, FIELD_HEIGHT);
        side = Side.LEFT;
        points = new Points(0, 120, 40);
        x = OFFSET;
    }

}
