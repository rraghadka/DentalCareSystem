package controller;

import entity.Consts;
import entity.Staff;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-access helper for <i>TblStaff</i> & <i>TblRole</i>.
 *
 * • Instance methods → classic DAO / CRUD style.  
 * • Static helpers   → thin wrappers with unique names (avoid duplicate-method errors).
 */
public class StaffController {

    /* ─────────────────────────────────────────────
       0. Generic role check (used by FrmLogin)
       ───────────────────────────────────────────── */
    public static boolean isInRole(int staffID, String roleName) {
        String sql = """
                     SELECT 1
                     FROM   TblStaff s
                     JOIN   TblRole  r ON s.roleID = r.roleID
                     WHERE  s.staffID              = ?
                       AND  UCASE(TRIM(r.roleName)) = UCASE(TRIM(?))
                     """;
        try (Connection con = DataBaseManager.connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt   (1, staffID);
            ps.setString(2, roleName);
            boolean ok = ps.executeQuery().next();
            System.out.printf("DEBUG  isInRole(%d, \"%s\") → %s%n",
                              staffID, roleName, ok);
            return ok;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /* ─────────────────────────────────────────────
       1. Instance helpers (DAO / CRUD style)
       ───────────────────────────────────────────── */

    /** Return one staff row, or {@code null}. */
    public Staff getStaff(int staffID) {
        String sql = "SELECT * FROM TblStaff WHERE staffID = ?";
        try (Connection con = DataBaseManager.connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, staffID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** True iff this staff-member is a dentist. */
    public boolean isDentist(int staffID) {
        String sql = "SELECT 1 FROM TblStaff WHERE staffID = ? AND roleID = ?";
        try (Connection con = DataBaseManager.connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, staffID);
            ps.setInt(2, Consts.ROLE_DENTIST);
            return ps.executeQuery().next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Convenience – list of all dentists. */
    public List<Staff> getAllDentists() {
        List<Staff> list = new ArrayList<>();
        String sql = """
                     SELECT *
                     FROM   TblStaff
                     WHERE  roleID = ?
                     ORDER  BY lastName, firstName
                     """;
        try (Connection con = DataBaseManager.connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, Consts.ROLE_DENTIST);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /* ─────────────────────────────────────────────
       2. Static convenience wrappers (unique names)
       ───────────────────────────────────────────── */

    public static Staff   getStaffById (int id) { return new StaffController().getStaff(id); }
    public static boolean isDentistById(int id) { return new StaffController().isDentist(id); }

    /* ─────────────────────────────────────────────
       3. Utility – full name by staff-ID
       ───────────────────────────────────────────── */
    public static String getStaffFullNameByID(int staffID) {
        String sql = "SELECT firstName, lastName FROM TblStaff WHERE staffID = ?";
        try (Connection con = DataBaseManager.connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, staffID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getString("firstName") + ' ' + rs.getString("lastName");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return "Unknown";
    }

    /* ─────────────────────────────────────────────
       Helper – map ResultSet → Staff object
       ───────────────────────────────────────────── */
    private Staff map(ResultSet rs) throws SQLException {
        return new Staff(
                rs.getInt   ("staffID"),
                rs.getInt   ("roleID"),
                rs.getString("firstName"),
                rs.getString("lastName"),
                rs.getString("phoneNumt"),      // column name in Access
                rs.getString("Email"),
                rs.getString("qualification"),
                rs.getString("specializationID"),
                rs.getInt   ("availabilityID")
        );
    }
    public List<Staff> getAvailableDentistsNow() {
        List<Staff> list = new ArrayList<>();

        String sql = """
            SELECT * FROM TblStaff
            WHERE roleID = 1 AND staffID NOT IN (
                SELECT staffID FROM TblAppointment
                WHERE appointmentDate = ? AND appointmentTime = ?
                  AND status IN ('Scheduled', 'Approved')
            )
            """;

        try (Connection conn = DataBaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            java.sql.Date today = java.sql.Date.valueOf(java.time.LocalDate.now());
            String currentTime = java.time.LocalTime.now().withSecond(0).withNano(0).toString();

            ps.setDate(1, today);
            ps.setString(2, currentTime);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Staff s = new Staff(
                            rs.getInt("staffID"),
                            rs.getString("firstName"),
                            rs.getString("lastName")
                            // add other fields if needed
                    );
                    list.add(s);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
    
    private static StaffController instance;

    public static StaffController getInstance() {
        if (instance == null) {
            instance = new StaffController();
        }
        return instance;
    }
    public String getStaffName(int staffID) {
        String sql = "SELECT firstName, lastName FROM TblStaff WHERE staffID = ?";
        try (Connection conn = DataBaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, staffID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("firstName") + " " + rs.getString("lastName");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }
    
}
