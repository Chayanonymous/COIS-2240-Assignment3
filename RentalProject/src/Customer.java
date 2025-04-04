
public class Customer {
    private int customerId;
    private String name;
/*
    private String contact;
    
    public Customer(int customerId, String name, String contact) {
    	this.customerId = customerId;
    	this.name = name;
    	this.contact = contact;
    }
    */

    public static Customer parse(String line)
    {  
        String[] parts = line.split(" \\| ");
        int id = Integer.parseInt(parts[0].split(": ")[1].trim());
        String name = parts[1].split(": ")[1].trim();
        return new Customer(id, name);
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