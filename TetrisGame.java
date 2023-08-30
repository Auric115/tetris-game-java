import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.util.HashMap;
import java.util.Map;

public class TetrisGame extends JPanel implements ActionListener, KeyListener {
    private final int BOARD_WIDTH = 10;
    private final int BOARD_HEIGHT = 22;
    private final int BLOCK_SIZE = 30;
    private int[][] board = new int[BOARD_HEIGHT][BOARD_WIDTH];
    private Timer timer;
    private Font largeFont;
    private Font mediumFont;

    private boolean gameEnd;
    private int highScore = 0;
    private int score;
    private int linesCleared;
    private int currentLevel;
    private int initialTimerDelay = 500; // Initial timer delay in milliseconds
    private int timerDelay = initialTimerDelay;
    private boolean pieceBanked;

    private Tetromino currentPiece;
    private Tetromino nextPiece;

    private Map<Integer, Color> shapeColorMap = new HashMap<>();

    public TetrisGame() {
        gameEnd = false;
        largeFont = new Font("Arial", Font.BOLD, 36);
        mediumFont = new Font("Arial", Font.BOLD, 30);
        timer = new Timer(500, this);
        pieceBanked = false;
        currentPiece = new Tetromino();
        nextPiece = new Tetromino();

        loadHighScore();
        score = 0;
        linesCleared = 0;
        currentLevel = 0;
        timer.setDelay(initialTimerDelay);

        setPreferredSize(new Dimension(BOARD_WIDTH * BLOCK_SIZE, BOARD_HEIGHT * BLOCK_SIZE));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        shapeColorMap.put(0, Color.BLACK);
        shapeColorMap.put(1, Color.CYAN);
        shapeColorMap.put(2, Color.MAGENTA);
        shapeColorMap.put(3, Color.GREEN);
        shapeColorMap.put(4, Color.RED);
        shapeColorMap.put(5, Color.YELLOW);
        shapeColorMap.put(6, Color.ORANGE);

        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        movePieceDown();
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!gameEnd) {
            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                movePiece(-1, 0);
            } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                movePiece(1, 0);
            } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                score += 1;
                movePieceDown();
            } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                currentPiece.rotate();
            } else if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                bankPiece();
            }
        }
        repaint();
    }

    private void movePiece(int dx, int dy) {
        currentPiece.move(dx, dy);
        if (collision()) {
            currentPiece.move(-dx, -dy);
        }
    }

    private void movePieceDown() {
        currentPiece.move(0, 1);
        if (collision()) {
            currentPiece.move(0, -1);
            currentPiece.merge();
            pieceBanked = false;
            clearFilledRows();

            if (currentPiece.y <= 2) {
                gameOver();
                return;
            }

            currentPiece = nextPiece;
            nextPiece = new Tetromino();
        }
    }

    private boolean collision() {
        for (int row = 0; row < currentPiece.shape.length; row++) {
            for (int col = 0; col < currentPiece.shape[row].length; col++) {
                if (currentPiece.shape[row][col] != 0) {
                    int boardX = currentPiece.x + col;
                    int boardY = currentPiece.y + row;
                    if (boardX < 0 || boardX >= BOARD_WIDTH || boardY >= BOARD_HEIGHT || board[boardY][boardX] != 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void clearFilledRows() {
        int currentLinesCleared = 0;
        for (int row = BOARD_HEIGHT - 1; row >= 0; row--) {
            boolean rowIsFilled = true;
            for (int col = 0; col < BOARD_WIDTH; col++) {
                if (board[row][col] == 0) {
                    rowIsFilled = false;
                    break;
                }
            }

            if (rowIsFilled) {
                for (int r = row; r > 0; r--) {
                    System.arraycopy(board[r - 1], 0, board[r], 0, BOARD_WIDTH);
                }
                for (int col = 0; col < BOARD_WIDTH; col++) {
                    board[0][col] = 0;
                }

                currentLinesCleared++;
                if (linesCleared % 10 == 0) {
                    currentLevel = linesCleared / 10;
                    timerDelay = initialTimerDelay - currentLevel * 50; // Decrease delay as level increases
                    timer.setDelay(timerDelay);
                }

                row++; // Check the same row again since we shifted rows down
            }
        }

        if (currentLinesCleared > 0) {
            int points = calculatePoints(currentLinesCleared);
            score += points;
            if (score > highScore) {
                highScore = score;
                saveHighScore();
            }
            linesCleared += currentLinesCleared;
        }
    }

    private int calculatePoints(int lines) {
        int basePoints;
        switch (lines) {
            case 1:
                basePoints = 40;
                break;
            case 2:
                basePoints = 100;
                break;
            case 3:
                basePoints = 300;
                break;
            case 4:
                basePoints = 1200;
                break;
            default:
                basePoints = 0;
                break;
        }
        return basePoints * (currentLevel + 1);
    }

    private void saveHighScore() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("highscore.txt"))) {
            writer.write(Integer.toString(highScore));
        } catch (IOException e) {
            // Handle file write error
        }
    }

    private void loadHighScore() {
        try (BufferedReader reader = new BufferedReader(new FileReader("highscore.txt"))) {
            String line = reader.readLine();
            if (line != null) {
                highScore = Integer.parseInt(line);
            }
        } catch (IOException e) {
            // Handle file read error
        }
    }

    private void bankPiece() {
        if (!pieceBanked) {
            Tetromino tempPiece = currentPiece;
            currentPiece = nextPiece;
            nextPiece = tempPiece;
            pieceBanked = true;
        }
    }

    private void gameOver() {
        gameEnd = true;
        timer.stop();
        System.out.println("Game Over");

        timerDelay = initialTimerDelay;
        timer.setDelay(timerDelay);

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Game Over! Play again?",
                "Game Over",
                JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            resetGame();
        } else {
            System.exit(0); // Quit the game
        }
    }

    private void resetGame() {
        gameEnd = false;
        // Clear the board
        for (int row = 0; row < BOARD_HEIGHT; row++) {
            for (int col = 0; col < BOARD_WIDTH; col++) {
                board[row][col] = 0;
            }
        }

        // Reset variables
        score = 0;
        linesCleared = 0;
        currentLevel = 0;
        timerDelay = initialTimerDelay;
        pieceBanked = false;

        currentPiece = new Tetromino();
        nextPiece = new Tetromino();

        timer.setDelay(timerDelay);
        timer.restart();
    }

    private class Tetromino {
        private int[][] shape;
        private int x, y;
        private int shapeIdentifier;

        private final int[][][] SHAPES = {
                // Tetromino shapes and rotations
                {}, // Empty Shape
                { { 1, 1, 1, 1 } }, // I shape
                { { 1, 1, 1 }, { 0, 1, 0 } }, // T shape
                { { 1, 1, 0 }, { 0, 1, 1 } }, // S shape
                { { 0, 1, 1 }, { 1, 1, 0 } }, // Z shape
                { { 1, 1 }, { 1, 1 } }, // O shape
                { { 1, 1, 1 }, { 0, 0, 1 } } // L shape
                // Add more shapes here
        };

        public Tetromino() {

            shapeIdentifier = (int) ((Math.random() * (SHAPES.length - 1) + 1));
            shape = SHAPES[shapeIdentifier];
            x = BOARD_WIDTH / 2 - shape[0].length / 2;
            y = 2;
        }

        public void move(int dx, int dy) {
            x += dx;
            y += dy;
        }

        public void rotate() {
            int[][] newShape = new int[shape[0].length][shape.length];
            for (int row = 0; row < shape.length; row++) {
                for (int col = 0; col < shape[row].length; col++) {
                    newShape[col][shape.length - 1 - row] = shape[row][col];
                }
            }

            if (!collisionAfterRotation(newShape)) {
                shape = newShape;
            }
        }

        private boolean collisionAfterRotation(int[][] newShape) {
            for (int row = 0; row < newShape.length; row++) {
                for (int col = 0; col < newShape[row].length; col++) {
                    if (newShape[row][col] != 0) {
                        int boardX = x + col;
                        int boardY = y + row;
                        if (boardX < 0 || boardX >= BOARD_WIDTH || boardY >= BOARD_HEIGHT
                                || board[boardY][boardX] != 0) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        public void merge() {
            for (int row = 0; row < shape.length; row++) {
                for (int col = 0; col < shape[row].length; col++) {
                    if (shape[row][col] != 0) {
                        board[y + row][x + col] = shapeIdentifier;
                    }
                }
            }
        }

        // Other methods
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Unused
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Unused
    }

    private void drawBlock(Graphics g, int row, int col, Color color) {
        int x = col * BLOCK_SIZE;
        int y = row * BLOCK_SIZE;
        g.setColor(color);
        g.fillRect(x, y, BLOCK_SIZE, BLOCK_SIZE);
        g.setColor(Color.BLACK);
        g.drawRect(x, y, BLOCK_SIZE, BLOCK_SIZE);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw the board
        for (int row = 0; row < BOARD_HEIGHT; row++) {
            for (int col = 0; col < BOARD_WIDTH; col++) {
                int blockValue = board[row][col];
                if (row == 2 && blockValue == 0) {
                    g.setColor(Color.WHITE); // Highlight top row in white
                } else {
                    g.setColor(getBlockColor(blockValue));
                }
                g.fillRect(col * BLOCK_SIZE, row * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
                g.setColor(Color.BLACK);
                g.drawRect(col * BLOCK_SIZE, row * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
            }
        }

        // Draw the current piece
        for (int row = 0; row < currentPiece.shape.length; row++) {
            for (int col = 0; col < currentPiece.shape[row].length; col++) {
                if (currentPiece.shape[row][col] != 0) {
                    int x = currentPiece.x + col;
                    int y = currentPiece.y + row;
                    drawBlock(g, y, x, getBlockColor(currentPiece.shapeIdentifier));
                }
            }
        }

        // Draw the top bar text
        g.setColor(Color.WHITE);
        g.drawString("Level: " + currentLevel, (int) (0.4 * BLOCK_SIZE), (int) (0.6 * BLOCK_SIZE));
        g.drawString("| Created by: BJ Anderson |", (int) (2.4 * BLOCK_SIZE), (int) (0.6 * BLOCK_SIZE));
        g.drawString("Next Piece:", (int) (7.8 * BLOCK_SIZE), (int) (0.6 * BLOCK_SIZE));

        g.drawString("Score: " + score, (int) (0.4 * BLOCK_SIZE), (int) (1.2 * BLOCK_SIZE));
        g.drawString("High Score: " + highScore, (int) (0.4 * BLOCK_SIZE), (int) (1.8 * BLOCK_SIZE));

        g.setFont(mediumFont);
        g.drawString("TETRIS", (int) (3.8 * BLOCK_SIZE), (int) (1.6 * BLOCK_SIZE));

        // Draw the preview of the next piece
        int previewX = (int) (8 * BLOCK_SIZE);
        int previewY = (int) (0.8 * BLOCK_SIZE);
        int previewBlockSize = BLOCK_SIZE / 2;

        for (int row = 0; row < nextPiece.shape.length; row++) {
            for (int col = 0; col < nextPiece.shape[row].length; col++) {
                if (nextPiece.shape[row][col] != 0) {
                    g.setColor(getBlockColor(nextPiece.shapeIdentifier));
                    g.fillRect(previewX + col * previewBlockSize, previewY + row * previewBlockSize,
                            previewBlockSize, previewBlockSize);
                    g.setColor(Color.BLACK);
                    g.drawRect(previewX + col * previewBlockSize, previewY + row * previewBlockSize,
                            previewBlockSize, previewBlockSize);
                }
            }
        }

        // Draw the game over screen
        if (gameEnd) {
            g.setColor(Color.RED);
            g.setFont(largeFont);
            g.drawString("GAME OVER", (int) (1.2 * BLOCK_SIZE), (BOARD_HEIGHT / 2) * BLOCK_SIZE);
        }
    }

    private Color getBlockColor(int shapeIdentifier) {
        return shapeColorMap.get(shapeIdentifier);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Tetris");
        TetrisGame game = new TetrisGame();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
