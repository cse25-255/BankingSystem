package bank.dao;

import bank.model.Account;
import bank.model.Customer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerDAO {

    // The next free customer ID (starts at 1001)
    public int nextId() {
        String sql = "SELECT COALESCE(MAX(customer_id), 1000) + 1 FROM customers";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 1001;
        } catch (SQLException e) {
            throw new RuntimeException("Could not get the next customer ID: " + e.getMessage(), e);
        }
    }

    public void save(Customer customer) {   // CREATE
        String sql = "INSERT INTO customers (customer_id, first_name, surname, address, "
                   + "phone_number, pin) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customer.getCustomerID());
            ps.setString(2, customer.getFirstName());
            ps.setString(3, customer.getSurname());
            ps.setString(4, customer.getAddress());
            ps.setInt(5, customer.getPhoneNumber());
            ps.setInt(6, customer.getPin());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not save customer: " + e.getMessage(), e);
        }
    }

    // READ: returns the customer together with their accounts (null if not found)
    public Customer find(int customerId) {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";
        Customer customer = null;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    customer = new Customer(rs.getInt("customer_id"), rs.getString("first_name"),
                            rs.getString("surname"), rs.getString("address"),
                            rs.getInt("phone_number"), rs.getInt("pin"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not find customer: " + e.getMessage(), e);
        }
        if (customer != null) {
            for (Account account : new AccountDAO().findByCustomer(customerId)) {
                customer.openAccount(account);
            }
        }
        return customer;
    }

    public void updateAddress(int customerId, String newAddress) {   // UPDATE
        String sql = "UPDATE customers SET address = ? WHERE customer_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newAddress);
            ps.setInt(2, customerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not update customer: " + e.getMessage(), e);
        }
    }

    public void delete(int customerId) {   // DELETE (only if the customer has no accounts)
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not delete customer: " + e.getMessage(), e);
        }
    }
}
