package scheduler;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * The AddCustomerController class is used to interact with the
 * addCustomer.fxml form and add functionality to it's elements.
 */
public class AddCustomerController implements Initializable {

    public TextField txtName;
    public ComboBox<String> cbCountry;
    public ComboBox<Region> cbRegion;
    public TextField txtAddress;
    public TextField txtPostalCode;
    public TextField txtPhone;
    public Button btnCancel;
    public Button btnAddCustomer;
    private HomeMenuController homeMenuController;

    /**
     * Sets the HomeMenuController so this form can call methods from the main window.
     * @param controller the Controller from the main window.
     */
    public void setHomeMenuController(HomeMenuController controller){
        this.homeMenuController = controller;
    }
    /**
     * Populates the combo boxes with country information when the form opens.
     * @param url not used.
     * @param resourceBundle not used.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Connection conn = DatabaseConnection.getConnection();
        ObservableList<String> countryList = FXCollections.observableArrayList();
        var countries = "SELECT Country FROM countries";

        try (var countryPS = conn.prepareStatement(countries)){
            ResultSet countryResults = countryPS.executeQuery();
            while (countryResults.next()){
                String ct = countryResults.getString("Country");
                countryList.add(ct);
            }
            cbCountry.setItems(countryList);
            cbCountry.getSelectionModel().selectFirst();
            populateRegions();
        }
        catch (SQLException e){
            e.printStackTrace();
        }
    }

    /**
     * Fills the region combo box based on the country selected.
     * The lambda expression is used to filter through the region list and make a new list with only ones matching the selected country.
     * @throws SQLException thrown if there is issues with connecting to database or executing SQL statement.
     */
    public void populateRegions() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        if (cbCountry.getSelectionModel().getSelectedItem() != null){
            int id;
            ObservableList<Region> regionList = FXCollections.observableArrayList();
            var idSQL = "SELECT Country_ID FROM countries WHERE Country = ?";
            var regionSQL = "SELECT Division, Division_ID, COUNTRY_ID FROM first_level_divisions";
            String selectedCountry = cbCountry.getSelectionModel().getSelectedItem();
            try(var idPS = conn.prepareStatement(idSQL)){
                idPS.setString(1, selectedCountry);
                ResultSet idResult = idPS.executeQuery();
                idResult.next();
                id = idResult.getInt("Country_ID");
                try (var regionPS = conn.prepareStatement(regionSQL)){
                    ResultSet regionResults = regionPS.executeQuery();
                    while(regionResults.next()){
                        regionList.add(new Region(regionResults.getString("Division"), regionResults.getInt("Division_ID"), regionResults.getInt("COUNTRY_ID")));
                    }
                    ObservableList<Region> filteredRegions = regionList.filtered(r -> {
                        if(r.getCountryID() == id){return true;}
                        else{return false;}
                    });
                    cbRegion.setItems(filteredRegions);
                    cbRegion.getSelectionModel().selectFirst();
                }
            }
        }
    }

    /**
     * executes the addCustomerToDatabase method when the Add button is pressed, updates the table, then closes the form.
     * If there is an empty text box, then it prompts the user to make a valid entry instead.
     * @throws SQLException when there is an issue with the SQL or connection in the other method this exception will be thrown.
     */
    public void btnAddCustomerAction() throws SQLException, IOException {
        if(elementsNotEmpty()){
            addCustomerToDatabase();
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
     * Takes the values in the different fields of the form, and adds them to the Customer Table in the Database as
     * a new customer.
     * @throws SQLException thrown when there is a connection issue or sql statement issue.
     */
    public void addCustomerToDatabase() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        String user = CurrentUser.getUsername();
        var customerSQL = "INSERT INTO customers VALUES(0, ?, ?, ?, ?, NOW(), ?, NOW(), ?, ?)";
        int divID;
        var divSQL = "SELECT Division_ID FROM first_level_divisions WHERE Division = ?";
        String div = cbRegion.getSelectionModel().getSelectedItem().toString();

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
                customerPS.setString(1, name);
                customerPS.setString(2, address);
                customerPS.setString(3, postal);
                customerPS.setString(4, phone);
                customerPS.setString(5, user);
                customerPS.setString(6, user);
                customerPS.setInt(7, divID);
                customerPS.executeUpdate();
            }
        }
    }

    /**
     * Closes the add customer window.
     */
    public void btnCancelAction() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
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
