package correctionalmanagement;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Modality;
import java.time.LocalDate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.DatePicker;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import java.util.Scanner;


public class CorrectionalFacilitySystem extends Application {

    // Database connection parameters
    private static final String DB_URL = "jdbc:mysql://localhost:3306/correctional_facility_dba";
    private static final String DB_USERNAME = "****";
    private static final String DB_PASSWORD = "**********";

    @Override
    public void start(Stage primaryStage) {
        // Set the window title
       primaryStage.setTitle("Correctional Facility Management System");

// Load and set the logo image
       Image logoImage = new Image("file:///C:/Users/Limpho%20Mokone/Documents/Bata27/PRISONS%20B/correctionalmanagement/justice.png");  
       ImageView logo = new ImageView(logoImage);
       logo.setFitHeight(40);  // Adjust logo height as needed
       logo.setPreserveRatio(true);

// Set the window icon
       primaryStage.getIcons().add(logoImage);

        //

        // Ensure default admin credentials exist
        initializeDatabase();

        // Login Screen UI
        VBox loginLayout = new VBox(10);
        loginLayout.setStyle("-fx-padding: 20;");

        Label lblUsername = new Label("Username:");
        TextField txtUsername = new TextField();

        Label lblPassword = new Label("Password:");
        PasswordField txtPassword = new PasswordField();

        Button btnLogin = new Button("Login");
        Label lblMessage = new Label();

        loginLayout.getChildren().addAll(lblUsername, txtUsername, lblPassword, txtPassword, btnLogin, lblMessage);

        // Main Dashboard
        TabPane dashboardTabs = new TabPane();

        Tab inmatesTab = new Tab("Inmates");
        inmatesTab.setContent(createInmateManagementUI());

        Tab visitsTab = new Tab("Visits");
        visitsTab.setContent(createVisitBookingUI());

        Tab interFacilityTab = new Tab("Inter-Facility Data Access");
        interFacilityTab.setContent(createInterFacilityAccessUI());
        
        Tab courtRollsTab = new Tab("Court-Rolls");
        courtRollsTab.setContent(createCourtRollsContentUI());

        dashboardTabs.getTabs().addAll(inmatesTab, visitsTab, interFacilityTab, courtRollsTab);

        BorderPane dashboardLayout = new BorderPane();
        dashboardLayout.setTop(createHeader());
        dashboardLayout.setCenter(dashboardTabs);
        dashboardLayout.setBottom(createFooter());

        Scene dashboardScene = new Scene(dashboardLayout, 800, 600);

        // Login Action
        btnLogin.setOnAction(e -> {
            String username = txtUsername.getText().trim();
            String password = txtPassword.getText();

            if (username.isEmpty() || password.isEmpty()) {
                lblMessage.setText("Username and password cannot be empty.");
                lblMessage.setStyle("-fx-text-fill: red;");
            } else if (authenticateUser(username, password)) {
                primaryStage.setScene(dashboardScene);
            } else {
                lblMessage.setText("Invalid username or password.");
                lblMessage.setStyle("-fx-text-fill: red;");
            }
        });

        Scene loginScene = new Scene(loginLayout, 400, 300);
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    private boolean authenticateUser(String username, String password) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String query = "SELECT password FROM users WHERE username = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String storedPassword = resultSet.getString("password");
                return BCrypt.checkpw(password, storedPassword);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void initializeDatabase() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String createUserTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(50) NOT NULL UNIQUE,
                    password VARCHAR(255) NOT NULL
                );
            """;

            String checkDefaultUser = "SELECT COUNT(*) FROM users WHERE username = ?";
            String insertDefaultUser = "INSERT INTO users (username, password) VALUES (?, ?)";

            PreparedStatement createTableStmt = connection.prepareStatement(createUserTable);
            createTableStmt.execute();

            PreparedStatement checkUserStmt = connection.prepareStatement(checkDefaultUser);
            checkUserStmt.setString(1, "admin");
            ResultSet resultSet = checkUserStmt.executeQuery();

            if (resultSet.next() && resultSet.getInt(1) == 0) {
                String hashedPassword = BCrypt.hashpw("**********", BCrypt.gensalt());
                PreparedStatement insertUserStmt = connection.prepareStatement(insertDefaultUser);
                insertUserStmt.setString(1, "admin");
                insertUserStmt.setString(2, hashedPassword);
                insertUserStmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
private VBox createInmateManagementUI() {
    VBox inmateLayout = new VBox(10);
    inmateLayout.setStyle("-fx-padding: 20;");

    Label lblTitle = new Label("Manage Inmates");
    lblTitle.setStyle("-fx-font-size: 18px;");

    TableView<Inmate> inmateTable = new TableView<>();

    TableColumn<Inmate, String> idColumn = new TableColumn<>("ID");
    idColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getId()));

    TableColumn<Inmate, String> nameColumn = new TableColumn<>("Name");
    nameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));

    TableColumn<Inmate, String> caseColumn = new TableColumn<>("Case");
    caseColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCaseDetails()));

    TableColumn<Inmate, String> facilityColumn = new TableColumn<>("Facility");
    facilityColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFacility()));

    TableColumn<Inmate, String> nationalityColumn = new TableColumn<>("Nationality");
    nationalityColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNationality()));
    
    TableColumn<Inmate, String> statusColumn = new TableColumn<>("Status");
    statusColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus()));

    inmateTable.getColumns().addAll(idColumn, nameColumn, caseColumn, facilityColumn, nationalityColumn, statusColumn);

    loadInmateData(inmateTable);

    // Add Inmate Button
    Button btnAddInmate = new Button("Add Inmate");
    btnAddInmate.setOnAction(e -> openAddInmateDialog());

    // Search Inmate Button
    Button btnSearchInmate = new Button("Search Inmate");
    btnSearchInmate.setOnAction(e -> openSearchInmateDialog(inmateTable));

    inmateLayout.getChildren().addAll(lblTitle, inmateTable, btnAddInmate, btnSearchInmate);
    return inmateLayout;
}

private void openSearchInmateDialog(TableView<Inmate> inmateTable) {
    // Create a dialog to collect search details
    Stage dialog = new Stage();
    dialog.initModality(Modality.APPLICATION_MODAL);
    dialog.setTitle("Search Inmate");

    VBox dialogLayout = new VBox(10);
    dialogLayout.setStyle("-fx-padding: 20;");

    // Fields for search details
    TextField txtInmateName = new TextField();
    txtInmateName.setPromptText("Inmate Name");

    TextField txtNationality = new TextField();
    txtNationality.setPromptText("Nationality (Optional)");

    TextField txtCaseDetails = new TextField();
    txtCaseDetails.setPromptText("Case Details (Optional)");

    TextField txtFacility = new TextField();
    txtFacility.setPromptText("Facility Name (Optional)");

    // Button to submit search data
    Button btnSearch = new Button("Search");
    btnSearch.setOnAction(e -> {
        String inmateName = txtInmateName.getText();
        String nationality = txtNationality.getText();
        String caseDetails = txtCaseDetails.getText();
        String facility = txtFacility.getText();

        // Search logic
        searchInmates(inmateTable, inmateName, nationality, caseDetails, facility);

        dialog.close();
    });

    dialogLayout.getChildren().addAll(txtInmateName, txtNationality, txtCaseDetails, txtFacility, btnSearch);

    Scene scene = new Scene(dialogLayout, 300, 250);
    dialog.setScene(scene);
    dialog.show();
}



private void searchInmates(TableView<Inmate> inmateTable, String inmateName, String nationality, String caseDetails, String facility) {
    // SQL query with a WHERE clause to filter by provided fields
    String query = "SELECT * FROM inmates WHERE 1=1";
    
    if (!inmateName.isEmpty()) {
        query += " AND full_name LIKE '%" + inmateName + "%'";
    }
    if (!nationality.isEmpty()) {
        query += " AND nationality LIKE '%" + nationality + "%'";
    }
    if (!caseDetails.isEmpty()) {
        query += " AND case_number LIKE '%" + caseDetails + "%'";
    }
    if (!facility.isEmpty()) {
        query += " AND facility_name LIKE '%" + facility + "%'";
    }

    // Clear the existing table data
    ObservableList<Inmate> inmateList = FXCollections.observableArrayList();

    // Execute the query (you should replace this with actual database code)
    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(query)) {

        while (rs.next()) {
            String id = rs.getString("inmate_id");
            String name = rs.getString("full_name");
            String caseDetail = rs.getString("case_number");
            String facilityName = rs.getString("facility_name");
            String nationalityValue = rs.getString("nationality");
            String status = rs.getString("status");
            Inmate inmate = new Inmate(id, name, caseDetail, "", facilityName, nationalityValue, status); // Add appropriate date fields if necessary
            inmateList.add(inmate);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    // Update the table with search results
    inmateTable.setItems(inmateList);
}


private void loadInmateData(TableView<Inmate> inmateTable) {
    ObservableList<Inmate> inmateList = FXCollections.observableArrayList();

    // Fetch data from the database and add to inmateList
    String query = "SELECT inmate_id, full_name, case_number, date_of_birth, facility_name, nationality, status FROM inmates";
    try (Connection conn = DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/correctional_facility_dba", "****", "**********");
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(query)) {

        while (rs.next()) {
            // Fetch each column from the ResultSet
            String id = rs.getString("inmate_id");
            String name = rs.getString("full_name");
            String caseDetails = rs.getString("case_number");
            String dateOfBirth = rs.getString("date_of_birth");
            String facility = rs.getString("facility_name");
            String nationality = rs.getString("nationality");
            String status = rs.getString("status");

            // Create a new Inmate object and add it to the list
            inmateList.add(new Inmate(id, name, caseDetails, dateOfBirth, facility, nationality, status));
        }
    } catch (SQLException e) {
        // Print the stack trace for debugging
        e.printStackTrace();

        // Optionally show an alert to the user
        showAlert("Error loading inmate data: " + e.getMessage());
    }

    // Set the data to the TableView
    inmateTable.setItems(inmateList);
}

private void openAddInmateDialog(TableView<Inmate> inmateTable) {
    // Open a dialog to add a new inmate
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Add Inmate");
    dialog.setHeaderText("Enter new inmate details");

    // Show the dialog and get the input
    dialog.showAndWait().ifPresent(result -> {
        // Process input and insert new inmate into the database
        String[] details = result.split(",");
        if (details.length == 3) {
            String name = details[0].trim();
            String caseNumber = details[1].trim();           
            String date_of_birth = details[2].trim();
            String facility = details[3].trim();
            String nationality = details[4].trim();
            String status = details[5].trim();

            // Insert new inmate into database
            String insertQuery = "INSERT INTO inmates (full_name, case_number, date_of_birth, facility, nationality, status,) VALUES (?, ?, ?)";
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/correctional_facility_dba", "****", "*******");
                 PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                stmt.setString(1, name);
                stmt.setString(2, caseNumber);                
                stmt.setString(3, date_of_birth);
                stmt.setString(4, facility);
                stmt.setString(5, nationality);
                stmt.setString(6, status);
                stmt.executeUpdate();
                
                // Reload table data after insertion
                loadInmateData(inmateTable);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    });
}

   private void openAddInmateDialog() {
    // Create a dialog to collect inmate details
    Stage dialog = new Stage();
    dialog.initModality(Modality.APPLICATION_MODAL);
    dialog.setTitle("Add Inmate");

    VBox dialogLayout = new VBox(10);
    dialogLayout.setStyle("-fx-padding: 20;");

    // Fields for inmate details
    TextField txtInmateName = new TextField();
    txtInmateName.setPromptText("Inmate Name");

    TextField txtCaseDetails = new TextField();
    txtCaseDetails.setPromptText("Case Details");

    TextField txtDateOfBirth = new TextField();
    txtDateOfBirth.setPromptText("Date of Birth (YYYY-MM-DD)");

    TextField txtFacility = new TextField();
    txtFacility.setPromptText("Facility Name");

    TextField txtNationality = new TextField();
    txtNationality.setPromptText("Nationality");

    TextField txtStatus = new TextField();
    txtStatus.setPromptText("Status (e.g., Active, Released)");

    // Button to submit inmate data
    Button btnSubmit = new Button("Submit");
    btnSubmit.setOnAction(e -> {
        String inmateName = txtInmateName.getText().trim();
        String caseDetails = txtCaseDetails.getText().trim();
        String dateOfBirth = txtDateOfBirth.getText().trim();
        String facility = txtFacility.getText().trim();
        String nationality = txtNationality.getText().trim();
        String status = txtStatus.getText().trim();

        // Validation
        if (inmateName.isEmpty() || caseDetails.isEmpty() || dateOfBirth.isEmpty() 
                || facility.isEmpty() || nationality.isEmpty() || status.isEmpty()) {
            showAlert("All fields must be filled out.");
            return;
        }

        // Check if date of birth format is valid
        if (!dateOfBirth.matches("\\d{4}-\\d{2}-\\d{2}")) {
            showAlert("Invalid Date of Birth format. Please use YYYY-MM-DD.");
            return;
        }

        try {
            // Insert new inmate into the database
            String id = generateInmateId(); // Generate a unique ID for the inmate
            Inmate newInmate = new Inmate(id, inmateName, caseDetails, dateOfBirth, facility, nationality, status);

            // Database logic: Insert the new inmate into the database
            insertInmateIntoDatabase(newInmate);

            // Display success message
            showAlert("Inmate successfully added!");

            // Close dialog
            dialog.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error while adding inmate: " + ex.getMessage());
        }
    });

    // Add fields and the button to the layout
    dialogLayout.getChildren().addAll(
        txtInmateName, txtCaseDetails, txtDateOfBirth, txtFacility, txtNationality, txtStatus, btnSubmit
    );

    Scene scene = new Scene(dialogLayout, 400, 400);
    dialog.setScene(scene);
    dialog.show();
}



private String generateInmateId() {
    return "INM-" + System.currentTimeMillis(); 
}

// Load data from the database into the inmate table
private void insertInmateIntoDatabase(Inmate inmate) throws SQLException {
    String query = "INSERT INTO inmates (id, name, case_details, date_of_birth, facility, nationality, status) " +
                   "VALUES (?, ?, ?, ?, ?, ?, ?)";

    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
         PreparedStatement stmt = conn.prepareStatement(query)) {

        stmt.setString(1, inmate.getId());
        stmt.setString(2, inmate.getName());
        stmt.setString(3, inmate.getCaseDetails());
        stmt.setString(4, inmate.getDateOfBirth());
        stmt.setString(5, inmate.getFacility());
        stmt.setString(6, inmate.getNationality());
        stmt.setString(7, inmate.getStatus());

        stmt.executeUpdate();
    }
}


// Search inmates in the database
private void searchInmates(String keyword) {
    // Implement search functionality (similar to loadInmateData, but with a WHERE clause)
}

// Delete an inmate from the database
private void deleteInmate(String inmateId) {
    try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
        String query = "DELETE FROM inmates WHERE id = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, inmateId);
        statement.executeUpdate();

        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Inmate deleted successfully.", ButtonType.OK);
        alert.showAndWait();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

    private VBox createVisitBookingUI() {
    VBox visitLayout = new VBox(10);
    visitLayout.setStyle("-fx-padding: 20;");

    Label lblTitle = new Label("Book Visits");
    lblTitle.setStyle("-fx-font-size: 18px;");

    // TableView for visit bookings
    TableView<VisitBooking> visitTable = new TableView<>();

    // Define columns for the VisitBooking TableView
    TableColumn<VisitBooking, String> bookingIdColumn = new TableColumn<>("Booking ID");
    bookingIdColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getBookingId()));

    TableColumn<VisitBooking, String> visitorNameColumn = new TableColumn<>("Visitor Name");
    visitorNameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getVisitorName()));

    TableColumn<VisitBooking, String> inmateNameColumn = new TableColumn<>("Inmate Name");
    inmateNameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getInmateName()));

    TableColumn<VisitBooking, LocalDate> visitDateColumn = new TableColumn<>("Visit Date");
    visitDateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getVisitDate()));

    // Add columns to the table
    visitTable.getColumns().addAll(bookingIdColumn, visitorNameColumn, inmateNameColumn, visitDateColumn);

    // Load existing visit bookings
    loadVisitBookingsData(visitTable);

    // Button to book a visit
    Button btnBookVisit = new Button("Book Visit");
    btnBookVisit.setOnAction(e -> openBookVisitDialog());

    visitLayout.getChildren().addAll(lblTitle, visitTable, btnBookVisit);
    return visitLayout;
}

private void loadVisitBookingsData(TableView<VisitBooking> table) {
    ObservableList<VisitBooking> visitList = FXCollections.observableArrayList();

    // Load visit data from the database or a data source
    // For example, assuming a database or mock data:
    visitList.add(new VisitBooking("V001", "Motho Mang", "Lira Halibonoe", LocalDate.of(2025, 1, 10)));
    visitList.add(new VisitBooking("V002", "Mpheng Eena", "Khethang Tsalona", LocalDate.of(2025, 1, 12)));

    table.setItems(visitList);
}
private void openBookVisitDialog() {
    // Create a dialog to collect visit booking details
    Stage dialog = new Stage();
    dialog.initModality(Modality.APPLICATION_MODAL);
    dialog.setTitle("Book Visit");

    VBox dialogLayout = new VBox(10);
    dialogLayout.setStyle("-fx-padding: 20;");

    // Visitor name
    TextField txtVisitorName = new TextField();
    txtVisitorName.setPromptText("Visitor Name");

    // Visitor contact
    TextField txtVisitorContact = new TextField();
    txtVisitorContact.setPromptText("Visitor Contact");

    // Visitor ID number
    TextField txtVisitorIdNumber = new TextField();
    txtVisitorIdNumber.setPromptText("Visitor ID Number");

    // Inmate ID
    TextField txtInmateId = new TextField();
    txtInmateId.setPromptText("Inmate ID");

    // Visit date picker
    DatePicker visitDatePicker = new DatePicker();
    visitDatePicker.setPromptText("Visit Date");

    // Visit time picker (using TextField as JavaFX does not have a built-in time picker)
    TextField txtVisitTime = new TextField();
    txtVisitTime.setPromptText("Visit Time (HH:mm)");

    // Submit button
    Button btnSubmit = new Button("Submit");
    btnSubmit.setOnAction(e -> {
        // Collect input data
        String visitorName = txtVisitorName.getText();
        String visitorContact = txtVisitorContact.getText();
        String visitorIdNumber = txtVisitorIdNumber.getText();
        String inmateId = txtInmateId.getText();
        LocalDate visitDate = visitDatePicker.getValue();
        String visitTime = txtVisitTime.getText();

        // Validation
        if (visitorName.isEmpty() || visitorContact.isEmpty() || visitorIdNumber.isEmpty()
                || inmateId.isEmpty() || visitDate == null || visitTime.isEmpty()) {
            showAlert("All fields must be filled out.");
            return;
        }

        // Generate a unique visit tag number
        String visitTagNumber = generateVisitTagNumber();

        // Insert the visit booking into the database
        String query = "INSERT INTO visits (visitor_name, visitor_contact, visitor_id_number, inmate_id, visit_date, visit_time, visit_tag_number, created_at, updated_at) "
                     + "VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/correctional_facility_dba", "****", "******");
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, visitorName);
            pstmt.setString(2, visitorContact);
            pstmt.setString(3, visitorIdNumber);
            pstmt.setInt(4, Integer.parseInt(inmateId));
            pstmt.setDate(5, java.sql.Date.valueOf(visitDate));
            pstmt.setTime(6, java.sql.Time.valueOf(visitTime + ":00")); 
            pstmt.setString(7, visitTagNumber);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                showAlert("Visit successfully booked with Tag Number: " + visitTagNumber);
                dialog.close();
            } else {
                showAlert("Failed to book the visit. Please try again.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert("An error occurred while booking the visit: " + ex.getMessage());
        } catch (NumberFormatException ex) {
            showAlert("Invalid Inmate ID. It must be a number.");
        }
    });

    // Add all fields to the dialog layout
    dialogLayout.getChildren().addAll(
            txtVisitorName, txtVisitorContact, txtVisitorIdNumber, txtInmateId,
            visitDatePicker, txtVisitTime, btnSubmit
    );

    Scene scene = new Scene(dialogLayout, 400, 400);
    dialog.setScene(scene);
    dialog.show();
}

//private String generateBookingId() {
    // Generate a unique booking ID (for example, using a UUID or an incremental counter)
  ///  return "V" + (int) (Math.random() * 1000);
//}
private String generateVisitTagNumber() {
    return "VT" + (int)(Math.random() * 1000000); // Generate a random 6-digit number prefixed with "VT"
}

private void showAlert(String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Error");
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
}


    private VBox createInterFacilityAccessUI() {
     VBox facilityLayout = new VBox(15);
     facilityLayout.setStyle("-fx-padding: 20; -fx-background-color: #f4f4f4;");

     // Title Label
     Label lblTitle = new Label("Inter-Facility Data Access");
     lblTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333333;");

     // Facility ComboBox (dropdown for selecting facility)
     ComboBox<String> facilityDropdown = new ComboBox<>();
     facilityDropdown.getItems().addAll("Facility A", "Facility B", "Facility C");
     facilityDropdown.setPromptText("Select Facility");

     // Action button
     Button btnAccessData = new Button("Access Data");
     btnAccessData.setOnAction(e -> {
         String selectedFacility = facilityDropdown.getValue();
         if (selectedFacility != null) {
             // Logic for accessing facility data
             showAlert("Accessing data for " + selectedFacility);
         } else {
             showAlert("Please select a facility.");
         }
     });

     facilityLayout.getChildren().addAll(lblTitle, facilityDropdown, btnAccessData);
     return facilityLayout;
 }

    private VBox createCourtRollsContentUI() {
        // Create the root VBox for the court rolls content
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        // Create an Accordion to hold district court buttons
        Accordion districtsAccordion = new Accordion();

        // District names (customize as needed)
        String[] districts = {
            "Mkg Magistrate Court", "BB Magistrate Court", "Lrb Magistrate Court", "Northern regional High Court",
            "TY Magistrate Court", "Msu Magistrate Court", "Mftn Magistrate Court", "TT Magistrate Court",
            "MH Magistrate Court", "Qtg Magistrate Court", "QN Magistrate Court", "National High Court"
        };

        // Loop to create district sections
        for (String district : districts) {
            TitledPane districtPane = new TitledPane();

            // Set the title of the pane to the district name
            districtPane.setText(district);

            // Create a button to view the court roll for this district
            Button viewCourtRollButton = new Button("View Court Roll for " + district);
            viewCourtRollButton.setOnAction(e -> showCourtRollTable(district));

            // Add the button to the TitledPane content
            VBox paneContent = new VBox(10, viewCourtRollButton);
            paneContent.setPadding(new Insets(10));
            districtPane.setContent(paneContent);

            // Add the pane to the accordion
            districtsAccordion.getPanes().add(districtPane);
        }

        // Add the accordion to the root VBox
        root.getChildren().add(districtsAccordion);

        return root;
    }

    private void showCourtRollTable(String districtName) {
        // Create a new stage to display the court roll table
        Stage courtRollStage = new Stage();
        courtRollStage.setTitle("Court Roll - " + districtName);

        // Create a TableView for the court rolls
        TableView<CourtRollEntry> tableView = new TableView<>();

        // Define columns for the TableView
        TableColumn<CourtRollEntry, String> caseNumberColumn = new TableColumn<>("Case Number");
        caseNumberColumn.setCellValueFactory(data -> data.getValue().caseNumberProperty());

        TableColumn<CourtRollEntry, String> partiesColumn = new TableColumn<>("Parties Involved");
        partiesColumn.setCellValueFactory(data -> data.getValue().partiesProperty());

        TableColumn<CourtRollEntry, String> natureColumn = new TableColumn<>("Nature of Case");
        natureColumn.setCellValueFactory(data -> data.getValue().natureProperty());

        TableColumn<CourtRollEntry, String> dateTimeColumn = new TableColumn<>("Date & Time");
        dateTimeColumn.setCellValueFactory(data -> data.getValue().dateTimeProperty());

        TableColumn<CourtRollEntry, String> courtroomColumn = new TableColumn<>("Courtroom");
        courtroomColumn.setCellValueFactory(data -> data.getValue().courtroomProperty());

        TableColumn<CourtRollEntry, String> judgesColumn = new TableColumn<>("Judges/Magistrates");
        judgesColumn.setCellValueFactory(data -> data.getValue().judgesProperty());

        // Add columns to the TableView
        tableView.getColumns().addAll(
                caseNumberColumn, partiesColumn, natureColumn, dateTimeColumn, courtroomColumn, judgesColumn
        );

        // Sample data (Replace with actual data from your database)
        ObservableList<CourtRollEntry> courtRollData = FXCollections.observableArrayList(
            new CourtRollEntry("C1234", "Phako vs Jane", "Civil", "2025-01-15 10:00", "Courtroom 1", "Judge Mokone"),
            new CourtRollEntry("C5678", "State vs Thabane", "Criminal", "2025-01-16 14:00", "Courtroom 2", "Magistrate Malikete")
        );

        tableView.setItems(courtRollData);

        // Create a layout and add the TableView
        VBox layout = new VBox(10, tableView);
        layout.setPadding(new Insets(10));

        // Set the scene and show the stage
        Scene scene = new Scene(layout, 800, 400);
        courtRollStage.setScene(scene);
        courtRollStage.show();
    }

    // CourtRollEntry class for TableView data
    public static class CourtRollEntry {
        private final SimpleStringProperty caseNumber;
        private final SimpleStringProperty parties;
        private final SimpleStringProperty nature;
        private final SimpleStringProperty dateTime;
        private final SimpleStringProperty courtroom;
        private final SimpleStringProperty judges;

        public CourtRollEntry(String caseNumber, String parties, String nature, String dateTime, String courtroom, String judges) {
            this.caseNumber = new SimpleStringProperty(caseNumber);
            this.parties = new SimpleStringProperty(parties);
            this.nature = new SimpleStringProperty(nature);
            this.dateTime = new SimpleStringProperty(dateTime);
            this.courtroom = new SimpleStringProperty(courtroom);
            this.judges = new SimpleStringProperty(judges);
        }

        public SimpleStringProperty caseNumberProperty() { return caseNumber; }
        public SimpleStringProperty partiesProperty() { return parties; }
        public SimpleStringProperty natureProperty() { return nature; }
        public SimpleStringProperty dateTimeProperty() { return dateTime; }
        public SimpleStringProperty courtroomProperty() { return courtroom; }
        public SimpleStringProperty judgesProperty() { return judges; }
    }

    private HBox createHeader() {
        HBox header = new HBox(10);
        header.setStyle("-fx-padding: 10; -fx-background-color: #2c3e50; -fx-alignment: center;");

        Image logoImage = new Image("file:///C:/Users/Limpho%20Mokone/Documents/Bata27/PRISONS%20B/correctionalmanagement/justice.png");  
       ImageView logo = new ImageView(logoImage);
       logo.setFitHeight(45);  // Adjust logo height as needed
       logo.setPreserveRatio(true);

        Label lblTitle = new Label("Correctional Facility Management System");
        lblTitle.setStyle("-fx-text-fill: orange; -fx-font-size: 20px;");

        header.getChildren().addAll(logo, lblTitle);
        return header;
    }

   private HBox createFooter() {
    // Create the HBox container for the footer
    HBox footer = new HBox(20);
    footer.setStyle("-fx-padding: 10; -fx-background-color: #34495e; -fx-alignment: center;");

    // Add the logo image
    Image logoImage = new Image("file:///C:/Users/Limpho%20Mokone/Documents/Bata27/PRISONS%20B/correctionalmanagement/Bata27.png");
ImageView logo = new ImageView(logoImage);
    logo.setFitHeight(30);  // Adjust logo height as needed
    logo.setPreserveRatio(true);

    // Create the footer label
    Label lblFooter = new Label("Powered by Bata27 | V1.0 2025");
    lblFooter.setStyle("-fx-text-fill: white;");

    // Create social media icons (as Hyperlinks with images)
    Hyperlink fbLink = createSocialMediaLink("https://https://www.facebook.com/profile.php?id=100005929066817", "fb.png");
    Hyperlink twitterLink = createSocialMediaLink("https://https://x.com/Iam_FrankM", "x.png");
    Hyperlink githubLink = createSocialMediaLink("https://https://github.com/bata27", "github.png");
    Hyperlink linkedinLink = createSocialMediaLink("https://https://www.linkedin.com/in/mokone-limpho-b21318102/", "l.png");

    // Add the logo, label, and social media links to the footer
    footer.getChildren().addAll(logo, lblFooter, fbLink, twitterLink, githubLink, linkedinLink);

    return footer;
}

// Helper method to create social media links with icons
private Hyperlink createSocialMediaLink(String url, String iconPath) {
    Image iconImage = new Image("file:///C:/Users/Limpho%20Mokone/Documents/Bata27/PRISONS%20B/correctionalmanagement/icons/" + iconPath);
ImageView icon = new ImageView(iconImage);
    icon.setFitHeight(20);  // Adjust icon size as needed
    icon.setPreserveRatio(true);

    Hyperlink link = new Hyperlink();
    link.setGraphic(icon);
    link.setOnAction(e -> getHostServices().showDocument(url));  // Opens the link in the default browser
    return link;
}


   

 

    public static void main(String[] args) {
        launch(args);
    }
}

