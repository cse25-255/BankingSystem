package bank.dao;

import bank.model.Transactions;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TransactionDAO {
    // Dates are stored in the database as text like 2026-11-20 13:15:00
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public void save(String accountNumber, Transactions t) {
        String sql = "INSERT INTO transactions (account_number, transaction_type, amount, "
                   + "transaction_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            ps.setString(2, t.getTransactionType());
            ps.setDouble(3, t.getAmount());
            ps.setString(4, new SimpleDateFormat(DATE_FORMAT).format(t.getTransactionDate()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not save transaction: " + e.getMessage(), e);
        }
    }

    // Oldest first, like a bank statement
    public List<Transactions> findByAccount(String accountNumber) {
        String sql = "SELECT * FROM transactions WHERE account_number = ? ORDER BY transaction_id ASC";
        List<Transactions> list = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date date = new SimpleDateFormat(DATE_FORMAT).parse(rs.getString("transaction_date"));
                    list.add(new Transactions(rs.getInt("transaction_id"),
                            rs.getString("transaction_type"), rs.getDouble("amount"), date));
                }
            }
        } catch (SQLException | ParseException e) {
            throw new RuntimeException("Could not load transactions: " + e.getMessage(), e);
        }
        return list;
    }
}
