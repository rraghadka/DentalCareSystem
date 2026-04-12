// === DentalHistoryController.java ===
package controller;

import entity.DentalHistory;
import java.sql.*;

public class DentalHistoryController {
    private static DentalHistoryController instance;
    private DentalHistoryController() {}

    public static DentalHistoryController getInstance() {
        if (instance == null)
            instance = new DentalHistoryController();
        return instance;
    }

    public DentalHistory getHistoryByPatientID(int patientID) {
        String sql = "SELECT * FROM TblDentalHistory WHERE patientID = ?";
        try (Connection conn = DataBaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new DentalHistory(
                        rs.getInt("DentalHistID"),
                        rs.getInt("patientID"),
                        rs.getString("pastTreatment"),
                        rs.getString("xRayPath"),
                        rs.getString("DateTaken")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new DentalHistory(0, patientID, "None", "None", "Unknown");
    }
    public String getDentalHistoryByPatientID(int patientID) {
        String result = "X-Ray: None\nDate Taken: Unknown";

        String sql = "SELECT pastTreatment, xRayPath, DateTaken FROM TblDentalHistory WHERE patientID = ?";

        try (Connection conn = DataBaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientID);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String treatment = rs.getString("pastTreatment");
                String xray = rs.getString("xRayPath");
                Date dateTaken = rs.getDate("DateTaken");

                result = String.format(
                    "Treatment: %s\nX-Ray: %s\nDate Taken: %s",
                    treatment != null ? treatment : "None",
                    (xray != null && !xray.isEmpty()) ? "Available" : "None",
                    (dateTaken != null) ? dateTaken.toString() : "Unknown"
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }


    public String getDetails(int patientID) {
        return getDentalHistoryByPatientID(patientID);
    }

}