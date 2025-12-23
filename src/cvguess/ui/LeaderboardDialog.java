package cvguess.ui;

import cvguess.model.Leaderboard;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class LeaderboardDialog extends JDialog {

    public LeaderboardDialog(JFrame owner, Leaderboard leaderboard) {
        super(owner, "Leaderboard", true);
        setSize(520, 360);
        setLocationRelativeTo(owner);

        String[] cols = {"#", "Name", "Correct", "TimeLeft"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        List<Leaderboard.Entry> entries = leaderboard.loadAllSorted();
        int rank = 1;
        for (Leaderboard.Entry e : entries) {
            model.addRow(new Object[]{rank++, e.name, e.correct, e.timeLeft});
        }

        JTable table = new JTable(model);
        JScrollPane sp = new JScrollPane(table);

        JButton close = new JButton("Kapat");
        close.addActionListener(e -> dispose());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(close);

        setLayout(new BorderLayout());
        add(sp, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }
}
