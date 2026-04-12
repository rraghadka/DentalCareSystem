package controller;
import java.time.format.DateTimeFormatter; 
import entity.Appointment;
import java.sql.*;
import java.time.LocalDate;   // ← new
import java.time.LocalTime;   // ← new
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import entity.Staff;

/**
 * CRUD + helpers for TblAppointment
 */
public class AppointmentController {

    /* If you ever change the table name you only need to edit here */
    private static final String TABLE = "TblAppointment";
    

    /* ───────────────────────────────────────────────────────────────
       UPCOMING appointments for one staff member
       – today or later
       – status NOT IN ('Cancelled','Completed')
       – sorted by date & time
    ─────────────────────────────────────────────────────────────── */
    public List<Appointment> getUpcomingAppointmentsForStaff(int staffID) {
        List<Appointment> list = new ArrayList<>();

        String sql = """
            SELECT * FROM %s
            WHERE staffID = ?
              AND appointmentDate >= DATE()
              AND status NOT IN ('Cancelled','Completed')
            ORDER BY appointmentDate, appointmentTime
        """.formatted(TABLE);

        try (Connection conn  = DataBaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, staffID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(buildAppointment(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /* ───────────────────────────────────────────────────────────────
       TODAY'S appointments for *all* staff (except cancelled/completed)
    ─────────────────────────────────────────────────────────────── */
    public List<Appointment> getTodaysAppointments() {
        List<Appointment> list = new ArrayList<>();

        String sql = """
            SELECT * FROM %s
            WHERE appointmentDate = DATE()
              AND status NOT IN ('Cancelled','Completed')
            ORDER BY appointmentTime
        """.formatted(TABLE);

        try (Connection c = DataBaseManager.connect();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(buildAppointment(rs));

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public List<Appointment> getAppointmentsInNext24h(int patientID) {
        List<Appointment> list = new ArrayList<>();
        String sql = """
            SELECT * FROM TblAppointment
            WHERE patientID = ? 
            AND status IN ('Scheduled', 'Approved')
            AND appointmentDate = ?
            """;

        try (Connection conn = DataBaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientID);
            stmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Appointment appt = new Appointment(
                        rs.getInt("appointmentID"),
                        rs.getInt("patientID"),
                        rs.getDate("appointmentDate"),
                        rs.getString("appointmentTime"),
                        rs.getInt("staffID"),
                        rs.getString("status"),
                        rs.getInt("reasonID"),
                        rs.getInt("treatmentID"),
                        rs.getBoolean("paid")
                    );
                    list.add(appt);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean isSlotFree(int staffID, LocalDate date, LocalTime newStart, int newDurationMin) {
        String sql = """
            SELECT appointmentTime, durationMin
            FROM TblAppointment
            WHERE staffID = ? AND appointmentDate = ?
              AND status IN ('Scheduled', 'Approved')
            """;

        try (Connection conn = DataBaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, staffID);
            ps.setDate(2, java.sql.Date.valueOf(date));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalTime existingStart = LocalTime.parse(rs.getString("appointmentTime"));
                    int existingDuration = rs.getInt("durationMin");
                    LocalTime existingEnd = existingStart.plusMinutes(existingDuration);

                    LocalTime newEnd = newStart.plusMinutes(newDurationMin);

                    // Check if time ranges overlap
                    boolean overlaps = !newStart.isAfter(existingEnd.minusMinutes(1)) &&
                                       !newEnd.isBefore(existingStart.plusMinutes(1));
                    if (overlaps) return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    /* ───────────────────────────────────────────────────────────────
       Insert a new appointment (basic version)
       – Performs slot-check first
       – Sets default status "Scheduled" if null
    ─────────────────────────────────────────────────────────────── */
    public boolean bookAppointment(Appointment a, String urgency)
 {
        // ✅ Convert to LocalDate/Time
    	LocalDate d = a.getAppointmentDate().toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();

String appTime = a.getAppointmentTime();
LocalTime t;

if (urgency != null && urgency.equalsIgnoreCase("urgent")) {
  t = LocalTime.now(); // Use current time for urgent
} else if (appTime != null && !appTime.isBlank()) {
  t = LocalTime.parse(appTime);
} else {
  t = LocalTime.of(8, 0); // fallback if time is missing
}


        // ✅ Check for conflict
        if (!isSlotFree(a.getStaffID(), d, t, 30)) {
            return false; // Slot is taken
        }

        // ✅ Insert
        String sql = """
            INSERT INTO %s
              (patientID, appointmentDate, appointmentTime,
               staffID, status, reasonID, treatmentID)
            VALUES (?,?,?,?,?,?,?)
        """.formatted(TABLE);

        try (Connection c  = DataBaseManager.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt    (1, a.getPatientID());
            ps.setDate   (2, new java.sql.Date(a.getAppointmentDate().getTime()));

            // ✅ Format the time as H:mm to match Access format
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("H:mm");
            ps.setString (3, t.format(fmt));

            ps.setInt    (4, a.getStaffID());
            ps.setString (5, (a.getStatus() == null) ? "Scheduled" : a.getStatus());
            ps.setInt    (6, a.getReasonID());
            ps.setInt    (7, a.getTreatmentID());

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /* ───────────────────────────────────────────────────────────────
       ResultSet → Appointment helper
       (Adjust column names if your schema differs)
    ─────────────────────────────────────────────────────────────── */
    private Appointment buildAppointment(ResultSet rs) throws SQLException {
        return new Appointment(
                rs.getInt   ("appointmentID"),
                rs.getInt   ("patientID"),
                rs.getInt   ("staffID"),
                rs.getInt   ("treatmentID"),
                rs.getDate  ("appointmentDate"),     // java.sql.Date
                rs.getString("appointmentTime"),     // "HH:mm"
                rs.getString("status"),              // 'Scheduled', 'Approved', …
                rs.getInt ("reasonID")             // FK or text
        );
    }
    /* find a slot on same date/staff with enough free minutes */
    public Integer findExpandableSlot(int staffID, Date sqlDate, int neededMin)
            throws SQLException {
        
        String q = """
               SELECT appointmentID
    FROM   TblAppointment
    WHERE  staffID = ?
      AND  appointmentDate = ?
      AND  status IN ('Scheduled', 'Approved')
            """;

        try (Connection c = DataBaseManager.connect();
             PreparedStatement ps = c.prepareStatement(q)) {

            ps.setInt(1, staffID);
            ps.setDate(2, sqlDate);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int apptID = rs.getInt(1);
                    int dur    = rs.getInt(2);
                    if (dur + neededMin <= 60) {   // 1-hour bucket example
                        return apptID;             // merge here
                    }
                }
            }
        }
        return null;  // none
    }
    public boolean confirmAppointment(int appointmentID) {
        String sql = "UPDATE TblAppointment SET status = 'Approved' WHERE appointmentID = ?";
        try (Connection conn = DataBaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appointmentID);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /* secretary calls to confirm / pay / sterilize */
   
    public void markPaid(int id) throws SQLException {
        exec("UPDATE TblAppointment SET paymentStatus='Paid' WHERE appointmentID=?", id);
    }
    public void markSterilized(int id) throws SQLException {
        exec("UPDATE TblAppointment SET sterilized=TRUE WHERE appointmentID=?", id);
    }

    /* helper */
    public void exec(String sql, Object... params) throws SQLException {
        try (Connection c = DataBaseManager.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }

            ps.executeUpdate();
        }
    }
    public List<String> getBlockedTimes(int staffID, Date date) {
        List<String> blocked = new ArrayList<>();

        String sql = """
            SELECT appointmentTime
            FROM TblAppointment
            WHERE staffID = ? AND appointmentDate = ?
              AND status IN ('Scheduled', 'Approved')
        """;

        try (Connection conn = DataBaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, staffID);
            ps.setDate(2, new java.sql.Date(date.getTime()));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String timeStr = rs.getString("appointmentTime"); // assumed "HH:mm"
                    blocked.add(timeStr);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return blocked;
    }
    public List<Appointment> getApprovedAppointments() {
        List<Appointment> list = new ArrayList<>();

        String sql = """
            SELECT * FROM TblAppointment
            WHERE status = 'Approved'
            ORDER BY appointmentDate, appointmentTime
        """;

        try (Connection conn = DataBaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Appointment a = new Appointment(
                    rs.getInt("appointmentID"),
                    rs.getInt("patientID"),
                    rs.getDate("appointmentDate"),
                    rs.getString("appointmentTime"),
                    rs.getInt("staffID"),
                    rs.getString("status"),
                    rs.getInt("reasonID"),
                    rs.getInt("treatmentID"),
                    rs.getBoolean("Paid")
                );
                list.add(a);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
    private static AppointmentController instance;

    public static AppointmentController getInstance() {
        if (instance == null) {
            instance = new AppointmentController();
        }
        return instance;
    }

    

    /* list of pending appointments for secretary */
    public List<Appointment> getPendingAppointments() {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT * FROM TblAppointment WHERE status = 'Scheduled'";
        try (Connection conn = DataBaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(Appointment.fromResultSet(rs)); // Include paid field in fromResultSet
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    public boolean bookAppointmentForPatient(Appointment appt) {
        String sql = """
            INSERT INTO TblAppointment (patientID, appointmentDate, appointmentTime,
                                        staffID, status, reasonID, treatmentID, paid)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DataBaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, appt.getPatientID());
            stmt.setDate(2, new java.sql.Date(appt.getAppointmentDate().getTime()));
            stmt.setString(3, appt.getAppointmentTime());
            stmt.setInt(4, appt.getStaffID());
            stmt.setString(5, appt.getStatus());
            stmt.setInt(6, appt.getReasonID());
            stmt.setInt(7, appt.getTreatmentID());
            stmt.setBoolean(8, appt.isPaid());

            return stmt.executeUpdate() > 0;

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
    public List<Staff> getAvailableStaffForPatient(java.sql.Date date, String time) {
        List<Staff> available = new ArrayList<>();

        String sql = """
            SELECT * FROM TblStaff
            WHERE roleID IN (2,3) AND staffID NOT IN (
                SELECT staffID FROM TblAppointment
                WHERE appointmentDate = ? AND appointmentTime = ?
                  AND status IN ('Scheduled', 'Approved')
            )
            """;

        try (Connection conn = DataBaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, date);
            stmt.setString(2, time);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Staff staff = new Staff(
                    rs.getInt("staffID"),
                    rs.getInt("roleID"),
                    rs.getString("firstName"),
                    rs.getString("lastName"),
                    rs.getString("phoneNumt"),
                    rs.getString("email")
                );
                available.add(staff);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return available;
    }
    public List<String> getAvailableTimesForPatient(java.sql.Date date) {
        List<String> availableTimes = new ArrayList<>();

        LocalTime start = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(17, 0);

        while (!start.isAfter(end.minusMinutes(30))) {
            availableTimes.add(start.toString()); // "08:00", "08:30", ...
            start = start.plusMinutes(30);
        }

        String sql = """
            SELECT appointmentTime
            FROM TblAppointment
            WHERE appointmentDate = ?
              AND status IN ('Scheduled', 'Approved')
            """;

        try (Connection conn = DataBaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, date);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String time = rs.getString("appointmentTime");
                availableTimes.remove(time); // הסר את השעות שכבר תפוסות
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return availableTimes;
    }
    public void cancelAppointment(int appointmentID) {
        String sql = "UPDATE TblAppointment SET status = 'Cancelled' WHERE appointmentID = ?";
        try (Connection conn = DataBaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, appointmentID);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void approveAppointment(int appointmentID) {
        String sql = "UPDATE TblAppointment SET status = 'Approved' WHERE appointmentID = ?";
        try (Connection conn = DataBaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, appointmentID);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public List<Appointment> getUpcomingAppointmentsForPatient(int patientID) {
        List<Appointment> list = new ArrayList<>();
        String sql = """
            SELECT * FROM TblAppointment
            WHERE patientID = ? AND appointmentDate >= Date()
              AND status NOT IN ('Cancelled', 'Completed')
            ORDER BY appointmentDate, appointmentTime
            """;

        try (Connection conn = DataBaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientID);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
            	list.add(new Appointment(
            		    rs.getInt("appointmentID"),
            		    rs.getInt("patientID"),
            		    rs.getDate("appointmentDate"),
            		    rs.getString("appointmentTime"),
            		    rs.getInt("staffID"),
            		    rs.getString("status"),
            		    rs.getInt("reasonID"),
            		    rs.getInt("treatmentID"),
            		    rs.getBoolean("paid")
            		));

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }


    /* ------------- keep/extend the rest of your controller as needed ------------- */
}

