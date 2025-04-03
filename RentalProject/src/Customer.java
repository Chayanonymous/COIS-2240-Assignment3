
public class Customer {
    private int customerId;
    private String name;

    public static Customer parse(String line)
    {  
        String[] parts = line.split(",");
        String id = parts[0];
        String name = parts[1];
        String contact = parts[2];
        return new Customer(id, name, contact);
    }

    public Customer(int customerId, String name) {
        this.customerId = customerId;
        this.name = name;
    }

    public int getCustomerId() {
    	return customerId;
    }

    public String getCustomerName() {
    	return name;
    }

    @Override
    public String toString() {
        return "Customer ID: " + customerId + " | Name: " + name;
    }
}