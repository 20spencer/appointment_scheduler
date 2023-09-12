package scheduler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Customer is used to mirror the needed values in the customers table in the database, and is used
 * to store values to help access data and make it visible on the table view.
 */
public class Customer {
    private final int id;
    private final String name;
    private final String address;
    private final String postalCode;
    private final String phone;
    private final String region;

    public Customer (int id, String name, String address, String postalCode, String phone, String region){
        this.id = id;
        this.name = name;
        this.address = address;
        this.postalCode = postalCode;
        this.phone = phone;
        this.region = region;
    }

    public int getId() {return id;}

    public String getAddress() {return address;}

    public String getName() {return name;}

    public String getPhone() {return phone;}

    public String getPostalCode() {return postalCode;}

    public String getRegion() {return region;}

    /**
     * Gets the region ID of the region assigned to this customer.
     * @return the region's ID number, or if connection fails returns 0.
     * @throws SQLException thrown if there is an issue with the database connection.
     */
    public int getRegionID() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        int id = 0;
        var sql = "SELECT Division_ID FROM first_level_divisions WHERE Division = ?";
        try (var idPS = conn.prepareStatement(sql)){
            idPS.setString(1, this.region);
            ResultSet rs = idPS.executeQuery();
            rs.next();
            id = rs.getInt("Division_ID");
        }
        return id;
    }

    /**
     * Gets the Country ID of the region assigned ot the customer.
     * @return the region's corresponding country ID, or if failed it returns 0.
     * @throws SQLException thrown if there is an issue with the database connection.
     */
    public int getCountryID () throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        int id = 0;
        var sql = "SELECT COUNTRY_ID FROM first_level_divisions WHERE Division = ?";
        try (var ps = conn.prepareStatement(sql)){
            ps.setString(1, this.region);
            ResultSet rs = ps.executeQuery();
            rs.next();
            id = rs.getInt("COUNTRY_ID");
        }
        return id;
    }

    /**
     * Returns the corresponding Country of the customer based on their country ID.
     * @return the name of the Country.
     * @throws SQLException thrown if there are issues with Database connection.
     */
    public String getCountry() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        var sql = "SELECT Country FROM countries WHERE Country_ID = " + getCountryID();
        String country = "";
        try(var ps = conn.prepareStatement(sql)){
            ResultSet rs = ps.executeQuery();
            rs.next();
            country = rs.getString("Country");
        }
        return country;
    }

    /**
     * Gets the Customer ID of the given Customer.
     * @param name the name of the Customer you want the Customer ID of.
     * @return the Customer ID of the given customer.
     */
    public static int getCustomerIDFromName(String name){
        int id = 0;
        Connection conn = DatabaseConnection.getConnection();
        var sql = "SELECT Customer_ID FROM customers WHERE Customer_Name = ?";
        try(var ps = conn.prepareStatement(sql)){
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            rs.next();
            id = rs.getInt("Customer_ID");
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        return id;
    }
}
