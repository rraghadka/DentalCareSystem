package boundary;

import controller.TreatmentController;
import controller.TreatmentPlanController;
import entity.Treatment;
import entity.TreatmentPlan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public class FrmPlansManagement extends JPanel {

    /* ───────── tables & models ───────── */
    private final JTable tblActive  = new JTable();
    private final JTable tblHistory = new JTable();

    private final DefaultTableModel mdlActive = new DefaultTableModel(
            new String[]{"Plan ID","Patient","Start","End","Status","Total Cost"},0){
        public boolean isCellEditable(int r,int c){ return false; }
    };
    private final DefaultTableModel mdlHistory = new DefaultTableModel(
            new String[]{"Plan ID","Patient","Start","End","Status","Total Cost"},0){
        public boolean isCellEditable(int r,int c){ return false; }
    };

    /* ───────── controls ───────── */
    private final JComboBox<String> cmbTreatment = new JComboBox<>();
    private final JButton btnAdd    = new JButton("Add Treatment");
    private final JButton btnEdit   = new JButton("Edit Plan");
    private final JButton btnFinish = new JButton("Finish Plan");
    private final JTextArea txtViewer = new JTextArea();

    /* ───────── state / utils ───────── */
    private final int staffID;
    private int selectedPlanID = -1;
    private final TreatmentController     trtCtl  = new TreatmentController();
    private final TreatmentPlanController planCtl = new TreatmentPlanController();
    private final NumberFormat money = NumberFormat.getCurrencyInstance(new Locale("en","IL"));

    public FrmPlansManagement(int staffID){
        this.staffID = staffID;
        buildUI();
        loadActivePlans();
        loadHistoryPlans();
    }

    /* ───────────────── build UI ───────────────── */
    private void buildUI(){
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JLabel hdr = new JLabel("Treatment Plans Management",SwingConstants.CENTER);
        hdr.setFont(new Font("Segoe UI",Font.BOLD,20));
        hdr.setForeground(new Color(0,51,102));
        add(hdr,BorderLayout.NORTH);

        /* table headers */
        java.util.function.Consumer<JTable> fmt = t -> {
            t.setRowHeight(24);
            t.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,14));
            t.getTableHeader().setBackground(new Color(0,51,102));
            t.getTableHeader().setForeground(Color.WHITE);
        };
        tblActive .setModel(mdlActive ); fmt.accept(tblActive );
        tblHistory.setModel(mdlHistory); fmt.accept(tblHistory);

        /* right-align cost column */
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        tblActive .getColumnModel().getColumn(5).setCellRenderer(right);
        tblHistory.getColumnModel().getColumn(5).setCellRenderer(right);

        /* selection */
        tblActive.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblActive.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = tblActive.getSelectedRow();
            selectedPlanID = (row>=0)? (int)mdlActive.getValueAt(row,0) : -1;
            loadTreatmentsForPlan();
        });

        /* split pane */
        JScrollPane scAct  = new JScrollPane(tblActive );
        JScrollPane scHist = new JScrollPane(tblHistory);
        scAct .setBorder(new TitledBorder(new LineBorder(new Color(0,51,102),2),
                "Active Plans",TitledBorder.CENTER,TitledBorder.TOP));
        scHist.setBorder(new TitledBorder(new LineBorder(new Color(0,51,102),2),
                "Plan History",TitledBorder.CENTER,TitledBorder.TOP));
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,scAct,scHist);
        split.setResizeWeight(.5);

        /* bottom bar */
        JPanel bar = new JPanel(new GridBagLayout());
        bar.setBackground(Color.WHITE);
        GridBagConstraints g = new GridBagConstraints(); g.insets=new Insets(8,8,8,4);

        cmbTreatment.setPreferredSize(new Dimension(200,26));
        trtCtl.getAllTreatmentTypes().forEach(cmbTreatment::addItem);

        style(btnAdd   , new Color(0,128,  0));
        style(btnEdit  , new Color(0, 70,130));
        style(btnFinish, new Color(0,160, 80));

        btnAdd   .addActionListener(e -> addTreatment());
        btnEdit  .addActionListener(e -> editPlan());
        btnFinish.addActionListener(e -> onFinishPlan());

        g.gridx=0; bar.add(new JLabel("Add Treatment:"),g);
        g.gridx=1; bar.add(cmbTreatment,g);
        g.gridx=2; bar.add(btnAdd   ,g);
        g.gridx=3; bar.add(btnEdit  ,g);
        g.gridx=4; bar.add(btnFinish,g);

        /* viewer */
        txtViewer.setEditable(false);
        txtViewer.setFont(new Font("Consolas",Font.PLAIN,13));
        txtViewer.setBackground(new Color(245,245,245));
        JScrollPane view = new JScrollPane(txtViewer);
        view.setPreferredSize(new Dimension(200,120));
        view.setBorder(new TitledBorder(new LineBorder(new Color(0,51,102),1),
                "Treatments in Plan",TitledBorder.CENTER,TitledBorder.TOP));

        JPanel center = new JPanel(new BorderLayout(8,8));
        center.setBorder(new EmptyBorder(15,18,15,18));
        center.setBackground(Color.WHITE);
        center.add(split,BorderLayout.CENTER);
        center.add(bar  ,BorderLayout.SOUTH);

        add(center,BorderLayout.CENTER);
        add(view  ,BorderLayout.SOUTH);
    }
    private void style(JButton b,Color bg){
        b.setBackground(bg); b.setForeground(Color.WHITE); b.setFocusPainted(false);
    }

    /* ───────────────── loaders ───────────────── */
    private void loadActivePlans(){
        mdlActive.setRowCount(0);
        for(TreatmentPlan p: planCtl.getActivePlansForStaff(staffID)){
            String patient = planCtl.getPatientNameByID(p.getPatientID());
            mdlActive.addRow(new Object[]{
                    p.getPlanID(), patient,
                    p.getStartDate(), p.getEndDate(),
                    p.getStatus(), money.format(p.getCost())
            });
        }
    }
    private void loadHistoryPlans(){
        mdlHistory.setRowCount(0);
        for(TreatmentPlan p: planCtl.getAllPlansForStaff(staffID)){
            String patient = planCtl.getPatientNameByID(p.getPatientID());
            mdlHistory.addRow(new Object[]{
                    p.getPlanID(), patient,
                    p.getStartDate(), p.getEndDate(),
                    p.getStatus(), money.format(p.getCost())
            });
        }
    }
    private void loadTreatmentsForPlan(){
        if(selectedPlanID<0){ txtViewer.setText(""); return; }
        List<Treatment> list = trtCtl.getTreatmentsForPlan(selectedPlanID);
        if(list.isEmpty()){ txtViewer.setText("— no treatments yet —"); return; }
        StringBuilder sb = new StringBuilder();
        list.forEach(t -> sb.append("• ")
                .append(t.getTreatmentType())
                .append(" (Cost:$").append(t.getCost())
                .append(", Days:").append(t.getDuration()).append(")\n"));
        txtViewer.setText(sb.toString());
    }

    /* ───────────────── actions ───────────────── */
    private void addTreatment(){
        if(selectedPlanID<0){
            JOptionPane.showMessageDialog(this,"Select an active plan."); return;
        }
        String name = (String)cmbTreatment.getSelectedItem();
        if(name==null) return;

        if(trtCtl.addTreatmentByName(selectedPlanID,staffID,name)){
            planCtl.refreshTotals(selectedPlanID);
            loadTreatmentsForPlan();
            loadActivePlans();
        }
    }
    private void editPlan(){
        if(selectedPlanID<0){
            JOptionPane.showMessageDialog(this,"Select an active plan."); return;
        }
        FrmEditTreatmentPlan dlg = new FrmEditTreatmentPlan(selectedPlanID);
        dlg.setModal(true); dlg.setVisible(true);

        TreatmentPlan p = planCtl.getPlan(selectedPlanID);
        if(p!=null){
            LocalDate s = new Date(p.getStartDate().getTime()).toLocalDate();
            LocalDate e = new Date(p.getEndDate()  .getTime()).toLocalDate();
            if(e.isBefore(s)){
                JOptionPane.showMessageDialog(this,
                        "❌ End-date cannot be before start-date.\nChanges were NOT saved.",
                        "Date Error",JOptionPane.ERROR_MESSAGE);
            }
        }
        loadActivePlans(); loadHistoryPlans();
    }
    private void onFinishPlan(){
        if(selectedPlanID<0){
            JOptionPane.showMessageDialog(this,"Select an active plan."); return;
        }
        if(planCtl.finishPlan(selectedPlanID)){                // completed OK
            JOptionPane.showMessageDialog(this,"Plan marked Completed ✔");
            afterFinish(); return;
        }
        int ch = JOptionPane.showConfirmDialog(
                this,
                """
                There are still appointments that are not completed.

                Cancel the plan and its appointments?
                """,
                "Cancel Plan?",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if(ch==JOptionPane.YES_OPTION){
            planCtl.cancelPlan(selectedPlanID);
            JOptionPane.showMessageDialog(this,"Plan & appointments cancelled.");
            afterFinish();
        }
    }
    private void afterFinish(){
        selectedPlanID = -1;
        loadActivePlans();
        loadHistoryPlans();
        txtViewer.setText("");
    }
}
