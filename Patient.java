package entity;

import java.util.Date;
import java.util.Objects;

public class Invoice {
    private int invoiceID;
    private int planID;
    private int patientID;
    private int staffID;
    private double totalAmount;
    private Date issuedDate;

    // Full constructor
    public Invoice(int invoiceID, int planID, int patientID, int staffID, double totalAmount, Date issuedDate) {
        this.invoiceID = invoiceID;
        this.planID = planID;
        this.patientID = patientID;
        this.staffID = staffID;
        this.totalAmount = totalAmount;
        this.issuedDate = issuedDate;
    }

    // Constructor without ID (for insertions)
    public Invoice(int planID, int patientID, int staffID, double totalAmount, Date issuedDate) {
        this(0, planID, patientID, staffID, totalAmount, issuedDate);
    }

    // Getters and setters
    public int getInvoiceID() { return invoiceID; }
    public void setInvoiceID(int invoiceID) { this.invoiceID = invoiceID; }

    public int getPlanID() { return planID; }
    public void setPlanID(int planID) { this.planID = planID; }

    public int getPatientID() { return patientID; }
    public void setPatientID(int patientID) { this.patientID = patientID; }

    public int getStaffID() { return staffID; }
    public void setStaffID(int staffID) { this.staffID = staffID; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public Date getIssuedDate() { return issuedDate; }
    public void setIssuedDate(Date issuedDate) { this.issuedDate = issuedDate; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Invoice)) return false;
        Invoice invoice = (Invoice) o;
        return invoiceID == invoice.invoiceID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(invoiceID);
    }

    @Override
    public String toString() {
        return "Invoice #" + invoiceID + " → total: ₪" + totalAmount;
    }
}

