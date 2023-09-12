package scheduler;

import com.mysql.jdbc.Connection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.*;

/**
 * LoginAlertController is the controller to add functionality to the elements of loginAlert.fxml
 * It's used to show an alert to the user upon successful login whether or not they have any appointments in the next 15 minutes.
 */
public class LoginAlertController {
    public Label lblContent;
    public Label lblHeader;
    public Button btnOkay;

    /**
     * Closes the loginAlert window.
     */
    public void closeWindow(){
        Stage stage = (Stage) btnOkay.getScene().getWindow();
        stage.close();
    }

    /**
     * Checks to see if the user has any upcoming Appointments in the next 15 minutes of when they log in. If they do then the form will
     * show that, and if not it will give a message that they have none. The method gets a list of all appointments from the database, and then filters through them
     * seeing if the user ID matches the current user as well as if the start time is within 15 minutes of a successful login.
     * The lambda expression is used to easily filter through the list without needing more complicated code.
     * @throws SQLException thrown when there is an issue connecting to the Database.
     */
    public void checkUpcomingAppointments() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        var contactSQL = "SELECT Contact_Name FROM contacts WHERE Contact_ID = ?";
        ObservableList<Appointment> apptList = FXCollections.observableArrayList();
        ObservableList<Appointment> filteredList;
        try (var apptPS = conn.prepareStatement("SELECT Appointment_ID, Title, Description, Location, Type, Contact_ID, Start, End, Customer_ID, User_ID FROM appointments")) {
            ResultSet apptRS = apptPS.executeQuery();

            while (apptRS.next()) {
                int id = apptRS.getInt("Appointment_ID");
                String title = apptRS.getString("Title");
                String description = apptRS.getString("Description");
                String location = apptRS.getString("Location");
                String type = apptRS.getString("Type");
                ZonedDateTime startDateUTC = ZonedDateTime.of(LocalDate.parse(apptRS.getDate("Start").toString()), LocalTime.parse(apptRS.getTime("Start").toString()), ZoneId.of("UTC"));
                ZonedDateTime startDate = CurrentUser.convertToUserTimeZone(startDateUTC);
                ZonedDateTime endDateUTC = ZonedDateTime.of(LocalDate.parse(apptRS.getDate("End").toString()), LocalTime.parse(apptRS.getTime("End").toString()), ZoneId.of("UTC"));
                ZonedDateTime endDate = CurrentUser.convertToUserTimeZone(endDateUTC);
                int customerId = apptRS.getInt("Customer_ID");
                int contactId = apptRS.getInt("Contact_ID");
                String contact;
                try (var contactPS = conn.prepareStatement(contactSQL)) {
                    contactPS.setInt(1, contactId);
                    ResultSet rs = contactPS.executeQuery();
                    rs.next();
                    contact = rs.getString("Contact_Name");
                }
                Appointment appt = new Appointment(id, title, description, location, type, contact, startDate, endDate, customerId);
                appt.setUserId(apptRS.getInt("User_ID"));
                apptList.add(appt);
            }
            LocalDateTime currentTime = LocalDateTime.now();
            LocalDateTime currentTimePlusFifteen = LocalDateTime.now().plusMinutes(15);
            filteredList = apptList.filtered(a -> {
                if(a.getStartDateTime().toLocalDateTime().isAfter(currentTime) && a.getStartDateTime().toLocalDateTime().isBefore(currentTimePlusFifteen) && CurrentUser.getUserId() == a.getUserId()){
                    return true;
                }
                else{
                    return false;
                }
            });

        }
        if(!filteredList.isEmpty()){
            Appointment appt = filteredList.get(0);
            lblHeader.setText("Upcoming Appointment");
            lblContent.setText("You have an upcoming appointment!\r\n" +
                    "Appointment ID:" + appt.getId() + " \"" + appt.getTitle() + "\" \r\nbegins at " + appt.getStart());
        }
        else{
            lblHeader.setText("No Upcoming Appointments");
            lblContent.setText("You have no immediately upcoming appointments!");
        }
    }
}
