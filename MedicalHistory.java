package entity;

import java.util.Date;

public class InventoryItem {
    private int itemID;
    private String itemName;
    private String description;
    private String category;
    private int quantityInSt;
    private int supplierID;
    private Date expirationDt;
    private String serialNumb;

    // Full constructor (used for reading and updates)
    public InventoryItem(int itemID, String itemName, String description, String category,
                         int quantityInSt, int supplierID, Date expirationDt, String serialNumb) {
        this.itemID = itemID;
        this.itemName = itemName;
        this.description = description;
        this.category = category;
        this.quantityInSt = quantityInSt;
        this.supplierID = supplierID;
        this.expirationDt = expirationDt;
        this.serialNumb = serialNumb;
    }

    // Constructor without ID (not used since itemID is required manually)
    public InventoryItem(String itemName, String description, String category,
                         int quantityInSt, int supplierID, Date expirationDt, String serialNumb) {
        this.itemName = itemName;
        this.description = description;
        this.category = category;
        this.quantityInSt = quantityInSt;
        this.supplierID = supplierID;
        this.expirationDt = expirationDt;
        this.serialNumb = serialNumb;
    }
    public boolean isExpired() {
        if (expirationDt == null) return false;
        java.util.Date today = new java.util.Date();
        return expirationDt.before(today);
    }


    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getQuantityInSt() {
        return quantityInSt;
    }

    public void setQuantityInSt(int quantityInSt) {
        this.quantityInSt = quantityInSt;
    }

    public int getSupplierID() {
        return supplierID;
    }

    public void setSupplierID(int supplierID) {
        this.supplierID = supplierID;
    }

    public Date getExpirationDt() {
        return expirationDt;
    }

    public void setExpirationDt(Date expirationDt) {
        this.expirationDt = expirationDt;
    }

    public String getSerialNumb() {
        return serialNumb;
    }

    public void setSerialNumb(String serialNumb) {
        this.serialNumb = serialNumb;
    }

    @Override
    public String toString() {
        return "InventoryItem{" +
                "itemID=" + itemID +
                ", itemName='" + itemName + '\'' +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", quantityInSt=" + quantityInSt +
                ", supplierID=" + supplierID +
                ", expirationDt=" + expirationDt +
                ", serialNumb='" + serialNumb + '\'' +
                '}';
    }
}
