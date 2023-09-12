package scheduler;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ResourceBundle;

/**
 * Used to give functionality to the contactSchedule.fxml form.
 * Displays a combo box with a list of the contacts available, and when selected it will populate the tableview
 * with a list of all their appointments in chronological order.
 */
public class ContactScheduleController implements Initializable {
    public TableView<Appointment> tvAppointments;
    public TableColumn colAppointmentID;
    public TableColumn colTitle;
    public TableColumn colType;
    public TableColumn colDescription;
    public TableColumn colStart;
    public TableColumn colEnd;
    public TableColumn colCustomerID;
    public Button btnClose;
    public Button btnReports;
    public ComboBox<String> cbContact;

    /**
     * Sets the cell value factories for the table columns so they can display the information correctly, and then
     * populates the combo box with the Contact names.
     * @param url not used.
     * @param resourceBundle not used.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colAppointmentID.setCellValueFactory(new PropertyValueFactory<Appointment, Integer>("id"));
        colTitle.setCellValueFactory(new PropertyValueFactory<Appointment, String>("title"));
        colType.setCellValueFactory(new PropertyValueFactory<Appointment, String>("type"));
        colDescription.setCellValueFactory(new PropertyValueFactory<Appointment, String>("description"));
        colStart.setCellValueFactory(new PropertyValueFactory<Appointment, String>("start"));
        colEnd.setCellValueFactory(new PropertyValueFactory<Appointment, String>("end"));
        colCustomerID.setCellValueFactory(new PropertyValueFactory<Appointment, Integer>("customerId"));

        fillContactComboBox();
    }
    /**
     * Opens the report menu window again and closes the current window.
     * @throws IOException thrown when there is an issue obtaining the reportForm.fxml file.
     */
    public void btnReportsAction() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("reportForm.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Choose a Report");
        stage.setScene(new Scene(loader.load()));
        stage.show();
        closeWindow();
    }
    /**
     * Closes the current window.
     */
    public void closeWindow(){
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }

    /**
     * Gets the names of all the contacts in the database and puts them in the Combo Box.
     */
    public void fillContactComboBox(){
        Connection conn = DatabaseConnection.getConnection();
        var sql = "SELECT Contact_Name FROM contacts";
        ObservableList<String> contactList = FXCollections.observableArrayList();
        try(var ps = conn.prepareStatement(sql)){
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                contactList.add(rs.getString("Contact_Name"));
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        cbContact.setItems(contactList);
    }

    /**
     * Fills the tableview with the appropriate data from the database. Gets the Selected Contact's contact ID to filter through the appointments,
     * then adds the appointments to a list, sorts them by date from earliest to latest chronologically, and then sets them to the table.
     * The lambda expression is used to easily implement a Comparator to determine how the list should be sorted.
     * @throws SQLException thrown if there is any issue with database connection or sql execution.
     */
    public void fillAppointmentTable() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        var contactSQL = "SELECT Contact_ID FROM contacts WHERE Contact_Name = ?";
        ObservableList<Appointment> apptList = FXCollections.observableArrayList();
        int contactID;
        try (var contactPS = conn.prepareStatement(contactSQL)){
            contactPS.setString(1, cbContact.getSelectionModel().getSelectedItem());
            ResultSet contactRS = contactPS.executeQuery();
            contactRS.next();
            contactID = contactRS.getInt("Contact_ID");
        }
        try (var apptPS = conn.prepareStatement("SELECT Appointment_ID, Title, Description, Type, Start, End, Customer_ID FROM appointments WHERE Contact_ID = ?")) {
            apptPS.setInt(1, contactID);
            ResultSet apptRS = apptPS.executeQuery();

            while (apptRS.next()) {
                int id = apptRS.getInt("Appointment_ID");
                String title = apptRS.getString("Title");
                String description = apptRS.getString("Description");
                String type = apptRS.getString("Type");
                ZonedDateTime startDateUTC = ZonedDateTime.of(LocalDate.parse(apptRS.getDate("Start").toString()), LocalTime.parse(apptRS.getTime("Start").toString()), ZoneId.of("UTC"));
                ZonedDateTime startDate = CurrentUser.convertToUserTimeZone(startDateUTC);
                ZonedDateTime endDateUTC = ZonedDateTime.of(LocalDate.parse(apptRS.getDate("End").toString()), LocalTime.parse(apptRS.getTime("End").toString()), ZoneId.of("UTC"));
                ZonedDateTime endDate = CurrentUser.convertToUserTimeZone(endDateUTC);
                int customerId = apptRS.getInt("Customer_ID");
                Appointment appt = new Appointment(id, title, description, "location", type, cbContact.getSelectionModel().getSelectedItem(), startDate, endDate, customerId);
                apptList.add(appt);
            }
        }
        apptList.sort((a,b) -> {
            if(a.getStartDateTime().isBefore(b.getStartDateTime())){return -1;}
            else if(a.getStartDateTime().isAfter(b.getStartDateTime())){return 1;}
            else{return 0;}
        });
        tvAppointments.setItems(apptList);
    }
}
