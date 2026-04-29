import java.io.Serializable;

public class Customer implements Serializable {
    private static final long serialVersionUID = 1L;
    private String customerId;
    private String name;
    private String phone;
    private String email;

    // This constructor MUST exist to fix your error
    public Customer(String customerId, String name, String phone, String email) {
        this.customerId = customerId;
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    // Getters
    public String getCustomerId() { return customerId; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }

    // This helps the ComboBox display the name properly
    @Override
    public String toString() {
        return name + " (" + customerId + ")";
    }
}