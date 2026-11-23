package bank.controller;

import bank.model.Customer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterController {
    @FXML private TextField firstNameField;
    @FXML private TextField surnameField;
    @FXML private TextField addressField;
    @FXML private TextField phoneField;
    @FXML private PasswordField pinField;
    @FXML private Label messageLabel;

    private final LoginController loginController = new LoginController();

    @FXML
    public void handleRegister(ActionEvent event) {
        try {
            Customer customer = loginController.register(firstNameField.getText(), surnameField.getText(),
                    addressField.getText(), phoneField.getText(), pinField.getText());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/bank/view/Dashboard.fxml"));
            Parent root = loader.load();
            AccountController dashController = loader.getController();
            dashController.initData(customer);

            Stage stage = (Stage) firstNameField.getScene().getWindow();
            stage.setScene(new Scene(root, 500, 820));

        } catch (RuntimeException ex) {
            messageLabel.setText(ex.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goToLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/bank/view/Login.fxml"));
            Stage stage = (Stage) firstNameField.getScene().getWindow();
            stage.setScene(new Scene(root, 380, 300));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}