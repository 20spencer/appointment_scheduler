package scheduler;

import com.mysql.jdbc.Connection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * The LoginController class is used to interact with the
 * login.fxml form and add functionality to it's elements.
 */
public class LoginController {

    @FXML
    public Button loginConfirmBtn;
    @FXML
    public Label loginLocationLbl;
    @FXML
    public TextField loginUsernameTxt;
    @FXML
    public PasswordField loginPasswordTxt;
    public Label lblLocationToSet;
    public Label lblLoginHeader;

    public LoginController() throws SQLException {
    }

    /**
     * Sets the Label on the login screen to display the user's current zone ID, and changes the language to either
     * English or French, based on the system's default language.
     *
     * @param zoneId the user's zoneID that will be retrieved using ZoneId.SystemDefault().
     * @param rb The Resourcebundle used to translate the text.
     */
    public void setAllText(ZoneId zoneId, ResourceBundle rb) {
        lblLoginHeader.setText(rb.getString("LOGIN"));
        loginConfirmBtn.setText(rb.getString("Confirm"));
        loginLocationLbl.setText(rb.getString("Location")+":");
        loginUsernameTxt.setPromptText(rb.getString("User ID"));
        loginPasswordTxt.setPromptText(rb.getString("Password"));
        lblLocationToSet.setText(" " + zoneId.toString());
    }

    /**
     * validateLogin is used to connect to the database and retrieve the username and password pairs
     * and store them in a map to check against the information entered by the user, and when false, calls
     * incorrectLoginMessage() to display a message to the user.
     *
     * @return returns true if the password and username exist and match. returns false otherwise
     * @throws SQLException If the connection to the database fails or one of the statements is incorrect it will throw an exception.
     */
    public boolean validateLogin() throws SQLException {
        Map<String, String> userToPasswordMap = new HashMap<>();
        boolean confirm;
        Connection conn = DatabaseConnection.getConnection();
        try (var ps = conn.prepareStatement("SELECT User_ID, User_Name, Password FROM users")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String username = rs.getString("User_Name");
                String password = rs.getString("Password");
                userToPasswordMap.put(username, password);
            }
            if (userToPasswordMap.containsKey(loginUsernameTxt.getText())) {
                if (userToPasswordMap.get(loginUsernameTxt.getText()).equals(loginPasswordTxt.getText())) {
                    confirm = true;
                    CurrentUser.setUsername(loginUsernameTxt.getText());
                    CurrentUser.setUserId();
                } else {
                    confirm = false;
                    incorrectLoginMessage();
                }
            } else {
                confirm = false;
                incorrectLoginMessage();
            }
        }
        return confirm;
    }

    /**
     * loginConfirmBtnAction runs when the Confirm button on the login form is pressed, and if successful will
     * open the home menu form, and close the login form. Also creates a file, login_activity.txt, and prints
     * information about the login attempt to that text file.     *
     * @throws SQLException if there is an error when connecting to the database it will throw this exception.
     * @throws IOException  if there is an issue with finding the homeMenu.fxml file, or
     * an issue with creating or writing to the text file, this exception will be thrown.
     */
    public void loginConfirmBtnAction() throws SQLException, IOException {
        try{
            File loginTxtFile = new File("login_activity.txt");
            loginTxtFile.createNewFile();
            try(FileWriter loginWriter = new FileWriter("login_activity.txt", true)){
                loginWriter.write("Login attempt by: \"" + loginUsernameTxt.getText() + "\" at " + ZonedDateTime.now()
                        + ". The attempt was: ");
            }
        } catch (IOException e){
            System.out.println("An error has occurred.");
            e.printStackTrace();
        }

        Stage currentStage = (Stage) loginConfirmBtn.getScene().getWindow();
        if (validateLogin()) {
            try(FileWriter loginWriter = new FileWriter("login_activity.txt", true)){
                loginWriter.write("SUCCESSFUL. \r\n");
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("homeMenu.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Appointment Scheduler");
            stage.setScene(new Scene(loader.load()));
            HomeMenuController homeMenuController = loader.getController();
            homeMenuController.updateCustomersTable();
            homeMenuController.updateAppointmentsTable(ViewBy.ALL);
            FXMLLoader loader2 = new FXMLLoader(getClass().getResource("loginAlert.fxml"));
            Stage stage2 = new Stage();
            stage2.setTitle("Upcoming Appointments");
            stage2.setScene(new Scene(loader2.load()));
            LoginAlertController loginAlertController = loader2.getController();
            loginAlertController.checkUpcomingAppointments();
            stage.show();
            stage2.showAndWait();
            currentStage.hide();
        }
        else{
            try(FileWriter loginWriter = new FileWriter("login_activity.txt",true)){
                loginWriter.write("UNSUCCESSFUL. \r\n");
            }
        }
    }

    /**
    * incorrectLoginMessage is used to show an error Alert pop up
    * to inform the user that there was not a valid username and password
    * combination entered into the text fields.
    */
    private void incorrectLoginMessage(){
        ResourceBundle rb = ResourceBundle.getBundle("scheduler/Login", Locale.getDefault());
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(rb.getString("Incorrect username or password."));
        alert.showAndWait();
    }

}
