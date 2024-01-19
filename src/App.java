import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "adminpass";
    private Admin adminUser;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/final";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";
    private MenuBar menuBar;

    private Stage primaryStage;
    private BorderPane root;
    private ObservableList<Product> products = FXCollections.observableArrayList();
    private ComboBox<String> productComboBox;
    private ComboBox<String> deleteProductComboBox;

    private TextField updateProductIdField;
    private TextField updateNameField;
    private TextField updateDescriptionField;
    private TextField updatePriceField;
    private TextField updateStockQuantityField;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("E-commerce App Admin");
    
        root = new BorderPane();
        createMenuBar(); // Initially create a simple menu bar
        showLoginScene();
    
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    // Create a simple menu bar initially
    private void createMenuBar() {
        menuBar = new MenuBar();
        Menu adminMenu = new Menu("Admin");
    
        MenuItem logoutItem = new MenuItem("Logout");
        logoutItem.setOnAction(event -> logout());
    
        adminMenu.getItems().add(logoutItem);
        menuBar.getMenus().add(adminMenu);
        root.setTop(menuBar);
    }
    
    // Create an admin-specific menu bar with product options
    private void createAdminMenuBar() {
        menuBar.getMenus().clear();
        Menu productMenu = new Menu("Product");
    
        MenuItem viewProductsItem = new MenuItem("View Products");
        MenuItem addProductItem = new MenuItem("Add Product");
        MenuItem updateProductItem = new MenuItem("Update Product");
        MenuItem deleteProductItem = new MenuItem("Delete Product");
    
        viewProductsItem.setOnAction(event -> showViewProductsScene());
        addProductItem.setOnAction(event -> showAddProductScene());
        updateProductItem.setOnAction(event -> showUpdateProductScene());
        deleteProductItem.setOnAction(event -> showDeleteProductScene());
    
        productMenu.getItems().addAll(viewProductsItem, addProductItem, updateProductItem, deleteProductItem);
      // Add Logout option
    MenuItem logoutItem = new MenuItem("Logout");
    logoutItem.setOnAction(event -> logout());

    Menu adminMenu = new Menu("Profile");
    adminMenu.getItems().addAll(logoutItem);

    menuBar.getMenus().addAll(adminMenu, productMenu);
    }
    
    // Modify the logout method
    private void logout() {
        adminUser = null;
        menuBar.getMenus().clear(); // Clear all menus
        createMenuBar(); // Recreate a simple menu bar
        showLoginScene();
    }
    private void showLoginScene() {
        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        Button loginButton = new Button("Login");
    
        VBox loginLayout = new VBox(10, new Label("Username:"), usernameField, new Label("Password:"), passwordField, loginButton);
        loginLayout.setPadding(new Insets(10));
    
        loginButton.setOnAction(event -> {
            String enteredUsername = usernameField.getText();
            String enteredPassword = passwordField.getText();
    
            if (validateAdminCredentials(enteredUsername, enteredPassword)) {
                adminUser = new Admin(enteredUsername, enteredPassword);
                showAdminOptions();
            } else {
                showAlert("Login Failed", "Invalid username or password. Please try again.");
            }
        });
    
        root.setCenter(loginLayout);
    }
    
    // Modify the showAdminOptions method
    private void showAdminOptions() {
        root.setCenter(null);
         // Clear previous content
        createAdminMenuBar(); // Recreate admin-specific menu bar
    
        // Placeholder message for the admin home screen
        Label adminHomeLabel = new Label("Welcome, Admin!\nPlease select an option from the menu.");
    
        root.setCenter(adminHomeLabel);
    }

    private void showViewProductsScene() {
        root.setCenter(null); // Clear previous content
        createAdminMenuBar(); 

        TableView<Product> productTableView = createProductTableView();
        loadProductsFromDatabase(productTableView);

        root.setCenter(productTableView);
    }

    private void showAddProductScene() {
        TextField productIdField = new TextField();
        TextField nameField = new TextField();
        TextField descriptionField = new TextField();
        TextField priceField = new TextField();
        TextField stockQuantityField = new TextField();
        
        Button addButton = new Button("Add Product");
    
        VBox addProductLayout = new VBox(10,
                new Label("Product ID:"), productIdField,
                new Label("Name:"), nameField,
                new Label("Description:"), descriptionField,
                new Label("Price:"), priceField,
                new Label("Stock Quantity:"), stockQuantityField,
                addButton);
    
        addProductLayout.setPadding(new Insets(10));
    
        addButton.setOnAction(event -> {
            String productId = productIdField.getText();
            String name = nameField.getText();
            String description = descriptionField.getText();
            double price = Double.parseDouble(priceField.getText());
            int stockQuantity = Integer.parseInt(stockQuantityField.getText());
    
            Product newProduct = new Product(productId, name, description, price, stockQuantity, null);
            addProductToDatabase(newProduct);
    
            showAlert("Add Product", "Product added successfully.");
        });
    
        root.setCenter(addProductLayout);
    }
    
    private void addProductToDatabase(Product product) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement()) {
            String query = String.format("INSERT INTO product (ProductID, Name, Description, Price, StockQuantity) VALUES ('%s', '%s', '%s', %.2f, %d)",
                    product.getProductId(), product.getName(), product.getDescription(), product.getPrice(), product.getStockQuantity());
    
            statement.executeUpdate(query);
            products.add(product); // Add the new product to the observable list
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    



    private void showUpdateProductScene() {
        root.setCenter(null); // Clear previous content
        createAdminMenuBar(); 
    
        Label updateProductLabel = new Label("Update Product");
    
        productComboBox = new ComboBox<>();
        productComboBox.setPromptText("Select Product");
        productComboBox.getItems().addAll(getProductNames());
        productComboBox.setOnAction(event -> populateProductDetails());
    
        updateProductIdField = new TextField();
        updateNameField = new TextField();
        updateDescriptionField = new TextField();
        updatePriceField = new TextField();
        updateStockQuantityField = new TextField();
    
        Button updateButton = new Button("Update");
    
        VBox updateLayout = new VBox(10, updateProductLabel, productComboBox, new Label("Product ID:"), updateProductIdField,
                new Label("Name:"), updateNameField, new Label("Description:"), updateDescriptionField,
                new Label("Price:"), updatePriceField, new Label("Stock Quantity:"), updateStockQuantityField, updateButton);
    
        updateButton.setOnAction(event -> updateProduct());
    
        root.setCenter(updateLayout);
    }
    
    private ObservableList<String> getProductNames() {
        ObservableList<String> productNames = FXCollections.observableArrayList();
        // Fetch product names from the database
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement()) {
            String query = "SELECT Name FROM product";
            ResultSet resultSet = statement.executeQuery(query);
    
            while (resultSet.next()) {
                String productName = resultSet.getString("Name");
                productNames.add(productName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productNames;
    }
    
    private void populateProductDetails() {
        String selectedProductName = productComboBox.getValue();
        if (selectedProductName != null) {
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 Statement statement = connection.createStatement()) {
                String query = String.format("SELECT * FROM product WHERE Name = '%s'", selectedProductName);
                ResultSet resultSet = statement.executeQuery(query);
    
                if (resultSet.next()) {
                    String productId = resultSet.getString("ProductID");
                    String name = resultSet.getString("Name");
                    String description = resultSet.getString("Description");
                    double price = resultSet.getDouble("Price");
                    int stockQuantity = resultSet.getInt("StockQuantity");
    
                    updateProductIdField.setText(productId);
                    updateNameField.setText(name);
                    updateDescriptionField.setText(description);
                    updatePriceField.setText(String.valueOf(price));
                    updateStockQuantityField.setText(String.valueOf(stockQuantity));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void showDeleteProductScene() {
        root.setCenter(null); // Clear previous content
        createAdminMenuBar(); // Recreate menu bar
    
        Label deleteProductLabel = new Label("Delete Product");
    
        deleteProductComboBox = new ComboBox<>();
        deleteProductComboBox.setPromptText("Select Product");
        deleteProductComboBox.getItems().addAll(getProductNames());
    
        Button deleteButton = new Button("Delete");
    
        VBox deleteLayout = new VBox(10, deleteProductLabel, deleteProductComboBox, deleteButton);
    
        deleteButton.setOnAction(event -> deleteProduct());
    
        root.setCenter(deleteLayout);
    }
    
    private void deleteProduct() {
        String selectedProductName = deleteProductComboBox.getValue();
        if (selectedProductName != null) {
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 Statement statement = connection.createStatement()) {
                String query = String.format("DELETE FROM product WHERE Name = '%s'", selectedProductName);
                int affectedRows = statement.executeUpdate(query);
    
                if (affectedRows > 0) {
                    showAlert("Product Deleted", "The product has been successfully deleted.");
                    deleteProductComboBox.getItems().clear();
                    deleteProductComboBox.getItems().addAll(getProductNames());
                } else {
                    showAlert("Deletion Failed", "Failed to delete the product. Please try again.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            showAlert("Selection Required", "Please select a product to delete.");
        }
    }

    private TableView<Product> createProductTableView() {
        TableView<Product> productTableView = new TableView<>();
        TableColumn<Product, String> productIdColumn = new TableColumn<>("Product ID");
        TableColumn<Product, String> nameColumn = new TableColumn<>("Name");
        TableColumn<Product, String> descriptionColumn = new TableColumn<>("Description");
        TableColumn<Product, Double> priceColumn = new TableColumn<>("Price");
        TableColumn<Product, Integer> stockQuantityColumn = new TableColumn<>("Stock Quantity");
        TableColumn<Product, java.sql.Timestamp> createdDateColumn = new TableColumn<>("Created Date");

        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        stockQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        createdDateColumn.setCellValueFactory(new PropertyValueFactory<>("createdDate"));

        productTableView.getColumns().addAll(productIdColumn, nameColumn, descriptionColumn,
                priceColumn, stockQuantityColumn, createdDateColumn);

        return productTableView;
    }

    private void loadProductsFromDatabase(TableView<Product> productTableView) {
        products.clear();
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement()) {
            String query = "SELECT * FROM product";
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                String productId = resultSet.getString("ProductID");
                String name = resultSet.getString("Name");
                String description = resultSet.getString("Description");
                double price = resultSet.getDouble("Price");
                int stockQuantity = resultSet.getInt("StockQuantity");
                java.sql.Timestamp createdDate = resultSet.getTimestamp("CreatedDate");

                Product product = new Product(productId, name, description, price, stockQuantity, createdDate);
                products.add(product);

            }

            productTableView.setItems(products);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean validateAdminCredentials(String username, String password) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement()) {
            String query = String.format("SELECT * FROM user WHERE Username = '%s' AND Password = '%s' AND IsAdmin = 1",
                    username, password);
            ResultSet resultSet = statement.executeQuery(query);
            return resultSet.next(); // If there is a result, the admin credentials are valid
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
     // Admin class representing an admin user
     public static class Admin {
        private final String username;
        private final String password;

        public Admin(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static class Product {
        private final String productId;
        private final String name;
        private final String description;
        private final double price;
        private final int stockQuantity;
        private final java.sql.Timestamp createdDate;

        public Product(String productId, String name, String description, double price, int stockQuantity, java.sql.Timestamp createdDate) {
            this.productId = productId;
            this.name = name;
            this.description = description;
            this.price = price;
            this.stockQuantity = stockQuantity;
            this.createdDate = createdDate;
        }

        public String getProductId() {
            return productId;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public double getPrice() {
            return price;
        }

        public int getStockQuantity() {
            return stockQuantity;
        }

        public java.sql.Timestamp getCreatedDate() {
            return createdDate;
        }
    }

    private void updateProduct() {
        String productId = updateProductIdField.getText();
        String name = updateNameField.getText();
        String description = updateDescriptionField.getText();
        String priceText = updatePriceField.getText();
        String stockQuantityText = updateStockQuantityField.getText();

        try {
            double price = Double.parseDouble(priceText);
            int stockQuantity = Integer.parseInt(stockQuantityText);

            updateProductInDatabase(productId, name, description, price, stockQuantity);
            showAlert("Product Updated", "Product details updated successfully!");
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter valid numeric values for Price and Stock Quantity.");
        }
    }

    private void updateProductInDatabase(String productId, String name, String description, double price, int stockQuantity) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement()) {
            String query = String.format("UPDATE product SET Name = '%s', Description = '%s', Price = %.2f, StockQuantity = %d WHERE ProductID = '%s'",
                    name, description, price, stockQuantity, productId);

            statement.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


