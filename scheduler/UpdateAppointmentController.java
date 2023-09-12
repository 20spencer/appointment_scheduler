package scheduler;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Controller class for the updateAppointment.fxml form.
 */
public class UpdateAppointmentController implements Initializable {

    public TextField txtApptID;
    public TextField txtTitle;
    public TextField txtDescription;
    public TextField txtLocation;
    public TextField txtType;
    public DatePicker datepickerDate;
    public ComboBox<Integer> cbStartHour;
    public ComboBox<Integer> cbStartMinute;
    public RadioButton rbStartAM;
    public ToggleGroup startTimeToggleGroup;
    public RadioButton rbStartPM;
    public ComboBox<Integer> cbEndHour;
    public ComboBox<Integer> cbEndMinute;
    public RadioButton rbEndAM;
    public ToggleGroup endTimeToggleGroup;
    public RadioButton rbEndPM;
    public ComboBox<String> cbCustomer;
    public ComboBox<String> cbContact;
    public Button btnCancel;
    public Button btnUpdate;
    public ComboBox<Integer> cbUserID;
    private Appointment selectedAppointment;
    private HomeMenuController homeMenuController;

    /**
     * Sets the homeMenuController.
     * @param controller the controller from the main window.
     */
    public void setHomeMenuController(HomeMenuController controller){
        this.homeMenuController = controller;
    }

    /**
     * Sets the appointment value to the appointment selected in the tableview, used to fill the text fields.
     * @param appointment the selected appointment to update.
     */
    public void setSelectedAppointment(Appointment appointment){
        this.selectedAppointment = appointment;
    }

    /**
     * Closes the current window.
     */
    public void closeWindow(){
       Stage stage = (Stage) btnCancel.getScene().getWindow();
       stage.close();
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
     * Uses the selected Appointment from the table to pre-fill all the values in the form.
     * @throws SQLException thrown if there is an issue with some of the methods called within that connect to the database.
     */
    public void fillAllFields() throws SQLException {
        Integer id = selectedAppointment.getId();
        txtApptID.setText(id.toString());
        txtDescription.setText(selectedAppointment.getDescription());
        txtTitle.setText(selectedAppointment.getTitle());
        txtLocation.setText(selectedAppointment.getLocation());
        txtType.setText(selectedAppointment.getType());
        cbContact.getSelectionModel().select(selectedAppointment.getContact());
        cbCustomer.getSelectionModel().select(selectedAppointment.getCustomerName());
        LocalDateTime startTimeFull = LocalDateTime.parse(selectedAppointment.getStart(), Appointment.getDtf());
        LocalDateTime endTimeFull = LocalDateTime.parse(selectedAppointment.getEnd(), Appointment.getDtf());
        cbUserID.getSelectionModel().select(Integer.valueOf(selectedAppointment.getUserId()));
        int startHour = startTimeFull.getHour();
        int startMinute = startTimeFull.getMinute();
        int endHour = endTimeFull.getHour();
        int endMinute = endTimeFull.getMinute();
        cbStartMinute.getSelectionModel().select(startMinute);
        cbEndMinute.getSelectionModel().select(endMinute);
        datepickerDate.setValue(startTimeFull.toLocalDate());
        if(startHour == 0){
            cbStartHour.getSelectionModel().select(Integer.valueOf(12));
        }
        else if(startHour <= 12){
            cbStartHour.getSelectionModel().select(startHour-1);
        }
        else {
            cbStartHour.getSelectionModel().select(startHour-13);
        }

        if(endHour == 0){
            cbEndHour.getSelectionModel().select(Integer.valueOf(12));
        }
        else if(endHour <= 12){
            cbEndHour.getSelectionModel().select(endHour-1);
        }
        else {
            cbEndHour.getSelectionModel().select(startHour-13);
        }

        if(startTimeFull.getHour() < 12){
            rbStartAM.setSelected(true);
        }else{
            rbStartPM.setSelected(true);
        }
        if(endTimeFull.getHour() < 12){
            rbEndAM.setSelected(true);
        }else{
            rbEndPM.setSelected(true);
        }
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
     * Checks to see if the entered times overlap with any existing appointments, aside from the selected one.
     * @return returns true if there are no overlapping appointments, and returns false if there is an overlap.
     * @throws SQLException thrown when there is an issue with connection to Database or with SQL statement execution.
     */
    public boolean isNotOverlapping() throws SQLException {
        boolean notOverlapping = true;
        Connection conn = DatabaseConnection.getConnection();
        var sql = "SELECT Start, End FROM appointments WHERE Appointment_ID NOT IN (?) AND Customer_ID IN (?)";
        try(var ps = conn.prepareStatement(sql)){
            ps.setInt(1,Integer.parseInt(txtApptID.getText()));
            ps.setInt(2,Customer.getCustomerIDFromName(cbCustomer.getSelectionModel().getSelectedItem()));
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
     * Adds the given information into the Database in the appointments table.
     * @throws SQLException thrown if there is an issue with SQL syntax of connection to Database.
     */
    public void updateAppointmentInDatabase() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        var updateSQL = "UPDATE appointments SET Title = ?, Description = ?, Location = ?, Type = ?, Start = ?, End = ?, Last_Update = NOW(), Last_Updated_By = ?, " +
                "Customer_ID = ?, User_ID = ?, Contact_ID = ? WHERE Appointment_ID = ?";
        try(var updatePS = conn.prepareStatement(updateSQL)){
            int id;
            var customerSQL = "SELECT Customer_ID FROM customers WHERE Customer_Name = ?";
            try(var customerPS = conn.prepareStatement(customerSQL)){
                customerPS.setString(1, cbCustomer.getSelectionModel().getSelectedItem());
                ResultSet customerRS = customerPS.executeQuery();
                customerRS.next();
                id = customerRS.getInt("Customer_ID");
            }
            Appointment appt = new Appointment(Integer.parseInt(txtApptID.getText()), txtTitle.getText(), txtDescription.getText(), txtLocation.getText(), txtType.getText(),
                    cbContact.getSelectionModel().getSelectedItem(), createStartDateTime(), createEndDateTime(), id);
            updatePS.setString(1, appt.getTitle());
            updatePS.setString(2, appt.getDescription());
            updatePS.setString(3, appt.getLocation());
            updatePS.setString(4, appt.getType());
            updatePS.setString(5, createStartDateTime().withZoneSameInstant(ZoneId.of("UTC")).format(Appointment.getDtf()));
            updatePS.setString(6, createEndDateTime().withZoneSameInstant(ZoneId.of("UTC")).format(Appointment.getDtf()));
            updatePS.setString(7, CurrentUser.getUsername());
            updatePS.setInt(8, appt.getCustomerId());
            updatePS.setInt(9, CurrentUser.getUserId());
            updatePS.setInt(10, appt.getContactID());
            updatePS.setInt(11, appt.getId());
            updatePS.executeUpdate();
        }
    }
    /**
     * Updates the appointment in the database when the button is pressed and then closes the form and updates the tables on the main window.
     * Fails if there is an empty text box, the time conflicts with another appointment or if it is not during operating hours.
     * Shows an alert corresponding for to the reason for failure.
     * @throws SQLException thrown if there is an issue of the SQL or Database connection in any of the methods called within.
     */
    public void btnUpdateAction() throws SQLException {
        if(elementsNotEmpty()){
            if(createEndDateTime().isAfter(createStartDateTime())){
                if(isDuringOpenHours()){
                    if(isNotOverlapping()){
                        updateAppointmentInDatabase();
                        homeMenuController.radioButtonToggleAction();
                        closeWindow();
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
