package boundary;

import controller.SupplierController;
import controller.XMLImportController;
import entity.Supplier;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class FrmSupplier extends JPanel {

    private final SupplierController controller = new SupplierController();
    private final XMLImportController xmlController = new XMLImportController();

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtId = new JTextField(), txtFirst = new JTextField(),
                       txtLast = new JTextField(), txtPhone = new JTextField(),
                       txtEmail = new JTextField();

    public FrmSupplier() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);

        JPanel form = buildFormPanel();
        buildTable();

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Registered Suppliers"));

        JPanel center = new JPanel(new BorderLayout(10, 0));
        center.setBorder(new EmptyBorder(10, 10, 10, 10));
        center.add(form, BorderLayout.WEST);
        center.add(tableScroll, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        JPanel south = buildButtonPanel();
        add(south, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> populateForm());
        loadSuppliers();
    }

    private JPanel buildFormPanel() {
        JPanel p = new JPanel(new GridLayout(5, 2, 10, 10));
        p.setBorder(BorderFactory.createTitledBorder("Supplier Details"));
        p.setBackground(Color.WHITE);
        p.setPreferredSize(new Dimension(400, 250));

        p.add(new JLabel("ID"));        p.add(txtId);
        p.add(new JLabel("First Name"));p.add(txtFirst);
        p.add(new JLabel("Last Name")); p.add(txtLast);
        p.add(new JLabel("Phone"));     p.add(txtPhone);
        p.add(new JLabel("Email"));     p.add(txtEmail);
        return p;
    }

    private void buildTable() {
        model = new DefaultTableModel(new String[]{
                "ID", "First Name", "Last Name", "Phone", "Email"}, 0);
        table = new JTable(model) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table.setRowHeight(25);
    }

    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

        JButton btnAdd    = makeButton("Add", new Color(0, 87, 146));
        JButton btnUpdate = makeButton("Update", new Color(0, 87, 146));
        JButton btnDelete = makeButton("Delete", new Color(0, 87, 146));
        JButton btnImport = makeButton("Import Monthly XML", new Color(34, 139, 34));

        panel.add(btnAdd);
        panel.add(btnUpdate);
        panel.add(btnDelete);
        panel.add(btnImport);

        btnAdd.addActionListener(e -> saveSupplier(true));
        btnUpdate.addActionListener(e -> saveSupplier(false));
        btnDelete.addActionListener(e -> deleteSupplier());

        btnImport.setToolTipText("Import supplier and inventory data from XML.");
        btnImport.addActionListener(e -> {
            boolean ok = xmlController.importSupplierAndItemsFromXML("resources/sample_inventory.xml");
            JOptionPane.showMessageDialog(this,
                    ok ? "✅ Imported." : "❌ Import failed.");
            loadSuppliers();
        });

        return panel;
    }

    private void saveSupplier(boolean isAdd) {
        try {
            int id = Integer.parseInt(txtId.getText().trim());
            String fName = txtFirst.getText().trim();
            String lName = txtLast.getText().trim();
            String phone = txtPhone.getText().trim();
            String email = txtEmail.getText().trim();

            Supplier s = new Supplier(id, fName, lName, phone, email);
            boolean ok = isAdd ? controller.addSupplier(s)
                               : controller.updateSupplier(s);
            JOptionPane.showMessageDialog(this,
                    ok ? (isAdd ? "Supplier added." : "Supplier updated.")
                       : (isAdd ? "Add failed." : "Update failed."));
            loadSuppliers();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid input.");
        }
    }

    private void deleteSupplier() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a row."); return; }

        int id = Integer.parseInt(model.getValueAt(row, 0).toString());
        boolean ok = controller.deleteSupplier(id);
        JOptionPane.showMessageDialog(this, ok ? "Supplier deleted." : "Delete failed.");
        loadSuppliers();
    }

    private void populateForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        txtId.setText(model.getValueAt(row, 0).toString());
        txtFirst.setText(model.getValueAt(row, 1).toString());
        txtLast.setText(model.getValueAt(row, 2).toString());
        txtPhone.setText(model.getValueAt(row, 3).toString());
        txtEmail.setText(model.getValueAt(row, 4).toString());
    }

    private void loadSuppliers() {
        model.setRowCount(0);
        List<Supplier> list = controller.getAllSuppliers();
        for (Supplier s : list) {
            model.addRow(new Object[]{
                    s.getSupplierID(), s.getFirstName(), s.getLastName(),
                    s.getPhoneNumber(), s.getEmail()
            });
        }
    }

    private JButton makeButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(180, 36));
        return b;
    }

    public JPanel getContentPanel() {
        return this;
    }
}
