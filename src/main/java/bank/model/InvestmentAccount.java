package bank.model;

public class InvestmentAccount extends Account implements InterestBearing {
    public static final double MIN_OPENING_DEPOSIT = 500;   // BWP 500
    private double interestRate = 0.05;                     // 5% per month

    public InvestmentAccount(String accountNumber, double balance, String branch) {
        super(accountNumber, balance, branch);
    }

    @Override
    public void withdraw(double amount) {
        takeOut(amount);
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
    public String getType() { return "Investment"; }
}
