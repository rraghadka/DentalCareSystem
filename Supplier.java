package entity;

import java.util.Objects;

public class InvoiceTreatment {
    private int invoiceID;
    private int treatmentID;
    private String description;
    private double cost;

    // Full constructor
    public InvoiceTreatment(int invoiceID, int treatmentID, String description, double cost) {
        this.invoiceID = invoiceID;
        this.treatmentID = treatmentID;
        this.description = description;
        this.cost = cost;
    }

    // Getters and setters
    public int getInvoiceID() { return invoiceID; }
    public void setInvoiceID(int invoiceID) { this.invoiceID = invoiceID; }

    public int getTreatmentID() { return treatmentID; }
    public void setTreatmentID(int treatmentID) { this.treatmentID = treatmentID; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InvoiceTreatment)) return false;
        InvoiceTreatment that = (InvoiceTreatment) o;
        return invoiceID == that.invoiceID && treatmentID == that.treatmentID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(invoiceID, treatmentID);
    }

    @Override
    public String toString() {
        return description + " → ₪" + cost;
    }
}
