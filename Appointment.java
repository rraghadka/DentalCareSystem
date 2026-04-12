package controller;

import entity.TreatmentItemUsage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** CRUD + helper for TblTreatmentItemUsage */
public class TreatmentItemUsageController {

    /* how many units were consumed in the last 30 days */
    public int getUsedLast30Days(int itemID) {
        String sql = """
            SELECT COALESCE(SUM(quantityUsed),0)
            FROM   TblTreatmentItemUsage
            WHERE  itemID = ?
              AND  usageDate >= DateAdd('d',-30, Date())
            """;
        try (Connection c = DataBaseManager.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, itemID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    /* optional: basic insert (useful for tests) */
    public boolean insert(TreatmentItemUsage u) {
        String sql = """
            INSERT INTO TblTreatmentItemUsage (itemID, quantityUsed, usageDate)
            VALUES (?,?,?)
            """;
        try (Connection c = DataBaseManager.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt   (1, u.getItemID());
            ps.setInt   (2, u.getQuantityUsed());
            ps.setDate  (3, new java.sql.Date(u.getUsageDate().getTime()));
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) { ex.printStackTrace(); return false; }
    }

    /* optional: fetch all rows */
    public List<TreatmentItemUsage> getAll() {
        List<TreatmentItemUsage> list = new ArrayList<>();
        String sql = "SELECT * FROM TblTreatmentItemUsage";
        try (Connection c = DataBaseManager.connect();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(new TreatmentItemUsage(
                        rs.getInt("usageID"),
                        rs.getInt("itemID"),
                        rs.getInt("quantityUsed"),
                        rs.getDate("usageDate")));
        } catch (SQLException ex) { ex.printStackTrace(); }
        return list;
    }
}
