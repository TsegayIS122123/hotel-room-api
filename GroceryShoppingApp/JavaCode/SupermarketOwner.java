package GroceryShoppingApp;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

class SupermarketOwner extends User {
    private String name;
    private String phone;
    private Supermarket supermarket;
    private String supermarketName;

    public SupermarketOwner() {
        super();
        name = "";
        phone = "";
        supermarket = null;
        supermarketName = "";
    }

    public void register(String u, String p, String n, String ph, String supermarketName) {
        setCredentials(u, p);
        name = n;
        phone = ph;
        this.supermarketName = supermarketName;
        this.supermarket = new Supermarket();
        this.supermarket.setName(supermarketName);
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getSupermarketName() { return supermarketName; }
    public void setSupermarketName(String supermarketName) { this.supermarketName = supermarketName; }
    public Supermarket getSupermarket() { return supermarket; }
    public void setSupermarket(Supermarket supermarket) { this.supermarket = supermarket; }

    @Override
    public void showMenu(JFrame parentFrame) {
        JFrame frame = new JFrame("Supermarket Owner Menu");
        frame.setSize(400, 300);
        frame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JButton addProductButton = new JButton("Add Product");
        gbc.gridx = 0;
        gbc.gridy = 0;
        addProductButton.addActionListener(e -> addProduct(frame));
        frame.add(addProductButton, gbc);

        JButton updateAvailabilityButton = new JButton("Update Product Availability");
        gbc.gridy = 1;
        updateAvailabilityButton.addActionListener(e -> updateAvailability(frame));
        frame.add(updateAvailabilityButton, gbc);

        JButton viewOrdersButton = new JButton("View Incoming Orders");
        gbc.gridy = 2;
        viewOrdersButton.addActionListener(e -> viewOrders(frame));
        frame.add(viewOrdersButton, gbc);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void addProduct(JFrame frame) {
        String productName = JOptionPane.showInputDialog(frame, "Enter Product Name:");
        if (productName != null && !productName.isEmpty()) {
            String priceStr = JOptionPane.showInputDialog(frame, "Enter Product Price:");
            String stockStr = JOptionPane.showInputDialog(frame, "Enter Initial Stock Quantity:");
            try {
                double price = Double.parseDouble(priceStr);
                int stock = Integer.parseInt(stockStr);
                if (stock < 0) throw new Exception("Stock quantity cannot be negative.");
                supermarket.addProduct(new Product(productName, price, true));
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("INSERT INTO products (name, price, available, stock_quantity, supermarket_id) VALUES (?, ?, ?, ?, (SELECT id FROM supermarkets WHERE name = ?))")) {
                    pstmt.setString(1, productName);
                    pstmt.setDouble(2, price);
                    pstmt.setBoolean(3, true);
                    pstmt.setInt(4, stock);
                    pstmt.setString(5, supermarketName);
                    pstmt.executeUpdate();
                }
                JOptionPane.showMessageDialog(frame, "Product added: " + productName);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(frame, "Invalid price or stock quantity.");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(frame, "Error adding product: " + e.getMessage());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage());
            }
        }
    }

    private void updateAvailability(JFrame frame) {
        String productName = JOptionPane.showInputDialog(frame, "Enter Product Name:");
        if (productName != null && !productName.isEmpty()) {
            String availableStr = JOptionPane.showInputDialog(frame, "Is Available? (true/false):");
            try {
                boolean available = Boolean.parseBoolean(availableStr);
                supermarket.updateProductAvailability(productName, available);
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("UPDATE products SET available = ? WHERE name = ? AND supermarket_id = (SELECT id FROM supermarkets WHERE name = ?)")) {
                    pstmt.setBoolean(1, available);
                    pstmt.setString(2, productName);
                    pstmt.setString(3, supermarketName);
                    pstmt.executeUpdate();
                }
                JOptionPane.showMessageDialog(frame, "Availability updated for " + productName);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(frame, "Error updating availability: " + e.getMessage());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage());
            }
        }
    }

    private void viewOrders(JFrame frame) {
        StringBuilder orderList = new StringBuilder("Incoming Orders:\n");
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT o.id, o.status FROM orders o JOIN supermarkets s ON o.supermarket_id = s.id WHERE s.name = ?")) {
            pstmt.setString(1, supermarketName);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                orderList.append("Order ID: ").append(rs.getInt("id")).append(", Status: ").append(rs.getString("status")).append("\n");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error fetching orders: " + e.getMessage());
            return;
        }
        JOptionPane.showMessageDialog(frame, orderList.toString());
    }
}