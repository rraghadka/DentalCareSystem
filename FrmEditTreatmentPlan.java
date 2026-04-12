package boundary;

import controller.*;
import entity.*;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class FrmBookAppointmentForPatient extends JFrame {

    private final Patient currentPatient;

    private JComboBox<String> cmbTime;
    private JComboBox<Staff> cmbStaff;
    private JComboBox<AppointmentReason> cmbReason;
    private JComboBox<Treatment> cmbTreatment;
    private JDateChooser dateChooser;

    public FrmBookAppointmentForPatient(Patient patient) {
        this.currentPatient = patient;

        setTitle("Book Appointment");
        setSize(550, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setContentPane(createContent());
    }

    private JPanel createContent() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));
        panel.setBackground(new Color(30, 75, 145));

        JLabel title = new JLabel("Book an Appointment", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        panel.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 15));
        form.setOpaque(false);

        // Date Picker
        JLabel lblDate = new JLabel("Choose Date:");
        lblDate.setForeground(Color.WHITE);
        form.add(lblDate);
        dateChooser = new JDateChooser();
        dateChooser.setMinSelectableDate(new Date());
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.setBackground(Color.WHITE);
        form.add(dateChooser);

        // Time combo
        JLabel lblTime = new JLabel("Choose Time:");
        lblTime.setForeground(Color.WHITE);
        form.add(lblTime);
        cmbTime = new JComboBox<>();
        form.add(cmbTime);

        // Staff combo
        JLabel lblStaff = new JLabel("Choose Dentist:");
        lblStaff.setForeground(Color.WHITE);
        form.add(lblStaff);
        cmbStaff = new JComboBox<>();
        form.add(cmbStaff);

        // Reason combo
        JLabel lblReason = new JLabel("Appointment Reason:");
        lblReason.setForeground(Color.WHITE);
        form.add(lblReason);
        cmbReason = new JComboBox<>();
        form.add(cmbReason);

        // Treatment combo
        JLabel lblTreatment = new JLabel("Treatment Type:");
        lblTreatment.setForeground(Color.WHITE);
        form.add(lblTreatment);
        cmbTreatment = new JComboBox<>();
        form.add(cmbTreatment);

        // Load initial data
        loadReasons();
        loadTreatments();

        // When date changes → load available times
        dateChooser.getDateEditor().addPropertyChangeListener("date", e -> loadTimes());

        // When time changes → load available staff
        cmbTime.addActionListener(e -> loadStaff());

        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);

        JButton btnBook = new JButton("Book Appointment");
        btnBook.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnBook.setBackground(new Color(135, 206, 250));
        btnBook.setForeground(Color.BLACK);
        btnBook.setFocusPainted(false);
        btnBook.addActionListener(e -> bookAppointment());
        btnPanel.add(btnBook);

        panel.add(form, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadReasons() {
        List<AppointmentReason> reasons = new AppointmentReasonController().getAllReasons();
        cmbReason.removeAllItems();
        for (AppointmentReason r : reasons)
            cmbReason.addItem(r);
    }

    private void loadTreatments() {
        List<Treatment> treatments = new TreatmentController().getAllTreatments();
        cmbTreatment.removeAllItems();
        for (Treatment t : treatments)
            cmbTreatment.addItem(t);
    }

    private void loadTimes() {
        cmbTime.removeAllItems();
        cmbStaff.removeAllItems();

        Date selectedDate = dateChooser.getDate();
        if (selectedDate == null)
            return;

        java.sql.Date sqlDate = new java.sql.Date(selectedDate.getTime());
        List<String> times = new AppointmentController().getAvailableTimesForPatient(sqlDate);

        for (String time : times)
            cmbTime.addItem(time);
    }

    private void loadStaff() {
        cmbStaff.removeAllItems();

        Date selectedDate = dateChooser.getDate();
        String selectedTime = (String) cmbTime.getSelectedItem();

        if (selectedDate == null || selectedTime == null)
            return;

        java.sql.Date sqlDate = new java.sql.Date(selectedDate.getTime());
        List<Staff> available = new AppointmentController().getAvailableStaffForPatient(sqlDate, selectedTime);

        for (Staff s : available)
            cmbStaff.addItem(s);
    }

    private void bookAppointment() {
        Date selectedDate = dateChooser.getDate();
        String selectedTime = (String) cmbTime.getSelectedItem();
        Staff selectedStaff = (Staff) cmbStaff.getSelectedItem();
        AppointmentReason selectedReason = (AppointmentReason) cmbReason.getSelectedItem();
        Treatment selectedTreatment = (Treatment) cmbTreatment.getSelectedItem();

        if (selectedDate == null || selectedTime == null || selectedStaff == null || selectedReason == null || selectedTreatment == null) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.", "Missing Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int reasonID = Integer.parseInt(selectedReason.getReasonID());
        int treatmentID = selectedTreatment.getTreatmentID();

        Appointment appointment = new Appointment(
            currentPatient.getPatientID(),
            selectedDate,
            selectedTime,
            selectedStaff.getStaffID(),
            "Scheduled",
            reasonID,
            treatmentID
        );

        boolean success = new AppointmentController().bookAppointmentForPatient(appointment);
        if (success) {
            JOptionPane.showMessageDialog(this, "Appointment booked successfully!");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Could not book appointment. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
