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
    
    
    public boolean addVehicle(Vehicle vehicle) {
        if (findVehicleByPlate(vehicle.getLicensePlate()) != null) {
            System.out.println("Vehicle with license plate " + vehicle.getLicensePlate() + " already exists.");
            return false;
        }
        vehicles.add(vehicle);
        saveVehicle(vehicle);
        return true;
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

    public boolean addCustomer(Customer customer) {
        if (findCustomerById(customer.getCustomerId()) != null) {
            System.out.println("Customer ID " + customer.getCustomerId() + " already exists.");
            return false;
        }
        customers.add(customer);
        saveCustomer(customer);
        return true;
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
                String type;
                if (v instanceof SportCar) type = "SportCar     ";
                else if (v instanceof Car) type = "Car          ";
                else if (v instanceof Motorcycle) type = "Motorcycle   ";
                else if (v instanceof Truck) type = "Truck        ";
                else type = "Unknown     ";
                
                System.out.println("|     " + type + "|\t" + v.getLicensePlate() + "\t|\t" + v.getMake() + "\t|\t" + v.getModel() + "\t|\t" + v.getYear() + "\t|");
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
                if (line.trim().isEmpty()) continue;
                try {
                    String[] parts = line.split("\\|");
                    List<String> tokens = new ArrayList<>();
                    for (String part : parts) {
                        if (!part.trim().isEmpty()) {
                            tokens.add(part.trim());
                        }
                    }

                    if (tokens.size() < 6) {
                        System.err.println("Skipping invalid vehicle line: " + line);
                        continue;
                    }

                    String licensePlate = tokens.get(0);
                    String make = tokens.get(1);
                    String model = tokens.get(2);
                    int year = Integer.parseInt(tokens.get(3));
                    Vehicle.VehicleStatus status = Vehicle.VehicleStatus.valueOf(tokens.get(4));
                    String typeDetails = tokens.get(5);

                    Vehicle vehicle = null;

                    if (typeDetails.startsWith("Seats:")) {
                        int seats = Integer.parseInt(typeDetails.split(":")[1].trim());
                        if (tokens.size() > 6 && tokens.get(6).startsWith("Horsepower:")) {
                            // This is a SportCar
                            int horsepower = Integer.parseInt(tokens.get(6).split(":")[1].trim());
                            boolean turbo = tokens.get(7).split(":")[1].trim().equalsIgnoreCase("Yes");
                            vehicle = new SportCar(make, model, year, seats, horsepower, turbo);
                        } else {
                            vehicle = new Car(make, model, year, seats);
                        }
                    } else if (typeDetails.startsWith("Sidecar:")) {
                        boolean sidecar = typeDetails.split(":")[1].trim().equalsIgnoreCase("Yes");
                        vehicle = new Motorcycle(make, model, year, sidecar);
                    } else if (typeDetails.startsWith("Cargo Capacity:")) {
                        double capacity = Double.parseDouble(typeDetails.split(":")[1].trim());
                        vehicle = new Truck(make, model, year, capacity);
                    }

                    if (vehicle != null) {
                        vehicle.setLicensePlate(licensePlate);
                        vehicle.setStatus(status);
                        vehicles.add(vehicle);
                    }

                } catch (Exception e) {
                    System.err.println("Failed to parse vehicle line: " + line);
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.out.println("Error");
        }
    }
    
    private void loadCustomer() {
    	try (BufferedReader reader = new BufferedReader(new FileReader("customers.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {

            	if (line.trim().isEmpty()) {
            		continue;
            	}
            	
                String[] parts = line.split(" \\| ");
                
                if (parts.length < 2) {
                	continue;
                }
                try {
                	int id = Integer.parseInt(parts[0].split(": ")[1].trim());
                    String name = parts[1].split(": ")[1].trim();
                    customers.add(new Customer(id, name));
                } catch (Exception e) {
                    System.err.println("Failed to parse customer line: " + line);
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading customers: " + e.getMessage());
        }
    }

    private void loadRecords() {
        try (BufferedReader reader = new BufferedReader(new FileReader("rental_records.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 6) continue; 
                
               
                String vehicleType = parts[0].trim();
                String licensePlate = parts[1].trim();
                Vehicle vehicle = findVehicleByPlate(licensePlate);
                if (vehicle == null) continue;
                
                
                int customerId = Integer.parseInt(parts[2].trim());
                Customer customer = findCustomerById(customerId);
                if (customer == null) continue;
                
                
                LocalDate date = LocalDate.parse(parts[3].trim());
                
                
                double amount = Double.parseDouble(parts[4].trim());
                String transactionType = parts[5].trim();
                
                RentalRecord record = new RentalRecord(vehicle, customer, date, amount, transactionType);
                rentalHistory.addRecord(record);
            }
        } catch (IOException e) {
            System.out.println("No existing rental records found or error reading file.");
        	}
    	}
    }