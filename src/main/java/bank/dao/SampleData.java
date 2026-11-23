package bank.dao;

import bank.model.Account;
import bank.model.ChequeAccount;
import bank.model.Customer;
import bank.model.Employer;
import bank.model.InvestmentAccount;
import bank.model.SavingAccount;
import bank.model.Transactions;

// Adds 10 sample customers (IDs 1001 to 1010, PIN 1234), each with one account.
// A customer that already exists is skipped, so it is safe to run every time.
public class SampleData {

    public static void load() {
        CustomerDAO customerDAO = new CustomerDAO();
        AccountDAO accountDAO = new AccountDAO();
        TransactionDAO transactionDAO = new TransactionDAO();

        String[] firstNames = {"Thabo", "Neo", "Kefilwe", "Lesego", "Boitumelo",
                               "Tshepo", "Naledi", "Kagiso", "Mpho", "Onalenna"};
        String[] surnames = {"Molefe", "Kgosi", "Dube", "Mokgosi", "Sebego",
                             "Modise", "Seretse", "Phiri", "Ramotswa", "Tau"};

        for (int i = 1; i <= 10; i++) {
            int customerId = 1000 + i;
            if (customerDAO.find(customerId) != null) {
                continue;   // already loaded
            }
            Customer customer = new Customer(customerId, firstNames[i - 1], surnames[i - 1],
                    "Plot " + (100 + i) + ", Gaborone", 71000000 + i, 1234);
            customerDAO.save(customer);

            String accountNumber = String.format("AC%04d", i);   // AC0001 ... AC0010
            Account account;
            if (i % 3 == 0) {
                account = new InvestmentAccount(accountNumber, 0, "Gaborone Main");
            } else if (i % 3 == 1) {
                account = new SavingAccount(accountNumber, 0, "Gaborone Main");
            } else {
                account = new ChequeAccount(accountNumber, 0, "Gaborone Main",
                        new Employer("Sample Employer " + i, "Plot " + i + ", Gaborone"));
            }
            account.deposit(1000.0 * i, "Opening deposit");

            accountDAO.save(account, customerId);
            for (Transactions t : account.getTransactions()) {
                transactionDAO.save(accountNumber, t);
            }
        }
    }
}
