package boundary;

import controller.TreatmentPlanController;
import entity.TreatmentPlan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Date;

/** Modal dialog – edit header fields of a treatment plan. */
public class FrmEditTreatmentPlan extends JDialog {

    private final TreatmentPlanController ctl = new TreatmentPlanController();
    private final JSpinner spnStart  = new JSpinner(new SpinnerDateModel());
    private final JSpinner spnEnd    = new JSpinner(new SpinnerDateModel());
    private final JComboBox<String> cmbStatus =
            new JComboBox<>(new String[]{"Active", "Completed", "Cancelled"});

    private final int planID;

    public FrmEditTreatmentPlan(int planID) {
        super((Frame) null, "Edit Treatment Plan", true);
        this.planID = planID;

        /* ---------- basic window ---------- */
        setSize(450, 300);
        setLocationRelativeTo(null);
        setResizable(false);

        /* ---------- root ---------- */
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(245, 245, 245));     // light grey backdrop
        add(root);

        /* ---------- white card ---------- */
        JPanel card = new JPanel(new GridBagLayout());
        card.setBorder(new EmptyBorder(25, 35, 25, 35));
        card.setBackground(Color.WHITE);
        root.add(card, BorderLayout.CENTER);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.anchor = GridBagConstraints.WEST;

        /* labels – bold navy */
        Font labelFont = new Font("Segoe UI", Font.BOLD, 14);
        Color navy     = new Color(0, 70, 130);

        JLabel lblStart  = new JLabel("Start Date:");   lblStart.setFont(labelFont);  lblStart.setForeground(navy);
        JLabel lblEnd    = new JLabel("End Date:");     lblEnd.setFont(labelFont);    lblEnd.setForeground(navy);
        JLabel lblStatus = new JLabel("Status:");       lblStatus.setFont(labelFont); lblStatus.setForeground(navy);

        /* spinners – keep them wide & pretty */
        Dimension fieldSz = new Dimension(180, 28);
        spnStart.setPreferredSize(fieldSz);
        spnEnd  .setPreferredSize(fieldSz);
        cmbStatus.setPreferredSize(fieldSz);

        g.gridx = 0; g.gridy = 0; card.add(lblStart,  g);
        g.gridx = 1;             card.add(spnStart, g);
        g.gridx = 0; g.gridy = 1; card.add(lblEnd,    g);
        g.gridx = 1;             card.add(spnEnd,   g);
        g.gridx = 0; g.gridy = 2; card.add(lblStatus, g);
        g.gridx = 1;             card.add(cmbStatus, g);

        /* ---------- button row ---------- */
        JButton btnSave = new JButton("Save");
        btnSave.setBackground(navy);
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnSave.setFocusPainted(false);
        btnSave.setPreferredSize(new Dimension(100, 36));
        btnSave.addActionListener(e -> saveAndClose());

        JPanel south = new JPanel();
        south.setOpaque(false);
        south.add(btnSave);
        root.add(south, BorderLayout.SOUTH);

        /* ---------- preload data ---------- */
        TreatmentPlan p = ctl.getPlan(planID);
        if (p != null) {
            spnStart .setValue(p.getStartDate());
            spnEnd   .setValue(p.getEndDate());
            cmbStatus.setSelectedItem(p.getStatus());
        }
    }// inside FrmEditTreatmentPlan (keep the rest of your class)
    private void saveAndClose() {
        TreatmentPlan p = ctl.getPlan(planID);
        if (p == null) { dispose(); return; }

        p.setStartDate((Date) spnStart.getValue());
        p.setEndDate  ((Date) spnEnd.getValue());
        p.setStatus   ((String) cmbStatus.getSelectedItem());

        ctl.updateHeader(p);         // ✅ now compiles

        dispose();
    }
}


