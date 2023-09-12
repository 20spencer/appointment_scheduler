package scheduler;

import com.mysql.jdbc.Connection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * The HomeMenuController class is used to interact with the
 * homeMenu.fxml form and add functionality to it's elements.
 */
public class HomeMenuController implements Initializable {


    public RadioButton rbWeek;
    public RadioButton rbMonth;
    public RadioButton rbAll;
    public Label lblCustomerMessage;
    public Button btnCustomerAdd;
    public Button btnCustomerDelete;
    public Button btnCustomerUpdate;
    public Label lblAppointmentMessage;
    public Button btnAppointmentAdd;
    public Button btnAppointmentDelete;
    public Button btnAppointmentUpdate;
    public TableView<Customer> tvCustomers;
    public TableColumn colCustomerId;
    public TableColumn colCustomerName;
    public TableColumn colCustomerAddress;
    public TableColumn colCustomerPostalCode;
    public TableColumn colCustomerPhone;
    public TableColumn colCustomerRegion;
    public TableView<Appointment> tvAppointments;
    public TableColumn colApptId;
    public TableColumn colApptTitle;
    public TableColumn colApptDescription;
    public TableColumn colApptLocation;
    public TableColumn colApptContact;
    public TableColumn colApptType;
    public TableColumn colApptStart;
    public TableColumn colApptEnd;
    public TableColumn colApptCustomerId;
    public ToggleGroup toggleGroupFilter;
    public Button btnReports;

    /**
     * initialize is used to set the Columns CellValues so that they will correctly sort
     * and display the information in the table views.
     * @param url .
     * @param resourceBundle .
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colCustomerId.setCellValueFactory(new PropertyValueFactory<Customer, Integer>("id"));
        colCustomerName.setCellValueFactory(new PropertyValueFactory<Customer, String>("name"));
        colCustomerAddress.setCellValueFactory(new PropertyValueFactory<Customer, String>("address"));
        colCustomerPostalCode.setCellValueFactory(new PropertyValueFactory<Customer, String>("postalCode"));
        colCustomerPhone.setCellValueFactory(new PropertyValueFactory<Customer, String>("phone"));
        colCustomerRegion.setCellValueFactory(new PropertyValueFactory<Customer, String>("region"));

        colApptId.setCellValueFactory(new PropertyValueFactory<Appointment, Integer>("id"));
        colApptTitle.setCellValueFactory(new PropertyValueFactory<Appointment, String>("title"));
        colApptDescription.setCellValueFactory(new PropertyValueFactory<Appointment, String>("description"));
        colApptLocation.setCellValueFactory(new PropertyValueFactory<Appointment, String>("location"));
        colApptContact.setCellValueFactory(new PropertyValueFactory<Appointment, String>("contact"));
        colApptType.setCellValueFactory(new PropertyValueFactory<Appointment, String>("type"));
        colApptStart.setCellValueFactory(new PropertyValueFactory<Appointment, String>("start"));
        colApptEnd.setCellValueFactory(new PropertyValueFactory<Appointment, String>("end"));
        colApptCustomerId.setCellValueFactory(new PropertyValueFactory<Appointment, Integer>("customerId"));

    }

    /**
     * updateCustomersTable connects to the database, gathers all the data from the customers table needed to
     * display on the TableView, iterates through the results and puts them in a list to populate the table.
     * @throws SQLException if there's trouble connecting to the database or executing the SQL then this will be thrown.
     */
    public void updateCustomersTable() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        var customerSQL = "SELECT Customer_ID, Customer_Name, Address, Postal_Code, Phone, Division_ID FROM customers";
        var divisionSQL = "SELECT Division FROM first_level_divisions WHERE Division_ID = ?";
        ObservableList<Customer> customerList = FXCollections.observableArrayList();
        try (var customerPS = conn.prepareStatement(customerSQL)){
            ResultSet customerRS = customerPS.executeQuery();

            while (customerRS.next()){
                int id = customerRS.getInt("Customer_ID");
                String name = customerRS.getString("Customer_Name");
                String address = customerRS.getString("Address");
                String postalCode = customerRS.getString("Postal_Code");
                String phone = customerRS.getString("Phone");
                int div = customerRS.getInt("Division_ID");
                String region;
                try (var divisionPS = conn.prepareStatement(divisionSQL)){
                    divisionPS.setInt(1, div);
                    ResultSet rs = divisionPS.executeQuery();
                    rs.next();
                    region = rs.getString("Division");
                }
                Customer customer = new Customer(id, name, address, postalCode, phone, region);
                customerList.add(customer);
            }
        }
        tvCustomers.setItems(customerList);

    }

    /**
     * fills/updates the appointments table by iterating through the appropriate columns in the appointments table of the
     * database, and filters them based on the radio button selected.
     * @param filter custom Enum value to determine how to filter the table.
     * @throws SQLException if there is an issue with connection or SQL statement this will be thrown.
     */
    public void updateAppointmentsTable(ViewBy filter) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        var contactSQL = "SELECT Contact_Name FROM contacts WHERE Contact_ID = ?";
        ObservableList<Appointment> apptList = FXCollections.observableArrayList();
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
                ZonedDateTime currentDateTime = ZonedDateTime.now();
                var dayOfWeek = currentDateTime.getDayOfWeek();
                LocalDate currentDate = currentDateTime.toLocalDate();

                switch (filter) {
                    case ALL:
                        apptList.add(appt);
                        break;
                    case MONTH:
                        if(currentDateTime.getMonth().equals(appt.getStartDateTime().getMonth()) && currentDateTime.getYear() == appt.getStartDateTime().getYear()){
                            apptList.add(appt);
                        }
                        break;
                    case WEEK:
                        LocalDate endOfWeek;
                        LocalDate startOfWeek;
                        LocalDate currentDay = appt.getStartLocalDate();
                        switch (dayOfWeek){
                            case MONDAY:
                                endOfWeek = currentDate.plusDays(7);
                                startOfWeek = currentDate.minusDays(1);
                                if(currentDay.isAfter(startOfWeek) && currentDay.isBefore(endOfWeek)){
                                    apptList.add(appt);
                                }
                                break;
                            case TUESDAY:
                                endOfWeek = currentDate.plusDays(6);
                                startOfWeek = currentDate.minusDays(2);
                                if(currentDay.isAfter(startOfWeek) && currentDay.isBefore(endOfWeek)){
                                    apptList.add(appt);
                                }
                                break;
                            case WEDNESDAY:
                                endOfWeek = currentDate.plusDays(5);
                                startOfWeek = currentDate.minusDays(3);
                                if(currentDay.isAfter(startOfWeek) && currentDay.isBefore(endOfWeek)){
                                    apptList.add(appt);
                                }
                                break;
                            case THURSDAY:
                                endOfWeek = currentDate.plusDays(4);
                                startOfWeek = currentDate.minusDays(4);
                                if(currentDay.isAfter(startOfWeek) && currentDay.isBefore(endOfWeek)){
                                    apptList.add(appt);
                                }
                                break;
                            case FRIDAY:
                                endOfWeek = currentDate.plusDays(3);
                                startOfWeek = currentDate.minusDays(5);
                                if(currentDay.isAfter(startOfWeek) && currentDay.isBefore(endOfWeek)){
                                    apptList.add(appt);
                                }
                                break;
                            case SATURDAY:
                                endOfWeek = currentDate.plusDays(2);
                                startOfWeek = currentDate.minusDays(6);
                                if(currentDay.isAfter(startOfWeek) && currentDay.isBefore(endOfWeek)){
                                    apptList.add(appt);
                                }
                                break;
                            case SUNDAY:
                                endOfWeek = currentDate.plusDays(1);
                                startOfWeek = currentDate.minusDays(7);
                                if(currentDay.isAfter(startOfWeek) && currentDay.isBefore(endOfWeek)){
                                    apptList.add(appt);
                                }
                                break;
                        }
                        break;
                }
            }
        }
        tvAppointments.setItems(apptList);
    }

    /**
     * Method to update both the table views at once using the other methods.
     * @throws SQLException if there is a connection issue with the Database this will be thrown.
     */
    public void updateBothTables() throws SQLException {
        updateCustomersTable();
        radioButtonToggleAction();
    }

    /**
     * This method occurs when a different radio button is selected, and will reupdate the table
     * based on which radio button was chosen.
     * @throws SQLException throws if there is issues with commands or connection to the database.
     */
    public void radioButtonToggleAction() throws SQLException {
        if(rbWeek.isSelected()){
            updateAppointmentsTable(ViewBy.WEEK);
        }
        if(rbMonth.isSelected()){
            updateAppointmentsTable(ViewBy.MONTH);
        }
        if(rbAll.isSelected()){
            updateAppointmentsTable(ViewBy.ALL);
        }
    }

    /**
     * opens the add customer form.
     * @throws IOException if there is an issue getting the fxml file this will be thrown.
     */
    public void btnCustomerAddAction() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("addCustomer.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Add Customer");
        stage.setScene( new Scene(loader.load()));
        AddCustomerController controller = loader.getController();
        controller.setHomeMenuController(this);
        stage.showAndWait();
    }

    /**
     * Opens the update customer form and passes this controller and the selected customer to the updateCustomerController.
     * If there is nothing selected in the Table, then it shows an alert box.
     * @throws IOException if there is trouble getting the fxml file then this is thrown.
     * @throws SQLException if there is issue with Database connection or SQL statements this is thrown.
     */
    public void btnCustomerUpdateAction() throws IOException, SQLException {
        if(tvCustomers.getSelectionModel().getSelectedItem() != null){
            FXMLLoader loader = new FXMLLoader(getClass().getResource("updateCustomer.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Update Customer");
            stage.setScene(new Scene(loader.load()));
            UpdateCustomerController controller = loader.getController();
            controller.setHomeMenuController(this);
            var customer = tvCustomers.getSelectionModel().getSelectedItem();
            controller.setCustomer(customer);
            controller.fillAllFields();
            stage.showAndWait();
        }
        else{
            noCustomerSelectedAlert();
        }
    }

    /**
     * Checks if a customer is selected, and then checks if the customer has any existing appointments scheduled.
     * If both checks are passed, then the customer is deleted and a message is displayed. Otherwise an alert box
     * pops up to inform the user.
     * @throws SQLException thrown when there are issues connecting to the database.
     */
    public void btnCustomerDeleteAction() throws SQLException {
        if(tvCustomers.getSelectionModel().getSelectedItem() != null){
            Connection conn = DatabaseConnection.getConnection();
            Customer customer = tvCustomers.getSelectionModel().getSelectedItem();
            var apptSQL = "SELECT Title FROM appointments WHERE Customer_ID = ?";
            int rows = 0;
            try(var apptPS = conn.prepareStatement(apptSQL)){
                apptPS.setInt(1, customer.getId());
                String exists = null;
                ResultSet apptRS = apptPS.executeQuery();
                while (apptRS.next()){
                    exists = apptRS.getString("Title");
                    rows++;
                }
                if(exists == null){
                    var sql = "DELETE FROM customers WHERE Customer_ID = ?";
                    try(var ps = conn.prepareStatement(sql)){
                        ps.setInt(1, customer.getId());
                        int result = ps.executeUpdate();
                        if(result > 0){
                            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh:mm:ss a");
                            lblCustomerMessage.setText(LocalTime.now().format(dtf) + ": Customer \"" + customer.getName() +"\" has been deleted.");
                        }
                        updateCustomersTable();
                    }
                }
                else{
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setHeaderText("Customer has active appointment(s).");
                    alert.setContentText(customer.getName() + " has " + rows +" active appointment(s).\r\nPlease remove" +
                            " them before you delete this customer.");
                    alert.showAndWait();
                }
            }
        }
        else{
            noCustomerSelectedAlert();
        }
    }

    /**
     * Pops up an alert box when a button is pressed that interacts with a current customer, but
     * none have been selected.
     */
    public void noCustomerSelectedAlert(){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText("No Customer Selected");
        alert.setContentText("Please select a valid customer from the table.");
        alert.showAndWait();
    }

    /**
     * Shows an alert box when the user tries to update or delete an appointment and hasn't selected one.
     */
    public void noAppointmentSelectedAlert(){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText("No Appointment Selected");
        alert.setContentText("Please select a valid appointment from the table.");
        alert.showAndWait();
    }

    /**
     * Opens the Add Appointment form.
     * @throws IOException thrown if there is an issue obtaining the fxml file.
     */
    public void btnAppointmentAddAction() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("addAppointment.fxml"));
        Stage stage = new Stage();
        stage.setScene(new Scene(loader.load()));
        stage.setTitle("Add Appointment");
        AddAppointmentController controller = loader.getController();
        controller.setHomeMenuController(this);
        stage.showAndWait();
    }

    /**
     * Opens the update appointment form, and fills all the fields with the selected appointment's current information.
     * If there isn't any currently selected item, then an alert box pops up instead.
     * @throws IOException thrown if there is an issue with obtaining the fxml file.
     * @throws SQLException thrown if there is an issue with database connection or sql execution.
     */
    public void btnAppointmentUpdateAction() throws IOException, SQLException {
       if(tvAppointments.getSelectionModel().getSelectedItem() != null) {
           FXMLLoader loader = new FXMLLoader(getClass().getResource("updateAppointment.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Update Appointment");
            UpdateAppointmentController controller = loader.getController();
            controller.setHomeMenuController(this);
            controller.setSelectedAppointment(tvAppointments.getSelectionModel().getSelectedItem());
            controller.fillAllFields();
            stage.showAndWait();
        }
       else{
           noAppointmentSelectedAlert();
       }
    }

    /**
     * Deletes the selected appointment from the table and the database, and displays a message and timestamp of
     * when and which appointment was deleted. If nothing is selected then a message box pops up telling the user to
     * select something first.
     * @throws SQLException thrown if there is an issue connecting to the database or with SQL execution.
     */
    public void btnAppointmentDeleteAction() throws SQLException {
        if(tvAppointments.getSelectionModel().getSelectedItem() != null){
            Connection conn = DatabaseConnection.getConnection();
            var sql = "DELETE FROM appointments WHERE Appointment_ID = ?";
            try(var ps = conn.prepareStatement(sql)){
                Appointment appt = tvAppointments.getSelectionModel().getSelectedItem();
                ps.setInt(1, appt.getId());
                int result = ps.executeUpdate();
                if(result > 0) {
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh:mm:ss a");
                    lblAppointmentMessage.setText(LocalTime.now().format(dtf) + ": \"" + appt.getTitle() + "\" - " + appt.getType() + " ID: " + appt.getId() + "  has been deleted.");
                }
            }
            radioButtonToggleAction();
        }
        else{
            noAppointmentSelectedAlert();
        }
    }

    /**
     * Opens the report menu form, where the user can select a specific kind of report they would like to see.
     * @throws IOException thrown if there is an issue obtaining the reportForm.fxml file.
     */
    public void btnReportsAction() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("reportForm.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Choose a Report");
        stage.setScene(new Scene(loader.load()));
        stage.show();
    }
}
