package boundary;

import controller.XMLImportController;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class FrmXMLImport extends JPanel {

    private final XMLImportController controller = new XMLImportController();

    public FrmXMLImport() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(240, 244, 248));

        // Info label
        JLabel lblInfo = new JLabel(
                "<html><center>Click the button below to import supplier and inventory items from:<br><b>resources/sample_inventory.xml</b></center></html>",
                JLabel.CENTER);
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblInfo.setForeground(new Color(0, 51, 102));
        lblInfo.setBorder(BorderFactory.createEmptyBorder(30, 10, 10, 10));
        add(lblInfo, BorderLayout.NORTH);

        // Empty center panel (can be expanded later)
        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(new Color(240, 244, 248));
        add(centerPanel, BorderLayout.CENTER);

        // Bottom panel with import button
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(240, 244, 248));

        JButton btnImport = new JButton("Import Supplier and Inventory");
        btnImport.setPreferredSize(new Dimension(300, 40));
        styleButton(btnImport);
        bottomPanel.add(btnImport);
        add(bottomPanel, BorderLayout.SOUTH);

        // Action listener
        btnImport.addActionListener(e -> {
        	String filePath = "sample_inventory.xml";
            File file = new File(filePath);
            if (!file.exists()) {
                JOptionPane.showMessageDialog(this,
                        "XML file not found in:\n" + filePath,
                        "Missing File", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean success = controller.importSupplierAndItemsFromXML(filePath);
            JOptionPane.showMessageDialog(this,
                    success ? "Import successful!" : "Import failed. Check XML or DB.",
                    success ? "Success" : "Import Error",
                    success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
        });
    }

    private void styleButton(JButton button) {
        Color darkBlue = new Color(0, 51, 102);
        button.setBackground(darkBlue);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(darkBlue));
    }

    public JPanel getContentPanel() {
        return this;
    }
}
