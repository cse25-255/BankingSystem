package bank.model;

// Only accounts that earn interest implement this interface.
public interface InterestBearing {
    double calculateInterest();
    double payInterest();   // adds the interest to the balance and returns it
}
