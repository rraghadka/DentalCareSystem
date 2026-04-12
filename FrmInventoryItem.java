package boundary;

import controller.*;
import entity.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Dentist “Treatment Progress” dashboard
 * --------------------------------------
 * • Lists every *active* treatment‑plan for a dentist
 * • Shows next appointments and treatment type
 * • Lets the user launch a Jasper report
 */
public class FrmDentistDashboard extends JPanel {

    /* ---------- UI fields ---------- */
    private final JLabel lblStatus  = new JLabel(" ");
    private final JLabel lblWelcome = new JLabel(" ");

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Plan ID", "Patient", "Start Date", "Plan Status",
                         "Next Appt.", "Treatment"}, 0);

    private final JTable  table     = new JTable(model);
    private final JButton btnReport = new JButton("Generate Progress Report");

    private int dentistID;

    /* ---------- c’tors ---------- */
    /** Zero‑arg constructor – only for test usage. */
    public FrmDentistDashboard() { buildUI(); }

    /** Called by main menu – dentistID already known. */
    public FrmDentistDashboard(int dentistID) {
        this.dentistID = dentistID;
        buildUI();
        loadData(dentistID);
    }

    /* ---------- UI builder ---------- */
    private void buildUI() {
        setLayout(new BorderLayout(10, 5));
        setBackground(Color.WHITE);

        add(buildTopPanel(),    BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);

        table.setRowHeight(24);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(Color.WHITE);
    }

    private JPanel buildTopPanel() {
        Color navy = new Color(0, 46, 102); // deep navy to match sidebar

        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setBackground(navy);
        pnl.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel title = new JLabel("Dentist – Treatment Progress");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(CENTER_ALIGNMENT);

        lblWelcome.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblWelcome.setForeground(Color.WHITE);
        lblWelcome.setAlignmentX(CENTER_ALIGNMENT);

        pnl.add(title);
        pnl.add(Box.createVerticalStrut(8));
        pnl.add(lblWelcome);
        return pnl;
    }

    private JPanel buildBottomPanel() {
        Color navy = new Color(0, 46, 102);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        south.setBackground(Color.WHITE);

        btnReport.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnReport.setBackground(navy);
        btnReport.setForeground(Color.WHITE);
        btnReport.setFocusPainted(false);
        btnReport.setBorderPainted(false);
        btnReport.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        btnReport.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReport.setEnabled(false);
        btnReport.addActionListener(e -> ReportLauncher.showTreatmentProgressReport(dentistID));

        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblStatus.setForeground(new Color(200, 40, 40));

        south.add(btnReport);
        south.add(lblStatus);
        return south;
    }

    /* ---------- core logic ---------- */
    private void loadData(int dentistID) {
        model.setRowCount(0);
        btnReport.setEnabled(false);
        lblStatus.setText(" ");

        StaffController sc = new StaffController();
        Staff staff = sc.getStaff(dentistID);

        if (staff == null) {
            lblStatus.setText("No such staff member");
            return;
        }
        if (!sc.isDentist(dentistID)) {
            lblStatus.setText("Not a dentist");
            return;
        }

        lblWelcome.setText("Welcome Dr. " + staff.getFirstName() + " " + staff.getLastName());

        TreatmentPlanController planCtrl = new TreatmentPlanController();
        TreatmentController     trtCtrl  = new TreatmentController();
        PatientController       patCtrl  = new PatientController();

        List<TreatmentPlan> plans = planCtrl.getActivePlansForStaff(dentistID);
        for (TreatmentPlan plan : plans) {
            Patient pat = patCtrl.getPatient(plan.getPatientID());
            String patientName = (pat == null) ? "-" : pat.getFirstName() + " " + pat.getLastName();

            List<Appointment> upcoming = trtCtrl.getUpcomingAppointmentsForPlan(plan.getPlanID());

            if (upcoming.isEmpty()) {
                model.addRow(new Object[]{plan.getPlanID(), patientName, plan.getStartDate(),
                                           plan.getStatus(), "-", "-"});
            } else {
                for (Appointment ap : upcoming) {
                    model.addRow(new Object[]{plan.getPlanID(), patientName, plan.getStartDate(),
                                               plan.getStatus(), ap.getAppointmentDate(),
                                               trtCtrl.getTreatmentTypeByID(ap.getTreatmentID())});
                }
            }
        }

        if (model.getRowCount() == 0)
            lblStatus.setText("No active plans");
        else
            btnReport.setEnabled(true);
    }

    /* Used by FrmMainMenu’s split-pane */
    public JPanel getContentPanel() { return this; }
}