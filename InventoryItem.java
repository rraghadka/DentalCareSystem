package entity;

public class AppointmentReason {
    private String reasonID;
    private String description;
    private String reasonName;

    public AppointmentReason(String reasonID, String description, String reasonName) {
        this.reasonID = reasonID;
        this.description = description;
        this.reasonName = reasonName;
    }

    public String getReasonID() {
        return reasonID;
    }

    public void setReasonID(String reasonID) {
        this.reasonID = reasonID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReasonName() {
        return reasonName;
    }

    public void setReasonName(String reasonName) {
        this.reasonName = reasonName;
    }

    @Override
    public String toString() {
        return reasonName; // ככה זה יוצג בקומבו
    }
}
