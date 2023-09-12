package scheduler;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.util.ResourceBundle;

/**
 * Used to add functionality to the appointmentTotals.fxml form.
 * Displays information to the user about the total amount of appointments by Type and by Month.
 */
public class AppointmentTotalsController implements Initializable {
    public TableView<ReportResult> tvType;
    public TableColumn colTypeType;
    public TableColumn colTypeAppointments;
    public TableView<ReportResult> tvMonth;
    public TableColumn colMonthMonth;
    public TableColumn colMonthAppointments;
    public Button btnClose;
    public Button btnReports;

    /**
     * On form creation, will set the value factory of the table columns so they will display the class information
     * correctly.
     * @param url not used.
     * @param resourceBundle not used.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colTypeType.setCellValueFactory(new PropertyValueFactory<ReportResult, String>("sort"));
        colTypeAppointments.setCellValueFactory(new PropertyValueFactory<ReportResult, Integer>("total"));

        colMonthMonth.setCellValueFactory(new PropertyValueFactory<ReportResult, String>("sort"));
        colMonthAppointments.setCellValueFactory(new PropertyValueFactory<ReportResult, Integer>("total"));
        fillTypeTable();
        fillMonthTable();
    }

    /**
     * Populates the Type report table by filtering through the appointment table in the database and
     * counting each appointment of a type and grouping by the Type name.
     */
    public void fillTypeTable(){
        Connection conn = DatabaseConnection.getConnection();
        var sql = "SELECT Type, Count(*) as c FROM appointments GROUP BY Type";
        ObservableList<ReportResult> resultList = FXCollections.observableArrayList();
        try(var ps = conn.prepareStatement(sql)){
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                ReportResult result = new ReportResult(rs.getString("Type"), rs.getInt("c"));
                resultList.add(result);
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        tvType.setItems(resultList);
    }
    /**
     * Populates the Month report table by going through the appointment table and adding 1 to a count value for each month
     * when there is an appointment during that month, then adds them to a list to set the table items.
     */
    public void fillMonthTable(){
        Connection conn = DatabaseConnection.getConnection();
        var sql = "SELECT Start FROM appointments";
        int jan = 0;
        int feb = 0;
        int mar = 0;
        int april = 0;
        int may = 0;
        int june = 0;
        int july = 0;
        int aug = 0;
        int sept = 0;
        int oct = 0;
        int nov = 0;
        int dec = 0;
        ObservableList<ReportResult> resultList = FXCollections.observableArrayList();
        try(var ps = conn.prepareStatement(sql)){
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                LocalDate date = rs.getDate("Start").toLocalDate();
                var month = date.getMonth();
                switch (month){
                    case JANUARY:
                        jan++;
                        break;
                    case FEBRUARY:
                        feb++;
                        break;
                    case MARCH:
                        mar++;
                        break;
                    case APRIL:
                        april++;
                        break;
                    case MAY:
                        may++;
                        break;
                    case JUNE:
                        june ++;
                        break;
                    case JULY:
                        july++;
                        break;
                    case AUGUST:
                        aug++;
                        break;
                    case SEPTEMBER:
                        sept++;
                        break;
                    case OCTOBER:
                        oct++;
                        break;
                    case NOVEMBER:
                        nov++;
                        break;
                    case DECEMBER:
                        dec++;
                        break;
                }
            }
            ReportResult januaryR = new ReportResult("January", jan);
            ReportResult februaryR = new ReportResult("February", feb);
            ReportResult marchR = new ReportResult("March", mar);
            ReportResult aprilR = new ReportResult("April", april);
            ReportResult mayR = new ReportResult("May", may);
            ReportResult juneR = new ReportResult("June", june);
            ReportResult julyR = new ReportResult("July", july);
            ReportResult augustR = new ReportResult("August", aug);
            ReportResult septemberR = new ReportResult("September", sept);
            ReportResult octoberR = new ReportResult("October", oct);
            ReportResult novemberR = new ReportResult("November", nov);
            ReportResult decemberR = new ReportResult("December", dec);
            resultList.addAll(januaryR, februaryR, marchR, aprilR, mayR, juneR, julyR, augustR, septemberR, octoberR, novemberR, decemberR);
            tvMonth.setItems(resultList);
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    /**
     * Closes the current window.
     */
    public void closeWindow(){
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
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
}
