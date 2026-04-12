package boundary;

import controller.InventoryItemController;
import controller.XMLImportController;
import entity.InventoryItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.util.List;

public class FrmInventoryItem extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtId = new JTextField(), txtName = new JTextField(),
                       txtDesc = new JTextField(), txtCategory = new JTextField(),
                       txtQty = new JTextField(), txtSupplierID = new JTextField(),
                       txtExpiry = new JTextField(), txtSerial = new JTextField();

    private final InventoryItemController controller = new InventoryItemController();
    private final XMLImportController xmlController = new XMLImportController();

    public FrmInventoryItem() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);

        JPanel form = buildFormPanel();
        buildTable();

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Registered Inventory Items"));

        JPanel center = new JPanel(new BorderLayout(10, 0));
        center.setBorder(new EmptyBorder(10, 10, 10, 10));
        center.add(form, BorderLayout.WEST);
        center.add(tableScroll, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        JPanel south = buildButtonPanel();
        add(south, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> populateForm());
        loadItems();
    }

    private JPanel buildFormPanel() {
        JPanel p = new JPanel(new GridLayout(8, 2, 10, 10));
        p.setBorder(BorderFactory.createTitledBorder("Inventory Item Details"));
        p.setBackground(Color.WHITE);
        p.setPreferredSize(new Dimension(400, 250));

        p.add(new JLabel("ID"));                p.add(txtId);
        p.add(new JLabel("Name"));              p.add(txtName);
        p.add(new JLabel("Description"));       p.add(txtDesc);
        p.add(new JLabel("Category"));          p.add(txtCategory);
        p.add(new JLabel("Qty"));               p.add(txtQty);
        p.add(new JLabel("Supplier ID"));       p.add(txtSupplierID);
        p.add(new JLabel("Expiry (yyyy-mm-dd)")); p.add(txtExpiry);
        p.add(new JLabel("Serial"));            p.add(txtSerial);
        return p;
    }

    private void buildTable() {
        model = new DefaultTableModel(new String[]{
                "ID", "Name", "Description", "Category",
                "Qty", "Supplier ID", "Expiry", "Serial"}, 0);
        table = new JTable(model) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table.setRowHeight(25);
    }

    private JPanel buildButtonPanel() {
        JPanel crud = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        JPanel sys  = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));

        JButton btnAdd    = makeBlue("Add");
        JButton btnUpdate = makeBlue("Update");
        JButton btnDelete = makeBlue("Delete");
        crud.add(btnAdd); crud.add(btnUpdate); crud.add(btnDelete);

        JButton btnImport = makeGreen("Import Monthly XML");
        sys.add(btnImport);

        btnAdd.addActionListener(e -> addItem());
        btnUpdate.addActionListener(e -> updateItem());
        btnDelete.addActionListener(e -> deleteItem());
        btnImport.addActionListener(e -> {
            boolean ok = xmlController.importSupplierAndItemsFromXML("resources/sample_inventory.xml");
            JOptionPane.showMessageDialog(this, ok ? "✅ Imported." : "❌ Import failed.");
            loadItems();
        });

        JPanel south = new JPanel(new GridLayout(2, 1, 5, 5));
        south.add(crud);
        south.add(sys);
        return south;
    }

    private void addItem() { saveItem(true); }
    private void updateItem() { saveItem(false); }

    private void saveItem(boolean isAdd) {
        try {
            int id          = Integer.parseInt(txtId.getText().trim());
            String name     = txtName.getText().trim();
            String desc     = txtDesc.getText().trim();
            String category = txtCategory.getText().trim();
            int qty         = Integer.parseInt(txtQty.getText().trim());
            int supplierId  = Integer.parseInt(txtSupplierID.getText().trim());
            String serial   = txtSerial.getText().trim();
            Date expiry = txtExpiry.getText().trim().isEmpty() ? null
                           : Date.valueOf(txtExpiry.getText().trim());

            InventoryItem item = new InventoryItem(id, name, desc, category,
                                                   qty, supplierId, expiry, serial);

            boolean ok = isAdd ? controller.addInventoryItem(item)
                               : controller.updateInventoryItem(item);

            JOptionPane.showMessageDialog(this,
                    ok ? (isAdd ? "Item added." : "Item updated.")
                       : (isAdd ? "Add failed." : "Update failed."));
            loadItems();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input.");
        }
    }

    private void deleteItem() {
        int r = table.getSelectedRow();
        if (r < 0) { JOptionPane.showMessageDialog(this, "Select a row."); return; }
        int id = Integer.parseInt(model.getValueAt(r, 0).toString());
        boolean ok = controller.deleteInventoryItem(id);
        JOptionPane.showMessageDialog(this, ok ? "Item deleted." : "Delete failed.");
        loadItems();
    }

    private void populateForm() {
        int r = table.getSelectedRow();
        if (r < 0) return;
        txtId.setText(model.getValueAt(r, 0).toString());
        txtName.setText(model.getValueAt(r, 1).toString());
        txtDesc.setText(model.getValueAt(r, 2).toString());
        txtCategory.setText(model.getValueAt(r, 3).toString());
        txtQty.setText(model.getValueAt(r, 4).toString());
        txtSupplierID.setText(model.getValueAt(r, 5).toString());
        txtExpiry.setText(model.getValueAt(r, 6) != null ? model.getValueAt(r, 6).toString() : "");
        txtSerial.setText(model.getValueAt(r, 7).toString());
    }

    private void loadItems() {
        model.setRowCount(0);
        List<InventoryItem> list = controller.getAllInventoryItems();
        for (InventoryItem i : list) {
            model.addRow(new Object[]{
                    i.getItemID(), i.getItemName(), i.getDescription(), i.getCategory(),
                    i.getQuantityInSt(), i.getSupplierID(), i.getExpirationDt(), i.getSerialNumb()
            });
        }
    }

    private JButton makeBlue(String t) { return makeButton(t, new Color(0,87,146), 150); }
    private JButton makeGreen(String t){ return makeButton(t, new Color(34,139,34), 240);}
    private JButton makeButton(String text, Color bg, int w) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(w, 36));
        return b;
    }

    // THIS LINE IS REQUIRED FOR SPLIT PANEL TO WORK:
    public JPanel getContentPanel() { return this; }
}
