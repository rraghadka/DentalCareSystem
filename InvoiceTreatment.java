package entity;

public class Insurance {
    private int insuranceID;
    private String providerName;
    private String policyNumber;
    private int patientID;

    public Insurance(int insuranceID, String providerName, String policyNumber, int patientID) {
        this.insuranceID = insuranceID;
        this.providerName = providerName;
        this.policyNumber = policyNumber;
        this.patientID = patientID;
    }

    public int getInsuranceID() { return insuranceID; }
    public String getProviderName() { return providerName; }
    public String getPolicyNumber() { return policyNumber; }
    public int getPatientID() { return patientID; }
}
