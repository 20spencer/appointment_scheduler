package scheduler;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Used to give functionality to the reportForm.fxml window.
 * Used to navigate to other windows that give specific report information.
 */
public class ReportFormController {
    public Button btnContactSchedules;
    public Button btnAppointmentTotals;
    public Button btnClose;
    public Button btnCustomersPerArea;

    /**
     * Closes the current window.
     */
    public void closeWindow(){
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }

    /**
     * Opens the appointment totals report form and closes the current window.
     * @throws IOException thrown if there is an issue obtaining the appointmentTotals.fxml file.
     */
    public void btnAppointmentTotalsAction() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("appointmentTotals.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Appointment Totals");
        stage.setScene(new Scene(loader.load()));
        stage.show();
        closeWindow();
    }

    /**
     * Opens the form with a table showing contact schedules based on the contact selected in the combo box, and closes this form.
     * @throws IOException thrown if there is an issue obtaining the contactSchedule.fxml file.
     */
    public void btnContactSchedulesAction() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("contactSchedule.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Contact Schedules");
        stage.setScene(new Scene(loader.load()));
        stage.show();
        closeWindow();
    }

    /**
     * Opens the customerLocationsReport form which has tables displaying how many customers are from each country and each region within a country.
     * @throws IOException thrown if there is an issue obtaining the customerLocationsReport.fxml file.
     */
    public void btnCustomersPerAreaAction() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("customerLocationsReport.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Customers per Area");
        stage.setScene(new Scene(loader.load()));
        stage.show();
        closeWindow();
    }
}
