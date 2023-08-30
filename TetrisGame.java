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

public class TetrisGame extends JPanel implements ActionListener, KeyListener {
    private final int BOARD_WIDTH = 10;
    private final int BOARD_HEIGHT = 20;
    private final int BLOCK_SIZE = 30;
    private int[][] board = new int[BOARD_HEIGHT][BOARD_WIDTH];
    private Timer timer;
    private Tetromino currentPiece;

    public TetrisGame() {
        timer = new Timer(500, this);
        currentPiece = new Tetromino();

        setPreferredSize(new Dimension(BOARD_WIDTH * BLOCK_SIZE, BOARD_HEIGHT * BLOCK_SIZE));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        timer.start();
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
            rotatePiece();
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
            currentPiece = new Tetromino();
        }
    }

    private void rotatePiece() {
        currentPiece.rotate();
        if (collision()) {
            currentPiece.rotateBack();
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

    private class Tetromino {
        private int[][] shape;
        private int x, y;
        private int rotation;

        private final int[][][] SHAPES = {
                // Tetromino shapes and rotations
                { { 1, 1, 1, 1 } }, // I shape
                { { 1, 1, 1 }, { 0, 1, 0 } }, // T shape
                { { 1, 1, 0 }, { 0, 1, 1 } }, // S shape
                { { 0, 1, 1 }, { 1, 1, 0 } }, // Z shape
                { { 1, 1 }, { 1, 1 } }, // O shape
                { { 1, 1, 1 }, { 0, 0, 1 } } // L shape
                // Add more shapes here
        };

        public Tetromino() {
            shape = SHAPES[(int) (Math.random() * SHAPES.length)];
            x = BOARD_WIDTH / 2 - shape[0].length / 2;
            y = 0;
            rotation = 0;
        }

        public void move(int dx, int dy) {
            x += dx;
            y += dy;
        }

        public void rotate() {
            rotation = (rotation + 1) % 4;
            int[][] newShape = SHAPES[rotation];
            if (x + newShape[0].length > BOARD_WIDTH) {
                x = BOARD_WIDTH - newShape[0].length;
            }
            shape = newShape;
        }

        public void rotateBack() {
            rotation = (rotation - 1 + 4) % 4;
            shape = SHAPES[rotation];
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

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw the board
        for (int row = 0; row < BOARD_HEIGHT; row++) {
            for (int col = 0; col < BOARD_WIDTH; col++) {
                int blockValue = board[row][col];
                if (blockValue != 0) {
                    g.setColor(getBlockColor(blockValue));
                    g.fillRect(col * BLOCK_SIZE, row * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
                    g.setColor(Color.BLACK);
                    g.drawRect(col * BLOCK_SIZE, row * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
                }
            }
        }

        // Draw the current piece
        for (int row = 0; row < currentPiece.shape.length; row++) {
            for (int col = 0; col < currentPiece.shape[row].length; col++) {
                if (currentPiece.shape[row][col] != 0) {
                    int x = (currentPiece.x + col) * BLOCK_SIZE;
                    int y = (currentPiece.y + row) * BLOCK_SIZE;
                    g.setColor(getBlockColor(currentPiece.shape[row][col]));
                    g.fillRect(x, y, BLOCK_SIZE, BLOCK_SIZE);
                    g.setColor(Color.BLACK);
                    g.drawRect(x, y, BLOCK_SIZE, BLOCK_SIZE);
                }
            }
        }
    }

    private Color getBlockColor(int value) {
        // Define a mapping of block values to colors
        switch (value) {
            case 1:
                return Color.CYAN; // I shape
            case 2:
                return Color.MAGENTA; // T shape
            case 3:
                return Color.GREEN; // S shape
            case 4:
                return Color.RED; // Z shape
            case 5:
                return Color.YELLOW; // O shape
            case 6:
                return Color.ORANGE; // L shape
            // Add more cases for additional shapes
            default:
                return Color.BLACK; // Empty space or unexpected value
        }
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
