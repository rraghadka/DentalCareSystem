package controller;

import entity.MedicalHistory;
import java.sql.*;

public class MedicalHistoryController {
    private static MedicalHistoryController instance;
    private MedicalHistoryController() {}

    public static MedicalHistoryController getInstance() {
        if (instance == null)
            instance = new MedicalHistoryController();
        return instance;
    }

    public MedicalHistory getHistoryByPatientID(int patientID) {
        String sql = "SELECT * FROM TblMedicalHistory WHERE patientID = ?";
        try (Connection conn = DataBaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new MedicalHistory(
                        rs.getInt("medicalHistID"),
                        rs.getInt("patientID"),
                        rs.getString("conditionID"),
                        rs.getString("allergies")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new MedicalHistory(0, patientID, "NONE", "None");
    }
    public String getMedicalHistoryByPatientID(int patientID) {
        String result = "Condition: Not Recorded\nAllergies: None";

        String sql = "SELECT conditionname, allergies FROM TblMedicalHistory WHERE patientID = ?";

        try (Connection conn = DataBaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientID);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String condition = rs.getString("conditionname");
                String allergies = rs.getString("allergies");

                result = String.format(
                    "Condition: %s\nAllergies: %s",
                    (condition != null && !condition.isEmpty()) ? condition : "Not Recorded",
                    (allergies != null && !allergies.isEmpty()) ? allergies : "None"
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public String getDetails(int patientID) {
        return getMedicalHistoryByPatientID(patientID);
    }

}