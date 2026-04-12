package controller;

import entity.AppointmentReason;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AppointmentReasonController {

    private final String tableName = "TblAppointmentReasons";

    public List<AppointmentReason> getAllReasons() {
        List<AppointmentReason> reasons = new ArrayList<>();

        try (Connection conn = DataBaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName)) {

            while (rs.next()) {
                String reasonID   = rs.getString("reasonID");
                String description = rs.getString("description");
                String reasonName = rs.getString("reasonName");

                reasons.add(new AppointmentReason(reasonID, description, reasonName));
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return reasons;
    }
}
