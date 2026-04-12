package controller;

import entity.Supplier;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierController {

    public boolean addSupplier(Supplier supplier) {
        String sql = "INSERT INTO TblSuppliers (supplierID, firstName, lastName, phoneNumber, Email) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DataBaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, supplier.getSupplierID());
            stmt.setString(2, supplier.getFirstName());
            stmt.setString(3, supplier.getLastName());
            stmt.setString(4, supplier.getPhoneNumber());
            stmt.setString(5, supplier.getEmail());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("⚠️ Supplier add failed: " + e.getMessage());
            return false;
        }
    }

    public boolean updateSupplier(Supplier supplier) {
        String sql = "UPDATE TblSuppliers SET firstName=?, lastName=?, phoneNumber=?, Email=? WHERE supplierID=?";

        try (Connection conn = DataBaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, supplier.getFirstName());
            stmt.setString(2, supplier.getLastName());
            stmt.setString(3, supplier.getPhoneNumber());
            stmt.setString(4, supplier.getEmail());
            stmt.setInt(5, supplier.getSupplierID());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating supplier: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteSupplier(int supplierID) {
        String deleteItemsSql = "DELETE FROM TblInventoryItems WHERE supplierID=?";
        String deleteSupplierSql = "DELETE FROM TblSuppliers WHERE supplierID=?";

        try (Connection conn = DataBaseManager.connect()) {
            conn.setAutoCommit(false); // start transaction

            try (
                PreparedStatement deleteItemsStmt = conn.prepareStatement(deleteItemsSql);
                PreparedStatement deleteSupplierStmt = conn.prepareStatement(deleteSupplierSql)
            ) {
                // Delete related inventory items first
                deleteItemsStmt.setInt(1, supplierID);
                deleteItemsStmt.executeUpdate();

                // Delete the supplier
                deleteSupplierStmt.setInt(1, supplierID);
                int rows = deleteSupplierStmt.executeUpdate();

                conn.commit();
                return rows > 0;
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("❌ Failed during delete cascade: " + e.getMessage());
                return false;
            } finally {
                conn.setAutoCommit(true); // reset
            }

        } catch (SQLException e) {
            System.err.println("❌ Connection error: " + e.getMessage());
            return false;
        }
    }

    public List<Supplier> getAllSuppliers() {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT * FROM TblSuppliers";

        try (Connection conn = DataBaseManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Supplier supplier = new Supplier(
                        rs.getInt("supplierID"),
                        rs.getString("firstName"),
                        rs.getString("lastName"),
                        rs.getString("phoneNumber"),
                        rs.getString("Email")
                );
                suppliers.add(supplier);
            }

        } catch (SQLException e) {
            System.err.println("Error loading suppliers: " + e.getMessage());
        }

        return suppliers;
    }
}
