package cvguess.ui;

import cvguess.model.Category;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiConsumer;

public class StartPanel extends JPanel {

    public interface StartCallback {
        void start(String playerName, Category category);
    }

    public StartPanel(StartCallback startCallback, Runnable leaderboardCallback) {
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8,8,8,8);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("CV Guess Game - Pixel Blocks", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));

        JTextField nameField = new JTextField(20);
        JComboBox<Category> categoryBox = new JComboBox<>(Category.values());

        JButton startBtn = new JButton("Oyuna Başla");
        JButton lbBtn = new JButton("Leaderboard");

        startBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Lütfen adını gir.");
                return;
            }
            Category cat = (Category) categoryBox.getSelectedItem();
            startCallback.start(name, cat);
        });

        lbBtn.addActionListener(e -> leaderboardCallback.run());

        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        add(title, c);

        c.gridy++;
        c.gridwidth = 1;
        add(new JLabel("Oyuncu Adı:"), c);

        c.gridx = 1;
        add(nameField, c);

        c.gridx = 0; c.gridy++;
        add(new JLabel("Kategori:"), c);

        c.gridx = 1;
        add(categoryBox, c);

        c.gridx = 0; c.gridy++;
        add(startBtn, c);

        c.gridx = 1;
        add(lbBtn, c);

        c.gridy++;
        c.gridx = 0; c.gridwidth = 2;
        JTextArea info = new JTextArea(
                "Kural:\n" +
                "- Süre: 60s\n" +
                "- Doğru: +3s\n" +
                "- Yanlış: -2s\n" +
                "- Yanlış yaptıkça görüntü daha hızlı açılır."
        );
        info.setEditable(false);
        info.setBackground(getBackground());
        add(info, c);
    }
}
