import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    int boardWidth = 360;
    int boardHeight = 640;

    //używane pliki
    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    //klasa bird
    int birdX = boardWidth / 8;
    int birdY = boardWidth / 2;
    int birdWidth = 34;
    int birdHeight = 24;

    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }

    //klasa pipe
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64;  //scaled by 1/6
    int pipeHeight = 512;

    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img) {
            this.img = img;
        }
    }

    //logika gry
    Bird bird;
    int velocityX = -4; //rury poruszają się do lewej strony symulując ruch ptaka w prawo
    int velocityY = 0; //poruszanie się ptaka góra/dół
    int gravity = 1;

    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipeTimer;
    boolean gameOver = false;
    double score = 0;

    // flaga określająca stan gry (menu lub gra)
    boolean inMenu = true;

    FlappyBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        //ładowanie plików
        backgroundImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        //ptak
        bird = new Bird(birdImg);
        pipes = new ArrayList<Pipe>();

        //timer dla rur
        placePipeTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });

        //timer gry
        gameLoop = new Timer(1000 / 60, this); //jak długo zajmuje uruchomienie timera, millisekundy pomiędzy klatkami
    }

    void placePipes() {
        //(0-1) * pipeHeight/2.
        // 0 -> -128 (pipeHeight/4)
        // 1 -> -128 - 256 (pipeHeight/4 - pipeHeight/2) = -3/4 pipeHeight
        int randomPipeY = (int) (pipeY - pipeHeight / 4 - Math.random() * (pipeHeight / 2));
        int openingSpace = boardHeight / 4;

        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        //tło
        g.drawImage(backgroundImg, 0, 0, this.boardWidth, this.boardHeight, null);

        if (inMenu) {
            drawMenu(g); // Draw menu jeśli jesteśmy w "menu mode"
        } else {
            drawGame(g); // jeśli nie to mamy gierkę
        }
    }

    public void drawMenu(Graphics g) {
        // Draw menu
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.BOLD, 48));
        g.drawString("Flappy Bird", 60, 200);

        g.setFont(new Font("Arial", Font.PLAIN, 24));
        g.drawString("Press SPACE to start", 60, 300);
    }

    public void drawGame(Graphics g) {
        //ptak
        g.drawImage(birdImg, bird.x, bird.y, bird.width, bird.height, null);

        //rury
        for (Pipe pipe : pipes) {
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        //wynik
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        if (gameOver) {
            g.drawString("Game Over: " + String.valueOf((int) score), 10, 35);
        } else {
            g.drawString(String.valueOf((int) score), 10, 35);
        }
    }

    public void move() {
        if (inMenu) return; // jeśli jesteśmy w menu nie rusza się nic

        //bird
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0); //dodanie "grawitacji" do bird.y, ograniczenie bird.y do górnej krawędzi

        //pipes
        for (Pipe pipe : pipes) {
            pipe.x += velocityX;

            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                score += 0.5; //0.5 ponieważ są dwie rury a więc 0.5*2 = 1, 1 dla każdego "zestawu" rur
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
        return a.x < b.x + b.width &&   //a lewy górny róg nie dosięga b prawego górnego rogu
                a.x + a.width > b.x &&   //a prawy górny róg przechodzi prawy lewy róg b
                a.y < b.y + b.height &&  //a lewy górny róg nie dosięga b dolnego lewego
                a.y + a.height > b.y;    //a dolny lewy róg przechodzi górny lewy róg b
    }

    @Override
    public void actionPerformed(ActionEvent e) { //co każde x milisekund przez timer gameLoop
        if (!inMenu) {
            move();
            repaint();
        }

        if (gameOver) {
            placePipeTimer.stop();
            gameLoop.stop();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (inMenu) {
                //rozpocznij grę
                inMenu = false;
                gameLoop.start();
                placePipeTimer.start();
            } else if (gameOver) {
                //restartuj grę
                bird.y = birdY;
                velocityY = 0;
                pipes.clear();
                gameOver = false;
                score = 0;
                gameLoop.start();
                placePipeTimer.start();
            } else {
                //skok ptaka
                velocityY = -9;
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}
