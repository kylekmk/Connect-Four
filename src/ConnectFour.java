import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ConnectFour extends JComponent {


    private static char[][] moves;
    private int turns;
    private static boolean gameOver;
    private int lastMoveRow;
    private int lastMoveCol;
    private static boolean undone;
    private static int colSelected;

    private static ConnectFourClickListener click = new ConnectFourClickListener();

    private static final short ROW = 6;
    private static final short COL = 7;
    private static final char UNUSED = '\u0000';
    private static final int STREAK = 4;
    private static final Color BOARD = new Color(102, 178, 255);
    private static final Color RED = new Color(255, 102, 102);
    private static final Color YELLOW = new Color(255, 255, 102);
    private static final Color HIGHLIGHT = new Color(102, 255, 255);
    private static final long serialVersionUID = 1L;


    public static void main(String[] args) {
        ConnectFour game = new ConnectFour();
        // Give connectFourClickListener component to paint
        click.setComponent(game);
        JFrame window = new JFrame();
        window.add(game);
        windowSetup(window);

        // Place all buttons
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;

        JButton addMove = new JButton("Red make your move");
        panel.add(addMove, c);

        JButton undo = new JButton("Undo");
        c.gridx = 2;
        panel.add(undo, c);

        JButton reset = new JButton("Reset Game");
        c.gridx = 3;
        panel.add(reset, c);
        addMove.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // Only progress for valid turns
                if (moves[0][colSelected] == UNUSED && !gameOver) {
                    undone = false;
                    game.makeMove(colSelected);
                    changeText(addMove, game);
                }
            }
        });

        undo.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                game.undo();
                changeText(addMove, game);
            }
        });

        reset.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                game.newGame();
                changeText(addMove, game);
            }

        });
        panel.add(reset, c);
        window.add(BorderLayout.SOUTH, panel);
        window.setVisible(true);

    }

    /**
     * Create a board of default size ROW and COL
     * ROW == 6
     * COL == 7
     */
    public ConnectFour() {
        moves = new char[ROW][COL];
    }

    /**
     * Changes board for the given position
     * <p>
     * Row 0 is the bottom of the board
     */
    public void makeMove(int col) {
        if (gameOver) {
            throw new IllegalStateException("Board must be cleared to play a new game");
        }
        // Start at bottom of the board
        int row = moves.length - 1;
        while (row >= 0) {
            // find an empty slot in the given column
            if (moves[row][col] == UNUSED) {
                moves[row][col] = getCurrentPlayer();
                // Kept track of for an efficient check for a win
                lastMoveRow = row;
                lastMoveCol = col;
                turns++;
                gameOver = gameStatus();
                repaint();
                return;
            }
            row--;
        }
    }

    /**
     * Goes back to the state of the game before the last move
     */
    public void undo() {
        // only undo if it hasn't been done in the current turn
        if (!undone) {
            undone = true;
            moves[lastMoveRow][lastMoveCol] = UNUSED;
            turns--;
            repaint();
        }
    }

    /**
     * Player's alternate turns, so the last player is determined by turns
     *
     * @return last player to make a move
     */
    public char getLastPlayer() {
        // opposite of current player
        return turns % 2 == 0 ? 'o' : 'x';
    }

    /**
     * Player's alternate turns, so the current player is determined by turns
     *
     * @return player currently in play
     */
    public char getCurrentPlayer() {
        // Player x goes first
        return turns % 2 == 0 ? 'x' : 'o';

    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        // Prints move choices
        for (int i = 0; i < moves[0].length; i++) {
            s.append("|" + (i + 1) + "|");
        }
        s.append("\n");
        // Prints board
        for (int r = moves.length - 1; r >= 0; r--) {
            for (int c = 0; c < moves[0].length; c++) {
                if (moves[r][c] == 'x') {
                    s.append("|x|");
                } else if (moves[r][c] == 'o') {
                    s.append("|o|");
                } else {
                    s.append("| |");
                }
            }
            s.append("\n");
        }
        return s.toString();
    }

    public boolean gameOver() {
        return gameOver;
    }

    /**
     * @return char result of game
     */
    public char winner() {
        if (gameOver) {
            // maxed out possible turns
            if (turns == moves.length * moves[0].length) {
                return 't'; // tied
            }
            return getLastPlayer();
        } else {
            // Winner has not been chosen
            return UNUSED;
        }
    }

    /**
     * Empties the game board and resets variables
     */
    public void newGame() {
        moves = new char[moves.length][moves[0].length];
        turns = 0;
        lastMoveRow = 0;
        lastMoveCol = 0;
        gameOver = false;
        undone = true;
        repaint();
    }

    /**
     * Checks for the possibility of a win from the most recent move
     *
     * @return if the game ended
     */
    private boolean gameStatus() {
        if (turns == moves.length * moves[0].length) {
            return true;
        }
        // Check in pairs. Horizontal, up-right, up-left, vertical
        int[] deltaR = {0, 0, -1, 1, -1, 1, -1, 1};
        int[] deltaC = {-1, 1, 1, -1, -1, 1, 0, 0};
        // Iterate through all directions
        for (int d = 0; d < deltaR.length; d += 2) {
            // amount in a row of the same color
            int inARow = 1 + checkDirection(deltaR[d], deltaC[d]) + checkDirection(deltaR[d + 1], deltaC[d + 1]);
            // true when more than 4 are in a row
            if (inARow >= STREAK) {
                // cannot undo a win
                undone = true;
                return true;
            }
        }
        return false;
    }

    /**
     * helper to check in the given direction for a streak from the last move
     *
     * @param deltaR
     * @param deltaC
     * @return number of consecutive pieces of the same color
     */
    private int checkDirection(int deltaR, int deltaC) {
        int row = lastMoveRow;
        int col = lastMoveCol;
        char player = getLastPlayer();
        int inARow = 0;
        int i = 0;
        boolean cont = true;
        // Only check for 3 consecutive moves
        while (i < STREAK && cont) {
            row += deltaR;
            col += deltaC;

            if (validMove(row, col) && moves[row][col] == player) {
                inARow++;
            } else {
                cont = false;
            }
            i++;
        }
        return inARow;
    }

    /**
     * Makes sure the row and column are in bounds
     *
     * @param row
     * @param col
     * @return true if move is in bounds
     */
    private boolean validMove(int row, int col) {
        return row > -1 && row < moves.length && col > -1 && col < moves[0].length;
    }

    /**
     * Window size
     */
    public Dimension getPreferredSize() {
        return new Dimension(700, 625);
    }

    protected void paintComponent(Graphics g) {
        // Draw Board
        g.setColor(BOARD);
        g.fillRect(0, 0, 700, 600);
        // Draw selected column
        g.setColor(HIGHLIGHT);
        g.drawRect((100 * colSelected), 0, 100, 600);

        // Base coordinates
        int x = 5;
        int y = 5;
        // draw each piece
        for (int r = 0; r < moves.length; r++) {
            for (int c = 0; c < moves[0].length; c++) {
                if (moves[r][c] == UNUSED) {
                    drawPiece(g, Color.WHITE, Color.WHITE, x, y);
                } else if (moves[r][c] == 'x') {
                    drawPiece(g, RED, Color.BLACK, x, y);
                } else {
                    drawPiece(g, YELLOW, Color.BLACK, x, y);
                }
                x += 100;
            }
            x = 5;
            y += 100;
        }

        // game over, draw results
        if (gameOver()) {
            g.setColor(Color.BLACK);
            g.setFont(new Font("serif", Font.BOLD, 20));
            if (winner() == 't') {
                g.drawString("It's a tie!", 320, 300);
            } else {
                String winner = winner() == 'x' ? "Red" : "Yellow";
                g.drawString(winner + " WINS!\n", 300, 300);
            }
        }

    }

    /**
     * Draw game pieces
     */
    private void drawPiece(Graphics g, Color primary, Color secondary, int x, int y) {
        g.setColor(primary);
        g.fillOval(x, y, 90, 90);
        g.setColor(secondary);
        g.drawOval(x + 5, y + 5, 80, 80);
    }

    /**
     * Helps create the JFrame window
     *
     * @param window
     */
    private static void windowSetup(JFrame window) {
        window.getContentPane().addMouseListener(click);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setTitle("ConnectFour");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /**
     * Set selected column where the move is made
     *
     * @param col
     */
    public void setCol(int col) {
        colSelected = col;
    }

    /**
     * Changes the button text
     */
    private static void changeText(JButton button, ConnectFour game) {
        String cp = game.getCurrentPlayer() == 'x' ? "Red" : "Yellow";
        button.setText(cp + " make your move");
    }

}
