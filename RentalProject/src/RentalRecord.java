import java.time.LocalDate;

public class RentalRecord {
    private Vehicle vehicle;
    private Customer customer;
    private LocalDate recordDate;
    private double totalAmount;
    private String recordType; // "RENT" or "RETURN"

    public RentalRecord(Vehicle vehicle, Customer customer, LocalDate recordDate, double totalAmount, String recordType) {
        this.vehicle = vehicle;
        this.customer = customer;
        this.recordDate = recordDate;
        this.totalAmount = totalAmount;
        this.recordType = recordType;
    }

    public Customer getCustomer(){
    	return customer;
    }
    
    public Vehicle getVehicle(){
    	return vehicle;
    }
    
    @Override
    public String toString() {
        return recordType + " | Plate: " + vehicle.getLicensePlate() + 
               " | Customer: " + customer.getCustomerName() + 
               " | Date: " + recordDate + 
               " | Amount: $" + totalAmount;
    }
    
    public static RentalRecord parse(String row) {
    	String[] parts = row.split(",");
        Vehicle vehicleId = parts[0];
        Customer customerId = parts[1];
        LocalDate date = LocalDate.parse(parts[2]);
        double amount = Double.parseDouble(parts[3]);
        String type = parts[4];
        return new RentalRecord(vehicleId, customerId, date, amount, type);
    }
}