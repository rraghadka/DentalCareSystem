package controller;

import entity.Patient;
import java.time.LocalTime;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientController {

    /* ───────────────────────── 0.  HELPERS FOR LOGIN ───────────────────────── */

    /** 0-A.  Does this patient ID exist?   (used by FrmLogin) */
    public static boolean exists(int patientID) {
        String sql = "SELECT 1 FROM TblPatients WHERE patientID = ?";
        try (Connection con = DataBaseManager.connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, patientID);
            return ps.executeQuery().next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    

    public List<Integer> getAllPatientIDs() {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT patientID FROM TblPatients ORDER BY patientID";
        try (Connection conn = DataBaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                ids.add(rs.getInt("patientID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ids;
    }
    public String getPatientName(int patientID) {
        String sql = "SELECT firstName, lastName FROM TblPatients WHERE patientID = ?";

        try (Connection c = DataBaseManager.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, patientID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String first = rs.getString("firstName");
                    String last  = rs.getString("lastName");
                    return first + " " + last;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }


    /* ───────────────────────── 1.  CRUD ───────────────────────── */
    public static String getFirstName(int patientID) {
        String sql = "SELECT firstName FROM TblPatients WHERE patientID = ?";
        try (Connection con = DataBaseManager.connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, patientID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("firstName");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 1. Get a single patient by ID */
    public Patient getPatient(int patientID) {
        String sql = "SELECT * FROM TblPatients WHERE patientID = ?";
        try (Connection con = DataBaseManager.connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, patientID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return buildPatient(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 2. Get all patients */
    public List<Patient> getAllPatients() {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT * FROM TblPatients ORDER BY lastName, firstName";
        try (Connection con = DataBaseManager.connect();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) list.add(buildPatient(rs));

        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
    private static PatientController instance;

public static PatientController getInstance() {
    if (instance == null) {
        instance = new PatientController();
    }
    return instance;

}
public Patient getPatientByID(int id) {
    String sql = "SELECT * FROM TblPatients WHERE patientID = ?";

    try (Connection conn = DataBaseManager.connect();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, id);

        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return new Patient(
                    rs.getInt("patientID"),
                    rs.getString("firstName"),
                    rs.getString("lastName"),
                    rs.getString("phoneNumber"),
                    rs.getString("email"),
                    rs.getDate("dateOfBirth") // שים לב! מחזיר java.sql.Date
                );
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return null; // או אפשר להחזיר אובייקט ריק
}



    /** 3. Add new patient (returns generated ID) */
    public int addPatient(Patient p) {
        String sql = """
                INSERT INTO TblPatients
                (firstName, lastName, email, phoneNumber, dateOfBirth)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection con = DataBaseManager.connect();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getFirstName());
            ps.setString(2, p.getLastName());
            ps.setString(3, p.getEmail());
            ps.setString(4, p.getPhoneNumber());
            ps.setDate  (5, new java.sql.Date(p.getDateOfBirth().getTime()));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }

        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }
    

    /** 4. Update patient */
    public boolean updatePatient(Patient p) {
        String sql = """
                UPDATE TblPatients
                SET firstName=?, lastName=?, email=?, phoneNumber=?, dateOfBirth=?
                WHERE patientID=?
                """;
        try (Connection con = DataBaseManager.connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getFirstName());
            ps.setString(2, p.getLastName());
            ps.setString(3, p.getEmail());
            ps.setString(4, p.getPhoneNumber());
            ps.setDate  (5, new java.sql.Date(p.getDateOfBirth().getTime()));
            ps.setInt   (6, p.getPatientID());

            return ps.executeUpdate() == 1;

        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /** 5. Delete patient */
    public boolean deletePatient(int patientID) {
        String sql = "DELETE FROM TblPatients WHERE patientID = ?";
        try (Connection con = DataBaseManager.connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, patientID);
            return ps.executeUpdate() == 1;

        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /* ───────────────────────── helper ───────────────────────── */
    private Patient buildPatient(ResultSet rs) throws SQLException {
        return new Patient(
            rs.getInt   ("patientID"),
            rs.getString("firstName"),
            rs.getString("lastName"),
            rs.getString("email"),
            rs.getString("phoneNumber"),
            rs.getDate  ("dateOfBirth")
        );
    }
}
