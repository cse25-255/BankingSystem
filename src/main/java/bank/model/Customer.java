package bank.model;

import java.util.ArrayList;
import java.util.List;

public class Customer {
    private final int customerID;
    private final String firstName;
    private final String surname;
    private final String address;
    private final int phoneNumber;
    private final int pin;
    // Composition: the customer OWNS the accounts. No customer = no accounts.
    private final List<Account> accounts = new ArrayList<>();

    public Customer(int customerID, String firstName, String surname,
                    String address, int phoneNumber, int pin) {
        this.customerID = customerID;
        this.firstName = firstName;
        this.surname = surname;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.pin = pin;
    }

    // Checks the details are filled in (the DAO saves them afterwards)
    public void register() {
        if (isEmpty(firstName) || isEmpty(surname) || isEmpty(address)) {
            throw new IllegalArgumentException("Fill in the first name, surname and address.");
        }
    }

    // True if the PIN is correct
    public boolean login(int enteredPin) {
        return pin == enteredPin;
    }

    public void openAccount(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("No account given.");
        }
        accounts.add(account);
    }

    public List<Account> viewAccounts() {
        return accounts;
    }

    private boolean isEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    public int getCustomerID() { return customerID; }
    public String getFirstName() { return firstName; }
    public String getSurname() { return surname; }
    public String getAddress() { return address; }
    public int getPhoneNumber() { return phoneNumber; }
    public int getPin() { return pin; }
}
