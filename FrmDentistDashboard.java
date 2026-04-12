package boundary;

import controller.*;
import entity.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class FrmBookAppointment {
    private final JPanel content = new JPanel(new BorderLayout());

    private final JComboBox<Patient> cmbPatient = new JComboBox<>();
    private final JComboBox<Staff> cmbDentist = new JComboBox<>();
    private final JSpinner spnDate = new JSpinner(
            new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH));
    private final JComboBox<String> cmbTime = new JComboBox<>();
    private final JComboBox<Treatment> cmbTreat = new JComboBox<>();
    private final JComboBox<AppointmentReason> cmbReason = new JComboBox<>(); // ✅ ADDED
    private final JComboBox<String> cmbUrgency = new JComboBox<>(new String[]{"Routine", "Urgent"});
    private final JSpinner spnDuration = new JSpinner(new SpinnerNumberModel(30, 15, 180, 15));

    private final StaffController staffController = new StaffController();

    public FrmBookAppointment() {
        buildUI();
        loadCombos();
        setupUrgencyListener();
        setupRoutineSlotRefresh();
    }

    public JPanel getContentPanel() {
        return content;
    }

    private void buildUI() {
        JPanel background = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(
                        0, 0, new Color(15, 55, 110),
                        getWidth(), getHeight(), new Color(30, 90, 140)));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        content.add(background, BorderLayout.CENTER);

        JPanel form = new JPanel(new GridLayout(0, 2, 15, 15));
        form.setOpaque(false);
        form.setBorder(new CompoundBorder(
                new EmptyBorder(30, 60, 20, 60),
                new LineBorder(new Color(200, 200, 255), 1, true)));

        Font labelFont = new Font("Segoe UI Emoji", Font.BOLD, 15);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);

        spnDate.setEditor(new JSpinner.DateEditor(spnDate, "dd/MM/yyyy"));
        spnDuration.setEditor(new JSpinner.NumberEditor(spnDuration, "##"));

        form.add(styledLabel("👤 Patient:", labelFont)); cmbPatient.setFont(fieldFont); form.add(cmbPatient);
        form.add(styledLabel("🦷 Dentist:", labelFont)); cmbDentist.setFont(fieldFont); form.add(cmbDentist);
        form.add(styledLabel("🗕️ Date:", labelFont)); spnDate.setFont(fieldFont); form.add(spnDate);
        form.add(styledLabel("⏰ Time:", labelFont)); cmbTime.setFont(fieldFont); form.add(cmbTime);
        form.add(styledLabel("⚡ Urgency:", labelFont)); cmbUrgency.setFont(fieldFont); form.add(cmbUrgency);
        form.add(styledLabel("⏱ Duration (min):", labelFont)); spnDuration.setFont(fieldFont); form.add(spnDuration);
        form.add(styledLabel("📌 Treatment:", labelFont)); cmbTreat.setFont(fieldFont); form.add(cmbTreat);
        form.add(styledLabel("📄 Reason:", labelFont)); cmbReason.setFont(fieldFont); form.add(cmbReason); // ✅ ADDED

        JButton btnBook = new JButton("📅 Book Appointment");
        btnBook.setBackground(new Color(0, 70, 130));
        btnBook.setForeground(Color.WHITE);
        btnBook.setFont(new Font("Segoe UI Emoji", Font.BOLD, 15));
        btnBook.setBorder(new RoundedBorder(10));
        btnBook.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBook.addActionListener(e -> onBook());

        JPanel south = new JPanel(); south.setOpaque(false); south.add(btnBook);

        background.add(form, BorderLayout.CENTER);
        background.add(south, BorderLayout.SOUTH);
    }

    private JLabel styledLabel(String text, Font font) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(font);
        lbl.setForeground(Color.WHITE);
        return lbl;
    }

    private void loadCombos() {
        PatientController pc = new PatientController();
        for (Patient p : pc.getAllPatients()) cmbPatient.addItem(p);

        loadAllDentists();

        TreatmentController tc = new TreatmentController();
        for (Treatment t : tc.getAllTreatments()) cmbTreat.addItem(t);

        AppointmentReasonController arc = new AppointmentReasonController(); // ✅ ADDED
        for (AppointmentReason r : arc.getAllReasons()) cmbReason.addItem(r); // ✅ ADDED
    }

    private void loadAllDentists() {
        cmbDentist.removeAllItems();
        for (Staff d : staffController.getAllDentists()) cmbDentist.addItem(d);
    }

    private void loadAvailableDentistsNow() {
        cmbDentist.removeAllItems();
        for (Staff d : staffController.getAvailableDentistsNow()) {
            cmbDentist.addItem(d);
        }
    }

    private void setupUrgencyListener() {
        cmbUrgency.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String urgency = (String) cmbUrgency.getSelectedItem();

                if ("Urgent".equals(urgency)) {
                    Date now = new Date();
                    spnDate.setValue(now);
                    spnDate.setEnabled(false);
                    cmbTime.removeAllItems();
                    cmbTime.addItem(new SimpleDateFormat("HH:mm").format(now));
                    cmbTime.setEnabled(false);
                    loadAvailableDentistsNow();
                } else {
                    spnDate.setEnabled(true);
                    cmbTime.setEnabled(true);
                    loadAllDentists();
                    refreshAvailableTimes();
                }
            }
        });
    }

    private void setupRoutineSlotRefresh() {
        cmbDentist.addItemListener(e -> refreshAvailableTimes());
        spnDate.addChangeListener(e -> refreshAvailableTimes());
        spnDuration.addChangeListener(e -> refreshAvailableTimes());
    }

    private void refreshAvailableTimes() {
        cmbTime.removeAllItems();

        if (cmbDentist.getSelectedItem() == null || cmbUrgency.getSelectedItem().equals("Urgent"))
            return;

        int staffID = ((Staff) cmbDentist.getSelectedItem()).getStaffID();
        java.sql.Date date = new java.sql.Date(((Date) spnDate.getValue()).getTime());

        AppointmentController ac = new AppointmentController();
        List<String> blocked = ac.getBlockedTimes(staffID, date);

        System.out.println("📅 Selected Date: " + date);
        System.out.println("🧑‍⚕️ Dentist ID: " + staffID);
        System.out.println("⛔ Blocked Times: " + blocked);

        boolean added = false;

        LocalTime start = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(17, 0);

        while (!start.isAfter(end.minusMinutes(15))) {
            String slot = start.toString();
            if (!blocked.contains(slot)) {
                cmbTime.addItem(slot);
                added = true;
            }
            start = start.plusMinutes(15);
        }

        if (!added) {
            cmbTime.addItem("❌ No Available Slots");
            cmbTime.setEnabled(false);
        } else {
            cmbTime.setEnabled(true);
        }
    }

    private void onBook() {
        if (cmbPatient.getSelectedItem() == null || cmbDentist.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(content,
                    "Patient and Dentist are required.", "Input",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (cmbReason.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(content,
                    "Please select an appointment reason.", "Input",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Patient pat = (Patient) cmbPatient.getSelectedItem();
        Staff den = (Staff) cmbDentist.getSelectedItem();
        Date d = (Date) spnDate.getValue();
        String hhmm = (String) cmbTime.getSelectedItem();
        Treatment tre = (Treatment) cmbTreat.getSelectedItem();
        AppointmentReason reason = (AppointmentReason) cmbReason.getSelectedItem();
        String urgency = (String) cmbUrgency.getSelectedItem();

        int reasonID = Integer.parseInt(reason.getReasonID());

        Appointment a = new Appointment(
                pat.getPatientID(),
                den.getStaffID(),
                (tre == null ? 0 : tre.getTreatmentID()),
                d,
                hhmm,
                urgency,
                reasonID
        );

        AppointmentController ctl = new AppointmentController();
        boolean ok;

        try {
            int duration = (Integer) spnDuration.getValue(); // optional
            urgency = cmbUrgency.getSelectedItem().toString();

            ok = ctl.bookAppointment(a, urgency);

        } catch (Exception ex) {
            ex.printStackTrace();
            ok = false;
        }

        if (ok) {
            JOptionPane.showMessageDialog(content, "🎉 Appointment booked successfully!");
        } else {
            JOptionPane.showMessageDialog(content,
                    "⚠️ Selected slot is already taken.", "Booking Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class RoundedBorder extends LineBorder {
        RoundedBorder(int r) {
            super(new Color(0, 70, 130), 2, true);
        }
    }
}
