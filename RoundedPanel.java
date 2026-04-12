package boundary;

import controller.PatientController;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/** Patient login – friendly centred card on top of smiling-patient background (no emoji). */
public class PatientLoginPanel extends JPanel {

    @FunctionalInterface
    public interface LoginSuccessListener {
        void onPatientLogin(int patientID);
    }

    private final JTextField txtID = new JTextField();
    private final JLabel     lblErr = new JLabel(" ");

    public PatientLoginPanel(LoginSuccessListener listener) {
        setLayout(new BorderLayout());

        /* ---------- background ---------- */
        JLabel bg = new JLabel(
                new ImageIcon(getClass().getResource("/backgrounds/patientbackground.png")));
        bg.setLayout(new GridBagLayout());          // centre the card
        add(bg, BorderLayout.CENTER);

        /* ---------- translucent card ---------- */
        JPanel card = new JPanel();
        card.setPreferredSize(new Dimension(400, 380));
        card.setBackground(new Color(255, 255, 255, 220)); // translucent white
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(180, 180, 180), 1, true),
                new EmptyBorder(30, 40, 30, 40)));

        /* ---------- heading & subtitle ---------- */
        JLabel heading = new JLabel("Welcome Back!");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 26));
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(heading);

        JLabel subtitle = new JLabel("How are you feeling today?");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(60, 60, 60));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(Box.createVerticalStrut(8));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(30));

        /* ---------- Patient ID label & field ---------- */
        JLabel lblID = new JLabel("Patient ID");
        lblID.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblID.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblID);

        txtID.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txtID.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtID.setBackground(new Color(245, 245, 245));
        txtID.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        txtID.setAlignmentX(Component.CENTER_ALIGNMENT);
        txtID.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) tryLogin(listener);
            }
        });
        card.add(txtID);
        card.add(Box.createVerticalStrut(12));

        /* ---------- error label ---------- */
        lblErr.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblErr.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblErr.setForeground(Color.RED);
        card.add(lblErr);
        card.add(Box.createVerticalStrut(20));

        /* ---------- Log In button ---------- */
        JButton login = new GradientButton("Log In",
                new Color(0, 46, 102), new Color(0, 34, 77));
        login.setAlignmentX(Component.CENTER_ALIGNMENT);
        login.addActionListener(e -> tryLogin(listener));
        card.add(login);
        card.add(Box.createVerticalStrut(20));

        /* ---------- friendly tip — no emoji ---------- */
        JLabel tip = new JLabel("Don\u2019t forget to brush and smile!");
        tip.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        tip.setForeground(new Color(60, 60, 60));
        tip.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(tip);

        bg.add(card); // centre card on background
    }

    /* ---------- validation ---------- */
    private void tryLogin(LoginSuccessListener listener) {
        lblErr.setText(" ");
        String txt = txtID.getText().trim();

        if (!txt.matches("\\d+")) {
            lblErr.setText("ID must be numeric");
            return;
        }
        int id = Integer.parseInt(txt);
        if (!PatientController.exists(id)) {
            lblErr.setText("Patient ID not found");
            return;
        }
        listener.onPatientLogin(id);
    }

    /* ---------- gradient button ---------- */
    static class GradientButton extends JButton {
        private final Color top, bottom;
        GradientButton(String text, Color top, Color bottom) {
            super(text);
            this.top = top; this.bottom = bottom;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 20));
            setPreferredSize(new Dimension(320, 60));
            setMaximumSize(new Dimension(320, 60));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bottom));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(),
                             getHeight(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
        @Override public void paintBorder(Graphics g) { /* none */ }
    }
}

