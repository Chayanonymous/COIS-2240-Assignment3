import java.util.List;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.io.FileReader;
import java.io.BufferedReader;

public class RentalSystem {
    private List<Vehicle> vehicles = new ArrayList<>();
    private List<Customer> customers = new ArrayList<>();
    private RentalHistory rentalHistory = new RentalHistory();
    public static RentalSystem instance;
    
    
    public void addVehicle(Vehicle vehicle) {
        vehicles.add(vehicle);
        saveVehicle(vehicle);
        
    }
    
    private RentalSystem() {
    	loadData();
    	
    }
    
    public static RentalSystem getInstance(){
    	if (instance == null) {
    		synchronized (RentalSystem.class) {
                if (instance == null) {
                    instance = new RentalSystem();
                }
            }
    	}
    	return instance;
    }

    public void addCustomer(Customer customer) {
        customers.add(customer);
        saveCustomer(customer);
        
    }

 
    public void rentVehicle(Vehicle vehicle, Customer customer, LocalDate date, double amount) {
        if (vehicle.getStatus() == Vehicle.VehicleStatus.AVAILABLE) {
            vehicle.setStatus(Vehicle.VehicleStatus.RENTED);
            RentalRecord rec = new RentalRecord(vehicle, customer, date, amount, "RENT");
            rentalHistory.addRecord(rec);
            saveRecord(rec);
            System.out.println("Vehicle rented to " + customer.getCustomerName());
        }
        else {
            System.out.println("Vehicle is not available for renting.");
        }
    }

    public void returnVehicle(Vehicle vehicle, Customer customer, LocalDate date, double extraFees) {
        if (vehicle.getStatus() == Vehicle.VehicleStatus.RENTED) {
            vehicle.setStatus(Vehicle.VehicleStatus.AVAILABLE);
            
            RentalRecord record = new RentalRecord(vehicle, customer, date, extraFees, "RETURN");
            rentalHistory.addRecord(record);
            saveRecord(record);
            
            System.out.println("Vehicle returned by " + customer.getCustomerName());
        }
        else {
            System.out.println("Vehicle is not rented.");
        }
    }    

    public void displayAvailableVehicles() {
    	System.out.println("|     Type         |\tPlate\t|\tMake\t|\tModel\t|\tYear\t|");
    	System.out.println("---------------------------------------------------------------------------------");
    	 
        for (Vehicle v : vehicles) {
            if (v.getStatus() == Vehicle.VehicleStatus.AVAILABLE) {
                System.out.println("|     " + (v instanceof Car ? "Car          " : "Motorcycle   ") + "|\t" + v.getLicensePlate() + "\t|\t" + v.getMake() + "\t|\t" + v.getModel() + "\t|\t" + v.getYear() + "\t|\t");
            }
        }
        System.out.println();
    }
    
    public void displayAllVehicles() {
        for (Vehicle v : vehicles) {
            System.out.println("  " + v.getInfo());
        }
    }

    public void displayAllCustomers() {
        for (Customer c : customers) {
            System.out.println("  " + c.toString());
        }
    }
    
    public void displayRentalHistory() {
        for (RentalRecord record : rentalHistory.getRentalHistory()) {
            System.out.println(record.toString());
        }
    }
    
    public Vehicle findVehicleByPlate(String plate) {
        for (Vehicle v : vehicles) {
            if (v.getLicensePlate().equalsIgnoreCase(plate)) {
                return v;
            }
        }
        return null;
    }
    
    public Customer findCustomerById(int id) {
        for (Customer c : customers)
            if (c.getCustomerId() == id)
                return c;
        return null;
    }

    public Customer findCustomerByName(String name) {
        for (Customer c : customers)
            if (c.getCustomerName().equalsIgnoreCase(name))
                return c;
        return null;
    }
    
    public void saveCustomer(Customer customer) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("customers.txt", true))) {
            writer.write(customer.toString() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    public void saveVehicle(Vehicle vehicle) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("vehicles.txt", true))) {
            writer.write(vehicle.getInfo() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    public void saveRecord(RentalRecord record) {
    	
    	try (BufferedWriter writer = new BufferedWriter(new FileWriter("rental_records.txt", true))) {
    		writer.write(record.toString() + "\n");
    		
    	}
    	catch (IOException e) {
    		System.out.println("Error");
    	}

    }
    
    private void loadData() {
    	loadVehicles();
    	loadCustomer();
    	loadRecords();
    }
    private void loadVehicles() {
        try (BufferedReader reader = new BufferedReader(new FileReader("vehicles.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 6) continue; // Skip invalid lines
                
                String type = parts[0].trim();
                String licensePlate = parts[1].trim();
                String make = parts[2].trim();
                String model = parts[3].trim();
                int year = Integer.parseInt(parts[4].trim());
                Vehicle.VehicleStatus status = Vehicle.VehicleStatus.valueOf(parts[5].trim());
                
                if (type.equals("Car")) {
                    int doors = Integer.parseInt(parts[6].trim());
                    Car car = new Car(licensePlate, make, model, year, doors);
                    car.setStatus(status);
                    vehicles.add(car);
                } else if (type.equals("Motorcycle")) {
                    String category = parts[6].trim();
                    Motorcycle motorcycle = new Motorcycle(licensePlate, make, model, year, category);
                    motorcycle.setStatus(status);
                    vehicles.add(motorcycle);
                }
            }
        } catch (IOException e) {
            System.out.println("Error");
    }
}
    private void loadCustomers() {
        try (BufferedReader reader = new BufferedReader(new FileReader("customers.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 3) continue; // Skip invalid lines
                
                int customerId = Integer.parseInt(parts[0].trim());
                String customerName = parts[1].trim();
                String phoneNumber = parts[2].trim();
                
                Customer customer = new Customer(customerId, customerName, phoneNumber);
                customers.add(customer);
            }
        } catch (IOException e) {
            System.out.println("No existing customer data found or error reading file.");
        } catch (Exception e) {
            System.out.println("Error parsing customer data: " + e.getMessage());
        }
    }

    private void loadRentalRecords() {
        try (BufferedReader reader = new BufferedReader(new FileReader("rental_records.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 6) continue; // Skip invalid lines
                
                // Parse vehicle
                String vehicleType = parts[0].trim();
                String licensePlate = parts[1].trim();
                Vehicle vehicle = findVehicleByPlate(licensePlate);
                if (vehicle == null) continue;
                
                // Parse customer
                int customerId = Integer.parseInt(parts[2].trim());
                Customer customer = findCustomerById(customerId);
                if (customer == null) continue;
                
                // Parse date
                LocalDate date = LocalDate.parse(parts[3].trim());
                
                // Parse amount and type
                double amount = Double.parseDouble(parts[4].trim());
                String transactionType = parts[5].trim();
                
                RentalRecord record = new RentalRecord(vehicle, customer, date, amount, transactionType);
                rentalHistory.addRecord(record);
            }
        } catch (IOException e) {
            System.out.println("No existing rental records found or error reading file.");
        } catch (Exception e) {
            System.out.println("Error parsing rental records: " + e.getMessage());
        }
    }
    }