package entity;

import java.util.Date;


public class Patient {
    private int patientID;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private Date dateOfBirth;

    public Patient(String firstName, String lastName, String email, String phoneNumber, Date dateOfBirth) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
    }
    

    public Patient(int patientID, String firstName, String lastName, String email, String phoneNumber, Date dateOfBirth) {
        this(firstName, lastName, email, phoneNumber, dateOfBirth);
        this.patientID = patientID;
    }

    // Getters & Setters...
    
	public int getPatientID() {
		return patientID;
	}

	public Date getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(Date dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public void setPatientID(int patientID) {
		this.patientID = patientID;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String toString() {
	    return String.valueOf(patientID);   // ← ONLY the numeric ID
	    // If you want ID + name instead, use:
	    // return patientID + " – " + firstName + " " + lastName;
	}


	public String getFullName() {
	    return firstName + " " + lastName;
	}
	


    // (המשך getters/setters ו־toString)
}

