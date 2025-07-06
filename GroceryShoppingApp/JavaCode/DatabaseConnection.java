package GroceryShoppingApp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseConnection {
    private static final String URL = "jdbc:sqlserver://DESKTOP-AIJAGOR\\SQLEXPRESS;databaseName=GroceryDB;encrypt=true;trustServerCertificate=true";
    private static final String USERNAME = System.getenv("DB_USERNAME") != null ? System.getenv("DB_USERNAME") : "sa";
    private static final String PASSWORD = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "1234";
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());

    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                LOGGER.info("Database connection established successfully.");
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to connect to the database: " + e.getMessage(), e);
                throw e;
            }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                LOGGER.info("Database connection closed.");
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing database connection: " + e.getMessage(), e);
            }
        }
    }

    public static void initializeDatabase() {
        String[] sqlStatements = {
                "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='users' AND xtype='U') " +
                        "CREATE TABLE users (id INT IDENTITY(1,1) PRIMARY KEY, username VARCHAR(50) UNIQUE, password VARCHAR(50), user_type VARCHAR(20))",

                "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='customers' AND xtype='U') " +
                        "CREATE TABLE customers (id INT PRIMARY KEY, name VARCHAR(100), phone VARCHAR(15), address VARCHAR(255), pickup_location VARCHAR(255), wallet_balance FLOAT, FOREIGN KEY (id) REFERENCES users(id))",

                "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='agents' AND xtype='U') " +
                        "CREATE TABLE agents (id INT PRIMARY KEY, name VARCHAR(100), phone VARCHAR(15), FOREIGN KEY (id) REFERENCES users(id))",

                "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='supermarkets' AND xtype='U') " +
                        "CREATE TABLE supermarkets (id INT IDENTITY(1,1) PRIMARY KEY, name VARCHAR(100), owner_id INT, location VARCHAR(255), FOREIGN KEY (owner_id) REFERENCES users(id))",

                "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='supermarket_owners' AND xtype='U') " +
                        "CREATE TABLE supermarket_owners (id INT PRIMARY KEY, name VARCHAR(100), phone VARCHAR(15), supermarket_id INT, FOREIGN KEY (id) REFERENCES users(id), FOREIGN KEY (supermarket_id) REFERENCES supermarkets(id))",

                "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='products' AND xtype='U') " +
                        "CREATE TABLE products (id INT IDENTITY(1,1) PRIMARY KEY, name VARCHAR(100), price FLOAT, available BIT, stock_quantity INT, supermarket_id INT, FOREIGN KEY (supermarket_id) REFERENCES supermarkets(id))",

                "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='orders' AND xtype='U') " +
                        "CREATE TABLE orders (id INT IDENTITY(1,1) PRIMARY KEY, customer_id INT, agent_id INT, supermarket_id INT, total FLOAT, status VARCHAR(20), order_date DATETIME, FOREIGN KEY (customer_id) REFERENCES customers(id), FOREIGN KEY (agent_id) REFERENCES agents(id), FOREIGN KEY (supermarket_id) REFERENCES supermarkets(id))",

                "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='order_items' AND xtype='U') " +
                        "CREATE TABLE order_items (id INT IDENTITY(1,1) PRIMARY KEY, order_id INT, product_id INT, quantity INT, subtotal FLOAT, FOREIGN KEY (order_id) REFERENCES orders(id), FOREIGN KEY (product_id) REFERENCES products(id))",

                "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='invoices' AND xtype='U') " +
                        "CREATE TABLE invoices (id INT IDENTITY(1,1) PRIMARY KEY, order_id INT, supermarket_id INT, amount FLOAT, invoice_details TEXT, upload_date DATETIME, FOREIGN KEY (order_id) REFERENCES orders(id), FOREIGN KEY (supermarket_id) REFERENCES supermarkets(id))",

                "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='deliveries' AND xtype='U') " +
                        "CREATE TABLE deliveries (id INT IDENTITY(1,1) PRIMARY KEY, order_id INT, agent_id INT, delivery_status VARCHAR(20), delivery_date DATETIME, FOREIGN KEY (order_id) REFERENCES orders(id), FOREIGN KEY (agent_id) REFERENCES agents(id))"
        };

        try (Connection conn = getConnection(); java.sql.Statement stmt = conn.createStatement()) {
            for (String sql : sqlStatements) {
                stmt.execute(sql);
            }
            LOGGER.info("Database schema initialized successfully.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error initializing database schema: " + e.getMessage(), e);
        }
    }
}