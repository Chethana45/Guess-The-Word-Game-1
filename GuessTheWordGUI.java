package game;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Advanced Swing UI for Guess The Word game.
 * Features included:
 * - Swing GUI with styled components
 * - Background timer thread (TimerThread)
 * - Polymorphism via GameWord interface
 * - Serialization (save/load) of game
 * - Exception handling for I/O and input
 * - Prevention of repeated guesses, shows missed letters
 */
public class GuessTheWordGUI extends JFrame {

    private static final String SAVE_FILENAME = "guess_save.dat";

    private GameWord word;
    private int triesLeft = 10;
    private Set<Character> missedLetters = new HashSet<>();

    // UI components
    private JLabel lblTitle = new JLabel("Guess The Word");
    private JLabel lblWord = new JLabel();
    private JLabel lblTries = new JLabel();
    private JLabel lblTimer = new JLabel();
    private JLabel lblMissed = new JLabel();
    private JTextField txtInput = new JTextField(1);
    private JButton btnGuess = new JButton("Guess");
    private JButton btnNew = new JButton("New Game");
    private JButton btnSave = new JButton("Save Now");

    private TimerThread timerThread;
    private final int START_SECONDS = 120; // more time for presentation/demo

    public GuessTheWordGUI() {
        setTitle("Guess The Word — Advanced GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 320);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        initLookAndFeel();
        loadOrNewGame();
        buildUI();
        startTimer(START_SECONDS);

        // Keyboard shortcut: Enter = Guess
        getRootPane().setDefaultButton(btnGuess);
    }

    private void initLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
    }

    private void buildUI() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(new EmptyBorder(12, 12, 12, 12));

        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 26));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        top.add(lblTitle, BorderLayout.NORTH);

        // Center panel: word display
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(new Color(245, 245, 250));
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0; gc.insets = new Insets(8,8,8,8);

        lblWord.setFont(new Font("Monospaced", Font.BOLD, 30));
        lblWord.setForeground(new Color(34,34,90));
        center.add(lblWord, gc);

        gc.gridy++;
        lblTries.setFont(new Font("SansSerif", Font.PLAIN, 16));
        center.add(lblTries, gc);

        gc.gridy++;
        lblTimer.setFont(new Font("SansSerif", Font.PLAIN, 16));
        center.add(lblTimer, gc);

        gc.gridy++;
        lblMissed.setFont(new Font("SansSerif", Font.PLAIN, 14));
        center.add(lblMissed, gc);

        top.add(center, BorderLayout.CENTER);

        // Bottom panel: controls
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        txtInput.setFont(new Font("SansSerif", Font.PLAIN, 20));
        txtInput.setHorizontalAlignment(SwingConstants.CENTER);
        Dimension d = new Dimension(60, 40);
        txtInput.setPreferredSize(d);

        btnGuess.setPreferredSize(new Dimension(120, 40));
        btnNew.setPreferredSize(new Dimension(120, 40));
        btnSave.setPreferredSize(new Dimension(120, 40));

        bottom.add(new JLabel("Enter letter:"));
        bottom.add(txtInput);
        bottom.add(btnGuess);
        bottom.add(btnNew);
        bottom.add(btnSave);

        add(top, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        // Wire events
        btnGuess.addActionListener(e -> onGuess());
        btnNew.addActionListener(e -> onNewGame());
        btnSave.addActionListener(e -> onSaveNow());

        txtInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                // allow only letters
                char ch = e.getKeyChar();
                if (!Character.isLetter(ch)) {
                    e.consume();
                }
            }
        });

        refreshUI();
    }

    private void loadOrNewGame() {
        // Try to load saved game
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(SAVE_FILENAME))) {
            SaveData data = (SaveData) in.readObject();
            this.word = data.word;
            this.triesLeft = data.triesLeft;
            this.missedLetters = data.missedLetters != null ? data.missedLetters : new HashSet<>();
            JOptionPane.showMessageDialog(this, "Loaded saved game!", "Resume", JOptionPane.INFORMATION_MESSAGE);
        } catch (FileNotFoundException fnf) {
            // No save — start new
            this.word = new Words();
            this.triesLeft = 10;
            this.missedLetters = new HashSet<>();
        } catch (Exception ex) {
            // Anything wrong while loading -> start fresh and show warning
            this.word = new Words();
            this.triesLeft = 10;
            this.missedLetters = new HashSet<>();
            System.err.println("Failed to load save: " + ex.getMessage());
        }
    }

    private void startTimer(int seconds) {
        if (timerThread != null) timerThread.stopTimer();
        timerThread = new TimerThread(seconds, new TimerThread.Listener() {
            @Override
            public void onTick(int secondsLeft) {
                SwingUtilities.invokeLater(() -> {
                    lblTimer.setText("Time left: " + secondsLeft + " s");
                });
            }

            @Override
            public void onFinish() {
                SwingUtilities.invokeLater(() -> {
                    lblTimer.setText("Time left: 0 s");
                    onTimeUp();
                });
            }
        });
        timerThread.start();
    }

    private void onGuess() {
        String txt = txtInput.getText().trim().toLowerCase();
        if (txt.isEmpty()) {
            showWarning("Please enter a letter.");
            return;
        }
        char ch = txt.charAt(0);
        if (!Character.isLetter(ch)) {
            showWarning("Only letters are allowed.");
            return;
        }

        // Prevent repeated correct or incorrect guesses
        String displayed = word.display().replace(" ", "");
        if (displayed.indexOf(ch) >= 0) {
            showWarning("You already revealed that letter.");
            txtInput.setText("");
            return;
        }
        if (missedLetters.contains(ch)) {
            showWarning("You already guessed '" + ch + "' and it was wrong.");
            txtInput.setText("");
            return;
        }

        try {
            boolean correct = word.guess(ch);
            if (!correct) {
                missedLetters.add(ch);
                triesLeft--;
                animateWrongGuess();
            } else {
                animateCorrectGuess();
            }

            saveGame(); // auto-save after each guess
            refreshUI();

            if (word.isGuessedCompletely()) {
                timerThread.stopTimer();
                JOptionPane.showMessageDialog(this, "Congratulations! You guessed the word: " + word.getWord(), "You Won", JOptionPane.INFORMATION_MESSAGE);
                safeDeleteSave();
                // offer new game
                int opt = JOptionPane.showConfirmDialog(this, "Play again?", "Restart", JOptionPane.YES_NO_OPTION);
                if (opt == JOptionPane.YES_OPTION) onNewGame();
                else System.exit(0);
            } else if (triesLeft <= 0) {
                timerThread.stopTimer();
                JOptionPane.showMessageDialog(this, "Game Over! The word was: " + word.getWord(), "Game Over", JOptionPane.INFORMATION_MESSAGE);
                safeDeleteSave();
                int opt = JOptionPane.showConfirmDialog(this, "Start a new game?", "New Game", JOptionPane.YES_NO_OPTION);
                if (opt == JOptionPane.YES_OPTION) onNewGame();
                else System.exit(0);
            }

        } catch (Exception ex) {
            showWarning("An error occurred while processing your guess.");
            ex.printStackTrace();
        } finally {
            txtInput.setText("");
        }
    }

    private void animateWrongGuess() {
        // briefly flash the tries label red
        Color orig = lblTries.getForeground();
        lblTries.setForeground(Color.RED);
        Timer t = new Timer(300, e -> lblTries.setForeground(orig));
        t.setRepeats(false);
        t.start();
    }

    private void animateCorrectGuess() {
        // briefly flash the word label green
        Color orig = lblWord.getForeground();
        lblWord.setForeground(new Color(10, 120, 10));
        Timer t = new Timer(300, e -> lblWord.setForeground(orig));
        t.setRepeats(false);
        t.start();
    }

    private void onNewGame() {
        if (timerThread != null) timerThread.stopTimer();
        this.word = new Words();
        this.triesLeft = 10;
        this.missedLetters.clear();
        safeDeleteSave();
        refreshUI();
        startTimer(START_SECONDS);
    }

    private void onSaveNow() {
        try {
            saveGame();
            JOptionPane.showMessageDialog(this, "Game saved successfully.", "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            showWarning("Failed to save game: " + ex.getMessage());
        }
    }

    private void onTimeUp() {
        // called on EDT
        JOptionPane.showMessageDialog(this, "Time's up! The word was: " + word.getWord(), "Time Up", JOptionPane.INFORMATION_MESSAGE);
        safeDeleteSave();
        int opt = JOptionPane.showConfirmDialog(this, "Play again?", "Restart", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) onNewGame();
        else System.exit(0);
    }

    private void refreshUI() {
        lblWord.setText(word.display());
        lblTries.setText("Tries left: " + triesLeft);
        lblTimer.setText("Time left: " + (timerThread == null ? START_SECONDS : timerThread.getSecondsLeft()) + " s");
        lblMissed.setText("Missed: " + (missedLetters.isEmpty() ? "-" : missedLetters.toString()));
    }

    private void saveGame() {
        // Save wrapper that contains serializable objects
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(SAVE_FILENAME))) {
            SaveData data = new SaveData(this.word, this.triesLeft, this.missedLetters);
            out.writeObject(data);
        } catch (IOException ex) {
            // bubble up with logging
            System.err.println("Save failed: " + ex.getMessage());
        }
    }

    private void safeDeleteSave() {
        try {
            java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(SAVE_FILENAME));
        } catch (IOException ignored) {}
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }
}
