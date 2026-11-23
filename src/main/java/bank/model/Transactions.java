package bank.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Transactions {
    private final int transactionId;
    private final String transactionType;   // e.g. Deposit, Withdrawal, Interest
    private final double amount;
    private Date transactionDate;

    // A brand new transaction (the database gives it an id later)
    public Transactions(String transactionType, double amount) {
        this(0, transactionType, amount, null);
    }

    // A transaction loaded from the database
    public Transactions(int transactionId, String transactionType, double amount, Date transactionDate) {
        this.transactionId = transactionId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.transactionDate = transactionDate;
    }

    // Stamps the transaction with the date and time it happened
    public void recordTransaction() {
        transactionDate = new Date();
    }

    // The text shown on screen
    public String getTransactionDetails() {
        String dateText = "-";
        if (transactionDate != null) {
            dateText = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(transactionDate);
        }
        return dateText + " | " + transactionType + " | BWP " + String.format("%.2f", amount);
    }

    public int getTransactionId() { return transactionId; }
    public String getTransactionType() { return transactionType; }
    public double getAmount() { return amount; }
    public Date getTransactionDate() { return transactionDate; }

    @Override
    public String toString() {
        return getTransactionDetails();
    }
}
