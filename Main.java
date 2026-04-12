package boundary;

import controller.TreatmentController;
import controller.TreatmentPlanController;
import entity.Treatment;
import entity.TreatmentPlan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.List;

/**
 * Dialog: list treatments + add new treatment (type, duration, cost)
 */
public class FrmTreatmentPlanBuilder extends JPanel {

    private static final Color NAVY = new Color(0, 46, 102);

    private final int dentistID;
    private final TreatmentPlan plan;
    private final TreatmentController trtCtl = new TreatmentController();
    private final TreatmentPlanController planCtl = new TreatmentPlanController();
    private final DefaultTableModel model = new DefaultTableModel(
    	    new String[]{"ID", "Type", "Duration", "Cost"}, 0) {
    	    public boolean isCellEditable(int r, int c) {
    	        return false;
    	    }
    	};


    private final JTextField txtType = new JTextField();
    private final JSpinner spnDays = new JSpinner(new SpinnerNumberModel(1, 1, 365, 1));
    private final JTextField txtCost = new JTextField();

    public FrmTreatmentPlanBuilder(int dentistID, TreatmentPlan plan) {
        this.dentistID = dentistID;
        this.plan = plan;
        buildUI();
        loadTreatments();
    }

    private void buildUI() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(18, 22, 22, 22));

        JLabel hdr = new JLabel(
                "Plan #" + plan.getPlanID() + " • PatientID " + plan.getPatientID(),
                SwingConstants.CENTER);
        hdr.setFont(new Font("Segoe UI", Font.BOLD, 18));
        hdr.setOpaque(true);
        hdr.setBackground(NAVY);
        hdr.setForeground(Color.WHITE);
        hdr.setBorder(new EmptyBorder(8, 0, 8, 0));
        add(hdr, BorderLayout.NORTH);

        JTable tbl = new JTable(model);
        tbl.setRowHeight(22);
        TableColumnModel tcm = tbl.getColumnModel();
        tcm.getColumn(1).setPreferredWidth(140);
        add(new JScrollPane(tbl), BorderLayout.CENTER);

        /* Add-form */
        JPanel add = new JPanel(new GridLayout(2, 4, 8, 8));
        add.setBorder(new EmptyBorder(10, 0, 0, 0));

        txtType.setPreferredSize(new Dimension(160, 22));
        txtType.setToolTipText("e.g. Cleaning, Filling");
        txtCost.setToolTipText("numeric");

        add.add(new JLabel("Type:"));
        add.add(txtType);
        add.add(new JLabel("Duration:"));
        add.add(spnDays);

        add.add(new JLabel("Cost:"));
        add.add(txtCost);
        JButton btn = new JButton("Add Treatment");
        btn.setBackground(new Color(0, 120, 215));
        btn.setForeground(Color.WHITE);
        btn.addActionListener(e -> onAddTreatment());

        add.add(new JLabel());
        add.add(btn);
        add(add, BorderLayout.SOUTH);
    }

    /* Load treatments */
    private void loadTreatments() {
        model.setRowCount(0);
        List<Treatment> list = trtCtl.getTreatmentsForPlan(plan.getPlanID());
        for (Treatment t : list) {model.addRow(new Object[]{
        	    t.getTreatmentID(),
        	    t.getTreatmentType(),
        	    t.getDuration(),
        	    t.getCost()
        	});

          
        }
    }
    private void onFinishPlan() {
        boolean ok = planCtl.finishPlan(plan.getPlanID());
        if (ok) {
            JOptionPane.showMessageDialog(this,
                "Plan marked as Completed ✔");
            plan.setStatus("Completed");   // keep in-memory copy up to date
            planCtl.refreshTotals(plan.getPlanID());
            loadTreatments();              // viewer stays read-only
        } else {
            JOptionPane.showMessageDialog(this,
                "Cannot complete plan – at least one appointment is still open.",
                "Finish Plan", JOptionPane.WARNING_MESSAGE);
        }
    }


    /* Add new treatment */
    private void onAddTreatment() {
        try {
            int days = (int) spnDays.getValue();
            double cost = Double.parseDouble(txtCost.getText().trim());
            String type = txtType.getText().trim();

            Treatment t = new Treatment(plan.getPlanID(), dentistID, type, cost, days);

            boolean success = trtCtl.addTreatmentByName(t.getPlanID(), t.getStaffID(), t.getTreatmentType());

            if (success) {
                JOptionPane.showMessageDialog(this, "Treatment added successfully.");
                loadTreatments(); // refresh the table
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add treatment – ensure template row exists.");
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric cost.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}

