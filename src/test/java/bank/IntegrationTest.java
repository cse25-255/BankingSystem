package bank;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import bank.controller.AccountController;
import bank.controller.LoginController;
import bank.dao.AccountDAO;
import bank.dao.Database;
import bank.model.Account;
import bank.model.Customer;
import bank.model.SavingAccount;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

// Tests the flow: Controller -> Model -> DAO -> Database (the screens sit on top of the controllers).
class IntegrationTest {

    @BeforeAll
    static void setUp() throws Exception {
        Files.deleteIfExists(Path.of("test-bank.db"));
        Database.setUrl("jdbc:sqlite:test-bank.db");   // separate database just for tests
        Database.init();
    }

    @AfterAll
    static void cleanUp() throws Exception {
        Files.deleteIfExists(Path.of("test-bank.db"));
    }

    // helper: registers a customer and logs them in
    private Customer newCustomer(String firstName) {
        LoginController login = new LoginController();
        Customer registered = login.register(firstName, "Tester", "Plot 1, Gaborone", "71234567", "1234");
        return login.login(String.valueOf(registered.getCustomerID()), "1234");
    }

    // helper: a controller working for this customer (no screen needed)
    private AccountController controllerFor(Customer customer) {
        AccountController controller = new AccountController();
        controller.setCustomer(customer);
        return controller;
    }

    @Test
    void fullUserFlow() {
        Customer customer = newCustomer("Full");
        assertEquals("Full", customer.getFirstName());

        AccountController controller = controllerFor(customer);
        Account account = controller.openAccount("Investment", "Gaborone Main", "1000", "", "");
        String number = account.getAccountNumber();

        controller.deposit(number, "500");
        controller.withdraw(number, "300");

        Account fromDb = new AccountDAO().find(number);   // read it back from the DATABASE
        assertNotNull(fromDb);
        assertEquals(1200.0, fromDb.getBalance(), 0.001);

        controller.payInterest();   // 5% of 1200 = 60
        Account afterInterest = new AccountDAO().find(number);
        assertEquals(1260.0, afterInterest.getBalance(), 0.001);

        // opening deposit, deposit, withdrawal, interest
        assertEquals(4, afterInterest.getTransactions().size());
    }

    @Test
    void wrongPinIsRejected() {
        Customer customer = newCustomer("Pin");
        assertThrows(IllegalArgumentException.class,
                () -> new LoginController().login(String.valueOf(customer.getCustomerID()), "9999"));
    }

    @Test
    void savingsAccountRefusesWithdrawals() {
        AccountController controller = controllerFor(newCustomer("Saver"));
        Account savings = controller.openAccount("Savings", "Gaborone Main", "200", "", "");

        assertThrows(IllegalStateException.class,
                () -> controller.withdraw(savings.getAccountNumber(), "50"));
    }

    @Test
    void investmentNeedsMinimumOpeningDeposit() {
        AccountController controller = controllerFor(newCustomer("Investor"));

        assertThrows(IllegalArgumentException.class,
                () -> controller.openAccount("Investment", "Gaborone Main", "100", "", ""));
    }

    @Test
    void chequeAccountNeedsCompanyDetails() {
        AccountController controller = controllerFor(newCustomer("Worker"));

        assertThrows(IllegalArgumentException.class,
                () -> controller.openAccount("Cheque", "Gaborone Main", "100", "", ""));
    }

    @Test
    void accountCannotExistWithoutACustomer() {
        // the database refuses an account that points to a customer that does not exist
        assertThrows(RuntimeException.class,
                () -> new AccountDAO().save(new SavingAccount("AC9999", 0, "Gaborone Main"), 99999));
    }

    @Test
    void phoneNumberMustBeExactlyEightDigits() {
        LoginController login = new LoginController();
        assertThrows(IllegalArgumentException.class,
                () -> login.register("Phone", "Tester", "Plot 1", "7123456", "1234"));     // 7 digits
        assertThrows(IllegalArgumentException.class,
                () -> login.register("Phone", "Tester", "Plot 1", "712345678", "1234"));   // 9 digits
    }
}