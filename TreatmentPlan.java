package entity;

public class Staff {
    private int staffID;
    private int roleID;
    private String firstName;
    private String lastName;
    private String phoneNumt;
    private String email;
    private String qualification;
    private String specializationID;
    private int availabilityID;

    // Constructor (ללא ID - לשימוש בהוספה)
    public Staff(int roleID, String firstName, String lastName, String phoneNumt,
                 String email, String qualification, String specializationID, int availabilityID) {
        this.roleID = roleID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumt = phoneNumt;
        this.email = email;
        this.qualification = qualification;
        this.specializationID = specializationID;
        this.availabilityID = availabilityID;
    }
    public Staff(int staffID, int roleID, String firstName, String lastName, String phoneNum, String email) {
        this.staffID = staffID;
        this.roleID = roleID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumt = phoneNum;
        this.email = email;
    }
    public Staff(int staffID, String firstName, String lastName) {
        this.staffID = staffID;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Constructor מלא (כולל ID)
    public Staff(int staffID, int roleID, String firstName, String lastName, String phoneNumt,
                 String email, String qualification, String specializationID, int availabilityID) {
        this(roleID, firstName, lastName, phoneNumt, email, qualification, specializationID, availabilityID);
        this.staffID = staffID;
    }

    // Getters and Setters
    public int getStaffID() { return staffID; }
    public void setStaffID(int staffID) { this.staffID = staffID; }

    public int getRoleID() { return roleID; }
    public void setRoleID(int roleID) { this.roleID = roleID; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhoneNumt() { return phoneNumt; }
    public void setPhoneNumt(String phoneNumt) { this.phoneNumt = phoneNumt; }
    public String getFullName() {
        return firstName + " " + lastName;
    }


    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public String getSpecializationID() { return specializationID; }
    public void setSpecializationID(String specializationID) { this.specializationID = specializationID; }

    public int getAvailabilityID() { return availabilityID; }
    public void setAvailabilityID(int availabilityID) { this.availabilityID = availabilityID; }
    @Override
    public String toString() {
        return firstName + " " + lastName;
    }



    
}
