package entity;

import java.util.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Objects;

/**
 * Maps one row of TblAppointment.
 */
public class Appointment {

    private int appointmentID;   // AutoNumber
    private int patientID;
    private Date appointmentDate;
    private String appointmentTime;   // HH:mm
    private int staffID;
    private String status;
    private int reasonID;      // change to int if numeric
    private int treatmentID;
    private boolean paid;

    /* ctor without PK – inserts */
    public Appointment(int patientID, int staffID, int treatmentID,
                       Date appointmentDate, String appointmentTime,
                       String status, int reasonID) {
        this.patientID       = patientID;
        this.staffID         = staffID;
        this.treatmentID     = treatmentID;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.status          = status;
        this.reasonID        = reasonID;
    }
    
 public Appointment(int appointmentID, int patientID, Date appointmentDate, String appointmentTime, int staffID,
			String status, int reasonID, int treatmentID, boolean paid) {
		super();
		this.appointmentID = appointmentID;
		this.patientID = patientID;
		this.appointmentDate = appointmentDate;
		this.appointmentTime = appointmentTime;
		this.staffID = staffID;
		this.status = status;
		this.reasonID = reasonID;
		this.treatmentID = treatmentID;
		this.paid = paid;
	}

// Appointment.java (inside entity package)
    public Appointment(int appointmentID, int treatmentID, java.sql.Date appointmentDate, String status) {
        this.appointmentID = appointmentID;
        this.treatmentID = treatmentID;
        this.appointmentDate = appointmentDate;
        this.status = status;
    }

    /* ctor with PK – reads */
    public Appointment(int appointmentID, int patientID, int staffID, int treatmentID,
                       Date appointmentDate, String appointmentTime,
                       String status, int reasonID) {
        this(patientID, staffID, treatmentID,
             appointmentDate, appointmentTime, status, reasonID);
        this.appointmentID = appointmentID;
    }

    /* getters / setters (only what you really need) */
    public int getAppointmentID()           { return appointmentID; }
    public Date getAppointmentDate()        { return appointmentDate; }
    public int  getTreatmentID()            { return treatmentID;  }
    public String getStatus()               { return status; }
    public String getAppointmentTime()      { return appointmentTime; }

    @Override public boolean equals(Object o) {
        return o instanceof Appointment && appointmentID == ((Appointment)o).appointmentID;
    }
    @Override public int hashCode() { return Objects.hash(appointmentID); }
	public int getPatientID() {
		return patientID;
	}
	public void setPatientID(int patientID) {
		this.patientID = patientID;
	}
	public int getStaffID() {
		return staffID;
	}
	public void setStaffID(int staffID) {
		this.staffID = staffID;
	}
	public int getReasonID() {
		return reasonID;
	}
	public void setReasonID(int reasonID) {
		this.reasonID = reasonID;
	}
	public void setAppointmentID(int appointmentID) {
		this.appointmentID = appointmentID;
	}
	public void setAppointmentDate(Date appointmentDate) {
		this.appointmentDate = appointmentDate;
	}
	public void setAppointmentTime(String appointmentTime) {
		this.appointmentTime = appointmentTime;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public void setTreatmentID(int treatmentID) {
		this.treatmentID = treatmentID;
	}
	public Appointment(int patientID, Date appointmentDate, String appointmentTime, int staffID, String status, int reasonID, int treatmentID) {
	    this.patientID = patientID;
	    this.appointmentDate = appointmentDate;
	    this.appointmentTime = appointmentTime;
	    this.staffID = staffID;
	    this.status = status;
	    this.reasonID = reasonID;
	    this.treatmentID = treatmentID;
	}
	

public boolean isPaid() {
    return paid;
}

public void setPaid(boolean paid) {
    this.paid = paid;
}
public static Appointment fromResultSet(ResultSet rs) throws SQLException {
    return new Appointment(
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
}


}
