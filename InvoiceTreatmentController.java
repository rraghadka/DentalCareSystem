package controller;

import entity.Insurance;
import java.sql.*;

public class InsuranceController {
    private static InsuranceController instance;
    private InsuranceController() {}

    public static InsuranceController getInstance() {
        if (instance == null)
            instance = new InsuranceController();
        return instance;
    }

    public Insurance getInsuranceByPatientID(int patientID) {
        String sql = "SELECT * FROM TblInsuranceDetails WHERE patientID = ?";
        try (Connection conn = DataBaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Insurance(
                        rs.getInt("insuranceID"),
                        rs.getString("providerName"),
                        rs.getString("PolicyNumber"),
                        rs.getInt("patientID")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Insurance(0, "Unknown", "", patientID);
    }

    public String getDetails(int patientID) {
        return getInsuranceDetailsByPatientID(patientID);
    }


    public String getProviderByPatient(int patientID) {
        return getInsuranceByPatientID(patientID).getProviderName();
    }
    public String getInsuranceDetailsByPatientID(int patientID) {
        String result = "Provider: Unknown\nPolicy: Not Assigned";

        String sql = "SELECT providerName, PolicyNumber FROM TblInsuranceDetails WHERE patientID = ?";

        try (Connection conn = DataBaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientID);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String provider = rs.getString("providerName");
                String policy = rs.getString("PolicyNumber");

                result = String.format(
                    "Provider: %s\nPolicy: %s",
                    (provider != null && !provider.isEmpty()) ? provider : "Unknown",
                    (policy != null && !policy.isEmpty()) ? policy : "Not Assigned"
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

}