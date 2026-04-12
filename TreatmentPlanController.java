package controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Fetch distinct treatment names from DB (no hard-coding) */
public class TreatmentCatalogController {

    /** Returns all distinct names ordered alphabetically */
    public String[] getAllNames() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT treatmentType FROM TblTreatments ORDER BY treatmentType";

        try (Connection conn = DataBaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(rs.getString(1));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list.toArray(String[]::new);
    }

    /** Returns cost of the FIRST matching row (null if not found) */
    public Double getCostByName(String name) {
        String sql = "SELECT cost FROM TblTreatments WHERE treatmentType = ? LIMIT 1";
        try (Connection conn = DataBaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}
