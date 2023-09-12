package scheduler;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import javax.xml.transform.Result;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Controller class used to interact and add functionality to the elements in
 * addAppointment.fxml form.
 */
public class AddAppointmentController implements Initializable {
    public Button btnAdd;
    public Button btnCancel;
    public RadioButton rbEndPM;
    public ToggleGroup endTimeToggleGroup;
    public RadioButton rbEndAM;
    public ComboBox<Integer> cbEndMinute;
    public ComboBox<Integer> cbEndHour;
    public RadioButton rbStartPM;
    public ToggleGroup startTimeToggleGroup;
    public RadioButton rbStartAM;
    public ComboBox<Integer> cbStartMinute;
    public ComboBox<Integer> cbStartHour;
    public DatePicker datepickerDate;
    public TextField txtLocation;
    public TextField txtDescription;
    public ComboBox<String> cbContact;
    public ComboBox<String> cbCustomer;
    public TextField txtType;
    public TextField txtTitle;
    public ComboBox<Integer> cbUserID;
    private HomeMenuController homeMenuController;

    public void setHomeMenuController(HomeMenuController controller){
        this.homeMenuController = controller;
    }

    /**
     * Populates the time boxes with numbers to select from, and the customer and contact combo boxes when
     * this form is created.
     * @param url not used.
     * @param resourceBundle not used.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        StringConverter<Integer> twoDigit = new StringConverter<Integer>() {
            @Override
            public String toString(Integer integer) {
                return String.format("%02d", integer);
            }

            @Override
            public Integer fromString(String s) {
                return Integer.parseInt(s);
            }
        };
        cbStartHour.setConverter(twoDigit);
        cbStartMinute.setConverter(twoDigit);
        cbEndHour.setConverter(twoDigit);
        cbEndMinute.setConverter(twoDigit);

        cbStartHour.setItems(IntStream.rangeClosed(1,12).boxed().collect(Collectors.toCollection(FXCollections::observableArrayList)));
        cbStartHour.getSelectionModel().selectFirst();
        cbStartMinute.setItems(IntStream.rangeClosed(0,59).boxed().collect(Collectors.toCollection(FXCollections::observableArrayList)));
        cbStartMinute.getSelectionModel().selectFirst();
        cbEndHour.setItems(IntStream.rangeClosed(1,12).boxed().collect(Collectors.toCollection(FXCollections::observableArrayList)));
        cbEndHour.getSelectionModel().selectFirst();
        cbEndMinute.setItems(IntStream.rangeClosed(0,59).boxed().collect(Collectors.toCollection(FXCollections::observableArrayList)));
        cbEndMinute.getSelectionModel().selectFirst();
        fillCustomerList();
        fillContactList();
        fillUserIDList();
    }

    /**
     * Populates the Customer combo box with all current customers in the database.
     */
    public void fillCustomerList(){
        Connection conn = DatabaseConnection.getConnection();
        var sql = "SELECT Customer_name FROM customers";
        ObservableList<String> list = FXCollections.observableArrayList();
        try(var ps = conn.prepareStatement(sql)){
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                list.add(rs.getString("Customer_Name"));
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        cbCustomer.setItems(list);
        cbCustomer.getSelectionModel().selectFirst();
    }

    /**
     * Populates the Contact combo box with all the current contacts in the database.
     */
    public void fillContactList(){
        Connection conn = DatabaseConnection.getConnection();
        var sql = "SELECT Contact_Name FROM contacts";
        ObservableList<String> list = FXCollections.observableArrayList();
        try(var ps = conn.prepareStatement(sql)){
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                list.add(rs.getString("Contact_Name"));
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        cbContact.setItems(list);
        cbContact.getSelectionModel().selectFirst();
    }

    /**
     * Closes the current window without doing anything else.
     */
    public void btnCancelAction(){
       Stage stage = (Stage) btnCancel.getScene().getWindow();
       stage.close();
    }

    /**
     * Checks all the text fields to see if they are empty.
     * @return returns true if all text fields have text in them, and false if any have nothing or only white space.
     */
    public boolean elementsNotEmpty(){
        boolean notEmpty = true;
        if (txtDescription.getText().trim().isBlank()){
            notEmpty = false;
        }
        if (txtLocation.getText().trim().isBlank()){
            notEmpty = false;
        }
        if (txtTitle.getText().trim().isBlank()){
            notEmpty = false;
        }
        if (txtType.getText().trim().isBlank()){
            notEmpty = false;
        }
        if (datepickerDate.getValue() == null){
            notEmpty = false;
        }
        return notEmpty;
    }

    /**
     * Adds the appointment to the database when the button is pressed and then closes the form and updates the tables.
     * Fails if there is an empty text box, the time conflicts with another appointment or if it is not during operating hours.
     * Shows an alert corresponding for to the reason for failure.
     * @throws SQLException thrown if there is an issue of the SQL or Database connection in any of the methods called within.
     */
    public void btnAddAction() throws SQLException {
        if(elementsNotEmpty()){
            if(createEndDateTime().isAfter(createStartDateTime())){
                if(isDuringOpenHours()){
                    if(isNotOverlapping()){
                        addAppointmentToDatabase();
                        homeMenuController.radioButtonToggleAction();
                        btnCancelAction();
                    }
                    else{
                        Alert overlapAlert = new Alert(Alert.AlertType.WARNING);
                        overlapAlert.setHeaderText("Overlapping Appointments");
                        overlapAlert.setContentText("Your proposed times conflict with an existing appointment's meeting time.\r\n" +
                                "Please select another time and try again.");
                        overlapAlert.showAndWait();
                    }
                }
                else{
                    Alert notOpenAlert = new Alert(Alert.AlertType.WARNING);
                    notOpenAlert.setHeaderText("Past Operating Hours");
                    String opening = Appointment.getZonedOpenTime().plusMinutes(1).format(DateTimeFormatter.ofPattern("hh:mm a"));
                    String closing = Appointment.getZonedCloseTime().format(DateTimeFormatter.ofPattern("hh:mm a"));
                    notOpenAlert.setContentText("The Company is not open during the times you selected.\r\n" +
                            "The operating hours are between "+ opening + " and " + closing + ".\r\n" +
                            "Please select another time and try again.");
                    notOpenAlert.showAndWait();
                }
            }
            else{
                Alert badEndAlert = new Alert(Alert.AlertType.WARNING);
                badEndAlert.setHeaderText("End time is before Start time");
                badEndAlert.setContentText("Your selected ending time has to be after the chosen starting time.\r\n" +
                        "Please revise your selection and try again.");
                badEndAlert.showAndWait();
            }
        }
        else{
            Alert emptyAlert = new Alert(Alert.AlertType.WARNING);
            emptyAlert.setHeaderText("Empty Text Field");
            emptyAlert.setContentText("Please make sure all text boxes have a valid entry in them.");
            emptyAlert.showAndWait();
        }
    }

    /**
     * Adds the given information into the Database in the appointments table.
     * @throws SQLException thrown if there is an issue with SQL syntax of connection to Database.
     */
    public void addAppointmentToDatabase() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        var insertSQL = "INSERT INTO appointments VALUES(0, ?, ?, ?, ?, ?, ?, NOW(), ?, NOW(), ?, ?, ?, ?)";
        try(var insertPS = conn.prepareStatement(insertSQL)){
            int id;
            var customerSQL = "SELECT Customer_ID FROM customers WHERE Customer_Name = ?";
            try(var customerPS = conn.prepareStatement(customerSQL)){
                customerPS.setString(1, cbCustomer.getSelectionModel().getSelectedItem());
                ResultSet customerRS = customerPS.executeQuery();
                customerRS.next();
                id = customerRS.getInt("Customer_ID");
            }
            Appointment appt = new Appointment(0, txtTitle.getText(), txtDescription.getText(), txtLocation.getText(), txtType.getText(),
                    cbContact.getSelectionModel().getSelectedItem(), createStartDateTime(), createEndDateTime(), id);
            insertPS.setString(1, appt.getTitle());
            insertPS.setString(2, appt.getDescription());
            insertPS.setString(3, appt.getLocation());
            insertPS.setString(4, appt.getType());
            insertPS.setString(5, createStartDateTime().withZoneSameInstant(ZoneId.of("UTC")).format(Appointment.getDtf()));
            insertPS.setString(6, createEndDateTime().withZoneSameInstant(ZoneId.of("UTC")).format(Appointment.getDtf()));
            insertPS.setString(7, CurrentUser.getUsername());
            insertPS.setString(8, CurrentUser.getUsername());
            insertPS.setInt(9, appt.getCustomerId());
            insertPS.setInt(10, cbUserID.getSelectionModel().getSelectedItem());
            insertPS.setInt(11, appt.getContactID());
            insertPS.executeUpdate();
        }
    }

    /**
     * Combines all the selected times to create a ZonedDateTime for the starting time, set in the user's ZoneID.
     * @return the ZonedDateTime for the selected starting time elements.
     */
    public ZonedDateTime createStartDateTime(){
        LocalDate date = datepickerDate.getValue();
        String hour = String.format("%02d", cbStartHour.getSelectionModel().getSelectedItem());
        String minute = String.format("%02d", cbStartMinute.getSelectionModel().getSelectedItem());
        String ampm = "AM";
        if(rbStartPM.isSelected()){
            ampm = "PM";
        }
        if(rbStartAM.isSelected()){
            ampm = "AM";
        }
        LocalTime time = LocalTime.parse(hour + ":" + minute + ":00 " + ampm, DateTimeFormatter.ofPattern("hh:mm:ss a", Locale.getDefault()));
        return ZonedDateTime.of(date, time, CurrentUser.getZoneId());
    }

    /**
     * Combines all the selected times to create a ZonedDateTime for the ending time, set in the user's ZoneID.
     * @return the ZonedDateTime for the selected ending time elements.
     */
    public ZonedDateTime createEndDateTime(){
        LocalDate date = datepickerDate.getValue();
        String hour = String.format("%02d", cbEndHour.getSelectionModel().getSelectedItem());
        String minute = String.format("%02d", cbEndMinute.getSelectionModel().getSelectedItem());
        String ampm = "AM";
        if(rbEndPM.isSelected()){
            ampm = "PM";
        }
        if(rbStartAM.isSelected()){
            ampm = "AM";
        }
        LocalTime time = LocalTime.parse(hour + ":" + minute + ":00 " + ampm, DateTimeFormatter.ofPattern("hh:mm:ss a", Locale.getDefault()));
        return ZonedDateTime.of(date, time, CurrentUser.getZoneId());
    }

    /**
     * Checks to see if the start time and end time selected by the user are during the companies open hours.
     * @return returns true if it is during open time, false if not.
     */
    public boolean isDuringOpenHours(){
        boolean open = false;
        if(Appointment.isOpenDuringTime(createStartDateTime()) && Appointment.isOpenDuringTime(createEndDateTime())){
            open = true;
        }
        return open;
    }

    /**
     * Checks to see if the entered times overlap with any existing appointments.
     * @return returns true if there are no overlapping appointments, and returns false if there is an overlap.
     * @throws SQLException thrown when there is an issue with connection to Database or with SQL statement execution.
     */
    public boolean isNotOverlapping() throws SQLException {
        boolean notOverlapping = true;
        Connection conn = DatabaseConnection.getConnection();
        var sql = "SELECT Start, End FROM appointments WHERE Customer_ID IN (?)";
        try(var ps = conn.prepareStatement(sql)){
            ps.setInt(1,Customer.getCustomerIDFromName(cbCustomer.getSelectionModel().getSelectedItem()));
            ResultSet rs = ps.executeQuery();
            ZonedDateTime start = createStartDateTime().plusSeconds(1);
            ZonedDateTime end = createEndDateTime().minusSeconds(1);
            while(rs.next()){
                ZonedDateTime startAppt = ZonedDateTime.of(rs.getDate("Start").toLocalDate(), rs.getTime("Start").toLocalTime(), ZoneId.of("UTC")).withZoneSameInstant(CurrentUser.getZoneId());
                ZonedDateTime endAppt = ZonedDateTime.of(rs.getDate("End").toLocalDate(), rs.getTime("End").toLocalTime(), ZoneId.of("UTC")).withZoneSameInstant(CurrentUser.getZoneId());
                if (start.isAfter(startAppt) && start.isBefore(endAppt)){
                    notOverlapping = false;
                }
                if (end.isAfter(startAppt) && end.isBefore(endAppt)){
                    notOverlapping = false;
                }
            }
        }
        return notOverlapping;
    }

    /**
     * Fills the User ID combo box with all the user id's from the users table in the database.
     */
    public void fillUserIDList(){
        Connection conn = DatabaseConnection.getConnection();
        var sql = "SELECT User_ID FROM users";
        ObservableList<Integer> idList = FXCollections.observableArrayList();
        try(var ps = conn.prepareStatement(sql)){
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                idList.add(rs.getInt("User_ID"));
            }
            cbUserID.setItems(idList);
            cbUserID.getSelectionModel().selectFirst();
        }
        catch (SQLException e){
            e.printStackTrace();
        }
    }
}
