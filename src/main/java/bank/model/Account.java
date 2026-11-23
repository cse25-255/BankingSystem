package bank.model;

import java.util.ArrayList;
import java.util.List;

public abstract class Account {
    protected String accountNumber;
    protected double balance;
    protected String branch;
    // Aggregation: the account keeps the list of its transactions
    private final List<Transactions> transactions = new ArrayList<>();

    public Account(String accountNumber, double balance, String branch) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.branch = branch;
    }

    // Overloading: two deposit methods with the same name but different parameters
    public void deposit(double amount) {
        deposit(amount, "Deposit");
    }

    public void deposit(double amount, String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit must be more than 0.");
        }
        balance = balance + amount;
        addTransaction(description, amount);
    }

    // Each account type decides if withdrawing is allowed (polymorphism).
    public abstract void withdraw(double amount);

    public abstract String getType();

    // Helper used by the accounts that allow withdrawals.
    protected void takeOut(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal must be more than 0.");
        }
        if (amount > balance) {
            throw new IllegalArgumentException("Not enough money in the account.");
        }
        balance = balance - amount;
        addTransaction("Withdrawal", amount);
    }

    private void addTransaction(String type, double amount) {
        Transactions transaction = new Transactions(type, amount);
        transaction.recordTransaction();
        transactions.add(transaction);
    }

    public double getBalance() { return balance; }
    public String getAccountNumber() { return accountNumber; }
    public String getBranch() { return branch; }
    public List<Transactions> getTransactions() { return transactions; }

    public Transactions getLastTransaction() {
        return transactions.get(transactions.size() - 1);
    }

    @Override
    public String toString() {
        return accountNumber + " | " + getType() + " | " + branch + " | BWP " + String.format("%.2f", balance);
    }
}
