package entity;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Constant and utility holder (not instantiable).
 */
public final class Consts {

    /* === USER-DEFINED CONSTANTS === */
	 public static final int ROLE_PATIENT        = 0; // לא בטבלת Staff
    public static final int ROLE_DENTIST = 1;   // roleID for dentists
    public static final int ROLE_SECRETARY = 2;
    public static final int ROLE_MANAGER   = 3; 

    /* === DATABASE LOCATION HANDLING === */
    /**
     * Absolute path to the Access (.accdb) database file, resolved
     * at runtime so it works in both Eclipse and a runnable JAR.
     */
    public static final String DB_FILEPATH = resolveDBPath();

    /**
     * UCanAccess JDBC connection string – COLUMNORDER=DISPLAY keeps
     * column order identical to the Access UI.
     */
    public static final String CONN_STR =
            "jdbc:ucanaccess://" + DB_FILEPATH + ";COLUMNORDER=DISPLAY";

    /* === Internal helper methods === */
    private Consts() { throw new AssertionError("Utility class"); }

    /**
     * Resolves the correct absolute path to <code>databaseDentalCare.accdb</code>.
     * <ul>
     *   <li><strong>When running from a&nbsp;JAR</strong> it looks in the same
     *       folder as the&nbsp;JAR.</li>
     *   <li><strong>When running in Eclipse/IDE</strong> it looks in
     *       <code>[project-root]/resources/</code>.</li>
     * </ul>
     */
    private static String resolveDBPath() {
        try {
            /* Path to the location of the compiled class/JAR */
            String codePath = Consts.class
                               .getProtectionDomain()
                               .getCodeSource()
                               .getLocation()
                               .getPath();
            String decodedPath = URLDecoder.decode(codePath,
                                    StandardCharsets.UTF_8.name());

            /* === 1. We are inside a JAR (production) === */
            if (decodedPath.endsWith(".jar")) {
                File jarDir = new File(decodedPath).getParentFile();
                return new File(jarDir, "databaseDentalCare.accdb")
                        .getAbsolutePath();
            }

            /* === 2. IDE / classes folder (development) === */
            // Assumes the .accdb lives in /resources at project root
            return new File(System.getProperty("user.dir"),
                            "resources/databaseDentalCare.accdb")
                    .getAbsolutePath();

        } catch (Exception ex) {
            ex.printStackTrace();
            // Fall back to current working dir – may still succeed
            return new File("databaseDentalCare.accdb").getAbsolutePath();
        }
    }
    
}
