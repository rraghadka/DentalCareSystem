package controller;

import entity.Appointment;
import entity.Treatment;

import javax.swing.JOptionPane;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD + helper look-ups for <b>TblTreatments</b>.
 * Durations are stored on each treatment row; start/end dates belong to the plan header.
 */
public class TreatmentController {

    /* ─────────────── READS ─────────────── */

    /** Upcoming appointments for one plan (soonest first) */
    public List<Appointment> getUpcomingAppointmentsForPlan(int planID) {
        List<Appointment> list = new ArrayList<>();
        String sql = """
            SELECT  a.*
            FROM    TblTreatments  t
            JOIN    TblAppointment a  ON a.treatmentID = t.treatmentID
            WHERE   t.planID          = ?
              AND   a.appointmentDate >= Date()
            ORDER   BY a.appointmentDate
        """;

        try (Connection c = DataBaseManager.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, planID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(buildAppointment(rs));
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return list;
    }
    /* ───── duplicate ACTIVE-plan check ───── */
    public boolean hasPlanForPatient(int patientID) {

        // 1. Access SQL that works under UCanAccess – no Date(), no extra filters
        final String sql = """
            SELECT 1
            FROM   TblTreatmentPlans
            WHERE  patientID = ?
            AND    status    = 'Active'
        """;

        try (Connection c = DataBaseManager.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, patientID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();            // true  → Active plan exists
            }

        } catch (SQLException e) {           // if anything goes wrong, log & be safe
            e.printStackTrace();
            return true; }                    // (block creation on real DB errors)
        }/* ────────────────── completed + unbilled ───────────────── */
    public List<Treatment> getCompletedUnbilled(int planID) {
        List<Treatment> list = new ArrayList<>();

        String sql = """
            SELECT DISTINCT t.*
            FROM   TblTreatments t
            JOIN   TblAppointment a ON t.treatmentID = a.treatmentID
            WHERE  t.planID = ?
              AND  a.status = 'Completed'
              AND  t.invoicedInvoiceID IS NULL
        """;

        try (Connection c = DataBaseManager.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, planID);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new Treatment(
                        rs.getInt("treatmentID"),
                        rs.getInt("planID"),
                        rs.getInt("staffID"),
                        rs.getString("treatmentType"),
                        rs.getDouble("cost"),
                        rs.getInt("duration")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
    public int getTreatmentIDByReasonID(int reasonID) {
        String sql = "SELECT treatmentID FROM TblTreatment WHERE reasonID = ?";
        try (Connection conn = DataBaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reasonID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("treatmentID");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0; // ← fallback
    }
    public int getTreatmentIDByType(String treatmentType) {
        String sql = "SELECT treatmentID FROM TblTreatment WHERE treatmentType = ?";
        try (Connection conn = DataBaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, treatmentType);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("treatmentID");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // not found
    }

        /* ────────────────── cost of un-billed treatments ───────────────── */
    public double sumUnbilledCostForPlan(int planID) {
        String sql = """
            SELECT SUM(t.cost)
            FROM   TblTreatments t
            JOIN   TblAppointment a ON t.treatmentID = a.treatmentID
            WHERE  t.planID = ?
              AND  a.status = 'Completed'
              AND  t.invoicedInvoiceID IS NULL
        """;

        try (Connection c = DataBaseManager.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, planID);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getDouble(1) : 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }


    public List<Treatment> getAllTreatments() {
        List<Treatment> list = new ArrayList<>();
        String sql = "SELECT * FROM TblTreatments";

        try (Connection c = DataBaseManager.connect();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Treatment t = new Treatment(
                    rs.getInt("treatmentID"),
                    rs.getString("treatmentType"),
                    rs.getDouble("cost"),
                    rs.getInt("duration")
                );
                list.add(t);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** All treatments for a plan (viewer helper) */
    public List<Treatment> getTreatmentsForPlan(int planID) {
        List<Treatment> list = new ArrayList<>();
        String sql = "SELECT * FROM TblTreatments WHERE planID = ?";

        try (Connection c = DataBaseManager.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, planID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(buildTreatment(rs));
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return list;
    }

    /** Distinct treatment-type names – fills the combo-box */
    public List<String> getAllTreatmentTypes() {
        List<String> names = new ArrayList<>();
        String sql = "SELECT DISTINCT treatmentType FROM TblTreatments ORDER BY treatmentType";

        try (Connection c = DataBaseManager.connect();
             Statement  s = c.createStatement();
             ResultSet  r = s.executeQuery(sql)) {

            while (r.next()) names.add(r.getString(1).trim());
        } catch (SQLException ex) { ex.printStackTrace(); }
        return names;
    }

    /* ─────────────── CREATES ─────────────── */

    /**
     * Add a treatment to a plan by copying <i>cost</i> and <i>duration</i>
     * from an existing template row with the same treatmentType.
     *
     * @return <code>true</code> if a row was inserted; <code>false</code> otherwise
     */
    public boolean addTreatmentByName(int planID, int staffID, String treatmentName) {

    	final String insertSql = """
    		    INSERT INTO TblTreatments (treatmentID, planID, staffID, treatmentType, cost, duration, invoicedInvoiceID)
    		    SELECT ?, ?, ?, treatmentType, cost, duration, NULL
    		    FROM   TblTreatments
    		    WHERE  LCASE(TRIM(treatmentType)) = LCASE(TRIM(?))
    		    LIMIT  1
    		""";


        try (Connection conn = DataBaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(insertSql)) {

            int newID = getNextTreatmentID(); // NEW LINE
            ps.setInt(1, newID);              // set treatmentID
            ps.setInt(2, planID);
            ps.setInt(3, staffID);
            ps.setString(4, treatmentName);

            int rows = ps.executeUpdate();

            if (rows == 0) {
                JOptionPane.showMessageDialog(null,
                    "Insert failed – no template row for '" + treatmentName + "'.",
                    "Add Treatment", JOptionPane.WARNING_MESSAGE);
                return false;
            }

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Database error while inserting treatment:\n" + e.getMessage(),
                "Add Treatment", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    public List<Integer> getAllPatientIDs() {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT DISTINCT patientID FROM TblTreatmentPlans";

        try (Connection conn = DataBaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) ids.add(rs.getInt("patientID"));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ids;
    }


    /* ─────────────── HELPERS ─────────────── */

    private Appointment buildAppointment(ResultSet rs) throws SQLException {
        return new Appointment(
                rs.getInt("appointmentID"),
                rs.getInt("treatmentID"),
                rs.getDate("appointmentDate"),
                rs.getString("status")
        );
    }
    /** Return the treatmentType (e.g. "Root Canal") for a given treatmentID. */
    public String getTreatmentTypeByID(int treatmentID) {
        String sql = "SELECT treatmentType FROM TblTreatments WHERE treatmentID = ?";
        try (Connection c = DataBaseManager.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, treatmentID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getString("treatmentType");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }
    private int getNextTreatmentID() {
        String sql = "SELECT MAX(treatmentID) FROM TblTreatments";
        try (Connection conn = DataBaseManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1) + 1; // max + 1
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1; // fallback
    }

    /** Build entity (no <code>status</code> field in table) */
    private Treatment buildTreatment(ResultSet rs) throws SQLException {
        return new Treatment(
                rs.getInt   ("treatmentID"),
                rs.getInt   ("planID"),
                rs.getInt   ("staffID"),
                rs.getString("treatmentType"),
                rs.getDouble("cost"),
                rs.getInt   ("duration")
        );
    }
    /** Are there completed treatments (via appointment) in the plan? */
    public boolean hasCompletedTreatments(int planID) {
        String sql = """
            SELECT 1 FROM TblAppointment a
            JOIN TblTreatments t ON a.treatmentID = t.treatmentID
            WHERE t.planID = ? AND a.status = 'Completed'
            LIMIT 1
        """;
        try (Connection conn = DataBaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, planID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public double getCompletedTreatmentsCost(int planID) {
        String sql = """
            SELECT SUM(t.cost)
            FROM   TblTreatments t
            JOIN   TblAppointment a ON a.treatmentID = t.treatmentID
            WHERE  t.planID = ? AND a.status = 'Completed'
        """;
        try (Connection c = DataBaseManager.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, planID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
}

