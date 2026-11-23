package bank.controller;

import bank.dao.AccountDAO;
import bank.dao.TransactionDAO;
import bank.model.Account;
import bank.model.ChequeAccount;
import bank.model.Customer;
import bank.model.Employer;
import bank.model.InterestBearing;
import bank.model.InvestmentAccount;
import bank.model.SavingAccount;
import bank.model.Transactions;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

// Controller for Dashboard.fxml.
// The fx:id names in Dashboard.fxml must be exactly the same as the field names below.
public class AccountController {
    @FXML private Label welcomeLabel;
    @FXML private ComboBox<Account> accountBox;       // the drop-down to choose an account
    @FXML private TextField amountField;
    @FXML private ListView<Transactions> transactionList;
    @FXML private ComboBox<String> typeBox;
    @FXML private TextField branchField;
    @FXML private TextField initialField;
    @FXML private TextField companyNameField;
    @FXML private TextField companyAddressField;
    @FXML private Label messageLabel;

    private Customer customer;
    private final AccountDAO accountDAO = new AccountDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    // JavaFX calls initialize() by itself when it loads Dashboard.fxml
    @FXML
    public void initialize() {
        typeBox.getItems().addAll("Savings", "Investment", "Cheque");
        typeBox.setValue("Savings");
        accountBox.setPromptText("Choose an account");

        // when another account is chosen, show its transactions
        accountBox.valueProperty().addListener((obs, oldAccount, newAccount) -> showTransactions());
    }

    // Called by LoginController / RegisterController right after login
    public void initData(Customer customer) {
        setCustomer(customer);
        welcomeLabel.setText("Welcome, " + customer.getFirstName() + " " + customer.getSurname()
                + " (Customer ID: " + customer.getCustomerID() + ")");
        refresh();   // this also chooses the first account automatically
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    // ---------- button handlers (called by Dashboard.fxml) ----------

    @FXML
    public void handleDeposit(ActionEvent event) {
        run(() -> deposit(selectedAccountNumber(), amountField.getText()), "Deposit successful.");
    }

    @FXML
    public void handleWithdraw(ActionEvent event) {
        run(() -> withdraw(selectedAccountNumber(), amountField.getText()), "Withdrawal successful.");
    }

    @FXML
    public void handleInterest(ActionEvent event) {
        try {
            String text = payInterest();
            refresh();
            showMessage(text, false);
        } catch (RuntimeException ex) {
            showMessage(ex.getMessage(), true);
        }
    }

    @FXML
    public void handleOpenAccount(ActionEvent event) {
        try {
            openAccount(typeBox.getValue(), branchField.getText(), initialField.getText(),
                    companyNameField.getText(), companyAddressField.getText());
            // empty the form for the next account
            branchField.clear();
            initialField.clear();
            companyNameField.clear();
            companyAddressField.clear();
            refresh();
            showMessage("Account opened. You can see it on the first tab.", false);
        } catch (RuntimeException ex) {
            showMessage(ex.getMessage(), true);
        }
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/bank/view/Login.fxml"));
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 380, 300));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------- the logic (no JavaFX in here, so it can be tested) ----------
    // Flow: check input -> use the model -> save with the DAO

    public List<Account> getAccounts() {
        return customer.viewAccounts();
    }

    public Account openAccount(String type, String branch, String depositText,
                               String companyName, String companyAddress) {
        if (type == null) {
            throw new IllegalArgumentException("Choose an account type.");
        }
        if (branch.trim().isEmpty()) {
            throw new IllegalArgumentException("Enter the branch.");
        }
        double deposit = 0;
        if (!depositText.trim().isEmpty()) {
            deposit = parseAmount(depositText);
        }

        String accountNumber = String.format("AC%04d", accountDAO.count() + 1);
        Account account;

        if (type.equals("Savings")) {
            account = new SavingAccount(accountNumber, 0, branch.trim());
        } else if (type.equals("Investment")) {
            if (deposit < InvestmentAccount.MIN_OPENING_DEPOSIT) {
                throw new IllegalArgumentException("An Investment account needs at least BWP 500 to open.");
            }
            account = new InvestmentAccount(accountNumber, 0, branch.trim());
        } else {
            if (companyName.trim().isEmpty() || companyAddress.trim().isEmpty()) {
                throw new IllegalArgumentException("A Cheque account needs the company name and address.");
            }
            account = new ChequeAccount(accountNumber, 0, branch.trim(),
                    new Employer(companyName.trim(), companyAddress.trim()));
        }

        if (deposit > 0) {
            account.deposit(deposit, "Opening deposit");
        }

        accountDAO.save(account, customer.getCustomerID());
        for (Transactions t : account.getTransactions()) {
            transactionDAO.save(accountNumber, t);
        }
        customer.openAccount(account);
        return account;
    }

    public void deposit(String accountNumber, String amountText) {
        Account account = findAccount(accountNumber);
        double amount = parseAmount(amountText);
        account.deposit(amount);                       // model: business rule
        accountDAO.updateBalance(account);             // DAO: save the balance
        transactionDAO.save(accountNumber, account.getLastTransaction());
    }

    public void withdraw(String accountNumber, String amountText) {
        Account account = findAccount(accountNumber);
        double amount = parseAmount(amountText);
        account.withdraw(amount);                      // Savings will refuse here
        accountDAO.updateBalance(account);
        transactionDAO.save(accountNumber, account.getLastTransaction());
    }

    // Pays interest only into accounts that earn it (Savings and Investment).
    public String payInterest() {
        double total = 0;
        int count = 0;
        for (Account account : customer.viewAccounts()) {
            if (account instanceof InterestBearing) {
                InterestBearing earner = (InterestBearing) account;
                double paid = earner.payInterest();
                if (paid > 0) {
                    accountDAO.updateBalance(account);
                    transactionDAO.save(account.getAccountNumber(), account.getLastTransaction());
                    total = total + paid;
                    count++;
                }
            }
        }
        return "Interest of BWP " + String.format("%.2f", total) + " paid into " + count + " account(s).";
    }

    // ---------- small helpers ----------

    private String selectedAccountNumber() {
        Account selected = accountBox.getValue();
        return selected == null ? null : selected.getAccountNumber();
    }

    private Account findAccount(String accountNumber) {
        if (accountNumber == null) {
            throw new IllegalArgumentException("Choose an account first.");
        }
        for (Account account : customer.viewAccounts()) {
            if (account.getAccountNumber().equals(accountNumber)) {
                return account;
            }
        }
        throw new IllegalArgumentException("Account not found.");
    }

    private double parseAmount(String text) {
        double amount;
        try {
            amount = Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Enter a valid number for the amount.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("The amount must be more than 0.");
        }
        return amount;
    }

    // runs an action, shows a green or red message, then reloads the screen
    private void run(Runnable action, String successText) {
        try {
            action.run();
            refresh();
            showMessage(successText, false);
        } catch (RuntimeException ex) {
            showMessage(ex.getMessage(), true);
        }
    }

    // reloads the drop-down (so the balances are up to date) and keeps the same account chosen
    private void refresh() {
        int chosenIndex = accountBox.getSelectionModel().getSelectedIndex();
        accountBox.getItems().setAll(customer.viewAccounts());
        accountBox.getSelectionModel().clearSelection();   // makes the box redraw the new balance

        if (!accountBox.getItems().isEmpty()) {
            if (chosenIndex < 0 || chosenIndex >= accountBox.getItems().size()) {
                chosenIndex = 0;   // nothing chosen yet: choose the first account
            }
            accountBox.getSelectionModel().select(chosenIndex);
        }
        showTransactions();
    }

    private void showTransactions() {
        Account selected = accountBox.getValue();
        if (selected == null) {
            transactionList.getItems().clear();
        } else {
            transactionList.getItems().setAll(selected.getTransactions());
        }
    }

    private void showMessage(String text, boolean isError) {
        messageLabel.setStyle(isError ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
        messageLabel.setText(text);
    }
}