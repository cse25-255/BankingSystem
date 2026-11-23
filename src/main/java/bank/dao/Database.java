package bank.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private static String url = "jdbc:sqlite:bank.db";

    public static void setUrl(String newUrl) {   // used by the tests
        url = newUrl;
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(url);
        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }
        return conn;
    }

    public static void init() {
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS customers ("
                    + "customer_id INTEGER PRIMARY KEY, "
                    + "first_name TEXT NOT NULL, "
                    + "surname TEXT NOT NULL, "
                    + "address TEXT NOT NULL, "
                    + "phone_number INTEGER NOT NULL, "
                    + "pin INTEGER NOT NULL)");

            st.execute("CREATE TABLE IF NOT EXISTS accounts ("
                    + "account_number TEXT PRIMARY KEY, "
                    + "customer_id INTEGER NOT NULL, "
                    + "account_type TEXT NOT NULL, "
                    + "balance REAL NOT NULL, "
                    + "branch TEXT NOT NULL, "
                    + "company_name TEXT, "
                    + "company_address TEXT, "
                    + "FOREIGN KEY (customer_id) REFERENCES customers(customer_id))");

            st.execute("CREATE TABLE IF NOT EXISTS transactions ("
                    + "transaction_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "account_number TEXT NOT NULL, "
                    + "transaction_type TEXT NOT NULL, "
                    + "amount REAL NOT NULL, "
                    + "transaction_date TEXT NOT NULL, "
                    + "FOREIGN KEY (account_number) REFERENCES accounts(account_number))");
        } catch (SQLException e) {
            throw new RuntimeException("Could not set up the database: " + e.getMessage(), e);
        }
    }
}
