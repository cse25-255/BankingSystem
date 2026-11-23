package bank.model;

// The company a Cheque account holder works for.
public class Employer {
    private final String companyName;
    private final String companyAddress;

    public Employer(String companyName, String companyAddress) {
        this.companyName = companyName;
        this.companyAddress = companyAddress;
    }

    public String getCompanyName() { return companyName; }
    public String getCompanyAddress() { return companyAddress; }
}
