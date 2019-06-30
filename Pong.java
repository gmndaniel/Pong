package PongGame;
// A new comment
import javax.sound.sampled.*;
import java.applet.Applet;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URL;

import static java.awt.event.KeyEvent.*;


public class Pong extends Applet implements Runnable, KeyListener {

    private static Mixer mixer;
    private static Clip paddleHitClip;
    private static Clip wallBounceClip;
    private static Clip ballMissClip;
    private static AudioInputStream audioStream;
    private URL paddleHitURL;
    private URL wallBounceURL;
    private URL ballMissURL;
    private static final int PvAI = 0;
    private static final int PvP = 1;
    private static final int AIvAI = 2;
    private static final int FIELD_WIDTH = 1000;
    private static final int FIELD_HEIGHT = 500;
    private static final int LEFT_UP = 0;
    private static final int LEFT_DOWN = 1;
    private static final int RIGHT_UP = 2;
    private static final int RIGHT_DOWN = 3;
    private static final int ESC = 4;
    //    private static int p1StartY = FIELD_HEIGHT / 2;
//    private static int p2StartY = p1StartY;
    private Paddle.PaddleType p1PaddleType;
    private Paddle.PaddleType p2PaddleType;
    private RightPaddle player1;
    private LeftPaddle player2;
    private Ball ball;
    private Graphics gfx;
    private Image img;
    private GameMenu gameMenu;
    private Thread gameThread;
    private boolean[] keysPressed;

    private boolean[] playMode;
    private boolean gameRunning;
    private boolean gameStarted;
    private boolean programRunning;


    @Override
    public void init() {
        setupSound();
        this.resize(FIELD_WIDTH, FIELD_HEIGHT);
        img = createImage(FIELD_WIDTH, FIELD_HEIGHT);
        gfx = img.getGraphics();
        gameMenu = new GameMenu();
        playMode = new boolean[3];
        gameThread = new Thread(this);
        gameThread.start();

        keysPressed = new boolean[5];
        this.addKeyListener(this);

    }

    private void setupSound() {
        Mixer.Info[] mixInfos = AudioSystem.getMixerInfo();
        mixer = AudioSystem.getMixer(mixInfos[0]);
        DataLine.Info dataInfo = new DataLine.Info(Clip.class, null);
        try {
            paddleHitClip = (Clip) mixer.getLine(dataInfo);
            wallBounceClip = (Clip) mixer.getLine(dataInfo);
            ballMissClip = (Clip) mixer.getLine(dataInfo);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

        try {
            paddleHitURL = Pong.class.getResource("/PongGame/paddle_hit.wav");
            wallBounceURL = Pong.class.getResource("/PongGame/wall_bounce.wav");
            ballMissURL = Pong.class.getResource("/PongGame/ball_miss.wav");
            audioStream = AudioSystem.getAudioInputStream(paddleHitURL);
            paddleHitClip.open(audioStream);
            audioStream = AudioSystem.getAudioInputStream(wallBounceURL);
            wallBounceClip.open(audioStream);
            audioStream = AudioSystem.getAudioInputStream(ballMissURL);
            ballMissClip.open(audioStream);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void pause(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void paint(Graphics g) {
        gfx.setColor(Color.BLACK);
        gfx.fillRect(0, 0, FIELD_WIDTH, FIELD_HEIGHT);
        if (gameStarted) {
            midLine(gfx);
            player1.draw(gfx);
            player2.draw(gfx);
            ball.draw(gfx);
        } else {
            gameMenu.draw(gfx);
        }
        g.drawImage(img, 0, 0, this);
    }

    private void midLine(Graphics g) {
        g.setColor(Color.WHITE);
        int x = FIELD_WIDTH / 2;

        Graphics2D g2d = (Graphics2D) g.create();

        Stroke dashed = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
        g2d.setStroke(dashed);
        g2d.drawLine(x, 0, x, FIELD_HEIGHT);
    }

    @Override
    public void update(Graphics g) {
        paint(g);
    }

    @Override
    public void run() {
        programRunning = true;

        while (programRunning) {
            showMenuLoop();
            startNewGame();
            runGameLoop();
        }
    }

    private void startNewGame() {
        createPaddles();
        ballReset();
        gameRunning = true;
    }

    private void runGameLoop() {
        while (gameRunning) {
            AIControl();
            movePieces();
            detectCollision();
            detectWallBounce();
            handleMisses();
            repaint();
            handleKeyPresses();
            pause(1);
        }
    }

    private void detectWallBounce() {
        bounceIfHitWall(ball.getY() < ball.getR());
        bounceIfHitWall(ball.getY() > FIELD_HEIGHT - ball.getR());
    }

    private void bounceIfHitWall(boolean b) {
        if (b) {
            playClip(wallBounceClip);
            ball.setYSpeed(ball.getYSpeed() * (-1));
        }
    }

    private void movePieces() {
        player1.move();
        player2.move();
        ball.move();
    }

    private void createPaddles() {
        player1 = new RightPaddle(FIELD_WIDTH, FIELD_HEIGHT);
        player2 = new LeftPaddle(FIELD_WIDTH, FIELD_HEIGHT);
    }

    private void showMenuLoop() {
        while (!gameStarted) {
            checkMenuInput();
            pause(10);
        }
    }

    private void AIControl() {
        AIBehavior(p1PaddleType, player1);
        AIBehavior(p2PaddleType, player2);
    }

    private void AIBehavior(Paddle.PaddleType paddleType, Paddle paddle) {
        int ballYMid = ball.getY();
        if (paddleType == Paddle.PaddleType.AI) {
            int pYMid = paddle.getY() + Paddle.getPaddleHeight() / 2;
            int side_down;
            int side_up;

            if (paddle.side == Paddle.Side.LEFT) {
                side_down = LEFT_DOWN;
                side_up = LEFT_UP;
            } else {
                side_down = RIGHT_DOWN;
                side_up = RIGHT_UP;
            }

            if (isBallMovingToPaddle(paddle)) {
                reactAggressively(ballYMid, pYMid, side_down, side_up);
            } else {
                setAIKeys(side_down, false, side_up, false);
                reactRelaxed(ballYMid, pYMid, side_down, side_up);
            }
        }
    }

    private void reactRelaxed(int ballYMid, int pYMid, int side_down, int side_up) {
        if (ballYMid > pYMid) {
            if (ball.getX() % 10 == 1) {
                setAIKeys(side_down, true, side_up, false);
            }
        } else {
            if (ball.getX() % 10 == 1) {
                setAIKeys(side_down, false, side_up, true);
            }
        }
    }

    private void setAIKeys(int side_down, boolean setSide_down, int side_up, boolean setSide_up) {
        keysPressed[side_down] = setSide_down;
        keysPressed[side_up] = setSide_up;
    }

    private void reactAggressively(int ballYMid, int pYMid, int side_down, int side_up) {
        if (ballYMid > pYMid) {
            keysPressed[side_down] = true;
            keysPressed[side_up] = false;
        } else {
            keysPressed[side_up] = true;
            keysPressed[side_down] = false;
        }
    }

    private boolean isBallMovingToPaddle(Paddle paddle) {
        if (ball.getXSpeed() < 0 && paddle.side == Paddle.Side.LEFT) {
            return true;
        } else {
            return ball.getXSpeed() > 0 && paddle.side == Paddle.Side.RIGHT;
        }
    }

    private void handleMisses() {
        if (ball.getX() + ball.getR() < 0) {
            playClip(ballMissClip);
            player1.addPoint();
            ballReset();
        } else if (ball.getX() - ball.getR() > FIELD_WIDTH) {
            playClip(ballMissClip);
            player2.addPoint();
            ballReset();
        }
    }

    private void ballReset() {
        ball = new Ball(FIELD_WIDTH, FIELD_HEIGHT);
    }

    private void detectCollision() {
        int ballMidX = ball.getX();
        int ballMidY = ball.getY();
        int ballTop = ballMidY - ball.getR();
        int ballBot = ballMidY + ball.getR();
        int ballLeft = ballMidX - ball.getR();
        int ballRight = ballMidX + ball.getR();
        int ballR = ball.getR();

        int p1Top = player1.getY();
        int p1Bot = p1Top + Paddle.getPaddleHeight();
        int p1Side = player1.getX();
        int p1Back = p1Side + Paddle.getPaddleWidth();
        int p1Mid = p1Top + Paddle.getPaddleHeight() / 2;

        int p2Top = player2.getY();
        int p2Bot = p2Top + Paddle.getPaddleHeight();
        int p2Back = player2.getX();
        int p2Side = p2Back + Paddle.getPaddleWidth();
        int p2Mid = p2Top + Paddle.getPaddleHeight() / 2;

        double yDif;


        yDif = (ball.getY() - p2Mid) * 0.01 + player2.getYSpeed();
        if (p2Side < ballMidX) {
            if (ballLeft < p2Side) {
                if (ballWithinPaddle(ballTop, ballBot, p2Top, p2Bot)) {
                    playClip(paddleHitClip);
                    ball.setX(p2Side + ballR);
                    ball.setXSpeed(-ball.getXSpeed());
                    ball.setYSpeed(yDif);
                }
            }
        } else if (p2Back < ballMidX) {
            if (ballWithinPaddle(ballTop, ballBot, p2Top, p2Bot)) {
                playClip(paddleHitClip);
                ball.setYSpeed(yDif);
            }
        }

        yDif = (ball.getY() - p1Mid) * 0.01 + player1.getYSpeed();
        if (ballMidX < p1Side) {
            if (p1Side < ballRight) {
                if (ballWithinPaddle(ballTop, ballBot, p1Top, p1Bot)) {
                    playClip(paddleHitClip);
                    ball.setX(p1Side - ballR);
                    ball.setXSpeed(-ball.getXSpeed());
                    ball.setYSpeed(yDif);
                }
            }
        } else if (ballMidX < p1Back) {
            if (ballWithinPaddle(ballTop, ballBot, p1Top, p1Bot)) {
                playClip(paddleHitClip);
                ball.setYSpeed(yDif);
            }
        }
    }

    private void playClip(Clip clip) {
        clip.setFramePosition(0);
        clip.start();
    }

    private boolean ballWithinPaddle(int ballTop, int ballBot, int pTop, int pBot) {
        return (pTop < ballBot && ballTop < pBot) || (pBot > ballTop && ballBot > pTop);
    }


    private void handleKeyPresses() {
        player1.setDownAccel(keysPressed[RIGHT_DOWN]);
        player1.setUpAccel(keysPressed[RIGHT_UP]);
        player2.setDownAccel(keysPressed[LEFT_DOWN]);
        player2.setUpAccel(keysPressed[LEFT_UP]);
        if (keysPressed[ESC]) {
            gameReset();
        }
    }

    private void checkMenuInput() {
        if (playMode[PvAI]) {
            p1PaddleType = Paddle.PaddleType.PLAYER;
            p2PaddleType = Paddle.PaddleType.AI;
            playMode[PvAI] = false;
            gameStarted = true;
        } else if (playMode[PvP]) {
            p1PaddleType = Paddle.PaddleType.PLAYER;
            p2PaddleType = Paddle.PaddleType.PLAYER;
            playMode[PvP] = false;
            gameStarted = true;
        } else if (playMode[AIvAI]) {
            p1PaddleType = Paddle.PaddleType.AI;
            p2PaddleType = Paddle.PaddleType.AI;
            playMode[AIvAI] = false;
            gameStarted = true;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Does nothing
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (playerPressedKey(key, VK_UP, p1PaddleType)) {
            setKeysPressed(RIGHT_UP, true, RIGHT_DOWN, false);
        } else if (playerPressedKey(key, VK_DOWN, p1PaddleType)) {
            setKeysPressed(RIGHT_DOWN, true, RIGHT_UP, false);
        } else if (playerPressedKey(key, VK_W, p2PaddleType)) {
            setKeysPressed(LEFT_UP, true, LEFT_DOWN, false);
        } else if (playerPressedKey(key, VK_S, p2PaddleType)) {
            setKeysPressed(LEFT_DOWN, true, LEFT_UP, false);
        } else if (menuKeyPressed(key, VK_1)) {
            setPlayMode(true, false, false);
        } else if (menuKeyPressed(key, VK_2)) {
            setPlayMode(false, true, false);
        } else if (menuKeyPressed(key, VK_3)) {
            setPlayMode(false, false, true);
        } else if (key == VK_ESCAPE) {
            keysPressed[ESC] = true;
        }
    }

    private boolean playerPressedKey(int key, int vkUp, Paddle.PaddleType p1PaddleType) {
        return key == vkUp && p1PaddleType == Paddle.PaddleType.PLAYER;
    }

    private boolean menuKeyPressed(int key, int compareKey) {
        return key == compareKey && !gameStarted;
    }

    private void setKeysPressed(int key1, boolean val1, int key2, boolean val2) {
        keysPressed[key1] = val1;
        keysPressed[key2] = val2;
    }

    private void setPlayMode(boolean PvAI, boolean PvP, boolean AIvAI) {
        playMode[Pong.PvAI] = PvAI;
        playMode[Pong.PvP] = PvP;
        playMode[Pong.AIvAI] = AIvAI;
    }

    private void gameReset() {
        gameStarted = false;
        gameRunning = false;
        resetKeysPressed();
    }

    private void resetKeysPressed() {
        for (int i = 0; i < keysPressed.length; ++i) {
            keysPressed[i] = false;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

        int key = e.getKeyCode();

        if (key == VK_UP) {
            keysPressed[RIGHT_UP] = false;
        } else if (key == VK_DOWN) {
            keysPressed[RIGHT_DOWN] = false;
        } else if (key == VK_W) {
            keysPressed[LEFT_UP] = false;
        } else if (key == VK_S) {
            keysPressed[LEFT_DOWN] = false;
        }
    }
}
