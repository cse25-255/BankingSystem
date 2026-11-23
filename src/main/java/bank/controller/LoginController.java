package bank.controller;

import bank.dao.CustomerDAO;
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

public class LoginController {
    @FXML private TextField idField;
    @FXML private PasswordField pinField;
    @FXML private Label messageLabel;

    private final CustomerDAO customerDAO = new CustomerDAO();

    // ---------- button handlers (called by Login.fxml) ----------

    @FXML
    public void handleLogin(ActionEvent event) {
        try {
            messageLabel.setText("");

            Customer customer = login(idField.getText(), pinField.getText());

            // Go to the Dashboard and give it the customer
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/bank/view/Dashboard.fxml"));
            Parent root = loader.load();
            AccountController dashController = loader.getController();
            dashController.initData(customer);

            Stage stage = (Stage) idField.getScene().getWindow();
            stage.setScene(new Scene(root, 500, 820));

        } catch (IllegalArgumentException ex) {
            messageLabel.setText(ex.getMessage());   // e.g. "Wrong customer ID or PIN."
        } catch (Exception ex) {
            messageLabel.setText("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML
    public void goToRegister(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/bank/view/Register.fxml"));
            Stage stage = (Stage) idField.getScene().getWindow();
            stage.setScene(new Scene(root, 380, 400));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------- the logic (no JavaFX in here, so it can be tested) ----------

    public Customer login(String idText, String pinText) {
        int id = parseNumber(idText, "customer ID");
        int pin = parseNumber(pinText, "PIN");

        Customer customer = customerDAO.find(id);
        if (customer == null || !customer.login(pin)) {
            throw new IllegalArgumentException("Wrong customer ID or PIN.");
        }
        return customer;
    }

    public Customer register(String firstName, String surname, String address,
                             String phoneText, String pinText) {
        if (!phoneText.trim().matches("[1-9]\\d{7}")) {
            throw new IllegalArgumentException("Phone number must be exactly 8 digits (it cannot start with 0).");
        }
        if (!pinText.trim().matches("\\d{4}")) {
            throw new IllegalArgumentException("PIN must be exactly 4 digits.");
        }

        Customer customer = new Customer(customerDAO.nextId(), firstName.trim(), surname.trim(),
                address.trim(), Integer.parseInt(phoneText.trim()), Integer.parseInt(pinText.trim()));
        customer.register();          // the model checks the details
        customerDAO.save(customer);   // the DAO saves them
        return customer;
    }

    private int parseNumber(String text, String label) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Enter a valid " + label + ".");
        }
    }
}