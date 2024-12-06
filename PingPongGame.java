import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class PingPongGame extends Frame implements Runnable, MouseMotionListener {
    private int paddleX, paddleY, paddleWidth = 100, paddleHeight = 10;
    private int ballX, ballY, ballDiameter = 20;
    private int ballXSpeed = 3, ballYSpeed = 3;
    private int frameWidth, frameHeight;
    private boolean gameRunning = true;
    private boolean showResetButton = false;
    private boolean showQuitButton = false;
    private int score = 0; // Score tracking

    private Image offscreenImage;
    private Graphics offscreenGraphics;

    public PingPongGame() {
        // Set up the frame
        setUndecorated(true); // Remove title bar and borders
        setResizable(false);

        // Enter full-screen mode
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (gd.isFullScreenSupported()) {
            gd.setFullScreenWindow(this);
            frameWidth = getWidth();
            frameHeight = getHeight();
        } else {
            frameWidth = 800; // Fallback to default size
            frameHeight = 600;
            setSize(frameWidth, frameHeight);
            setVisible(true);
        }

        paddleY = frameHeight - 50;
        ballX = frameWidth / 2;
        ballY = frameHeight / 2;

        addMouseMotionListener(this);

        // Add a mouse listener for the reset and quit buttons
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (showResetButton) {
                    resetGame();
                } else if (showQuitButton) {
                    System.exit(0);
                }
            }
        });

        // Hide the mouse pointer initially
        hideMousePointer();

        // Start the game thread
        Thread gameThread = new Thread(this);
        gameThread.start();
    }

    private void hideMousePointer() {
        setCursor(getToolkit().createCustomCursor(
                new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), new Point(), null));
    }

    private void showMousePointer() {
        setCursor(Cursor.getDefaultCursor());
    }

    @Override
    public void update(Graphics g) {
        if (offscreenImage == null) {
            offscreenImage = createImage(frameWidth, frameHeight);
            offscreenGraphics = offscreenImage.getGraphics();
        }

        // Clear the offscreen image
        offscreenGraphics.setColor(Color.BLACK);
        offscreenGraphics.fillRect(0, 0, frameWidth, frameHeight);

        // Draw the paddle
        offscreenGraphics.setColor(Color.GREEN);
        offscreenGraphics.fillRect(paddleX, paddleY, paddleWidth, paddleHeight);

        // Draw the ball
        offscreenGraphics.setColor(Color.RED);
        offscreenGraphics.fillOval(ballX, ballY, ballDiameter, ballDiameter);

        // Display the score
        offscreenGraphics.setColor(Color.WHITE);
        offscreenGraphics.setFont(new Font("Arial", Font.BOLD, 20));
        offscreenGraphics.drawString("Score: " + score, 10, 30);

        // Show "Game Over", reset button, and quit button if the game is over
        if (!gameRunning) {
            offscreenGraphics.setColor(Color.WHITE);
            offscreenGraphics.setFont(new Font("Arial", Font.BOLD, 30));
            offscreenGraphics.drawString("Game Over!", frameWidth / 2 - 90, frameHeight / 2 - 60);
            offscreenGraphics.setFont(new Font("Arial", Font.PLAIN, 20));
            offscreenGraphics.drawString("Your Score: " + score, frameWidth / 2 - 70, frameHeight / 2 - 20);
            offscreenGraphics.drawString("Click to Reset", frameWidth / 2 - 70, frameHeight / 2 + 20);

            // Draw quit button text
            showQuitButton = true;
            offscreenGraphics.drawString("Click to Quit", frameWidth / 2 - 70, frameHeight / 2 + 60);
        }

        // Draw the offscreen image to the screen
        g.drawImage(offscreenImage, 0, 0, this);
    }

    @Override
    public void run() {
        final int targetFPS = 60;
        final long frameDuration = 1000 / targetFPS; // Frame duration in milliseconds

        while (true) {
            if (gameRunning) {
                // Update the ball's position
                ballX += ballXSpeed;
                ballY += ballYSpeed;

                // Check for collisions with the walls
                if (ballX <= 0 || ballX >= frameWidth - ballDiameter) {
                    ballXSpeed = -ballXSpeed;
                }
                if (ballY <= 0) {
                    ballYSpeed = -ballYSpeed;
                }

                // Check for collisions with the paddle
                if (ballY + ballDiameter >= paddleY &&
                        ballX + ballDiameter >= paddleX &&
                        ballX <= paddleX + paddleWidth) {
                    ballYSpeed = -ballYSpeed;
                    ballY = paddleY - ballDiameter; // Prevent sticking

                    // Increase ball speed
                    ballXSpeed += (ballXSpeed > 0) ? 1 : -1;
                    ballYSpeed += (ballYSpeed > 0) ? 1 : -1;

                    // Increase score
                    score++;
                }

                // Check if the ball hits the bottom
                if (ballY >= frameHeight) {
                    gameRunning = false;
                    showMousePointer(); // Show mouse pointer when the game ends
                    showResetButton = true;
                }

                // Redraw the frame
                repaint();
            }

            // Maintain consistent frame rate
            try {
                Thread.sleep(frameDuration);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // Move the paddle horizontally based on the mouse position
        paddleX = e.getX() - paddleWidth / 2;

        // Ensure the paddle doesn't go outside the frame
        if (paddleX < 0) paddleX = 0;
        if (paddleX > frameWidth - paddleWidth) paddleX = frameWidth - paddleWidth;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // Handle mouse dragging in the same way as mouse movement
        mouseMoved(e);
    }

    // Reset the game
    private void resetGame() {
        ballX = frameWidth / 2;
        ballY = frameHeight / 2;
        ballXSpeed = 3;
        ballYSpeed = 3;
        score = 0; // Reset score
        gameRunning = true;
        showResetButton = false;
        showQuitButton = false;
        hideMousePointer(); // Hide mouse pointer when the game starts
        repaint();
    }

    public static void main(String[] args) {
        new PingPongGame();
    }
}
