package boundary;

import controller.StaffController;
import entity.Staff;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/** Staff login – modern centered card with welcome subtitle and cleaner layout. */
public class StaffLoginPanel extends JPanel {

    @FunctionalInterface
    public interface LoginSuccessListener {
        void onStaffLogin(Staff staff);
    }

    private final JTextField txtID = new JTextField();
    private final JLabel lblErr = new JLabel(" ");

    public StaffLoginPanel(LoginSuccessListener listener) {
        setLayout(new BorderLayout());

        // Background image
        JLabel background = new JLabel(new ImageIcon(getClass().getResource("/backgrounds/staffbackground.png")));
        background.setLayout(new GridBagLayout()); // center the login card
        add(background, BorderLayout.CENTER);

        // Login card panel
        JPanel card = new JPanel();
        card.setPreferredSize(new Dimension(400, 380));
        card.setBackground(new Color(255, 255, 255, 210)); // semi-transparent
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(30, 40, 30, 40)));

        // Heading
        JLabel heading = new JLabel("Staff Login");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 26));
        heading.setForeground(Color.DARK_GRAY);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(heading);

        // Welcome subtitle
        JLabel welcome = new JLabel("Welcome back! Please enter your ID to continue.");
        welcome.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        welcome.setForeground(new Color(60, 60, 60));
        welcome.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(Box.createVerticalStrut(8));
        card.add(welcome);
        card.add(Box.createVerticalStrut(30));

        // Staff ID label (centered)
        JLabel lblID = new JLabel("Staff ID");
        lblID.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblID.setForeground(Color.DARK_GRAY);
        lblID.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblID);

        // ID input
        txtID.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txtID.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtID.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        txtID.setBackground(new Color(245, 245, 245));
        txtID.setAlignmentX(Component.CENTER_ALIGNMENT);
        txtID.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) tryLogin(listener);
            }
        });
        card.add(txtID);
        card.add(Box.createVerticalStrut(12));

        // Error label
        lblErr.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblErr.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblErr.setForeground(Color.RED);
        card.add(lblErr);
        card.add(Box.createVerticalStrut(20));

        // Log In button
        JButton login = new JButton("Log In");
        login.setAlignmentX(Component.CENTER_ALIGNMENT);
        login.setFont(new Font("Segoe UI", Font.BOLD, 16));
        login.setForeground(Color.WHITE);
        login.setBackground(new Color(0, 46, 102));
        login.setFocusPainted(false);
        login.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        login.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        login.addActionListener(e -> tryLogin(listener));
        card.add(login);
        card.add(Box.createVerticalStrut(20));

        // Forgot Password only
        JLabel forgot = new JLabel("Forgot Password?");
        forgot.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        forgot.setForeground(new Color(90, 90, 90));
        forgot.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(forgot);

        background.add(card); // center the login box on the background
    }

    private void tryLogin(LoginSuccessListener listener) {
        lblErr.setText(" ");

        String txt = txtID.getText().trim();
        if (!txt.matches("\\d+")) {
            lblErr.setText("ID must be numeric");
            return;
        }
        int id = Integer.parseInt(txt);
        Staff staff = StaffController.getStaffById(id);
        if (staff == null) {
            lblErr.setText("Staff ID not found");
            return;
        }
        listener.onStaffLogin(staff);
    }
}

