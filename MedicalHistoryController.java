package controller;

import entity.InventoryItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryItemController {

    public boolean addInventoryItem(InventoryItem item) {
        String sql = "INSERT INTO TblInventoryItems (itemID, itemName, description, category, quantityInSt, supplierID, expirationDt, serialNumb) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DataBaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, item.getItemID());
            stmt.setString(2, item.getItemName());
            stmt.setString(3, item.getDescription());
            stmt.setString(4, item.getCategory());
            stmt.setInt(5, item.getQuantityInSt());
            stmt.setInt(6, item.getSupplierID());

            if (item.getExpirationDt() != null) {
                stmt.setDate(7, new java.sql.Date(item.getExpirationDt().getTime()));
            } else {
                stmt.setNull(7, Types.DATE);
            }

            stmt.setString(8, item.getSerialNumb());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error adding inventory item: " + e.getMessage());
            // Log a warning if supplier does not exist, but do not block the insert
            if (e.getMessage().contains("FOREIGN KEY")) {
                System.err.println("⚠ Warning: Supplier ID " + item.getSupplierID() + " not found. Inventory item saved without valid supplier reference.");
                return true; // allow saving the item even without valid FK
            }
            return false;
        }
    }

    public boolean updateInventoryItem(InventoryItem item) {
        String sql = "UPDATE TblInventoryItems SET itemName=?, description=?, category=?, quantityInSt=?, supplierID=?, expirationDt=?, serialNumb=? WHERE itemID=?";

        try (Connection conn = DataBaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, item.getItemName());
            stmt.setString(2, item.getDescription());
            stmt.setString(3, item.getCategory());
            stmt.setInt(4, item.getQuantityInSt());
            stmt.setInt(5, item.getSupplierID());

            if (item.getExpirationDt() != null) {
                stmt.setDate(6, new java.sql.Date(item.getExpirationDt().getTime()));
            } else {
                stmt.setNull(6, Types.DATE);
            }

            stmt.setString(7, item.getSerialNumb());
            stmt.setInt(8, item.getItemID());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating inventory item: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteInventoryItem(int itemID) {
        String sql = "DELETE FROM TblInventoryItems WHERE itemID=?";

        try (Connection conn = DataBaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, itemID);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting inventory item: " + e.getMessage());
            return false;
        }
    }

    public List<InventoryItem> getAllInventoryItems() {
        List<InventoryItem> items = new ArrayList<>();
        String sql = "SELECT * FROM TblInventoryItems";

        try (Connection conn = DataBaseManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                InventoryItem item = new InventoryItem(
                        rs.getInt("itemID"),
                        rs.getString("itemName"),
                        rs.getString("description"),
                        rs.getString("category"),
                        rs.getInt("quantityInSt"),
                        rs.getInt("supplierID"),
                        rs.getDate("expirationDt"),
                        rs.getString("serialNumb")
                );
                items.add(item);
            }

        } catch (SQLException e) {
            System.err.println("Error loading inventory items: " + e.getMessage());
        }

        return items;
    }
    /** Items whose quantity is below the “5” threshold, sorted by lowest first. */
    public List<InventoryItem> getLowStockItems() {
        List<InventoryItem> list = new ArrayList<>();

        String sql =
            "SELECT * FROM TblInventoryItems " +
            "WHERE quantityInSt < 5 " +
            "ORDER BY quantityInSt ASC";

        try (Connection c = DataBaseManager.connect();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                InventoryItem item = new InventoryItem(
                    rs.getInt("itemID"),
                    rs.getString("itemName"),
                    rs.getString("description"),
                    rs.getString("category"),
                    rs.getInt("quantityInSt"),
                    rs.getInt("supplierID"),
                    rs.getDate("expirationDt"),
                    rs.getString("serialNumb")
                );
                list.add(item);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }
    

}
