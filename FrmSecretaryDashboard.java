package boundary;

import controller.AppointmentController;
import entity.Appointment;
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class FrmPendingAppointments extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private AppointmentController ctl = new AppointmentController();

    public FrmPendingAppointments() {
        setLayout(new BorderLayout());
        setBackground(new Color(0, 32, 63));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Pending Appointments to Confirm");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        add(title, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{
                "ID", "Patient ID", "Date", "Time", "Staff ID", "Treatment", "Reason", "Paid", "Status"
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

        JButton btnConfirm = new JButton("Confirm Appointment");
        btnConfirm.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setBackground(new Color(0, 102, 204));
        btnConfirm.setFocusPainted(false);
        btnConfirm.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnConfirm.addActionListener(this::confirmAppointment);

        JPanel bottom = new JPanel();
        bottom.setBackground(new Color(0, 32, 63));
        bottom.add(btnConfirm);
        add(bottom, BorderLayout.SOUTH);

        loadPendingAppointments();
    }

    private void loadPendingAppointments() {
        model.setRowCount(0);
        List<Appointment> list = ctl.getPendingAppointments();
        for (Appointment a : list) {
            model.addRow(new Object[]{
                    a.getAppointmentID(),
                    a.getPatientID(),
                    a.getAppointmentDate(),
                    a.getAppointmentTime(),
                    a.getStaffID(),
                    a.getTreatmentID(),
                    a.getReasonID(),
                    a.isPaid() ? "Yes" : "No",
                    a.getStatus()
            });
        }
    }

    private void confirmAppointment(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment.");
            return;
        }
        int apptID = (int) table.getValueAt(row, 0);
        String paid = ((String) table.getValueAt(row, 7)).trim();
        if (!"Yes".equalsIgnoreCase(paid)) {
            JOptionPane.showMessageDialog(this, "\u274C Cannot confirm unpaid appointment.");
            return;
        }

        boolean ok = ctl.confirmAppointment(apptID);
        if (ok) {
            JOptionPane.showMessageDialog(this, "\u2705 Appointment confirmed!");
            loadPendingAppointments();
        } else {
            JOptionPane.showMessageDialog(this, "\u274C Failed to confirm appointment.");
        }
    }

    public JPanel getContentPanel() {
        return this;
    }
}
