// =========================  InvoiceController.java  =========================
package controller;
import entity.InvoiceRow;

import entity.Invoice;
import entity.Treatment;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * CRUD + generator for TblInvoices (header) and TblInvoiceTreatments (lines).
 *
 * ‼  _Behaviour requested by user_
 *     •  Every plan already has a **placeholder** row in **TblInvoices** whose
 *        primary‑key (invoiceID) is the *same* as the planID.
 *     •  When the dentist clicks **Generate Invoice** we **must NOT** insert a
 *        brand‑new header; we simply update that existing row’s *totalAmount*
 *        and *issuedDate*.
 *     •  If – for some reason – the placeholder row is missing we fall back to
 *        inserting a fresh one (using the same ID) so the FK constraint on the
 *        line‑items table is always satisfied.
 */
public class InvoiceController {

    /* ─────────────────────── Read‑all helper (unused by GUI) ─────────────────────── */
    public List<Invoice> getAll(){
        List<Invoice> list = new ArrayList<>();
        String sql = "SELECT * FROM TblInvoices ORDER BY issuedDate DESC";
        try(Connection c = DataBaseManager.connect();
            Statement  s = c.createStatement();
            ResultSet  r = s.executeQuery(sql)){
            while(r.next()) list.add(build(r));
        }catch(SQLException e){ e.printStackTrace(); }
        return list;
    }

    /* ─────────────────────────────  MAIN ENTRY  ───────────────────────────── */
    public Invoice generateInvoice(int planID,int patientID,int staffID) throws SQLException{

        /* 1) gather treatments that are completed + not yet billed */
        List<Treatment> trts = new TreatmentController().getCompletedUnbilled(planID);
        if(trts.isEmpty()) return null;               // nothing to do

        double total = trts.stream().mapToDouble(Treatment::getCost).sum();
        Date   today = new Date();

        try(Connection c = DataBaseManager.connect()){
            c.setAutoCommit(false);

            //-------------------------------------------------------------
            // 2) make sure header row exists – then UPDATE it
            //-------------------------------------------------------------
            int invoiceID = ensureHeaderRow(planID,patientID,staffID,total,today,c);
            Invoice inv   = new Invoice(invoiceID,planID,patientID,staffID,total,today);

            //-------------------------------------------------------------
            // 3) insert line‑items + mark treatments as billed
            //-------------------------------------------------------------
            insertLines(inv,trts,c);
            markAsBilled(inv,trts,c);

            c.commit();
            return inv;
        }}
    public List<InvoiceRow> getInvoiceRows(int invoiceID) {
        List<InvoiceRow> rows = new ArrayList<>();

        String sql = """
            SELECT t.treatmentID, p.fullName AS patientName, t.treatmentType, t.cost
            FROM TblInvoiceTretaments it
            JOIN TblTreatment t ON it.treatmentID = t.treatmentID
            JOIN TblTreatmentPlan tp ON t.planID = tp.planID
            JOIN TblPatient p ON tp.patientID = p.patientID
            WHERE it.invoiceID = ?
        """;

        try (Connection conn = DataBaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, invoiceID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                	int treatmentID = rs.getInt("treatmentID");
                    String patientName = rs.getString("patientName");
                    String treatmentType = rs.getString("treatmentType");
                    double cost = rs.getDouble("cost");

                    InvoiceRow row = new InvoiceRow(treatmentID, patientName, treatmentType, cost);
                    rows.add(row);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rows;
    }


    /* ────────────────────────────  header logic  ─────────────────────────── */
    private int ensureHeaderRow(int planID,int patientID,int staffID,double total,Date today,Connection c) throws SQLException{
        // we *expect* invoiceID == planID, but check anyway.
        String sel = "SELECT invoiceID FROM TblInvoices WHERE planID = ?";
        try(PreparedStatement ps = c.prepareStatement(sel)){
            ps.setInt(1,planID);
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    int id = rs.getInt(1);
                    // UPDATE totals/date – leave patient/staff as‑is
                    String upd = "UPDATE TblInvoices SET totalAmount=?, issuedDate=? WHERE invoiceID=?";
                    try(PreparedStatement u = c.prepareStatement(upd)){
                        u.setDouble(1,total);
                        u.setDate  (2,new java.sql.Date(today.getTime()));
                        u.setInt   (3,id);
                        u.executeUpdate();
                    }
                    return id;
                }
            }
        }
        // ------------ missing header → insert placeholder -------------
        int id = planID;  // keep 1‑to‑1 relation
        String ins = "INSERT INTO TblInvoices (invoiceID,planID,patientID,staffID,totalAmount,issuedDate) VALUES (?,?,?,?,?,?)";
        try(PreparedStatement p = c.prepareStatement(ins)){
            p.setInt   (1,id);
            p.setInt   (2,planID);
            p.setInt   (3,patientID);
            p.setInt   (4,staffID);
            p.setDouble(5,total);
            p.setDate  (6,new java.sql.Date(today.getTime()));
            p.executeUpdate();
        }
        return id;
    }

    /* ────────────────────────────  line items  ──────────────────────────── */
    private void insertLines(Invoice inv,List<Treatment> trts,Connection c) throws SQLException{
        String sql = "INSERT INTO TblInvoiceTretaments (invoiceID,treatmentID,description,cost) VALUES (?,?,?,?)";
        try(PreparedStatement ps = c.prepareStatement(sql)){
            for(Treatment t: trts){
                ps.setInt   (1,inv.getInvoiceID());
                ps.setInt   (2,t.getTreatmentID());
                ps.setString(3,t.getTreatmentType());
                ps.setDouble(4,t.getCost());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void markAsBilled(Invoice inv,List<Treatment> trts,Connection c) throws SQLException{
        String sql = "UPDATE TblTreatments SET invoicedInvoiceID=? WHERE treatmentID=?";
        try(PreparedStatement ps = c.prepareStatement(sql)){
            for(Treatment t: trts){
                ps.setInt(1,inv.getInvoiceID());
                ps.setInt(2,t.getTreatmentID());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /* ────────────────────────────  mapper  ───────────────────────────── */
    private Invoice build(ResultSet r) throws SQLException{
        return new Invoice(
                r.getInt   ("invoiceID"),
                r.getInt   ("planID"),
                r.getInt   ("patientID"),
                r.getInt   ("staffID"),
                r.getDouble("totalAmount"),
                r.getDate  ("issuedDate"));
    }
}

 

