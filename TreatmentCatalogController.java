package controller;

import entity.Consts;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.view.JasperViewer;

import javax.swing.*;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class ReportLauncher {

    /**
     * Shows the treatment progress report (PDF viewer).
     */
	public static void showTreatmentProgressReport(int staffID) {
	    DataBaseManager dbm = new DataBaseManager();
	    if (!dbm.isDentist(staffID)) {
	        JOptionPane.showMessageDialog(null,
	                "רק רופא שיניים מורשה לצפות בדוח זה.",
	                "שגיאת הרשאה", JOptionPane.ERROR_MESSAGE);
	        return;
	    }

	    Connection conn = null;
	    try {
	        // ✅ Correct way to load from resources
	    	InputStream jrxml = Consts.class.getResourceAsStream("/TreatmentPlanReport.jrxml");





	    	if (jrxml == null) {
	    	    throw new RuntimeException("❌ JRXML not found! Make sure it's in src/reports/");
	    	}

	        System.out.println("✅ [DEBUG] Compiling JRXML...");
	        JasperReport jasper = JasperCompileManager.compileReport(jrxml);

	        Map<String, Object> parameters = new HashMap<>();
	        parameters.put("staffID", staffID);

	        conn = DataBaseManager.connect();

	        System.out.println("📄 [DEBUG] Filling report...");
	        JasperPrint print = JasperFillManager.fillReport(jasper, parameters, conn);

	        System.out.println("✅ [DEBUG] Report filled successfully. Opening viewer...");
	        JasperViewer viewer = new JasperViewer(print, false);
	        viewer.setTitle("Treatment Progress Report");
	        viewer.setVisible(true);

	    } catch (Exception e) {
	        System.err.println("❌ [ERROR] Exception while generating report:");
	        e.printStackTrace();
	        JOptionPane.showMessageDialog(null,
	                "שגיאה בעת הצגת הדוח.", "שגיאה", JOptionPane.ERROR_MESSAGE);
	    } finally {
	        DataBaseManager.close(conn);
	    }
	}


    /**
     * Exports the report directly to a PDF file chosen by the user.
     */
    public static void exportReportToPdf(int staffID) {
        if (!new DataBaseManager().isDentist(staffID)) {
            JOptionPane.showMessageDialog(null,
                    "Only dentists can export this report.",
                    "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DataBaseManager.connect()) {
            System.out.println("🔍 [DEBUG] Loading JRXML for PDF export...");
            InputStream jrxml = Consts.class.getClassLoader().getResourceAsStream("TreatmentPlanReport.jrxml");

            if (jrxml == null) {
                System.err.println("❌ [ERROR] JRXML not found for export.");
                JOptionPane.showMessageDialog(null, "JRXML file not found!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JasperReport jasper = JasperCompileManager.compileReport(jrxml);
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("staffID", staffID);

            JasperPrint print = JasperFillManager.fillReport(jasper, parameters, conn);

            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Save Report as PDF");
            chooser.setSelectedFile(new java.io.File("TreatmentReport_Staff" + staffID + ".pdf"));

            int result = chooser.showSaveDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                String pdfPath = chooser.getSelectedFile().getAbsolutePath();
                JasperExportManager.exportReportToPdfFile(print, pdfPath);
                JOptionPane.showMessageDialog(null,
                        "Report exported successfully to:\n" + pdfPath);
            }

        } catch (Exception e) {
            System.err.println("❌ [ERROR] Failed to export PDF:");
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error exporting report.", "Export Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}