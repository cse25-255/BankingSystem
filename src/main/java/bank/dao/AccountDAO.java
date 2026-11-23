package bank.dao;

import bank.model.Account;
import bank.model.ChequeAccount;
import bank.model.Employer;
import bank.model.InvestmentAccount;
import bank.model.SavingAccount;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {

    // CREATE: the customerId links the account to its owner
    public void save(Account account, int customerId) {
        String sql = "INSERT INTO accounts (account_number, customer_id, account_type, balance, "
                   + "branch, company_name, company_address) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String companyName = null;
        String companyAddress = null;
        if (account instanceof ChequeAccount) {
            ChequeAccount cheque = (ChequeAccount) account;
            companyName = cheque.getCompanyName();
            companyAddress = cheque.getCompanyAddress();
        }
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, account.getAccountNumber());
            ps.setInt(2, customerId);
            ps.setString(3, account.getType());
            ps.setDouble(4, account.getBalance());
            ps.setString(5, account.getBranch());
            ps.setString(6, companyName);
            ps.setString(7, companyAddress);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not save account: " + e.getMessage(), e);
        }
    }

    // READ one, with its transactions (null if not found)
    public Account find(String accountNumber) {
        String sql = "SELECT * FROM accounts WHERE account_number = ?";
        Account account = null;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    account = buildAccount(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not find account: " + e.getMessage(), e);
        }
        if (account != null) {
            loadTransactions(account);
        }
        return account;
    }

    // READ many: all accounts of one customer, with their transactions
    public List<Account> findByCustomer(int customerId) {
        String sql = "SELECT * FROM accounts WHERE customer_id = ? ORDER BY account_number";
        List<Account> accounts = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    accounts.add(buildAccount(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not load accounts: " + e.getMessage(), e);
        }
        for (Account account : accounts) {
            loadTransactions(account);
        }
        return accounts;
    }

    public void updateBalance(Account account) {   // UPDATE
        String sql = "UPDATE accounts SET balance = ? WHERE account_number = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, account.getBalance());
            ps.setString(2, account.getAccountNumber());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not update account: " + e.getMessage(), e);
        }
    }

    public void delete(String accountNumber) {   // DELETE (only if it has no transactions)
        String sql = "DELETE FROM accounts WHERE account_number = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not delete account: " + e.getMessage(), e);
        }
    }

    public int count() {   // used to make the next account number
        String sql = "SELECT COUNT(*) FROM accounts";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("Could not count accounts: " + e.getMessage(), e);
        }
    }

    // Turns a database row into the correct Java object
    private Account buildAccount(ResultSet rs) throws SQLException {
        String number = rs.getString("account_number");
        String type = rs.getString("account_type");
        double balance = rs.getDouble("balance");
        String branch = rs.getString("branch");

        if (type.equals("Savings")) {
            return new SavingAccount(number, balance, branch);
        } else if (type.equals("Investment")) {
            return new InvestmentAccount(number, balance, branch);
        } else {
            Employer employer = new Employer(rs.getString("company_name"), rs.getString("company_address"));
            return new ChequeAccount(number, balance, branch, employer);
        }
    }

    // Fills the account's transaction list from the database
    private void loadTransactions(Account account) {
        account.getTransactions().addAll(new TransactionDAO().findByAccount(account.getAccountNumber()));
    }
}
