import java.awt.*;
import javax.swing.*;

public class ReservationFrame extends JFrame {

    public ReservationFrame(DataStore dataStore) {
        super("Manage Reservations");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // ----- Background Image Panel -----
        BackgroundPanel background = new BackgroundPanel();
        background.setLayout(new BorderLayout());
        setContentPane(background);

        // ----- Main Reservation Panel -----
        ReservationPanel panel = new ReservationPanel(dataStore);
        panel.setOpaque(false); // allow background image to show
        add(panel, BorderLayout.CENTER);

        // ----- Bottom Panel -----
        JPanel bottom = new JPanel();
        bottom.setOpaque(false); // transparent bottom panel
        bottom.setLayout(new GridBagLayout());

        JButton btnBack = new JButton("Back");
        btnBack.setBackground(new Color(212, 175, 55));
        btnBack.setForeground(Color.WHITE);
        btnBack.setFocusPainted(false);
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 22));
        btnBack.setPreferredSize(new Dimension(200, 40));
        btnBack.setBorder(
                BorderFactory.createLineBorder(
                        new Color(212, 175, 55).darker(), 2, true));
        btnBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> dispose());

        bottom.add(btnBack);
        add(bottom, BorderLayout.SOUTH);

        fadeInFrame(this);
    }

    // ===== Background Panel =====
    class BackgroundPanel extends JPanel {
        private Image bg;

        public BackgroundPanel() {
            // Image is in the SAME project folder
            bg = new ImageIcon("golden_background.png").getImage();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bg != null) {
                g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    // ===== Fade-in Animation =====
    private void fadeInFrame(JFrame frame) {
        frame.setOpacity(0f);
        frame.setVisible(true);
        Timer timer = new Timer(15, null);
        timer.addActionListener(e -> {
            float opacity = frame.getOpacity();
            opacity += 0.05f;
            if (opacity >= 1f) {
                opacity = 1f;
                ((Timer) e.getSource()).stop();
            }
            frame.setOpacity(opacity);
        });
        timer.start();
    }
}
