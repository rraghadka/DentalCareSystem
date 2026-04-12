package entity;

public class InvoiceRow {
    private int treatmentID;
    private String patientName;
    private String treatmentType;
    private double cost;

    public InvoiceRow(int treatmentID, String patientName, String treatmentType, double cost) {
        this.treatmentID = treatmentID;
        this.patientName = patientName;
        this.treatmentType = treatmentType;
        this.cost = cost;
    }

    public int getTreatmentID() {
        return treatmentID;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getTreatmentType() {
        return treatmentType;
    }

    public double getCost() {
        return cost;
    }
    
}
