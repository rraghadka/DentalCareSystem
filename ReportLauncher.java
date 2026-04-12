package controller;

import entity.InvoiceTreatment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** CRUD for TblInvoiceTreataments (lines). */
public class InvoiceTreatmentController {

    public boolean insert(InvoiceTreatment it) {
        String sql = "INSERT INTO TblInvoiceTreataments (invoiceID, treatmentID, description, cost) VALUES (?,?,?,?)";
        try (Connection c = DataBaseManager.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt   (1, it.getInvoiceID());
            ps.setInt   (2, it.getTreatmentID());
            ps.setString(3, it.getDescription());
            ps.setDouble(4, it.getCost());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<InvoiceTreatment> getByInvoice(int invoiceID) {
        List<InvoiceTreatment> list = new ArrayList<>();
        String sql = "SELECT * FROM TblInvoiceTreataments WHERE invoiceID = ?";
        try (Connection c = DataBaseManager.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, invoiceID);
            ResultSet r = ps.executeQuery();
            while (r.next())
                list.add(new InvoiceTreatment(r.getInt("invoiceID"), r.getInt("treatmentID"),
                                              r.getString("description"), r.getDouble("cost")));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}
