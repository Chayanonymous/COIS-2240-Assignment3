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
    	String[] parts = row.split(" \\| ");
        String licensePlate = parts[1].split(": ")[1].trim();
        String customerName = parts[2].split(": ")[1].trim();
        LocalDate date = LocalDate.parse(parts[3].split(": ")[1].trim());
        double amount = Double.parseDouble(parts[4].split("\\$")[1].trim());
        String type = parts[0].trim();

        // Find existing Vehicle and Customer
        Vehicle vehicle = RentalSystem.getInstance().findVehicleByPlate(licensePlate);
        Customer customer = RentalSystem.getInstance().findCustomerByName(customerName);

        return new RentalRecord(vehicle, customer, date, amount, type);
    }
}