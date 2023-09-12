package scheduler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Appointment class is made to mirror the visible variables from the appointments table in the database, and serves
 * to store the variables to help in displaying the information on the TableViews in the forms.
 */
public class Appointment {
    private final int id;
    private final String title;
    private final String description;
    private final String location;
    private final String type;
    private final String contact;
    private final String start;
    private final String end;
    private int userId;
    private final int customerId;
    private final static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ZonedDateTime startDateTime;
    private final ZonedDateTime endDateTime;
    private final static LocalTime openTimeEST = LocalTime.parse("07:59:59 AM", DateTimeFormatter.ofPattern("hh:mm:ss a", Locale.US));
    private final static LocalTime closeTimeEST = LocalTime.parse("10:00:01 PM", DateTimeFormatter.ofPattern("hh:mm:ss a", Locale.US));

    public Appointment(int id, String title, String description, String location, String type, String contact, ZonedDateTime start, ZonedDateTime end, int customerId){
        this.id = id;
        this.title =  title;
        this.description = description;
        this.location = location;
        this.type = type;
        this.contact = contact;
        this.start = dtf.format(start);
        this.startDateTime = start;
        this.end = dtf.format(end);
        this.endDateTime = end;
        this.customerId = customerId;
    }
    public int getId(){return this.id;}

    public String getTitle(){return this.title;}

    public String getDescription(){return this.description;}

    public String getLocation(){return this.location;}

    public String getType(){return this.type;}

    public int getUserId(){return this.userId;}

    public void setUserId(int userID){this.userId = userID;}

    public String getContact(){return this.contact;}

    public String getStart(){return this.start;}

    public String getEnd(){return end;}

    public int getCustomerId(){return customerId;}

    public ZonedDateTime getStartDateTime() {return startDateTime;}

    public ZonedDateTime getEndDateTime() {return endDateTime;}

    public static DateTimeFormatter getDtf(){return dtf;}

    /**
     * Returns the ZonedDateTime as a LocalDate
     * @return the current Date based on the StartDateTime.
     */
    public LocalDate getStartLocalDate() {
        LocalDate date = this.startDateTime.toLocalDate();
        return date;
    }

    /**
     * obtains the customer's name based on the appointment's corresponding customer ID.
     * @return the customer's name.
     * @throws SQLException thrown when there is an issue connecting to the database.
     */
    public String getCustomerName() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        var sql = "SELECT Customer_Name FROM customers WHERE Customer_ID = ?";
        String result = "";
        try(var ps = conn.prepareStatement(sql)){
            ps.setInt(1,this.customerId);
            ResultSet rs = ps.executeQuery();
            rs.next();
            result = rs.getString("Customer_Name");
        }
        return result;
    }

    /**
     * Gets the corresponding contact ID based on the contact's name.
     * @return the Contact's ID
     * @throws SQLException thrown when there is an issue with database connection.
     */
    public int getContactID() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        var sql = "SELECT Contact_ID FROM contacts WHERE Contact_Name = ?";
        int result = 0;
        try(var ps = conn.prepareStatement(sql)){
            ps.setString(1,this.contact);
            ResultSet rs = ps.executeQuery();
            rs.next();
            result = rs.getInt("Contact_ID");
        }
        return result;
    }

    /**
     * Takes a ZonedDateTime and checks to see if it is between 8:00 am EST and 10:00 pm EST.
     * @param userTime the time you are checking.
     * @return returns true if it was in between 8 AM and 10 PM EST, and returns false if it is not.
     */
    public static boolean isOpenDuringTime(ZonedDateTime userTime){
        boolean open = false;
        LocalTime convertedTime = userTime.withZoneSameInstant(ZoneId.of("America/New_York")).toLocalTime();
        if(convertedTime.isAfter(openTimeEST) && convertedTime.isBefore(closeTimeEST)){
            open = true;
        }
       return open;
    }

    /**
     * Returns the opening time of the company to the user in their own time zone.
     * @return the adjusted opening time.
     */
    public static LocalTime getZonedOpenTime(){
        ZonedDateTime estTime = ZonedDateTime.of(LocalDate.now(ZoneId.of("America/New_York")), openTimeEST, ZoneId.of("America/New_York"));
        return estTime.withZoneSameInstant(CurrentUser.getZoneId()).toLocalTime();
    }

    /**
     * Returns the closing time of the company to the user in their own time zone.
     * @return the adjusted closing time.
     */
    public static LocalTime getZonedCloseTime(){
        ZonedDateTime estTime = ZonedDateTime.of(LocalDate.now(ZoneId.of("America/New_York")), closeTimeEST, ZoneId.of("America/New_York"));
        return estTime.withZoneSameInstant(CurrentUser.getZoneId()).toLocalTime();
    }
}
