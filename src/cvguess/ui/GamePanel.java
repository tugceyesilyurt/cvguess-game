package cvguess.ui;

import cvguess.core.GameController;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class GamePanel extends JPanel {

    private final GameController controller;
    private final Runnable backCallback;
    private final Runnable leaderboardCallback;

    private final JLabel timeLabel = new JLabel("Time: 60");
    private final JLabel scoreLabel = new JLabel("Correct: 0");
    private final JLabel hintLabel = new JLabel(" "); // doğru/yanlış mesajı

    private final ImageCanvas canvas = new ImageCanvas();
    private final JTextField guessField = new JTextField(20);

    private Timer timer;

    public GamePanel(GameController controller, Runnable backCallback, Runnable leaderboardCallback) {
        this.controller = controller;
        this.backCallback = backCallback;
        this.leaderboardCallback = leaderboardCallback;

        setLayout(new BorderLayout(10,10));
        add(buildTop(), BorderLayout.NORTH);
        add(canvas, BorderLayout.CENTER);
        add(buildBottom(), BorderLayout.SOUTH);
    }

    public void start() {
        refreshUIFromState();

        if (timer != null) timer.stop();
        timer = new Timer(1000, e -> {
            controller.tickOneSecond();
            if (controller.isGameOver()) {
                endGame();
            } else {
                refreshUIFromState();
            }
        });
        timer.start();
    }

    private JPanel buildTop() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        timeLabel.setFont(timeLabel.getFont().deriveFont(Font.BOLD, 16f));
        scoreLabel.setFont(scoreLabel.getFont().deriveFont(Font.BOLD, 16f));
        p.add(timeLabel);
        p.add(scoreLabel);

        JButton lbBtn = new JButton("Leaderboard");
        lbBtn.addActionListener(e -> leaderboardCallback.run());

        JButton backBtn = new JButton("Çık / Ana Menü");
        backBtn.addActionListener(e -> {
            if (timer != null) timer.stop();
            backCallback.run();
        });

        p.add(lbBtn);
        p.add(backBtn);
        return p;
    }

    private JPanel buildBottom() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);
        c.fill = GridBagConstraints.HORIZONTAL;

        JButton submitBtn = new JButton("Tahmin Et");
        submitBtn.addActionListener(e -> submitGuess());

        guessField.addActionListener(e -> submitGuess());

        c.gridx = 0; c.gridy = 0; c.weightx = 0;
        p.add(new JLabel("Tahmin:"), c);

        c.gridx = 1; c.weightx = 1;
        p.add(guessField, c);

        c.gridx = 2; c.weightx = 0;
        p.add(submitBtn, c);

        c.gridx = 0; c.gridy = 1; c.gridwidth = 3;
        hintLabel.setForeground(new Color(30, 80, 180));
        p.add(hintLabel, c);

        return p;
    }

    private void submitGuess() {
        if (controller.isGameOver()) return;

        String guess = guessField.getText().trim();
        guessField.setText("");

        GameController.GuessResult r = controller.submitGuess(guess);
        if (r == GameController.GuessResult.CORRECT) {
            hintLabel.setText("✅ Doğru! +3s (Yeni görsel)");
        } else if (r == GameController.GuessResult.WRONG) {
            hintLabel.setText("❌ Yanlış! -2s (Detay açılıyor...)");
        } else {
            endGame();
            return;
        }

        if (controller.isGameOver()) {
            endGame();
        } else {
            refreshUIFromState();
        }
    }

    private void refreshUIFromState() {
        GameController.RoundState st = controller.getCurrentState();
        timeLabel.setText("Time: " + st.timeLeft + "s");
        scoreLabel.setText("Correct: " + st.correctCount);
        canvas.setImage(st.shownImage);
    }

    private void endGame() {
        if (timer != null) timer.stop();
        controller.finalizeAndSave();

        int correct = controller.getPlayer().getCorrectCount();
        int t = controller.getTimeLeft();
        
        // Resmi ve cevabı al
        BufferedImage finalImg = controller.getCurrentImage();
        String answer = controller.getCurrentAnswer();
        
        // Resmi biraz küçült (dialoga sığsın)
        ImageIcon icon = null;
        if (finalImg != null) {
            int maxDim = 300;
            int w = finalImg.getWidth();
            int h = finalImg.getHeight();
            double scale = Math.min((double)maxDim/w, (double)maxDim/h);
            int nw = (int)(w * scale);
            int nh = (int)(h * scale);
            Image scaled = finalImg.getScaledInstance(nw, nh, Image.SCALE_SMOOTH);
            icon = new ImageIcon(scaled);
        }

        JOptionPane.showMessageDialog(this,
                "Oyun Bitti!\n" +
                "Oyuncu: " + controller.getPlayer().getName() + "\n" +
                "Doğru: " + correct + "\n" +
                "Kalan Süre: " + Math.max(0, t) + "s\n\n" +
                "Doğru Cevap: " + answer.toUpperCase() + "\n\n" +
                "Skor Leaderboard'a kaydedildi.",
                "Oyun Sonu",
                JOptionPane.INFORMATION_MESSAGE,
                icon);

        backCallback.run();
    }

    // Basit image panel
    private static class ImageCanvas extends JPanel {
        private BufferedImage image;

        public void setImage(BufferedImage img) {
            this.image = img;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image == null) return;

            int w = getWidth();
            int h = getHeight();

            int side = Math.min(w, h) - 20;
            int x = (w - side) / 2;
            int y = (h - side) / 2;

            g.drawImage(image, x, y, side, side, null);
            g.setColor(Color.DARK_GRAY);
            g.drawRect(x, y, side, side);
        }
    }
}
