package bank.model;

public class SavingAccount extends Account implements InterestBearing {
    private double interestRate = 0.0005;   // 0.05% per month

    public SavingAccount(String accountNumber, double balance, String branch) {
        super(accountNumber, balance, branch);
    }

    @Override
    public void withdraw(double amount) {
        throw new IllegalStateException("Savings accounts do not allow withdrawals.");
    }

    @Override
    public double calculateInterest() {
        return balance * interestRate;
    }

    @Override
    public double payInterest() {
        double interest = calculateInterest();
        if (interest > 0) {
            deposit(interest, "Interest");
        }
        return interest;
    }

    @Override
    public String getType() { return "Savings"; }
}
