package scheduler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * CurrentUser class is used to store the username of the person who successfully
 * logs in, so the information can easily be retrieved again when needed for storing
 * who created or updated information in the database.
 */
public final class CurrentUser {

    private static String username;
    private static ZoneId zoneId;
    private static int userId;

    /**
     * CurrentUser private constructor is to prevent an instance of
     * CurrentUser to be created, so there can't be any confusion or multiple users.
     */
    private CurrentUser() {}

    /**
     * sets the username to the CurrentUser
     * @param name is the username of the current user who successfully logs in.
     */
    public static void setUsername (String name){
        username = name;
    }

    /**
     * Returns the username of the current user.
     * @return current username of the person who logged in.
     */
    public static String getUsername(){
        return username;
    }

    /**
     * Stores the current user's ZoneId.
     */
    public static void establishZoneId(){
        zoneId = ZoneId.systemDefault();
    }

    /**
     * Returns the current user's ZoneId.
     * @return the user's ZoneId.
     */
    public static ZoneId getZoneId(){
        return zoneId;
    }

    /**
     * Used to convert another ZonedDateTime to the local Time Zone of the current user.
     * @param sourceTime the ZonedDateTime to be converted.
     * @return returns the sourceTime after conversion to user's local timezone.
     */
    public static ZonedDateTime convertToUserTimeZone(ZonedDateTime sourceTime){
        ZonedDateTime userTime = sourceTime.withZoneSameInstant(zoneId);
        return userTime;
    }

    /**
     * Sets the user ID of the current user logged in by going through the database and matching it up.
     * @throws SQLException thrown when there is an issue connecting to the database or executing SQL statement.
     */
    public static void setUserId() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        var sql = "SELECT User_ID FROM users WHERE User_Name = ?";
        int id = 0;
        try(var ps = conn.prepareStatement(sql)){
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            rs.next();
            id = rs.getInt("User_ID");
        }
        userId = id;
    }

    /**
     * Gets the user ID of the current user.
     * @return the user id.
     */
    public static int getUserId() {return userId;}

}
