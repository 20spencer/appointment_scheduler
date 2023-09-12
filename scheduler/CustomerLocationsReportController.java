package scheduler;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * Used to add functionality to the customerLocationsReport.fxml form, which will display
 * information about how many customers are from which country, as well as a drop down box that allows
 * the user to select one of the countries and see the numbers divided among the regions.
 */
public class CustomerLocationsReportController implements Initializable {
    public Button btnClose;
    public Button btnReports;
    public TableView<ReportResult> tvRegion;
    public TableColumn colRegionMain;
    public TableColumn colRegionCustomers;
    public ComboBox<String> cbCountry;
    public Label lblTotal;

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
     * Sets column cell value factories so they can display data correctly, and populates the combo box with country names.
     * Then fills the country table with values.
     * @param url not used.
     * @param resourceBundle not used.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colRegionMain.setCellValueFactory(new PropertyValueFactory<ReportResult, String>("sort"));
        colRegionCustomers.setCellValueFactory(new PropertyValueFactory<ReportResult, Integer>("total"));

        fillCountryComboBox();
    }

    /**
     * Gets a list of all the countries from the database, then puts their names onto the Combo Box
     */
    public void fillCountryComboBox(){
        Connection conn = DatabaseConnection.getConnection();
        var sql = "SELECT Country FROM countries";
        ObservableList<String> countryList = FXCollections.observableArrayList();
        try (var ps = conn.prepareStatement(sql)){
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                countryList.add(rs.getString("Country"));
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        cbCountry.setItems(countryList);
    }

    /**
     * Filters through the customer's based on the Country ID corresponding to their Division ID, counts how many are
     * in each region and finds the name of that regions, puts them all in a list a displays that on the table view.
     * Also displays the total in that country at the bottom on a label.
     * @throws SQLException thrown if there is issue with connection to Database or with SQL syntax.
     */
    public void fillRegionTable() throws SQLException{
        Connection conn = DatabaseConnection.getConnection();
        var countryIDSQL = "SELECT Country_ID FROM countries WHERE Country = ?";
        var divisionNumberSQL = "SELECT Division_ID, Count(*) as c FROM customers WHERE Division_ID IN(" +
                "SELECT Division_ID FROM first_level_divisions WHERE COUNTRY_ID = ?) GROUP BY Division_ID";
        var divisionNameSQL = "SELECT Division FROM first_level_divisions WHERE Division_ID = ?";
        ObservableList<ReportResult> resultList = FXCollections.observableArrayList();
        int countryTotal = 0;
        try(var idPS = conn.prepareStatement(countryIDSQL)){
            idPS.setString(1, cbCountry.getSelectionModel().getSelectedItem());
            ResultSet idRS = idPS.executeQuery();
            idRS.next();
            int countryID = idRS.getInt("Country_ID");

            try(var divisionPS = conn.prepareStatement(divisionNumberSQL)){
                divisionPS.setInt(1, countryID);
                String name;
                ResultSet divisionRS = divisionPS.executeQuery();
                while(divisionRS.next()){
                    int total = divisionRS.getInt("c");
                    try(var namePS = conn.prepareStatement(divisionNameSQL)){
                        namePS.setInt(1, divisionRS.getInt("Division_ID"));
                        ResultSet nameRS = namePS.executeQuery();
                        nameRS.next();
                        name = nameRS.getString("Division");
                    }
                    ReportResult result = new ReportResult(name, total);
                    countryTotal += total;
                    resultList.add(result);
                }

            }
        }
        tvRegion.setItems(resultList);
        lblTotal.setText("Total: " + countryTotal);
    }
}
