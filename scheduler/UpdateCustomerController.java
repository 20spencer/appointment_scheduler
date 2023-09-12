package scheduler;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * The UpdateCustomerController class is used to interact with the
 * updateCustomer.fxml form and add functionality to it's elements.
 */
public class UpdateCustomerController implements Initializable {
    public TextField txtName;
    public ComboBox<String> cbCountry;
    public ComboBox<String> cbRegion;
    public TextField txtAddress;
    public TextField txtPostalCode;
    public TextField txtPhone;
    public Button btnCancel;
    public Button btnUpdate;
    public TextField txtCustomerID;
    private Customer customer;
    private HomeMenuController homeMenuController;

    /**
     * Sets the current customer being updated.
     *
     * @param customer Selected customer to be updated.
     */
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    /**
     * Stores the Controller of the main window so this form can call methods to update the tables.
     *
     * @param controller the controller of the main window.
     */
    public void setHomeMenuController(HomeMenuController controller) {
        this.homeMenuController = controller;
    }

    /**
     * When the form is opened this populates the Country combo box so that the values can be
     * preselected.
     * @param url not used.
     * @param resourceBundle not used.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Connection conn = DatabaseConnection.getConnection();
        ObservableList<String> countryList = FXCollections.observableArrayList();
        var countries = "SELECT Country FROM countries";

        try (var countryPS = conn.prepareStatement(countries)) {
            ResultSet countryResults = countryPS.executeQuery();
            while (countryResults.next()) {
                String ct = countryResults.getString("Country");
                countryList.add(ct);
            }
            cbCountry.setItems(countryList);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * Populates all the UI elements with the current data of the customer to be updated.
     * @throws SQLException thrown when there is issue with Database connection or SQL statement.
     */
    public void fillAllFields() throws SQLException {
        Integer id = customer.getId();
        txtCustomerID.setText(id.toString());
        txtName.setText(customer.getName());
        txtAddress.setText(customer.getAddress());
        txtPhone.setText(customer.getPhone());
        txtPostalCode.setText(customer.getPostalCode());
        cbCountry.getSelectionModel().select(customer.getCountry());
        populateRegions();
        cbRegion.getSelectionModel().select(customer.getRegion());
    }
    /**
     * Fills the region combo box based on the country selected.
     * @throws SQLException thrown if there is issues with connecting to database or executing SQL statement.
     */
    public void populateRegions() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        if (cbCountry.getSelectionModel().getSelectedItem() != null) {
            int id;
            ObservableList<String> regionList = FXCollections.observableArrayList();
            var idSQL = "SELECT Country_ID FROM countries WHERE Country = ?";
            var regionSQL = "SELECT Division FROM first_level_divisions WHERE COUNTRY_ID = ?";
            String selectedCountry = cbCountry.getSelectionModel().getSelectedItem();
            try (var idPS = conn.prepareStatement(idSQL)) {
                idPS.setString(1, selectedCountry);
                ResultSet idResult = idPS.executeQuery();
                idResult.next();
                id = idResult.getInt("Country_ID");
                try (var regionPS = conn.prepareStatement(regionSQL)) {
                    regionPS.setInt(1, id);
                    ResultSet regionResults = regionPS.executeQuery();
                    while (regionResults.next()) {
                        regionList.add(regionResults.getString("Division"));
                    }
                    cbRegion.setItems(regionList);
                    cbRegion.getSelectionModel().selectFirst();
                }
            }
        }
    }
    /**
     * Closes the update customer window.
     */
    public void btnCancelAction() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    /**
     * Updates the customer at the selected Customer_ID in the customers table to the new values typed in by
     * the user.
     * @throws SQLException thrown when there is SQL syntax issues or issue with the Database Connection.
     */
    public void updateCustomer() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        String user = CurrentUser.getUsername();
        var customerSQL = "UPDATE customers SET Customer_Name = ?, Address = ?, Postal_Code = ?, Phone = ?, " +
                "Last_Update = NOW(), Last_Updated_By = ?, Division_ID = ? WHERE Customer_ID = ?";
        int divID;
        var divSQL = "SELECT Division_ID FROM first_level_divisions WHERE Division = ?";
        String div = cbRegion.getSelectionModel().getSelectedItem();

        try(var divPS = conn.prepareStatement(divSQL)){
            divPS.setString(1, div);
            ResultSet divResult = divPS.executeQuery();
            divResult.next();
            divID = divResult.getInt("Division_ID");
            try(var customerPS = conn.prepareStatement(customerSQL)){
                String name = txtName.getText();
                String address = txtAddress.getText();
                String postal = txtPostalCode.getText();
                String phone = txtPhone.getText();
                int id = Integer.parseInt(txtCustomerID.getText());
                customerPS.setString(1, name);
                customerPS.setString(2, address);
                customerPS.setString(3, postal);
                customerPS.setString(4, phone);
                customerPS.setString(5, user);
                customerPS.setInt(6, divID);
                customerPS.setInt(7, id);
                customerPS.executeUpdate();
            }
        }
    }

    /**
     * updates the customer to new values, refreshes the table view in the main window, and closes the update form.
     * If there is an empty text field, then it prompts the user to enter something into all of them.
     * @throws SQLException thrown when there is an issue connecting to the database.
     */
    public void btnUpdateAction() throws SQLException {
        if(elementsNotEmpty()){
            updateCustomer();
            homeMenuController.updateCustomersTable();
            btnCancelAction();
        }
        else{
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Empty Text Field");
            alert.setContentText("Please make sure all text boxes have a valid entry in them.");
            alert.showAndWait();
        }
    }


    /**
     * Checks all the text fields to see if they are empty.
     * @return returns true if all text fields have text in them, and false if any have nothing or only white space.
     */
    public boolean elementsNotEmpty(){
        boolean notEmpty = true;
        if (txtName.getText().trim().isBlank()){
            notEmpty = false;
        }
        if (txtPhone.getText().trim().isBlank()){
            notEmpty = false;
        }
        if (txtPostalCode.getText().trim().isBlank()){
            notEmpty = false;
        }
        if (txtAddress.getText().trim().isBlank()){
            notEmpty = false;
        }
        return notEmpty;
    }
}
