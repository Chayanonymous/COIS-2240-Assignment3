import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.time.LocalDate;
import java.util.List;

class VehicleRentalTest {
	@Test
    public void testLicensePlateValidation() {
        
        Vehicle vehicle1 = new Car();
        Vehicle vehicle2 = new Car();
        Vehicle vehicle3 = new Car();

        assertDoesNotThrow(() -> vehicle1.setLicensePlate("AAA100"));
        assertEquals("AAA100", vehicle1.getLicensePlate());

        assertDoesNotThrow(() -> vehicle2.setLicensePlate("ABC567"));
        assertEquals("ABC567", vehicle2.getLicensePlate());

        assertDoesNotThrow(() -> vehicle3.setLicensePlate("ZZZ999"));
        assertEquals("ZZZ999", vehicle3.getLicensePlate());

       
        Vehicle vehicleInvalid = new Car();

        assertThrows(IllegalArgumentException.class, () -> vehicleInvalid.setLicensePlate(""));
        assertThrows(IllegalArgumentException.class, () -> vehicleInvalid.setLicensePlate(null));
        assertThrows(IllegalArgumentException.class, () -> vehicleInvalid.setLicensePlate("AAA1000"));
        assertThrows(IllegalArgumentException.class, () -> vehicleInvalid.setLicensePlate("ZZZ99"));
    }
    
    private RentalSystem rentalSystem;
    private Vehicle testCar;
    private Vehicle testMotorcycle;
    private Customer testCustomer;
    
    @BeforeEach
    void setUp() {
     
        RentalSystem.instance = null;
        
       
        rentalSystem = RentalSystem.getInstance();
        
    
        testCar = new Car("Toyota", "Corolla", 2020, 5);
        testCar.setLicensePlate("ABC123");
        
        testMotorcycle = new Motorcycle("Harley", "Davidson", 2019, false);
        testMotorcycle.setLicensePlate("XYZ789");
        
       
        testCustomer = new Customer(1001, "John Doe");
        
        
        clearTestFiles();
    }
    
    @AfterEach
    void tearDown() {
      
        clearTestFiles();
    }
    
    private void clearTestFiles() {
        String[] files = {"vehicles.txt", "customers.txt", "rental_records.txt"};
        for (String file : files) {
            try {
                new FileWriter(file, false).close();
            } catch (IOException e) {
                System.err.println("Error clearing test file: " + file);
            }
        }
    }
    
    @Test
    @DisplayName("Test Singleton Pattern")
    void testSingletonPattern() {
        RentalSystem anotherInstance = RentalSystem.getInstance();
        assertSame(rentalSystem, anotherInstance, "Should return the same instance");
    }
    
    @Test
    @DisplayName("Add Vehicle Successfully")
    void testAddVehicleSuccess() {
        assertTrue(rentalSystem.addVehicle(testCar), "Should add vehicle successfully");
        assertNotNull(rentalSystem.findVehicleByPlate("ABC123"), "Vehicle should exist in system");
    }
    
    @Test
    @DisplayName("Add Duplicate Vehicle")
    void testAddDuplicateVehicle() {
        rentalSystem.addVehicle(testCar);
        assertFalse(rentalSystem.addVehicle(testCar), "Should not add duplicate vehicle");
    }
    
    @Test
    @DisplayName("Add Customer Successfully")
    void testAddCustomerSuccess() {
        assertTrue(rentalSystem.addCustomer(testCustomer), "Should add customer successfully");
        assertNotNull(rentalSystem.findCustomerById(1001), "Customer should exist in system");
    }
    
    @Test
    @DisplayName("Add Duplicate Customer")
    void testAddDuplicateCustomer() {
        rentalSystem.addCustomer(testCustomer);
        assertFalse(rentalSystem.addCustomer(testCustomer), "Should not add duplicate customer");
    }
    
    @Test
    @DisplayName("Rent Available Vehicle")
    void testRentAvailableVehicle() {
        rentalSystem.addVehicle(testCar);
        rentalSystem.addCustomer(testCustomer);
        
        rentalSystem.rentVehicle(testCar, testCustomer, LocalDate.now(), 50.0);
        
        assertEquals(Vehicle.VehicleStatus.RENTED, testCar.getStatus(), "Vehicle status should be RENTED");
    }
    
    @Test
    @DisplayName("Rent Unavailable Vehicle")
    void testRentUnavailableVehicle() {
        rentalSystem.addVehicle(testCar);
        rentalSystem.addCustomer(testCustomer);
        
       
        rentalSystem.rentVehicle(testCar, testCustomer, LocalDate.now(), 50.0);
        
        Customer anotherCustomer = new Customer(1002, "Jane Smith");
        rentalSystem.addCustomer(anotherCustomer);
        
        rentalSystem.rentVehicle(testCar, anotherCustomer, LocalDate.now(), 50.0);
        assertEquals(Vehicle.VehicleStatus.RENTED, testCar.getStatus(), "Vehicle should remain RENTED");
    }
    
    @Test
    @DisplayName("Return Rented Vehicle")
    void testReturnRentedVehicle() {
        rentalSystem.addVehicle(testCar);
        rentalSystem.addCustomer(testCustomer);
        
        
        rentalSystem.rentVehicle(testCar, testCustomer, LocalDate.now(), 50.0);
        
        rentalSystem.returnVehicle(testCar, testCustomer, LocalDate.now().plusDays(1), 0.0);
        
        assertEquals(Vehicle.VehicleStatus.AVAILABLE, testCar.getStatus(), "Vehicle status should be AVAILABLE");
    }
    
    @Test
    @DisplayName("Find Vehicle By Plate")
    void testFindVehicleByPlate() {
        rentalSystem.addVehicle(testCar);
        rentalSystem.addVehicle(testMotorcycle);
        
        Vehicle found = rentalSystem.findVehicleByPlate("XYZ789");
        assertNotNull(found, "Should find motorcycle by plate");
        assertEquals("Harley", found.getMake(), "Should be Harley motorcycle");
    }
    
    @Test
    @DisplayName("Find Non-existent Vehicle")
    void testFindNonExistentVehicle() {
        assertNull(rentalSystem.findVehicleByPlate("NON123"), "Should return null for non-existent vehicle");
    }
    
    @Test
    @DisplayName("Load Data from Files")
    void testLoadData() {
        try {
            BufferedWriter vehicleWriter = new BufferedWriter(new FileWriter("vehicles.txt"));
            vehicleWriter.write("Car | ABC123 | Toyota | Corolla | 2020 | AVAILABLE | Seats: 5\n");
            vehicleWriter.close();
            
            BufferedWriter customerWriter = new BufferedWriter(new FileWriter("customers.txt"));
            customerWriter.write("ID: 1001 | Name: John Doe\n");
            customerWriter.close();
        } catch (IOException e) {
            fail("Failed to setup test files");
        }
        
        RentalSystem newInstance = RentalSystem.getInstance();
        
        assertNotNull(newInstance.findVehicleByPlate("ABC123"), "Should load vehicle from file");
        assertNotNull(newInstance.findCustomerById(1001), "Should load customer from file");
    }
    
    @Test
    @DisplayName("Display Available Vehicles")
    void testDisplayAvailableVehicles() {
        rentalSystem.addVehicle(testCar);
        rentalSystem.addVehicle(testMotorcycle);
        
        rentalSystem.addCustomer(testCustomer);
        rentalSystem.rentVehicle(testCar, testCustomer, LocalDate.now(), 50.0);
        
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        
        rentalSystem.displayAvailableVehicles();
        
        System.setOut(System.out);
        
        String output = outContent.toString();
        assertTrue(output.contains("XYZ789"), "Should display available motorcycle");
        assertFalse(output.contains("ABC123"), "Should not display rented car");
    }
}