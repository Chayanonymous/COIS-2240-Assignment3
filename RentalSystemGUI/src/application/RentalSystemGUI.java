package application;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import model.*;
import system.*;

public class RentalSystemGUI extends Application {
    private RentalSystem rentalSystem = RentalSystem.getInstance();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // UI Components
    private ListView<String> availableVehiclesList = new ListView<>();
    private ListView<String> customersList = new ListView<>();
    private ListView<String> rentalHistoryList = new ListView<>();
    private TextArea detailsArea = new TextArea();
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Vehicle Rental System");
        
        // Create main tab pane with better styling
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Create tabs with improved layouts
        Tab dashboardTab = createDashboardTab();
        Tab addVehicleTab = createAddVehicleTab();
        Tab addCustomerTab = createAddCustomerTab();
        Tab rentVehicleTab = createRentVehicleTab();
        Tab returnVehicleTab = createReturnVehicleTab();
        Tab historyTab = createHistoryTab();
        
        tabPane.getTabs().addAll(dashboardTab, addVehicleTab, addCustomerTab, 
                                rentVehicleTab, returnVehicleTab, historyTab);
        
        Scene scene = new Scene(tabPane, 1000, 700);
        scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Load initial data
        refreshData();
    }
    
    private Tab createDashboardTab() {
        Tab tab = new Tab("Dashboard");
        VBox dashboard = new VBox(15);
        dashboard.setPadding(new Insets(15));
        
        // Available Vehicles Section
        TitledPane vehiclesPane = new TitledPane();
        vehiclesPane.setText("Available Vehicles");
        vehiclesPane.setContent(availableVehiclesList);
        availableVehiclesList.setPrefHeight(200);
        availableVehiclesList.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> showVehicleDetails(newVal));
        
        // Customers Section
        TitledPane customersPane = new TitledPane();
        customersPane.setText("Registered Customers");
        customersPane.setContent(customersList);
        customersList.setPrefHeight(200);
        
        // Details Area with better styling
        TitledPane detailsPane = new TitledPane();
        detailsPane.setText("Vehicle Details");
        detailsArea.setEditable(false);
        detailsArea.setPrefHeight(200);
        detailsArea.setStyle("-fx-font-family: monospace; -fx-font-size: 14;");
        detailsPane.setContent(new ScrollPane(detailsArea));
        
        // Quick Actions
        HBox quickActions = new HBox(15);
        Button refreshBtn = createStyledButton("Refresh Data");
        refreshBtn.setOnAction(e -> refreshData());
        Button exitBtn = createStyledButton("Exit");
        exitBtn.setOnAction(e -> System.exit(0));
        quickActions.getChildren().addAll(refreshBtn, exitBtn);
        
        dashboard.getChildren().addAll(
            vehiclesPane,
            customersPane,
            detailsPane,
            quickActions
        );
        
        tab.setContent(new ScrollPane(dashboard));
        return tab;
    }
    
    private Tab createAddVehicleTab() {
        Tab tab = new Tab("Add Vehicle");
        VBox form = new VBox(15);
        form.setPadding(new Insets(15));
        
        // Vehicle Type Selection
        HBox typeBox = new HBox(10);
        Label typeLabel = new Label("Vehicle Type:");
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Car", "Motorcycle", "Truck");
        typeCombo.setPromptText("Select vehicle type");
        typeBox.getChildren().addAll(typeLabel, typeCombo);
        
        // Common Fields
        GridPane commonFields = new GridPane();
        commonFields.setHgap(10);
        commonFields.setVgap(10);
        commonFields.setPadding(new Insets(10));
        
        addFormField(commonFields, "License Plate:", new TextField(), 0);
        addFormField(commonFields, "Make:", new TextField(), 1);
        addFormField(commonFields, "Model:", new TextField(), 2);
        addFormField(commonFields, "Year:", new TextField(), 3);
        
        // Type-specific Fields
        GridPane typeSpecificFields = new GridPane();
        typeSpecificFields.setHgap(10);
        typeSpecificFields.setVgap(10);
        typeSpecificFields.setPadding(new Insets(10));
        
        addFormField(typeSpecificFields, "Number of Seats:", new TextField(), 0);
        addFormField(typeSpecificFields, "Has Sidecar:", new CheckBox(), 1);
        addFormField(typeSpecificFields, "Cargo Capacity (tons):", new TextField(), 2);
        
        // Initially hide all type-specific fields
        typeSpecificFields.setVisible(false);
        
        // Show/hide type-specific fields based on selection
        typeCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            typeSpecificFields.setVisible(newVal != null);
            if (newVal != null) {
                typeSpecificFields.getChildren().forEach(node -> {
                    if (node instanceof Label) {
                        String labelText = ((Label) node).getText();
                        node.setVisible(
                            (labelText.contains("Seats") && "Car".equals(newVal)) ||
                            (labelText.contains("Sidecar") && "Motorcycle".equals(newVal)) ||
                            (labelText.contains("Cargo") && "Truck".equals(newVal))
                        );
                    } else if (GridPane.getRowIndex(node) != null) {
                        int row = GridPane.getRowIndex(node);
                        node.setVisible(
                            (row == 0 && "Car".equals(newVal)) ||
                            (row == 1 && "Motorcycle".equals(newVal)) ||
                            (row == 2 && "Truck".equals(newVal))
                        );
                    }
                });
            }
        });
        
        // Submit Button
        Button submitBtn = createStyledButton("Add Vehicle");
        submitBtn.setOnAction(e -> {
            try {
                String type = typeCombo.getValue();
                if (type == null) {
                    showAlert("Error", "Please select a vehicle type", Alert.AlertType.ERROR);
                    return;
                }
                
                TextField plateField = (TextField) commonFields.getChildren().get(1);
                TextField makeField = (TextField) commonFields.getChildren().get(3);
                TextField modelField = (TextField) commonFields.getChildren().get(5);
                TextField yearField = (TextField) commonFields.getChildren().get(7);
                
                String plate = plateField.getText().toUpperCase().trim();
                if (plate.isEmpty()) {
                    showAlert("Error", "License plate cannot be empty", Alert.AlertType.ERROR);
                    return;
                }
                
                String make = makeField.getText().trim();
                String model = modelField.getText().trim();
                int year = Integer.parseInt(yearField.getText().trim());
                
                Vehicle vehicle = createVehicle(type, typeSpecificFields, make, model, year);
                
                if (vehicle != null) {
                    vehicle.setLicensePlate(plate);
                    if (rentalSystem.addVehicle(vehicle)) {
                        showAlert("Success", "Vehicle added successfully!", Alert.AlertType.INFORMATION);
                        clearFormFields(commonFields);
                        clearFormFields(typeSpecificFields);
                        typeCombo.getSelectionModel().clearSelection();
                        typeSpecificFields.setVisible(false);
                        refreshData();
                    } else {
                        showAlert("Error", "Vehicle with this license plate already exists.", Alert.AlertType.ERROR);
                    }
                }
            } catch (NumberFormatException ex) {
                showAlert("Input Error", "Please enter valid numbers for numeric fields.", Alert.AlertType.ERROR);
            } catch (Exception ex) {
                showAlert("Error", "Failed to add vehicle: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        });
        
        form.getChildren().addAll(
            typeBox,
            new TitledPane("Common Details", commonFields),
            new TitledPane("Type-Specific Details", typeSpecificFields),
            submitBtn
        );
        
        tab.setContent(new ScrollPane(form));
        return tab;
    }
    
    private Vehicle createVehicle(String type, GridPane typeSpecificFields, String make, String model, int year) {
        switch (type) {
            case "Car":
                TextField seatsField = (TextField) typeSpecificFields.getChildren().get(1);
                int seats = Integer.parseInt(seatsField.getText().trim());
                return new Car(make, model, year, seats);
            case "Motorcycle":
                CheckBox sidecarCheck = (CheckBox) typeSpecificFields.getChildren().get(3);
                boolean sidecar = sidecarCheck.isSelected();
                return new Motorcycle(make, model, year, sidecar);
            case "Truck":
                TextField cargoField = (TextField) typeSpecificFields.getChildren().get(5);
                double cargo = Double.parseDouble(cargoField.getText().trim());
                return new Truck(make, model, year, cargo);
            default:
                return null;
        }
    }
    
    private Tab createAddCustomerTab() {
        Tab tab = new Tab("Add Customer");
        VBox form = new VBox(15);
        form.setPadding(new Insets(15));
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        
        addFormField(grid, "Customer ID:", new TextField(), 0);
        addFormField(grid, "Name:", new TextField(), 1);
        
        Button submitBtn = createStyledButton("Add Customer");
        submitBtn.setOnAction(e -> {
            try {
                TextField idField = (TextField) grid.getChildren().get(1);
                TextField nameField = (TextField) grid.getChildren().get(3);
                
                int id = Integer.parseInt(idField.getText().trim());
                String name = nameField.getText().trim();
                
                if (name.isEmpty()) {
                    showAlert("Error", "Customer name cannot be empty", Alert.AlertType.ERROR);
                    return;
                }
                
                if (rentalSystem.addCustomer(new Customer(id, name))) {
                    showAlert("Success", "Customer added successfully!", Alert.AlertType.INFORMATION);
                    clearFormFields(grid);
                    refreshData();
                } else {
                    showAlert("Error", "Customer with this ID already exists.", Alert.AlertType.ERROR);
                }
            } catch (NumberFormatException ex) {
                showAlert("Input Error", "Please enter a valid number for customer ID.", Alert.AlertType.ERROR);
            }
        });
        
        form.getChildren().addAll(
            new TitledPane("Customer Details", grid),
            submitBtn
        );
        
        tab.setContent(new ScrollPane(form));
        return tab;
    }
    
    private Tab createRentVehicleTab() {
        Tab tab = new Tab("Rent Vehicle");
        VBox form = new VBox(15);
        form.setPadding(new Insets(15));
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        
        // Vehicle Selection
        Label vehicleLabel = new Label("Select Vehicle:");
        ComboBox<String> vehicleCombo = new ComboBox<>();
        vehicleCombo.setPromptText("Select available vehicle");
        updateAvailableVehiclesCombo(vehicleCombo);
        
        // Customer Selection
        Label customerLabel = new Label("Select Customer:");
        ComboBox<String> customerCombo = new ComboBox<>();
        customerCombo.setPromptText("Select customer");
        updateCustomersCombo(customerCombo);
        
        // Rental Details
        Label amountLabel = new Label("Rental Amount:");
        TextField amountField = new TextField();
        
        Label dateLabel = new Label("Rental Date:");
        TextField dateField = new TextField(LocalDate.now().format(dateFormatter));
        dateField.setEditable(false);
        
        grid.add(vehicleLabel, 0, 0);
        grid.add(vehicleCombo, 1, 0);
        grid.add(customerLabel, 0, 1);
        grid.add(customerCombo, 1, 1);
        grid.add(amountLabel, 0, 2);
        grid.add(amountField, 1, 2);
        grid.add(dateLabel, 0, 3);
        grid.add(dateField, 1, 3);
        
        Button submitBtn = createStyledButton("Rent Vehicle");
        submitBtn.setOnAction(e -> {
            try {
                String selectedVehicle = vehicleCombo.getValue();
                String selectedCustomer = customerCombo.getValue();
                double amount = Double.parseDouble(amountField.getText().trim());
                
                if (selectedVehicle == null || selectedCustomer == null) {
                    showAlert("Error", "Please select both a vehicle and a customer.", Alert.AlertType.ERROR);
                    return;
                }
                
                String plate = selectedVehicle.split(" - ")[0];
                int customerId = Integer.parseInt(selectedCustomer.split(" - ")[0]);
                
                Vehicle vehicle = rentalSystem.findVehicleByPlate(plate);
                Customer customer = rentalSystem.findCustomerById(customerId);
                
                if (vehicle != null && customer != null) {
                    if (rentalSystem.rentVehicle(vehicle, customer, LocalDate.now(), amount)) {
                        showAlert("Success", "Vehicle rented successfully!", Alert.AlertType.INFORMATION);
                        amountField.clear();
                        refreshData();
                        updateAvailableVehiclesCombo(vehicleCombo);
                    } else {
                        showAlert("Error", "Vehicle is not available for rent.", Alert.AlertType.ERROR);
                    }
                } else {
                    showAlert("Error", "Vehicle or customer not found.", Alert.AlertType.ERROR);
                }
            } catch (NumberFormatException ex) {
                showAlert("Input Error", "Please enter a valid number for rental amount.", Alert.AlertType.ERROR);
            }
        });
        
        form.getChildren().addAll(
            new TitledPane("Rental Details", grid),
            submitBtn
        );
        
        tab.setContent(new ScrollPane(form));
        return tab;
    }
    
    private Tab createReturnVehicleTab() {
        Tab tab = new Tab("Return Vehicle");
        VBox form = new VBox(15);
        form.setPadding(new Insets(15));
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        
        // Vehicle Selection (only rented vehicles)
        Label vehicleLabel = new Label("Select Rented Vehicle:");
        ComboBox<String> vehicleCombo = new ComboBox<>();
        vehicleCombo.setPromptText("Select rented vehicle");
        updateRentedVehiclesCombo(vehicleCombo);
        
        // Customer Selection
        Label customerLabel = new Label("Select Customer:");
        ComboBox<String> customerCombo = new ComboBox<>();
        customerCombo.setPromptText("Select customer");
        updateCustomersCombo(customerCombo);
        
        // Return Details
        Label feesLabel = new Label("Return Fees:");
        TextField feesField = new TextField();
        
        Label dateLabel = new Label("Return Date:");
        TextField dateField = new TextField(LocalDate.now().format(dateFormatter));
        dateField.setEditable(false);
        
        grid.add(vehicleLabel, 0, 0);
        grid.add(vehicleCombo, 1, 0);
        grid.add(customerLabel, 0, 1);
        grid.add(customerCombo, 1, 1);
        grid.add(feesLabel, 0, 2);
        grid.add(feesField, 1, 2);
        grid.add(dateLabel, 0, 3);
        grid.add(dateField, 1, 3);
        
        Button submitBtn = createStyledButton("Return Vehicle");
        submitBtn.setOnAction(e -> {
            try {
                String selectedVehicle = vehicleCombo.getValue();
                String selectedCustomer = customerCombo.getValue();
                double fees = Double.parseDouble(feesField.getText().trim());
                
                if (selectedVehicle == null || selectedCustomer == null) {
                    showAlert("Error", "Please select both a vehicle and a customer.", Alert.AlertType.ERROR);
                    return;
                }
                
                String plate = selectedVehicle.split(" - ")[0];
                int customerId = Integer.parseInt(selectedCustomer.split(" - ")[0]);
                
                Vehicle vehicle = rentalSystem.findVehicleByPlate(plate);
                Customer customer = rentalSystem.findCustomerById(customerId);
                
                if (vehicle != null && customer != null) {
                    if (rentalSystem.returnVehicle(vehicle, customer, LocalDate.now(), fees)) {
                        showAlert("Success", "Vehicle returned successfully!", Alert.AlertType.INFORMATION);
                        feesField.clear();
                        refreshData();
                        updateRentedVehiclesCombo(vehicleCombo);
                    } else {
                        showAlert("Error", "Vehicle is not currently rented.", Alert.AlertType.ERROR);
                    }
                } else {
                    showAlert("Error", "Vehicle or customer not found.", Alert.AlertType.ERROR);
                }
            } catch (NumberFormatException ex) {
                showAlert("Input Error", "Please enter a valid number for return fees.", Alert.AlertType.ERROR);
            }
        });
        
        form.getChildren().addAll(
            new TitledPane("Return Details", grid),
            submitBtn
        );
        
        tab.setContent(new ScrollPane(form));
        return tab;
    }
    
    private Tab createHistoryTab() {
        Tab tab = new Tab("Rental History");
        VBox historyBox = new VBox(15);
        historyBox.setPadding(new Insets(15));
        
        rentalHistoryList.setPrefHeight(600);
        rentalHistoryList.setStyle("-fx-font-family: monospace; -fx-font-size: 14;");
        
        Button refreshBtn = createStyledButton("Refresh History");
        refreshBtn.setOnAction(e -> refreshRentalHistory());
        
        historyBox.getChildren().addAll(
            new TitledPane("Complete Rental History", new ScrollPane(rentalHistoryList)),
            refreshBtn
        );
        
        tab.setContent(historyBox);
        return tab;
    }
    
    // Helper methods
    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-base: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        return button;
    }
    
    private void addFormField(GridPane grid, String labelText, Control field, int row) {
        Label label = new Label(labelText);
        grid.add(label, 0, row);
        grid.add(field, 1, row);
    }
    
    private void clearFormFields(GridPane grid) {
        grid.getChildren().forEach(node -> {
            if (node instanceof TextField) {
                ((TextField) node).clear();
            } else if (node instanceof CheckBox) {
                ((CheckBox) node).setSelected(false);
            }
        });
    }
    
    private void refreshData() {
        // Refresh available vehicles list
        ObservableList<String> availableVehicles = FXCollections.observableArrayList();
        for (Vehicle v : rentalSystem.getVehicles()) {
            if (v.getStatus() == Vehicle.VehicleStatus.AVAILABLE) {
                availableVehicles.add(v.getLicensePlate() + " - " + v.getMake() + " " + v.getModel());
            }
        }
        availableVehiclesList.setItems(availableVehicles);
        
        // Refresh customers list
        ObservableList<String> customers = FXCollections.observableArrayList();
        for (Customer c : rentalSystem.getCustomers()) {
            customers.add(c.getCustomerId() + " - " + c.getCustomerName());
        }
        customersList.setItems(customers);
        
        // Refresh rental history
        refreshRentalHistory();
    }
    
    private void refreshRentalHistory() {
        ObservableList<String> history = FXCollections.observableArrayList();
        for (RentalRecord record : rentalSystem.getRentalHistory().getRentalHistory()) {
            history.add(record.toString());
        }
        rentalHistoryList.setItems(history);
    }
    
    private void updateAvailableVehiclesCombo(ComboBox<String> combo) {
        ObservableList<String> vehicles = FXCollections.observableArrayList();
        for (Vehicle v : rentalSystem.getVehicles()) {
            if (v.getStatus() == Vehicle.VehicleStatus.AVAILABLE) {
                vehicles.add(v.getLicensePlate() + " - " + v.getMake() + " " + v.getModel());
            }
        }
        combo.setItems(vehicles);
    }
    
    private void updateRentedVehiclesCombo(ComboBox<String> combo) {
        ObservableList<String> vehicles = FXCollections.observableArrayList();
        for (Vehicle v : rentalSystem.getVehicles()) {
            if (v.getStatus() == Vehicle.VehicleStatus.RENTED) {
                vehicles.add(v.getLicensePlate() + " - " + v.getMake() + " " + v.getModel());
            }
        }
        combo.setItems(vehicles);
    }
    
    private void updateCustomersCombo(ComboBox<String> combo) {
        ObservableList<String> customers = FXCollections.observableArrayList();
        for (Customer c : rentalSystem.getCustomers()) {
            customers.add(c.getCustomerId() + " - " + c.getCustomerName());
        }
        combo.setItems(customers);
    }
    
    private void showVehicleDetails(String vehicleInfo) {
        if (vehicleInfo == null || vehicleInfo.isEmpty()) return;
        
        String plate = vehicleInfo.split(" - ")[0];
        Vehicle vehicle = rentalSystem.findVehicleByPlate(plate);
        
        if (vehicle != null) {
            StringBuilder details = new StringBuilder();
            details.append("License Plate: ").append(vehicle.getLicensePlate()).append("\n");
            details.append("Make: ").append(vehicle.getMake()).append("\n");
            details.append("Model: ").append(vehicle.getModel()).append("\n");
            details.append("Year: ").append(vehicle.getYear()).append("\n");
            details.append("Status: ").append(vehicle.getStatus()).append("\n");
            
            if (vehicle instanceof Car) {
                Car car = (Car) vehicle;
                details.append("Type: Car\n");
                details.append("Seats: ").append(car.getNumberOfSeats()).append("\n");
                
                if (vehicle instanceof SportCar) {
                    SportCar sportCar = (SportCar) vehicle;
                    details.append("Subtype: Sport Car\n");
                    details.append("Horsepower: ").append(sportCar.getHorsepower()).append("\n");
                    details.append("Turbo: ").append(sportCar.hasTurbo() ? "Yes" : "No").append("\n");
                }
            } else if (vehicle instanceof Motorcycle) {
                Motorcycle motorcycle = (Motorcycle) vehicle;
                details.append("Type: Motorcycle\n");
                details.append("Sidecar: ").append(motorcycle.hasSidecar() ? "Yes" : "No").append("\n");
            } else if (vehicle instanceof Truck) {
                Truck truck = (Truck) vehicle;
                details.append("Type: Truck\n");
                details.append("Cargo Capacity: ").append(truck.getCargoCapacity()).append(" tons\n");
            }
            
            detailsArea.setText(details.toString());
        }
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}