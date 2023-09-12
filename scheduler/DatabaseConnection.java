package scheduler;

import com.mysql.jdbc.Connection;

import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConnection is used to establish a connection with the database and then
 * use a method to return the connection anytime another method needs to use it.
 */
public class DatabaseConnection {
    private static Connection connection;

    private DatabaseConnection() throws SQLException {
    }

    public static Connection getConnection() {
        return connection;
    }

    /**
     * establishConnection connects to the database, so that other methods can gain access to it
     * through the getConnection method.
     * @throws SQLException if there is an issue connecting to the database it will throw this exception.
     */
    public static void establishConnection() throws SQLException {
        connection = (Connection) DriverManager.getConnection("jdbc:mysql:URL", "username", "password");
    }
}
