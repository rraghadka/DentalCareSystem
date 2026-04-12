package boundary;

import controller.TreatmentPlanController;
import controller.PatientController;
import entity.TreatmentPlan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Date;

/** Dialog – create a new Treatment Plan (DentalCare theme) */
public class FrmNewTreatmentPlan extends JDialog {

    /* brand colours & fonts */
    private static final Color PRIMARY_BLUE = new Color(0, 71, 171);
    private static final Font  UI_FONT      = new Font("Segoe UI", Font.PLAIN, 14);

    /* ───── components ───── */
    private final JComboBox<Integer> cmbPatient = new JComboBox<>();
    private final JSpinner           spnStart   = new JSpinner(new SpinnerDateModel());
    private final JButton            btnSave    = new JButton("Save Plan");

    /* ───── controllers & data ───── */
    private final int staffID;
    private final TreatmentPlanController planCtl   = new TreatmentPlanController();
    private final PatientController       patientCtl = new PatientController();

    /* ───── ctor ───── */
    public FrmNewTreatmentPlan(Frame owner, int staffID) {
        super(owner, "New Treatment Plan", true);
        this.staffID = staffID;
        buildUI();
    }

    /* ───── UI ───── */
    private void buildUI() {
        /* frame */
        setSize(430, 270);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);
        getRootPane().setBorder(new LineBorder(PRIMARY_BLUE, 2));

        /* header */
        JLabel header = new JLabel("New Treatment Plan", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.setForeground(PRIMARY_BLUE);
        header.setBorder(new EmptyBorder(12, 0, 6, 0));
        add(header, BorderLayout.NORTH);

        /* form */
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(10, 25, 10, 25));
        GridBagConstraints g = new GridBagConstraints();
        g.insets  = new Insets(10, 10, 10, 10);
        g.anchor  = GridBagConstraints.WEST;

        /** populate patient combo */
        patientCtl.getAllPatientIDs().forEach(cmbPatient::addItem);
        cmbPatient.setBackground(Color.WHITE);
        cmbPatient.setFont(UI_FONT);
        cmbPatient.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setFont(UI_FONT);
                if (value != null) {
                    int id   = (Integer) value;
                    String n = patientCtl.getPatientName(id);   // "" if not found
                    setText(id + " – " + n);
                }
                return this;
            }
        });

        /* start date spinner */
        spnStart.setEditor(new JSpinner.DateEditor(spnStart, "yyyy-MM-dd"));
        spnStart.setValue(new Date());
        ((JSpinner.DefaultEditor) spnStart.getEditor())
                .getTextField().setFont(UI_FONT);

        /* labels */
        JLabel lblPatient = new JLabel("Patient:");
        JLabel lblStart   = new JLabel("Start Date:");
        lblPatient.setFont(UI_FONT);
        lblStart.setFont(UI_FONT);

        /* layout */
        g.gridx = 0; g.gridy = 0; form.add(lblPatient, g);
        g.gridx = 1;               form.add(cmbPatient, g);
        g.gridx = 0; g.gridy = 1; form.add(lblStart, g);
        g.gridx = 1;               form.add(spnStart, g);

        add(form, BorderLayout.CENTER);

        /* save button */
        btnSave.setBackground(PRIMARY_BLUE);
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnSave.setFocusPainted(false);
        btnSave.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSave.setBorder(new EmptyBorder(10, 40, 10, 40));
        btnSave.addActionListener(e -> save());

        JPanel south = new JPanel();
        south.setOpaque(false);
        south.setBorder(new EmptyBorder(10, 10, 20, 10));
        south.add(btnSave);

        add(south, BorderLayout.SOUTH);
    }

    /* ───── save logic ───── */
    private void save() {
        Integer pid = (Integer) cmbPatient.getSelectedItem();
        if (pid == null) {
            JOptionPane.showMessageDialog(this, "Choose a patient.");
            return;
        }
        Date start = (Date) spnStart.getValue();
        TreatmentPlan p = new TreatmentPlan(
                0,          // planID → controller assigns MAX+1
                pid,
                staffID,
                start,
                start,      // endDate = start for now
                0.0,
                "Active"
        );
        if (planCtl.insert(p)) {
            JOptionPane.showMessageDialog(this,
                    "Plan #" + p.getPlanID() + " created ✔");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Patient already has an Active plan.",
                    "Cannot create",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}

