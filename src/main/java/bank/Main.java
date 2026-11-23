package bank;

import bank.dao.Database;
import bank.dao.SampleData;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Database.init();
        SampleData.load(); 

        Parent root = FXMLLoader.load(getClass().getResource("/bank/view/Login.fxml"));
        primaryStage.setTitle("Banking System");
        primaryStage.setScene(new Scene(root, 380, 300));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}