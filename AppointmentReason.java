package controller;
import java.util.LinkedHashMap;
import java.util.Map;

import entity.TreatmentPlan;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** CRUD + roll-ups for TblTreatmentPlans */
public class TreatmentPlanController {

    /* ──────── CREATE ──────── */

    /** planID is NOT AutoNumber → MAX+1 */
    private int nextPlanID() throws SQLException {
        String sql = "SELECT MAX(planID) FROM TblTreatmentPlans";
        try (Connection c = DataBaseManager.connect();
             Statement  s = c.createStatement();
             ResultSet  r = s.executeQuery(sql)) {
            return (r.next() ? r.getInt(1) : 0) + 1;
        }
    }
    public List<TreatmentPlan> getPlansForPatient(int patientID) {
        List<TreatmentPlan> plans = new ArrayList<>();

        String sql = "SELECT * FROM TblTreatmentPlans WHERE patientID = ?";


        try (Connection conn = DataBaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientID);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TreatmentPlan plan = new TreatmentPlan(
                        rs.getInt("planID"),
                        rs.getInt("patientID"),
                        rs.getInt("staffID"),
                        rs.getDate("startDate"),
                        rs.getDate("endDate"),
                        rs.getDouble("cost"),
                        rs.getString("status")
                    );
                    plans.add(plan);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return plans;
    }

    public List<Integer> getPatientsWithCompletedPlans(int staffID) {
        List<Integer> list = new ArrayList<>();
        String sql = """
            SELECT DISTINCT patientID FROM TblTreatmentPlans
            WHERE staffID = ? AND status = 'Completed'
            """;

        try (Connection c = DataBaseManager.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, staffID);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(rs.getInt("patientID"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    

    public List<TreatmentPlan> getCompletedPlans(int patientID, int staffID) {
        List<TreatmentPlan> list = new ArrayList<>();
        String sql = """
            SELECT * FROM TblTreatmentPlans
            WHERE patientID = ? AND staffID = ? AND status = 'Completed'
            """;

        try (Connection c = DataBaseManager.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, patientID);
            ps.setInt(2, staffID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new TreatmentPlan(
                    rs.getInt("planID"),
                    rs.getInt("patientID"),
                    rs.getInt("staffID"),
                    rs.getDate("startDate"),
                    rs.getDate("endDate"),
                    rs.getDouble("cost"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    /* every patient that has at least ONE completed & un-billed treatment
    in a plan that belongs to this dentist                                         */
 public List<Integer> getPatientsWithUnbilledTreatments(int dentistID){
     String sql = """
         SELECT DISTINCT tp.patientID
         FROM   TblTreatmentPlans  tp
         JOIN   TblTreatments      t  ON t.planID      = tp.planID
         JOIN   TblAppointment     a  ON a.treatmentID = t.treatmentID
         WHERE  tp.staffID = ?               -- dentist
           AND  a.status  = 'Completed'      -- treatment done
           AND  t.invoicedInvoiceID IS NULL  -- still un-billed
     """;

     List<Integer> ids = new ArrayList<>();
     try (Connection c = DataBaseManager.connect();
          PreparedStatement ps = c.prepareStatement(sql)) {

         ps.setInt(1, dentistID);
         ResultSet rs = ps.executeQuery();
         while (rs.next()) ids.add(rs.getInt(1));

     } catch (SQLException e) { e.printStackTrace(); }
     return ids;
 }


    public double sumUnbilledCostForPlan(int planID) {
        String sql = """
            SELECT SUM(cost) FROM TblTreatments
            WHERE planID = ? AND status = 'Completed' AND invoicedInvoiceID IS NULL
            """;

        try (Connection c = DataBaseManager.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, planID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }


    public boolean insert(TreatmentPlan p) {

        if (hasPlanForPatient(p.getPatientID())) return false;   // duplicate ACTIVE

        String sql = """
            INSERT INTO TblTreatmentPlans
            (planID, patientID, staffID, startDate, endDate, cost, status)
            VALUES (?,?,?,?,?,?,?)
            """;

        try (Connection c = DataBaseManager.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {

            int id = nextPlanID();
            p.setPlanID(id);

            ps.setInt   (1, id);
            ps.setInt   (2, p.getPatientID());
            ps.setInt   (3, p.getStaffID());
            ps.setDate  (4, new java.sql.Date(p.getStartDate().getTime()));
            ps.setDate  (5, new java.sql.Date(p.getEndDate().getTime()));
            ps.setDouble(6, p.getCost());
            ps.setString(7, p.getStatus());

            return ps.executeUpdate() == 1;
        } catch (SQLException ex) { ex.printStackTrace(); }
        return false;
    }
 // ────────────────────────────────────────────────────────────────
 // Combobox helper – returns every patientID that ever had a plan
 // ────────────────────────────────────────────────────────────────
 public List<Integer> getAllPatientIDs() {
     List<Integer> ids = new ArrayList<>();
     String sql = "SELECT DISTINCT patientID FROM TblTreatmentPlans ORDER BY patientID";
     try (Connection conn = DataBaseManager.connect();
          Statement  st   = conn.createStatement();
          ResultSet  rs   = st.executeQuery(sql)) {

         while (rs.next()) ids.add(rs.getInt(1));

     } catch (SQLException ex) { ex.printStackTrace(); }
     return ids;
 }


    /* ──────── DUPLICATE CHECK ──────── */
 public boolean hasPlanForPatient(int patientID){
	    String sql = """
	        SELECT 1 FROM TblTreatmentPlans
	        WHERE patientID = ?
	        AND status = 'Active'
	        AND endDate >= Date()
	        """;
	    try (Connection c = DataBaseManager.connect();
	         PreparedStatement ps = c.prepareStatement(sql)){
	        ps.setInt(1, patientID);
	        try(ResultSet rs = ps.executeQuery()){
	            return rs.next();
	        }
	    } catch(SQLException e){
	        e.printStackTrace();
	    }
	    return true; // default: block if error
	}


    /* ──────── READS ──────── */

    public List<TreatmentPlan> getActivePlansForStaff(int staffID){
        return runQuery(staffID, "status='Active' ORDER BY startDate DESC");
    }

    /** history table – *every* plan */
    public List<TreatmentPlan> getAllPlansForStaff(int staffID){
        return runQuery(staffID, "1=1 ORDER BY startDate DESC");
    }

    private List<TreatmentPlan> runQuery(int staffID,String where){
        List<TreatmentPlan> list = new ArrayList<>();
        String sql = "SELECT * FROM TblTreatmentPlans WHERE staffID=? AND " + where;
        try(Connection c = DataBaseManager.connect();
            PreparedStatement ps = c.prepareStatement(sql)){
            ps.setInt(1,staffID);
            try(ResultSet rs = ps.executeQuery()){ while(rs.next()) list.add(build(rs)); }
        }catch(SQLException e){ e.printStackTrace(); }
        return list;
    }

    public TreatmentPlan getPlan(int id){
        String sql="SELECT * FROM TblTreatmentPlans WHERE planID=?";
        try(Connection c=DataBaseManager.connect();
            PreparedStatement ps=c.prepareStatement(sql)){
            ps.setInt(1,id);
            try(ResultSet rs=ps.executeQuery()){ return rs.next()? build(rs):null; }
        }catch(SQLException e){ e.printStackTrace(); }
        return null;
    }

    /* ──────── ROLL-UPS ──────── */

    private void recalcPlanCost(int id){
        String sum="SELECT SUM(cost) FROM TblTreatments WHERE planID=?";
        String upd="UPDATE TblTreatmentPlans SET cost=? WHERE planID=?";
        try(Connection c=DataBaseManager.connect();
            PreparedStatement s1=c.prepareStatement(sum);
            PreparedStatement s2=c.prepareStatement(upd)){
            s1.setInt(1,id);
            double tot=0; try(ResultSet r=s1.executeQuery()){ if(r.next()) tot=r.getDouble(1); }
            s2.setDouble(1,tot); s2.setInt(2,id); s2.executeUpdate();
        }catch(SQLException e){ e.printStackTrace(); }
    }

    private void recalcEndDate(int id){
        String sum="SELECT SUM(duration) FROM TblTreatments WHERE planID=?";
        String upd="UPDATE TblTreatmentPlans SET endDate=DateAdd('d',?,startDate) WHERE planID=?";
        try(Connection c=DataBaseManager.connect();
            PreparedStatement s1=c.prepareStatement(sum);
            PreparedStatement s2=c.prepareStatement(upd)){
            s1.setInt(1,id);
            int d=0; try(ResultSet r=s1.executeQuery()){ if(r.next()) d=r.getInt(1); }
            s2.setInt(1,d); s2.setInt(2,id); s2.executeUpdate();
        }catch(SQLException e){ e.printStackTrace(); }
    }

    /** UI helper */
    public void refreshTotals(int id){ recalcPlanCost(id); recalcEndDate(id); }

    /* ──────── COMPLETE / CANCEL ──────── */

    private boolean readyToComplete(int planID){
        String sql = """
            SELECT 1 FROM TblTreatments t
            JOIN   TblAppointment a ON a.treatmentID=t.treatmentID
            WHERE  t.planID=? AND a.status<>'Completed'
            """;
        try(Connection c=DataBaseManager.connect();
            PreparedStatement ps=c.prepareStatement(sql)){
            ps.setInt(1,planID);
            try(ResultSet rs=ps.executeQuery()){ return !rs.next(); }
        }catch(SQLException e){ e.printStackTrace(); }
        return false;
    }
    private TreatmentPlan buildPlan(ResultSet rs) throws SQLException {
        return new TreatmentPlan(
            rs.getInt("planID"),
            rs.getInt("patientID"),
            rs.getInt("staffID"),
            rs.getDate("startDate"),
            rs.getDate("endDate"),
            rs.getDouble("cost"),
            rs.getString("status")
        );
    }
    /** refuse if *any* ACTIVE plan exists for this patient */
    private boolean hasActivePlan(int patientID) {
        String sql = """
            SELECT 1
            FROM   TblTreatmentPlans
            WHERE  patientID = ?
              AND  UCASE(Trim(status))='ACTIVE'
            """;

        try (Connection c = DataBaseManager.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, patientID);
            return ps.executeQuery().next();   // true ⇒ block new insert
        } catch (SQLException ex) {
            ex.printStackTrace();
            return true;
        }
    }

    /** normal path → Completed + endDate=today */
    public boolean finishPlan(int planID){
        if (!readyToComplete(planID)) return false;
        updateStatus(planID,"Completed");
        return true;
    }
 // ----------------------------------------------
//  put both methods somewhere in TreatmentPlanController
// ----------------------------------------------

/** every patient that has at least ONE completed & un-billed treatment
 *  in a plan that belongs to this dentist (staffID)                    */

/** plans (any status) that still contain at least one un-billed completed
 *  treatment for the given patient & dentist                             */
public List<TreatmentPlan> getPlansWithUnbilledTreatments(int patientID, int dentistID) {
    List<TreatmentPlan> list = new ArrayList<>();

    String sql = """
        SELECT DISTINCT tp.*
        FROM   TblTreatmentPlans tp
        JOIN   TblTreatments     t  ON tp.planID     = t.planID
        JOIN   TblAppointment    a  ON t.treatmentID = a.treatmentID
        WHERE  tp.patientID = ?
          AND  tp.staffID   = ?
          AND  a.status     = 'Completed'
          AND  t.invoicedInvoiceID IS NULL
    """;

    try (Connection c = DataBaseManager.connect();
         PreparedStatement ps = c.prepareStatement(sql)) {

        ps.setInt(1, patientID);
        ps.setInt(2, dentistID);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            list.add(new TreatmentPlan(
                    rs.getInt("planID"),
                    rs.getInt("patientID"),
                    rs.getInt("staffID"),
                    rs.getDate("startDate"),
                    rs.getDate("endDate"),
                    rs.getDouble("cost"),
                    rs.getString("status")
            ));
        }

    } catch (SQLException e) { e.printStackTrace(); }
    return list;
}
/* ---------------------------------------------------------------------------
 *  Plans that still have at least one COMPLETED + UN-BILLED treatment.
 *  Returns rows for the JTable:  {planID, fullName, treatments, unbilled ₪}
 * -------------------------------------------------------------------------- */
public List<Object[]> getPlanRowsForTable(int patientID, int dentistID) {

    String sql = """
        SELECT tp.planID,
               p.firstName & ' ' & p.lastName   AS fullName,
               t.treatmentType                  AS trtName,
               t.cost                           AS trtCost
        FROM   TblTreatmentPlans  tp
        JOIN   TblPatients        p  ON p.patientID   = tp.patientID
        JOIN   TblTreatments      t  ON t.planID      = tp.planID
        JOIN   TblAppointment     a  ON a.treatmentID = t.treatmentID
        WHERE  tp.patientID = ?            -- chosen patient
          AND  tp.staffID   = ?            -- logged-in dentist
          AND  a.status     = 'Completed'  -- treatment done
          AND  t.invoicedInvoiceID IS NULL -- not yet invoiced
        ORDER  BY tp.planID
    """;

    /* group rows by planID (so we can concatenate treatment names) */
    Map<Integer,Object[]> map = new LinkedHashMap<>();

    try (Connection c  = DataBaseManager.connect();
         PreparedStatement ps = c.prepareStatement(sql)) {

        ps.setInt(1, patientID);
        ps.setInt(2, dentistID);
        try (ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int    planID   = rs.getInt   ("planID");
                String fullName = rs.getString("fullName");
                String trt      = rs.getString("trtName");
                double cost     = rs.getDouble("trtCost");

                if (map.containsKey(planID)) {              // append
                    Object[] row = map.get(planID);
                    row[2] = ((String)  row[2]) + ", " + trt;
                    row[3] = ((Double)  row[3]) + cost;
                } else {                                    // first time
                    map.put(planID, new Object[]{ planID, fullName, trt, cost });
                }
            }
        }
    } catch (SQLException e) { e.printStackTrace(); }

    return new ArrayList<>(map.values());   // ready for JTable
}



    /** cancel open appointments + plan → Cancelled + endDate=today */
    public void cancelPlan(int planID){
        String cancelAppt = """
            UPDATE TblAppointment
            SET    status='Cancelled'
            WHERE  planID=? AND status IN ('Approved','Scheduled')
            """;
        try(Connection c=DataBaseManager.connect();
            PreparedStatement a=c.prepareStatement(cancelAppt)){
            a.setInt(1,planID); a.executeUpdate();
        }catch(SQLException e){ e.printStackTrace(); }
        updateStatus(planID,"Cancelled");
    }

    private void updateStatus(int planID,String status){
        String upd="UPDATE TblTreatmentPlans SET status=?, endDate=Date() WHERE planID=?";
        try(Connection c=DataBaseManager.connect();
            PreparedStatement ps=c.prepareStatement(upd)){
            ps.setString(1,status); ps.setInt(2,planID); ps.executeUpdate();
            refreshTotals(planID);
        }catch(SQLException e){ e.printStackTrace(); }
    }

    /* ──────── HEADER EDIT ──────── */
    public void updateHeader(TreatmentPlan p){
        String sql="""
            UPDATE TblTreatmentPlans
            SET startDate=?, endDate=?, status=?
            WHERE planID=?""";
        try(Connection c=DataBaseManager.connect();
            PreparedStatement ps=c.prepareStatement(sql)){
            ps.setDate  (1,new java.sql.Date(p.getStartDate().getTime()));
            ps.setDate  (2,new java.sql.Date(p.getEndDate()  .getTime()));
            ps.setString(3,p.getStatus());
            ps.setInt   (4,p.getPlanID());
            ps.executeUpdate();
        }catch(SQLException e){ e.printStackTrace(); }
    }

    /* ──────── mapper ──────── */
    private TreatmentPlan build(ResultSet rs)throws SQLException{
        return new TreatmentPlan(
                rs.getInt   ("planID"),
                rs.getInt   ("patientID"),
                rs.getInt   ("staffID"),
                rs.getDate  ("startDate"),
                rs.getDate  ("endDate"),
                rs.getDouble("cost"),
                rs.getString("status"));
    }
    

    public String getPatientNameByID(int patientID) {
        String sql = "SELECT firstName, lastName FROM TblPatients WHERE patientID = ?";
        try (Connection conn = DataBaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("firstName") + " " + rs.getString("lastName");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }
    /* ───── combo-box helper: all patient IDs that have at least one plan ───── */

 // ---------- already inside TreatmentPlanController ----------

    /* helper used by Invoice screen */
    public List<TreatmentPlan> getAllPlansForPatient(int patientID){
        List<TreatmentPlan> plans = new ArrayList<>();
        String sql = "SELECT * FROM TblTreatmentPlans WHERE patientID=?";
        try(Connection c = DataBaseManager.connect();
            PreparedStatement ps = c.prepareStatement(sql)){
            ps.setInt(1,patientID);
            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()) plans.add(build(rs));
            }
        }catch(SQLException e){ e.printStackTrace(); }
        return plans;
    }

    /* quick roll-up cost for “completed only” treatments */
    public double getCompletedTreatmentsCost(int planID){
        String sql = """
            SELECT SUM(cost) FROM TblTreatments tr
            JOIN   TblAppointment ap ON ap.treatmentID = tr.treatmentID
            WHERE  tr.planID=? AND ap.status='Completed'
        """;
        try(Connection c = DataBaseManager.connect();
            PreparedStatement ps = c.prepareStatement(sql)){
            ps.setInt(1,planID);
            try(ResultSet rs = ps.executeQuery()){
                return rs.next()? rs.getDouble(1):0;
            }
        }catch(SQLException e){ e.printStackTrace(); }
        return 0;
    }

}


