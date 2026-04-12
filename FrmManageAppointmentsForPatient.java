package boundary;
import entity.InvoiceRow;
import entity.Consts;
import java.io.InputStream;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.view.JasperViewer;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import controller.InvoiceController;
import controller.TreatmentController;
import controller.TreatmentPlanController;
import entity.Invoice;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import entity.InvoiceRow;

/**
 * Invoice-by-patient panel (styled to match DentalCare main UI).
 */
public class FrmInvoiceByPatient extends JPanel {

    /* ─── brand look ─── */
    private static final Color PRIMARY_BLUE = new Color(0, 71, 171);
    private static final Font  UI_FONT      = new Font("Segoe UI", Font.PLAIN, 14);

    /* ─── UI ─── */
    private final JComboBox<Integer> cmbPatients = new JComboBox<>();
    private final DefaultTableModel mdlPlans = new DefaultTableModel(
            new String[]{"Plan ID", "Patient", "Treatments", "Un-billed ₪"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tblPlans = new JTable(mdlPlans);

    /* ─── helpers ─── */
    private final TreatmentPlanController planCtl = new TreatmentPlanController();
    private final TreatmentController     trtCtl  = new TreatmentController();   // reserved – may be used in future
    private final InvoiceController       invCtl  = new InvoiceController();
    private final NumberFormat            nf      = NumberFormat.getCurrencyInstance(new Locale("he", "IL"));

    private final int        dentistID;
    private final Component  owner;   // parent frame (for dialogs)

    /* ─── constructor ─── */
    public FrmInvoiceByPatient(Component parent, int dentistID) {
        this.owner     = parent;
        this.dentistID = dentistID;
        buildUI();
        loadPatients();
    }

    /* ───────────────────────────────────────────────────────── UI */
    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(15, 20, 15, 20));

        /* header */
        JLabel lblHeader = new JLabel("Generate Invoice", SwingConstants.CENTER);
        lblHeader.setForeground(PRIMARY_BLUE);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHeader.setBorder(new EmptyBorder(0, 0, 8, 0));
        add(lblHeader, BorderLayout.NORTH);

        /* top – patient selector row */
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        top.setOpaque(false);

        JLabel labSelect = new JLabel("Select Patient:");
        labSelect.setFont(UI_FONT);
        top.add(labSelect);

        cmbPatients.setPreferredSize(new Dimension(130, 26));
        cmbPatients.setFont(UI_FONT);
        top.add(cmbPatients);

        JButton btnLoad = new JButton("Load Plans");
        btnLoad.setFont(UI_FONT);
        btnLoad.setBackground(PRIMARY_BLUE);
        btnLoad.setForeground(Color.WHITE);
        btnLoad.setFocusPainted(false);
        btnLoad.addActionListener(e -> refreshTable());
        top.add(btnLoad);

        add(top, BorderLayout.BEFORE_FIRST_LINE);

        /* center – plans table */
        tblPlans.setRowHeight(26);
        tblPlans.setFont(UI_FONT);
        tblPlans.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblPlans.getTableHeader().setForeground(Color.WHITE);
        tblPlans.getTableHeader().setBackground(PRIMARY_BLUE);
        tblPlans.setFillsViewportHeight(true);
        tblPlans.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblPlans.setGridColor(new Color(230, 230, 230));

        // zebra rows
        tblPlans.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected)
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 250));
                return c;
            }
        });

        JScrollPane scroller = new JScrollPane(tblPlans);
        scroller.setBorder(new LineBorder(new Color(210, 210, 210)));
        add(scroller, BorderLayout.CENTER);

        /* bottom – save PDF button */
        JButton btnPdf = new JButton("Save PDF");
        btnPdf.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnPdf.setBackground(PRIMARY_BLUE);
        btnPdf.setForeground(Color.WHITE);
        btnPdf.setFocusPainted(false);
        btnPdf.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnPdf.setBorder(new EmptyBorder(8, 25, 8, 25));
        btnPdf.addActionListener(e -> createInvoice());

        JPanel south = new JPanel();
        south.setOpaque(false);
        south.setBorder(new EmptyBorder(12, 0, 0, 0));
        south.add(btnPdf);
        add(south, BorderLayout.SOUTH);
    }

    /* ─────────────────────────────────────────────── data helpers */
    private void loadPatients() {
        try {
            List<Integer> ids = planCtl.getPatientsWithUnbilledTreatments(dentistID);
            cmbPatients.removeAllItems();
            ids.forEach(cmbPatients::addItem);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(owner, "Failed loading patients");
        }
    }

    private void refreshTable() {
        mdlPlans.setRowCount(0);
        Integer pid = (Integer) cmbPatients.getSelectedItem();
        if (pid == null) return;

        try {
            List<Object[]> rows = planCtl.getPlanRowsForTable(pid, dentistID);
            for (Object[] r : rows) {
                double amount = (double) r[3];
                if (amount > 0) {
                    mdlPlans.addRow(new Object[]{
                            r[0],              // planID
                            r[1],              // patient name
                            r[2],              // treatments summary
                            nf.format(amount)  // formatted ₪
                    });
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed loading plans");
        }
    }

    /* ───────────────────────────────────────────── invoice logic */
    private void createInvoice() {
        int row = tblPlans.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(owner, "Select a plan first.");
            return;
        }
        int planID    = (Integer) mdlPlans.getValueAt(row, 0);
        int patientID = (Integer) cmbPatients.getSelectedItem();

        try {
            Invoice inv = invCtl.generateInvoice(planID, patientID, dentistID);
            if (inv == null) {
                JOptionPane.showMessageDialog(owner,
                        "Nothing left to invoice for this plan.");
            } else {
                JOptionPane.showMessageDialog(owner,
                        "Invoice #" + inv.getInvoiceID() +
                                " saved to PDF\nTotal: " + nf.format(inv.getTotalAmount()) +
                                "\nDate: " + inv.getIssuedDate());

                // 🟡 Jasper report logic
                try {
                    List<InvoiceRow> rows = invCtl.getInvoiceRows(inv.getInvoiceID());
                    InputStream reportStream = getClass().getClassLoader().getResourceAsStream("InvoiceReport.jrxml");

                    JasperReport report = JasperCompileManager.compileReport(reportStream);
                    JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);
                    JasperPrint print = JasperFillManager.fillReport(report, null, dataSource);

                    // 1. Show the Jasper report preview window
                    JasperViewer.viewReport(print, false);

                    // 2. Save the report as a PDF file
                    JasperExportManager.exportReportToPdfFile(print,
                            "Invoice_" + inv.getInvoiceID() + ".pdf");

                    System.out.println("✔ Invoice exported to: Invoice_" + inv.getInvoiceID() + ".pdf");
                } catch (Exception jex) {
                    jex.printStackTrace();
                    JOptionPane.showMessageDialog(owner, "Error generating report.");
                }

                refreshTable();
                loadPatients();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(owner, "Failed to create invoice.");
        }
    }

}
