package controller;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class XMLImportController {

    public boolean importSupplierAndItemsFromXML(String filePath) {
        try {
            File xmlFile = new File(filePath);
            if (!xmlFile.exists()) {
                System.err.println("File not found: " + filePath);
                return false;
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            Connection conn = DataBaseManager.connect();

            Element supplierElem = (Element) doc.getElementsByTagName("Supplier").item(0);
            int supplierID = Integer.parseInt(getTagValue("supplierID", supplierElem));
            String firstName = getTagValue("firstName", supplierElem);
            String lastName = getTagValue("lastName", supplierElem);
            String phone = getTagValue("phoneNumber", supplierElem);
            String email = getTagValue("Email", supplierElem);

            String supplierSQL = "INSERT INTO TblSuppliers (supplierID, firstName, lastName, phoneNumber, Email) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(supplierSQL)) {
                stmt.setInt(1, supplierID);
                stmt.setString(2, firstName);
                stmt.setString(3, lastName);
                stmt.setString(4, phone);
                stmt.setString(5, email);
                stmt.executeUpdate();
            }

            NodeList itemList = supplierElem.getElementsByTagName("InventoryItem");

            for (int i = 0; i < itemList.getLength(); i++) {
                Node node = itemList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element itemElem = (Element) node;

                    int itemID = Integer.parseInt(getTagValue("itemID", itemElem));
                    String itemName = getTagValue("itemName", itemElem);
                    String description = getTagValue("description", itemElem);
                    String category = getTagValue("category", itemElem);
                    int quantity = Integer.parseInt(getTagValue("quantityInSt", itemElem));
                    String expiration = getTagValue("expirationDt", itemElem);
                    String serial = getTagValue("serialNumb", itemElem);

                    String itemSQL = "INSERT INTO TblInventoryItems (itemID, itemName, description, category, quantityInSt, supplierID, expirationDt, serialNumb) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(itemSQL)) {
                        stmt.setInt(1, itemID);
                        stmt.setString(2, itemName);
                        stmt.setString(3, description);
                        stmt.setString(4, category);
                        stmt.setInt(5, quantity);
                        stmt.setInt(6, supplierID);
                        stmt.setString(7, expiration);
                        stmt.setString(8, serial);
                        stmt.executeUpdate();
                    }
                }
            }

            conn.close();
            return true;

        } catch (Exception e) {
            System.err.println("Error importing from XML: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String getTagValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag);
        if (nodeList.getLength() == 0) return "";
        NodeList childNodes = nodeList.item(0).getChildNodes();
        Node node = (Node) childNodes.item(0);
        return node != null ? node.getNodeValue() : "";
    }
}

