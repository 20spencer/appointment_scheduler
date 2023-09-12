//Author: 20Spencer
//Date: 2-1-2021
//Appointment Scheduler - Application to add and update customers and appointments.
//Version 2 (Edited the output text of the Appointment Delete button)

package scheduler;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.ZoneId;
import java.util.Locale;
import java.util.ResourceBundle;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        ResourceBundle rb = ResourceBundle.getBundle("scheduler.Login", Locale.getDefault());

        DatabaseConnection.establishConnection();
        CurrentUser.establishZoneId();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
        primaryStage.setTitle(rb.getString("Appointment Scheduler"));
        primaryStage.setScene(new Scene(loader.load()));
        LoginController loginController = loader.getController();
        loginController.setAllText(CurrentUser.getZoneId(), rb);
        primaryStage.show();
    }


    public static void main(String[] args) throws SQLException {
        launch(args);
    }
}
