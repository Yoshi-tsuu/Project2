import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Random;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    private int boardWidth = 360;
    private int boardHeight = 640;

    private Image backgroundImg;
    private Image birdImg;
    private Image topPipeImg;
    private Image bottomPipeImg;

    private Bird bird;
    private int velocityX = -4;
    private int velocityY = 0;
    private int gravity = 1;

    private ArrayList<Pipe> pipes;
    private Random random = new Random();

    private Timer gameLoop;
    private Timer placePipeTimer;
    private boolean gameOver = false;
    private double score = 0;

    private boolean inMenu = true;
    private boolean enterNickname = true;
    private String nickname = "";

    private RankingManager rankingManager;

    public FlappyBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        rankingManager = new RankingManager();

        backgroundImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        bird = new Bird(birdImg);
        pipes = new ArrayList<>();

        placePipeTimer = new Timer(1500, e -> placePipes());
        gameLoop = new Timer(1000 / 60, this);
    }

    void placePipes() {
        int randomPipeY = (int) (-Pipe.PIPE_HEIGHT / 4 - Math.random() * (Pipe.PIPE_HEIGHT / 2));
        int openingSpace = boardHeight / 4;

        Pipe topPipe = new Pipe(topPipeImg, boardWidth, randomPipeY);
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg, boardWidth, randomPipeY + Pipe.PIPE_HEIGHT + openingSpace);
        pipes.add(bottomPipe);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        g.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight, null);
        if (enterNickname) {
            drawNicknameEntry(g);
        } else if (inMenu) {
            drawMenu(g);
        } else {
            drawGame(g);
        }
    }

    // Ekran wprowadzania nicku
    public void drawNicknameEntry(Graphics g) {
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.BOLD, 32));
        g.drawString("Enter your nickname:", 30, 200);
        g.drawString(nickname + "_", 30, 250);  // Wyświetlamy nick i podkreślenie jako wskaźnik
    }

    public void drawMenu(Graphics g) {
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.BOLD, 48));
        g.drawString("Flappy Bird", 60, 200);
        g.setFont(new Font("Arial", Font.PLAIN, 24));
        g.drawString("Press SPACE to start", 60, 300);
    }

    public void drawGame(Graphics g) {
        g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);

        for (Pipe pipe : pipes) {
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        g.drawString(gameOver ? "Game Over: " + (int) score : String.valueOf((int) score), 10, 35);
    }

    public void move() {
        if (inMenu || enterNickname) return;

        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0);

        for (Pipe pipe : pipes) {
            pipe.x += velocityX;
            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                score += 0.5;
                pipe.passed = true;
            }
            if (collision(bird, pipe)) {
                gameOver = true;
            }
        }
        if (bird.y > boardHeight) {
            gameOver = true;
        }
    }

    boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width && a.x + a.width > b.x && a.y < b.y + b.height && a.y + a.height > b.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!inMenu && !enterNickname) {
            move();
            repaint();
        }
        if (gameOver) {
            rankingManager.updateScore(nickname, (int) score);
            placePipeTimer.stop();
            gameLoop.stop();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (enterNickname) {
            if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && !nickname.isEmpty()) {
                nickname = nickname.substring(0, nickname.length() - 1);
                repaint();  // Odświeżamy ekran po usunięciu znaku
            } else if (e.getKeyCode() == KeyEvent.VK_ENTER && !nickname.isEmpty()) {
                enterNickname = false;
                inMenu = true;
                repaint();  // Przejście do menu
            } else {
                char keyChar = e.getKeyChar();
                if (Character.isLetterOrDigit(keyChar) && nickname.length() < 10) {
                    nickname += keyChar;
                    repaint();  // Odświeżamy ekran po dodaniu znaku
                }
            }
        } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (inMenu) {
                inMenu = false;
                gameLoop.start();
                placePipeTimer.start();
            } else if (gameOver) {
                bird.resetPosition();
                velocityY = 0;
                pipes.clear();
                gameOver = false;
                score = 0;
                gameLoop.start();
                placePipeTimer.start();
            } else {
                velocityY = -9;
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}
