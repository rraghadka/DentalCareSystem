package entity;

public class MedicalHistory {
    private int medicalHistID;
    private int patientID;
    private String conditionName;
    private String allergies;

    public MedicalHistory(int medicalHistID, int patientID, String conditionName, String allergies) {
        this.medicalHistID = medicalHistID;
        this.patientID = patientID;
        this.conditionName = conditionName;
        this.allergies = allergies;
    }


    public int getMedicalHistID() { return medicalHistID; }
    public int getPatientID() { return patientID; }
    public String getConditionID() { return conditionName; }
    public String getAllergies() { return allergies; }
}
