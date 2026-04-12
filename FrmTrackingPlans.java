package boundary;

import controller.AppointmentController;
import entity.Appointment;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class FrmSterilizationRequests extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private AppointmentController ctl = new AppointmentController();

    public FrmSterilizationRequests() {
        setLayout(new BorderLayout());
        setBackground(new Color(0, 32, 63));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("Sterilization Requests – Approved Appointments");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        add(lblTitle, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{
                "ID", "Patient ID", "Date", "Time", "Staff ID", "Treatment", "Reason"
        }, 0);
        table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setForeground(Color.WHITE);
        table.setBackground(new Color(20, 40, 80));
        table.setGridColor(Color.LIGHT_GRAY);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setForeground(Color.WHITE);
        header.setBackground(new Color(10, 25, 50));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(new Color(0, 32, 63));
        add(scrollPane, BorderLayout.CENTER);

        JButton btnSend = new JButton("Send Sterilization Request");
        btnSend.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnSend.setForeground(Color.WHITE);
        btnSend.setBackground(new Color(0, 102, 204));
        btnSend.setFocusPainted(false);
        btnSend.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSend.addActionListener(this::sendMessage);

        JPanel bottom = new JPanel();
        bottom.setBackground(new Color(0, 32, 63));
        bottom.add(btnSend);
        add(bottom, BorderLayout.SOUTH);

        loadApprovedAppointments();
    }

    private void loadApprovedAppointments() {
        model.setRowCount(0);
        List<Appointment> list = ctl.getApprovedAppointments();
        for (Appointment a : list) {
            model.addRow(new Object[]{
                    a.getAppointmentID(),
                    a.getPatientID(),
                    a.getAppointmentDate(),
                    a.getAppointmentTime(),
                    a.getStaffID(),
                    a.getTreatmentID(),
                    a.getReasonID()
            });
        }
    }

    private void sendMessage(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment.");
            return;
        }

        int apptID = (int) table.getValueAt(row, 0);
        String date = table.getValueAt(row, 2).toString();
        String time = table.getValueAt(row, 3).toString();

        JOptionPane.showMessageDialog(this,
                "\u2709 Sterilization request sent for Appointment #" + apptID +
                "\non " + date + " at " + time +
                "\nPlease sterilize the room before the appointment.",
                "Request Sent", JOptionPane.INFORMATION_MESSAGE);
    }

    public JPanel getContentPanel() {
        return this;
    }
}
