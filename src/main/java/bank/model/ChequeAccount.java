package bank.model;

// Salary account: no interest, so it does NOT implement InterestBearing.
public class ChequeAccount extends Account {
    private String companyName;
    private String companyAddress;

    // Uses an Employer object to get the company details
    public ChequeAccount(String accountNumber, double balance, String branch, Employer employer) {
        super(accountNumber, balance, branch);
        this.companyName = employer.getCompanyName();
        this.companyAddress = employer.getCompanyAddress();
    }

    @Override
    public void withdraw(double amount) {
        takeOut(amount);
    }

    @Override
    public String getType() { return "Cheque"; }

    public String getCompanyName() { return companyName; }
    public String getCompanyAddress() { return companyAddress; }
}
