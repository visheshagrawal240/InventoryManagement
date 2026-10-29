import java.sql.*;
import java.util.Scanner;

public class main {

    // Database connection details
    static final String URL =
            "jdbc:mysql://localhost:3306/retail_db";

    static final String USER = "root";

    // Replace with your actual MySQL password
    static final String PASSWORD = "Vishesh@11";

    static Scanner sc = new Scanner(System.in);

    // Establish JDBC connection
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void main(String[] args) {

        try {
            // Check database connection
            try (Connection con = getConnection()) {
                System.out.println("Connected to MySQL successfully!");
            }

            int choice;

            do {
                System.out.println("\n===== INVENTORY MANAGEMENT SYSTEM =====");
                System.out.println("1. Insert Three Products");
                System.out.println("2. Retrieve Products by Category");
                System.out.println("3. Update Product Stock");
                System.out.println("4. Search and Update Product Price");
                System.out.println("5. Display Total Inventory Value");
                System.out.println("6. Delete Product if Quantity is Zero");
                System.out.println("0. Exit");

                System.out.print("Enter your choice: ");
                choice = sc.nextInt();
                sc.nextLine();

                switch (choice) {
                    case 1:
                        insertProducts();
                        break;

                    case 2:
                        retrieveProducts();
                        break;

                    case 3:
                        updateStock();
                        break;

                    case 4:
                        updatePrice();
                        break;

                    case 5:
                        displayInventoryValue();
                        break;

                    case 6:
                        deleteProduct();
                        break;

                    case 0:
                        System.out.println("Exiting program...");
                        break;

                    default:
                        System.out.println("Invalid choice!");
                }

            } while (choice != 0);

        } catch (SQLException e) {
            System.out.println("Database connection failed!");
            System.out.println("Error: " + e.getMessage());
        }

        sc.close();
    }

    // (a) Insert three product records
    public static void insertProducts() {

        String sql = "INSERT INTO Product " +
                "(product_id, product_name, category, quantity, price) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 1; i <= 3; i++) {

                System.out.println("\nEnter details for Product " + i);

                System.out.print("Product ID: ");
                int id = sc.nextInt();
                sc.nextLine();

                System.out.print("Product Name: ");
                String name = sc.nextLine();

                System.out.print("Category: ");
                String category = sc.nextLine();

                System.out.print("Quantity: ");
                int quantity = sc.nextInt();

                System.out.print("Price: ");
                double price = sc.nextDouble();
                sc.nextLine();

                ps.setInt(1, id);
                ps.setString(2, name);
                ps.setString(3, category);
                ps.setInt(4, quantity);
                ps.setDouble(5, price);

                try {
                    ps.executeUpdate();
                    System.out.println("Product inserted successfully!");

                } catch (SQLIntegrityConstraintViolationException e) {
                    System.out.println(
                            "Duplicate product ID! Product not inserted."
                    );
                }
            }

        } catch (SQLException e) {
            System.out.println("Insertion error: " + e.getMessage());
        }
    }

    // (b) Retrieve products by category and sort by price
    public static void retrieveProducts() {

        String sql = "SELECT * FROM Product " +
                "WHERE category = ? AND quantity < 20 " +
                "ORDER BY price DESC";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            System.out.print("Enter category: ");
            String category = sc.nextLine();

            ps.setString(1, category);

            try (ResultSet rs = ps.executeQuery()) {

                boolean found = false;

                while (rs.next()) {
                    found = true;

                    System.out.println("\nProduct ID: "
                            + rs.getInt("product_id"));

                    System.out.println("Product Name: "
                            + rs.getString("product_name"));

                    System.out.println("Category: "
                            + rs.getString("category"));

                    System.out.println("Quantity: "
                            + rs.getInt("quantity"));

                    System.out.println("Price: "
                            + rs.getDouble("price"));
                }

                if (!found) {
                    System.out.println("No matching products found.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Retrieval error: " + e.getMessage());
        }
    }

    // (c) Add quantity to existing stock
    public static void updateStock() {

        String sql = "UPDATE Product " +
                "SET quantity = quantity + ? " +
                "WHERE product_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            System.out.print("Enter product ID: ");
            int id = sc.nextInt();

            System.out.print("Enter quantity to add: ");
            int quantity = sc.nextInt();
            sc.nextLine();

            if (quantity <= 0) {
                System.out.println("Quantity to add must be positive.");
                return;
            }

            ps.setInt(1, quantity);
            ps.setInt(2, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Stock updated successfully!");
            } else {
                System.out.println("Product does not exist.");
            }

        } catch (SQLException e) {
            System.out.println("Stock update error: " + e.getMessage());
        }
    }

    // (d) Search product and update its price
    public static void updatePrice() {

        System.out.print("Enter product ID: ");
        int id = sc.nextInt();

        System.out.print("Enter new price: ");
        double newPrice = sc.nextDouble();
        sc.nextLine();

        if (newPrice <= 0) {
            System.out.println("Price must be greater than zero.");
            return;
        }

        String searchSql =
                "SELECT * FROM Product WHERE product_id = ?";

        String updateSql =
                "UPDATE Product SET price = ? WHERE product_id = ?";

        try (Connection con = getConnection();
             PreparedStatement searchPs =
                     con.prepareStatement(searchSql)) {

            searchPs.setInt(1, id);

            try (ResultSet rs = searchPs.executeQuery()) {

                if (rs.next()) {

                    System.out.println("\nProduct found!");
                    System.out.println("Product Name: "
                            + rs.getString("product_name"));

                    System.out.println("Old Price: "
                            + rs.getDouble("price"));

                    try (PreparedStatement updatePs =
                                 con.prepareStatement(updateSql)) {

                        updatePs.setDouble(1, newPrice);
                        updatePs.setInt(2, id);

                        updatePs.executeUpdate();
                    }

                    System.out.println("Price updated successfully!");
                    System.out.println("Updated Price: " + newPrice);

                } else {
                    System.out.println("Product does not exist.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Price update error: " + e.getMessage());
        }
    }

    // (e) Calculate total inventory value
    public static void displayInventoryValue() {

        String sql =
                "SELECT SUM(quantity * price) AS total_value FROM Product";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                double total = rs.getDouble("total_value");

                if (rs.wasNull()) {
                    total = 0.0;
                }

                System.out.println("Total Inventory Value: " + total);
            }

        } catch (SQLException e) {
            System.out.println("Calculation error: " + e.getMessage());
        }
    }

    // (e) Delete only when product quantity is zero
    public static void deleteProduct() {

        String sql = "DELETE FROM Product " +
                "WHERE product_id = ? AND quantity = 0";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            System.out.print("Enter product ID to delete: ");
            int id = sc.nextInt();
            sc.nextLine();

            ps.setInt(1, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Product deleted successfully!");
            } else {

                String checkSql =
                        "SELECT quantity FROM Product WHERE product_id = ?";

                try (PreparedStatement checkPs =
                             con.prepareStatement(checkSql)) {

                    checkPs.setInt(1, id);

                    try (ResultSet rs = checkPs.executeQuery()) {

                        if (rs.next()) {
                            System.out.println(
                                    "Product cannot be deleted because " +
                                            "its quantity is not zero."
                            );
                        } else {
                            System.out.println("Product does not exist.");
                        }
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("Deletion error: " + e.getMessage());
        }
    }
}