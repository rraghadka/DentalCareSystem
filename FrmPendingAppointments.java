package boundary;

import javax.swing.*;

public class FrmPatientDashboard extends JFrame {
    public FrmPatientDashboard(int patientID) {
        setTitle("Patient Dashboard – ID " + patientID);
        add(new JLabel("TODO → implement patient features",
                       SwingConstants.CENTER));
        setSize(400, 200);
        setLocationRelativeTo(null);
    }
}
