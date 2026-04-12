package boundary;

import controller.*;
import entity.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

public class FrmPatientMenu extends JFrame {

    private final Patient currentPatient;
    private JPanel rightPanel;

    public FrmPatientMenu(Patient patient) {
        this.currentPatient = patient;
        setTitle("Welcome Patient");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(createContent());
    }

    private JPanel createContent() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(30, 75, 145));

        // Top Welcome Message
        JLabel welcome = new JLabel("Welcome, " + currentPatient.getFullName() + "!", SwingConstants.CENTER);
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 26));
        welcome.setForeground(Color.WHITE);
        welcome.setBorder(new EmptyBorder(20, 0, 10, 0));
        main.add(welcome, BorderLayout.NORTH);

        // Left Navigation Panel
        main.add(createNavigationPanel(), BorderLayout.WEST);

        // Center Content Panel (initialize rightPanel field here)
        rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(new Color(30, 75, 145));
        rightPanel.add(createDefaultContent(), BorderLayout.CENTER); // show default view
        main.add(rightPanel, BorderLayout.CENTER);

        // Bottom Envelopes
        main.add(createBottomEnvelopesPanel(), BorderLayout.SOUTH);

        return main;
    }

    // 🔄 Moved your center content here, unchanged
    private JPanel createDefaultContent() {
        JPanel center = new JPanel();
        center.setBackground(new Color(30, 75, 145));
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(Box.createVerticalStrut(15));
        center.add(createProfilePanel());
        center.add(Box.createVerticalStrut(20));
        center.add(createUpcomingAppointmentsPanel());
        center.add(Box.createVerticalStrut(20));
        center.add(createPaymentDataPanel());
        return center;
    }

    // ✅ FIXED: this now only replaces the CENTER content
    private void setRight(JPanel panel) {
        rightPanel.removeAll();
        rightPanel.add(panel, BorderLayout.CENTER);
        rightPanel.revalidate();
        rightPanel.repaint();
    }


    private JPanel createNavigationPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(20, 60, 120));
        panel.setBorder(new EmptyBorder(20, 10, 20, 10));
        panel.setPreferredSize(new Dimension(240, 0));

        // Header
        JLabel hdr = new JLabel("🔷 Menu");
        hdr.setFont(new Font("Segoe UI Emoji", Font.BOLD, 18));
        hdr.setForeground(Color.WHITE);
        hdr.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(hdr);
        panel.add(Box.createVerticalStrut(20));

        // Buttons
        panel.add(createNavButton("My Profile", () -> 
            JOptionPane.showMessageDialog(this, "Already on profile.")));
        panel.add(Box.createVerticalStrut(15));

        panel.add(createNavButton("Book Appointments", () -> 
            new FrmBookAppointmentForPatient(currentPatient).setVisible(true)));
        panel.add(Box.createVerticalStrut(15));

        panel.add(createNavButton("Manage Appointments", () -> 
            setRight(new FrmManageAppointmentsForPatient(currentPatient).getContentPanel())));
        panel.add(Box.createVerticalStrut(15));

        panel.add(createNavButton(" View Treatment History", () -> 
            setRight(new FrmTrackingPlans(currentPatient).getContentPanel())));
        panel.add(Box.createVerticalStrut(15));

        panel.add(createNavButton("Logout", () -> dispose()));
        panel.add(Box.createVerticalStrut(15));

        return panel;
    }


    private JButton createNavButton(String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setBackground(new Color(230, 230, 255));
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50)); // ← FIX: fill width but consistent height
        btn.setPreferredSize(new Dimension(200, 50));
        btn.setBorder(new LineBorder(new Color(80, 80, 150), 2));
        btn.addActionListener(e -> action.run());
        return btn;
    }
    

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel img = new JLabel();
        try {
            java.net.URL url = getClass().getResource("/icons/pro.jpg");
            if (url == null) {
                throw new Exception("Image not found at /icons/pro.jpg");
            }
            ImageIcon icon = new ImageIcon(url);
            Image scaled = icon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
            img.setIcon(new ImageIcon(scaled));
        } catch (Exception e) {
            img.setText("📷");
            img.setFont(new Font("Segoe UI", Font.PLAIN, 24));
            img.setForeground(Color.WHITE);
            System.err.println("⚠️ Image Load Error: " + e.getMessage());
        }
        img.setBorder(BorderFactory.createLineBorder(Color.WHITE, 3));
        img.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(makeLabel("📧 Email: " + currentPatient.getEmail()));
        infoPanel.add(makeLabel("📞 Phone: " + currentPatient.getPhoneNumber()));
        infoPanel.add(makeLabel("🎂 DOB: " + currentPatient.getDateOfBirth()));
        infoPanel.add(makeLabel("🧮 Age: " + calculateAge(currentPatient.getDateOfBirth())));

        panel.add(img);
        panel.add(Box.createVerticalStrut(10));
        panel.add(infoPanel);

        return panel;
    }

    private JPanel createUpcomingAppointmentsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel title = new JLabel("🗓️ Your Appointments in the Next 24 Hours:");
        title.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        title.setBorder(new EmptyBorder(5, 10, 5, 0));
        panel.add(title);

        List<Appointment> upcoming = AppointmentController.getInstance().getAppointmentsInNext24h(currentPatient.getPatientID());

        if (upcoming == null || upcoming.isEmpty()) {
            panel.add(makeLabel("• No upcoming appointments in the next 24 hours."));
        } else {
            for (Appointment appt : upcoming) {
                panel.add(makeLabel("• " + appt.getAppointmentDate() + " at " + appt.getAppointmentTime()));
            }
        }
        return panel;
    }

    private JPanel createPaymentDataPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel title = new JLabel("🧾 Issued Payments:");
        title.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        title.setBorder(new EmptyBorder(5, 10, 5, 0));
        panel.add(title);

        panel.add(makeLabel("• Total Payments: ₪450"));
        panel.add(makeLabel("• Last payment: 2025-07-12"));
        panel.add(makeLabel("• All payments are up to date ✅"));

        return panel;
    }

    private JPanel createBottomEnvelopesPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        panel.setBackground(new Color(20, 60, 120));

        JButton btnMed = createEnvelopeButton("📋 Medical History", () ->
                JOptionPane.showMessageDialog(this,
                        MedicalHistoryController.getInstance().getDetails(currentPatient.getPatientID()),
                        "Medical History", JOptionPane.INFORMATION_MESSAGE));

        JButton btnDental = createEnvelopeButton("🦷 Dental History", () ->
                JOptionPane.showMessageDialog(this,
                        DentalHistoryController.getInstance().getDetails(currentPatient.getPatientID()),
                        "Dental History", JOptionPane.INFORMATION_MESSAGE));

        JButton btnInsurance = createEnvelopeButton("📄 Insurance Details", () ->
                JOptionPane.showMessageDialog(this,
                        InsuranceController.getInstance().getDetails(currentPatient.getPatientID()),
                        "Insurance Details", JOptionPane.INFORMATION_MESSAGE));

        panel.add(btnMed);
        panel.add(btnDental);
        panel.add(btnInsurance);
        return panel;
    }

    private JButton createEnvelopeButton(String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(220, 50));  // ⬅ wider preferred size
        btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBackground(new Color(230, 230, 255));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(100, 100, 150), 2),
                new EmptyBorder(10, 20, 10, 20)
        ));
        btn.addActionListener(e -> action.run());
        return btn;
    }

    private JLabel makeLabel(String txt) {
        JLabel label = new JLabel(txt);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        label.setBorder(new EmptyBorder(3, 15, 3, 0));
        return label;
    }

    private int calculateAge(Date dob) {
        if (dob == null) return 0;
        java.sql.Date sqlDate = new java.sql.Date(dob.getTime());
        LocalDate birthDate = sqlDate.toLocalDate();
        return (int) ChronoUnit.YEARS.between(birthDate, LocalDate.now());
    }

    public static void main(String[] args) {
        Patient demo = new Patient(1, "Daniel", "Cohen", "daniel.cohen@example.com", "053-5989070", java.sql.Date.valueOf("2009-10-20"));
        new FrmPatientMenu(demo).setVisible(true);
    }

  
}
