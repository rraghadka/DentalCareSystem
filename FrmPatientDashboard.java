package boundary;

import controller.AppointmentController;
import entity.Appointment;
import entity.Patient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.time.LocalTime;


public class FrmManageAppointmentsForPatient extends JFrame {

    private final Patient currentPatient;
    private JPanel contentPanel; // ✅ Added field for getContentPanel()

    public FrmManageAppointmentsForPatient(Patient patient) {
        this.currentPatient = patient;
        setTitle("Manage My Appointments");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.contentPanel = createContent(); // ✅ assign for later use
        setContentPane(contentPanel);
    }

    private JPanel createContent() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(30, 75, 145));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        List<Appointment> appointments = AppointmentController.getInstance()
                .getUpcomingAppointmentsForPatient(currentPatient.getPatientID());

        if (appointments.isEmpty()) {
            JLabel lbl = new JLabel("You have no upcoming appointments.");
            lbl.setForeground(Color.WHITE);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
            panel.add(lbl);
        } else {
            for (Appointment appt : appointments) {
                JPanel apptPanel = createAppointmentPanel(appt);
                panel.add(apptPanel);
                panel.add(Box.createVerticalStrut(20));
            }
        }

        // Wrap scroll pane inside a JPanel to satisfy return type
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(new JScrollPane(panel), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createAppointmentPanel(Appointment appt) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(new Color(20, 55, 110));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                new EmptyBorder(15, 20, 15, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 0, 10, 0);

        // --- Row 1: Appointment Info ---
        JLabel info = new JLabel(
                appt.getAppointmentDate() + " at " + appt.getAppointmentTime() +
                " | Status: " + appt.getStatus() +
                " | Paid: " + (appt.isPaid() ? "Yes" : "No")
        );
        info.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        info.setForeground(Color.WHITE);
        card.add(info, gbc);

        // --- Row 2: Button Panel ---
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        buttons.setOpaque(false);

        // Convert to LocalDateTime
        LocalDateTime apptDateTime = ((java.sql.Date) appt.getAppointmentDate())
                .toLocalDate()
                .atTime(LocalTime.parse(appt.getAppointmentTime()));

        // Cancel (only if > 24h remaining)
        if (LocalDateTime.now().isBefore(apptDateTime.minusHours(24))) {
            JButton btnCancel = new JButton("Cancel");
            btnCancel.setPreferredSize(new Dimension(120, 40));
            btnCancel.setBackground(new Color(139, 0, 0)); // Dark red
            btnCancel.setForeground(Color.WHITE);
            btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btnCancel.setFocusPainted(false);
            btnCancel.addActionListener(e -> {
                int choice = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to cancel this appointment?",
                        "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    AppointmentController.getInstance().cancelAppointment(appt.getAppointmentID());
                    JOptionPane.showMessageDialog(this, "Appointment cancelled.");
                    dispose();
                    new FrmManageAppointmentsForPatient(currentPatient).setVisible(true);
                }
            });
            buttons.add(btnCancel);
        }

        // Suspend
        JButton btnSuspend = new JButton("Suspend");
        btnSuspend.setPreferredSize(new Dimension(120, 40));
        btnSuspend.setBackground(new Color(218, 165, 32)); // Dark yellow
        btnSuspend.setForeground(Color.BLACK);
        btnSuspend.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSuspend.setFocusPainted(false);
        btnSuspend.addActionListener(e ->
                JOptionPane.showMessageDialog(this,
                        "Note: If 24 hours pass since suspension, appointment will be cancelled and refund required.",
                        "Suspension Info", JOptionPane.WARNING_MESSAGE));
        buttons.add(btnSuspend);

        // Approve (only if not yet approved)
        if (!appt.getStatus().equalsIgnoreCase("Approved")) {
            JButton btnApprove = new JButton("Approve");
            btnApprove.setPreferredSize(new Dimension(120, 40));
            btnApprove.setBackground(new Color(0, 128, 0)); // Dark green
            btnApprove.setForeground(Color.WHITE);
            btnApprove.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btnApprove.setFocusPainted(false);
            btnApprove.addActionListener(e -> {
                if (!appt.isPaid()) {
                    String[] options = {"Pay by Visa", "Pay in Clinic"};
                    int payOption = JOptionPane.showOptionDialog(this,
                            "Please choose a payment method before confirming the appointment.",
                            "Payment Required", JOptionPane.DEFAULT_OPTION,
                            JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

                    if (payOption == -1) return; // user cancelled
                }

                AppointmentController.getInstance().approveAppointment(appt.getAppointmentID());
                JOptionPane.showMessageDialog(this, "Appointment approved!");
                dispose();
                new FrmManageAppointmentsForPatient(currentPatient).setVisible(true);
            });
            buttons.add(btnApprove);
        }

        // Add button row
        card.add(buttons, gbc);

        return card;
    }


    private JButton styledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 40)); // ⬅️ Bigger size
        btn.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        return btn;
    }


    // ✅ NEW METHOD so FrmPatientMenu can embed it using setRight(...)
    public JPanel getContentPanel() {
        return contentPanel;
    }
}
