package controller;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import entity.Consts;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Utility class to manage connections to the DentalCare Access DB. */
public class DataBaseManager {

    /** Opens a connection (prints the resolved path for easy debug). */
	public static Connection connect() {
	    try {
	        System.out.println("🔍 [DEBUG] Trying to resolve database path...");
	        System.out.println("📁 Resolved path: " + Consts.DB_FILEPATH);
	        System.out.println("🔗 JDBC URL: " + Consts.CONN_STR);

	        Connection conn = DriverManager.getConnection(Consts.CONN_STR);
	        System.out.println("✅ Connected to Access database successfully.");
	        return conn;

	    } catch (SQLException e) {
	        System.err.println("❌ Failed to connect to the Access database:");
	        e.printStackTrace();
	        return null;
	    }
	}
	public static Connection getConnection() throws SQLException {
	    return connect(); // פשוט מפעיל את connect שכבר כתובה היטב
	}


    /** Closes the given connection (safe-null). */
    public static void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("🔌 Connection closed.");
            } catch (SQLException e) {
                System.err.println("❌ Error closing database connection:");
                e.printStackTrace();
            }
        }
    }

    /** Convenience check – returns true if staffID has ROLE_DENTIST. */
    public boolean isDentist(int staffID) {
        String sql = "SELECT 1 FROM TblStaff WHERE staffID = ? AND roleID = ?";
        try (Connection conn = connect();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, staffID);
            ps.setInt(2, Consts.ROLE_DENTIST);
            return ps.executeQuery().next();

        } catch (SQLException e) {
            System.err.println("❌ Error checking staff role:");
            e.printStackTrace();
            return false;
        }
    }
}