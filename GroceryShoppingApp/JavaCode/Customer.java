package GroceryShoppingApp;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

class Customer extends User {
    private String name;
    private String phone;
    private String address;
    private String pickupLocation;
    private DigitalWallet wallet;
    private Order[] orders;
    private int orderCount;
    private static final int MAX_ORDERS = 10;

    public Customer() {
        super();
        this.name = "";
        this.phone = "";
        this.address = "";
        this.pickupLocation = "";
        this.wallet = new DigitalWallet(0.0);
        this.orders = new Order[MAX_ORDERS];
        this.orderCount = 0;
    }

    public void register(String username, String password, String name, String phone, String address, String pickupLocation, double initialBalance) {
        setCredentials(username, password);
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.pickupLocation = pickupLocation;
        this.wallet = new DigitalWallet(initialBalance);
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }
    public DigitalWallet getWallet() { return wallet; }
    public void setWallet(DigitalWallet wallet) { this.wallet = wallet; }

    public void placeOrder(Order order) {
        try {
            if (orderCount >= MAX_ORDERS) throw new Exception("Order limit reached.");
            if (!wallet.pay(order.getTotal())) throw new Exception("Payment failed: Insufficient balance.");
            orders[orderCount++] = order;
            JOptionPane.showMessageDialog(null, "Order placed! Total: $" + order.getTotal() + "\nOrder ID: " + order.getOrderId());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    public String viewInvoices(String orderId) {
        for (int i = 0; i < orderCount; i++)
            if (orders[i] != null && orders[i].getOrderId().equals(orderId)) return orders[i].getInvoice();
        return "Order not found.";
    }

    public void confirmDelivery(String orderId) {
        for (int i = 0; i < orderCount; i++) {
            if (orders[i] != null && orders[i].getOrderId().equals(orderId)) {
                orders[i].updateStatus("Delivered");
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("UPDATE orders SET status = ? WHERE id = ?")) {
                    pstmt.setString(1, "Delivered");
                    pstmt.setString(2, orderId);
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(null, "Error updating order status: " + e.getMessage());
                    return;
                }
                JOptionPane.showMessageDialog(null, "Delivery confirmed for order " + orderId);
                return;
            }
        }
        JOptionPane.showMessageDialog(null, "Order not found.");
    }

    public void trackOrder(String orderId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT status FROM orders WHERE id = ?")) {
            pstmt.setString(1, orderId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                JOptionPane.showMessageDialog(null, "Order Status: " + rs.getString("status"));
                return;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error tracking order: " + e.getMessage());
            return;
        }
        JOptionPane.showMessageDialog(null, "Order not found.");
    }

    @Override
    public void showMenu(JFrame parentFrame) {
        JFrame frame = new JFrame("Customer Menu");
        frame.setSize(400, 400);
        frame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JButton browseButton = new JButton("Browse Products");
        gbc.gridx = 0;
        gbc.gridy = 0;
        browseButton.addActionListener(e -> browseProducts(frame));
        frame.add(browseButton, gbc);

        JButton trackButton = new JButton("Track Order");
        gbc.gridy = 1;
        trackButton.addActionListener(e -> {
            String orderId = JOptionPane.showInputDialog(frame, "Enter Order ID:");
            if (orderId != null && !orderId.isEmpty()) trackOrder(orderId);
        });
        frame.add(trackButton, gbc);

        JButton viewInvoiceButton = new JButton("View Invoices");
        gbc.gridy = 2;
        viewInvoiceButton.addActionListener(e -> {
            String orderId = JOptionPane.showInputDialog(frame, "Enter Order ID:");
            if (orderId != null && !orderId.isEmpty()) JOptionPane.showMessageDialog(frame, viewInvoices(orderId));
        });
        frame.add(viewInvoiceButton, gbc);

        JButton confirmButton = new JButton("Confirm Delivery");
        gbc.gridy = 3;
        confirmButton.addActionListener(e -> {
            String orderId = JOptionPane.showInputDialog(frame, "Enter Order ID to confirm delivery:");
            if (orderId != null && !orderId.isEmpty()) confirmDelivery(orderId);
        });
        frame.add(confirmButton, gbc);

        JButton depositButton = new JButton("Deposit to Wallet");
        gbc.gridy = 4;
        depositButton.addActionListener(e -> {
            String amountStr = JOptionPane.showInputDialog(frame, "Enter amount to deposit:");
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount > 0) {
                    wallet.deposit(amount);
                    try (Connection conn = DatabaseConnection.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement("UPDATE customers SET wallet_balance = ? WHERE id = (SELECT id FROM users WHERE username = ?)")) {
                        pstmt.setDouble(1, wallet.getBalance());
                        pstmt.setString(2, username);
                        pstmt.executeUpdate();
                    }
                    JOptionPane.showMessageDialog(frame, "Deposited $" + amount + ". New balance: $" + wallet.getBalance());
                } else {
                    JOptionPane.showMessageDialog(frame, "Invalid amount.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid input.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "Error updating wallet: " + ex.getMessage());
            }
        });
        frame.add(depositButton, gbc);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void browseProducts(JFrame frame) {
        StringBuilder products = new StringBuilder();
        try {
            ResultSet rs = GroceryDeliveryApp.getSupermarkets();
            while (rs.next()) {
                String supermarketName = rs.getString("name");
                products.append("Supermarket: ").append(supermarketName).append("\n");
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM products WHERE supermarket_id = ? AND available = 1")) {
                    pstmt.setInt(1, rs.getInt("id"));
                    ResultSet rsProducts = pstmt.executeQuery();
                    while (rsProducts.next()) {
                        products.append(rsProducts.getString("name")).append(" - $").append(rsProducts.getDouble("price")).append("\n");
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error fetching products: " + e.getMessage());
            return;
        }

        String input = JOptionPane.showInputDialog(frame, "Products:\n" + products + "\nEnter order (e.g., SupermarketName:ProductName,Quantity):");
        if (input != null && !input.isEmpty()) {
            try {
                String[] parts = input.split(":");
                String[] items = parts[1].split(",");
                String supermarketName = parts[0].trim();
                String productName = items[0].trim();
                int quantity = Integer.parseInt(items[1].trim());

                try (Connection conn = DatabaseConnection.getConnection()) {
                    // Get supermarket
                    PreparedStatement pstmt1 = conn.prepareStatement("SELECT id FROM supermarkets WHERE name = ?");
                    pstmt1.setString(1, supermarketName);
                    ResultSet rsSupermarket = pstmt1.executeQuery();
                    if (!rsSupermarket.next()) throw new Exception("Supermarket not found.");
                    int supermarketId = rsSupermarket.getInt("id");

                    // Get product and check stock
                    PreparedStatement pstmt2 = conn.prepareStatement("SELECT * FROM products WHERE name = ? AND supermarket_id = ? AND available = 1");
                    pstmt2.setString(1, productName);
                    pstmt2.setInt(2, supermarketId);
                    ResultSet rsProduct = pstmt2.executeQuery();
                    if (!rsProduct.next()) throw new Exception("Product not available.");
                    int stockQuantity = rsProduct.getInt("stock_quantity");
                    if (quantity > stockQuantity) throw new Exception("Insufficient stock. Available: " + stockQuantity);
                    Product product = new Product(rsProduct.getString("name"), rsProduct.getDouble("price"), true);

                    // Create order
                    Order order = new Order();
                    for (int i = 0; i < quantity; i++) order.addProduct(product);
                    Supermarket supermarket = new Supermarket();
                    supermarket.setName(supermarketName);
                    order.setSupermarket(supermarket);

                    // Insert order into database
                    PreparedStatement pstmt3 = conn.prepareStatement("INSERT INTO orders (customer_id, supermarket_id, total, status, order_date) VALUES ((SELECT id FROM users WHERE username = ?), ?, ?, ?, GETDATE())", Statement.RETURN_GENERATED_KEYS);
                    pstmt3.setString(1, username);
                    pstmt3.setInt(2, supermarketId);
                    pstmt3.setDouble(3, order.getTotal());
                    pstmt3.setString(4, "Pending");
                    pstmt3.executeUpdate();
                    ResultSet rsOrder = pstmt3.getGeneratedKeys();
                    if (!rsOrder.next()) throw new Exception("Failed to create order.");
                    int orderId = rsOrder.getInt(1);
                    order.setOrderId(String.valueOf(orderId));

                    // Insert order items
                    PreparedStatement pstmt4 = conn.prepareStatement("INSERT INTO order_items (order_id, product_id, quantity, subtotal) VALUES (?, ?, ?, ?)");
                    pstmt4.setInt(1, orderId);
                    pstmt4.setInt(2, rsProduct.getInt("id"));
                    pstmt4.setInt(3, quantity);
                    pstmt4.setDouble(4, product.getPrice() * quantity);
                    pstmt4.executeUpdate();

                    // Update stock quantity
                    PreparedStatement pstmtStock = conn.prepareStatement("UPDATE products SET stock_quantity = stock_quantity - ? WHERE id = ?");
                    pstmtStock.setInt(1, quantity);
                    pstmtStock.setInt(2, rsProduct.getInt("id"));
                    pstmtStock.executeUpdate();

                    // Assign to agent
                    PreparedStatement pstmt5 = conn.prepareStatement("SELECT id FROM agents WHERE id NOT IN (SELECT agent_id FROM orders WHERE status != 'Delivered' AND agent_id IS NOT NULL) LIMIT 1");
                    ResultSet rsAgents = pstmt5.executeQuery();
                    if (rsAgents.next()) {
                        int agentId = rsAgents.getInt("id");
                        PreparedStatement pstmt6 = conn.prepareStatement("UPDATE orders SET agent_id = ? WHERE id = ?");
                        pstmt6.setInt(1, agentId);
                        pstmt6.setInt(2, orderId);
                        pstmt6.executeUpdate();
                    }

                    placeOrder(order);
                    supermarket.addOrder(order);
                    JOptionPane.showMessageDialog(frame, "Order placed! Total: $" + order.getTotal() + "\nOrder ID: " + order.getOrderId());
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage());
            }
        }
    }
}