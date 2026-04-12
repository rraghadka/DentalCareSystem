package entity;

public class DentalHistory {
    private int dentalHistID;
    private int patientID;
    private String pastTreatment;
    private String xRayPath;
    private String dateTaken;

    public DentalHistory(int dentalHistID, int patientID, String pastTreatment, String xRayPath, String dateTaken) {
        this.dentalHistID = dentalHistID;
        this.patientID = patientID;
        this.pastTreatment = pastTreatment;
        this.xRayPath = xRayPath;
        this.dateTaken = dateTaken;
    }

    public int getDentalHistID() {
        return dentalHistID;
    }

    public int getPatientID() {
        return patientID;
    }

    public String getPastTreatment() {
        return pastTreatment;
    }

    public String getXRayPath() {
        return xRayPath;
    }

    public String getDateTaken() {
        return dateTaken;
    }

    public void setDentalHistID(int dentalHistID) {
        this.dentalHistID = dentalHistID;
    }

    public void setPatientID(int patientID) {
        this.patientID = patientID;
    }

    public void setPastTreatment(String pastTreatment) {
        this.pastTreatment = pastTreatment;
    }

    public void setXRayPath(String xRayPath) {
        this.xRayPath = xRayPath;
    }

    public void setDateTaken(String dateTaken) {
        this.dateTaken = dateTaken;
    }
}
