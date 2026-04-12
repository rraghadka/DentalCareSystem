package boundary;

import controller.TreatmentPlanController;
import entity.Patient;
import entity.TreatmentPlan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class FrmTrackingPlans extends JPanel {

    private final Patient currentPatient;
    private JTable table;

    public FrmTrackingPlans(Patient patient) {
        this.currentPatient = patient;
        setLayout(new BorderLayout());
        setBackground(new Color(25, 60, 120));

        JLabel title = new JLabel(" Your Treatment History", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        title.setBorder(new EmptyBorder(20, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        // Table setup
        table = new JTable();
        table.setModel(new DefaultTableModel(new Object[][]{}, new String[]{"Plan ID", "Start Date", "End Date", "Status", "Cost"}) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 30, 20, 30));
        add(scrollPane, BorderLayout.CENTER);

        styleTable();
        loadData();
    }

    private void loadData() {
        TreatmentPlanController controller = new TreatmentPlanController();
        List<TreatmentPlan> plans = controller.getPlansForPatient(currentPatient.getPatientID());

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0); // Clear previous data

        if (plans.isEmpty()) {
            model.addRow(new Object[]{"-", "-", "-", "-", "No records"});
        } else {
            for (TreatmentPlan plan : plans) {
                model.addRow(new Object[]{
                        plan.getPlanID(),
                        plan.getStartDate(),
                        plan.getEndDate(),
                        plan.getStatus(),
                        String.format("%.2f₪", plan.getCost())
                });
            }
        }
    }

    private void styleTable() {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        table.getTableHeader().setBackground(new Color(80, 100, 160));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setBackground(new Color(245, 250, 255));
        table.setSelectionBackground(new Color(200, 220, 255));
        table.setGridColor(Color.LIGHT_GRAY);
    }

    public JPanel getContentPanel() {
        return this;
    }
}
