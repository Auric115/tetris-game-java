import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.util.HashMap;
import java.util.Map;

public class TetrisGame extends JPanel implements ActionListener, KeyListener {
    private final int BOARD_WIDTH = 10;
    private final int BOARD_HEIGHT = 20;
    private final int BLOCK_SIZE = 30;
    private int[][] board = new int[BOARD_HEIGHT][BOARD_WIDTH];
    private Timer timer;
    private Tetromino currentPiece;

    private Map<Integer, Color> shapeColorMap = new HashMap<>();

    public TetrisGame() {
        timer = new Timer(500, this);
        currentPiece = new Tetromino();

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

    private void clearFilledRows() {
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
                row++; // Check the same row again since we shifted rows down
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        movePieceDown();
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            movePiece(-1, 0);
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            movePiece(1, 0);
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            movePieceDown();
        } else if (e.getKeyCode() == KeyEvent.VK_UP) {
            currentPiece.rotate();
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
            clearFilledRows();

            if (currentPiece.y <= 0) {
                gameOver();
                return;
            }

            currentPiece = new Tetromino();
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

    private void gameOver() {
        // Implement game over logic, such as displaying a message or resetting the game
        // For instance:
        timer.stop();
        System.out.println("Game Over");
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

            shapeIdentifier = (int) ((Math.random() * SHAPES.length) + 1);
            shape = SHAPES[shapeIdentifier];
            x = BOARD_WIDTH / 2 - shape[0].length / 2;
            y = 0;
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
                        board[y + row][x + col] = shape[row][col];
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
                if (row == 0) {
                    g.setColor(Color.RED); // Highlight top row in red
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
    }

    private Color getBlockColor(int shapeIdentifier) {
        return shapeColorMap.getOrDefault(shapeIdentifier, Color.BLACK);
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
