package GroceryShoppingApp;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

class Agent extends User {
    private String name;
    private String phone;
    private Order[] orders;
    private int orderCount;
    private static final int MAX_ORDERS = 20;

    public Agent() {
        super();
        name = "";
        phone = "";
        orders = new Order[MAX_ORDERS];
        orderCount = 0;
    }

    public void register(String u, String p, String n, String ph) {
        setCredentials(u, p);
        name = n;
        phone = ph;
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public void assignOrder(Order o) {
        if (orderCount < MAX_ORDERS) orders[orderCount++] = o;
    }

    public void uploadInvoice(String id, String i) {
        for (int j = 0; j < orderCount; j++) {
            if (orders[j] != null && orders[j].getOrderId().equals(id)) {
                orders[j].setInvoice(i);
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("INSERT INTO invoices (order_id, supermarket_id, amount, invoice_details, upload_date) VALUES (?, (SELECT supermarket_id FROM orders WHERE id = ?), ?, ?, GETDATE())")) {
                    pstmt.setInt(1, Integer.parseInt(id));
                    pstmt.setInt(2, Integer.parseInt(id));
                    pstmt.setDouble(3, orders[j].getTotal());
                    pstmt.setString(4, i);
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(null, "Error uploading invoice: " + e.getMessage());
                    return;
                }
                JOptionPane.showMessageDialog(null, "Invoice uploaded for order " + id);
                return;
            }
        }
        JOptionPane.showMessageDialog(null, "Order not found.");
    }

    public void communicate(String s, String m) {
        JOptionPane.showMessageDialog(null, "To " + s + ": " + m);
    }

    @Override
    public void showMenu(JFrame parentFrame) {
        JFrame frame = new JFrame("Agent Menu");
        frame.setSize(400, 300);
        frame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JButton viewOrdersButton = new JButton("View Assigned Orders");
        gbc.gridx = 0;
        gbc.gridy = 0;
        viewOrdersButton.addActionListener(e -> viewOrders(frame));
        frame.add(viewOrdersButton, gbc);

        JButton uploadInvoiceButton = new JButton("Upload Invoice");
        gbc.gridy = 1;
        uploadInvoiceButton.addActionListener(e -> uploadInvoiceDialog(frame));
        frame.add(uploadInvoiceButton, gbc);

        JButton communicateButton = new JButton("Communicate with Supermarket");
        gbc.gridy = 2;
        communicateButton.addActionListener(e -> communicateDialog(frame));
        frame.add(communicateButton, gbc);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void viewOrders(JFrame frame) {
        StringBuilder orderList = new StringBuilder("Assigned Orders:\n");
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT o.id, o.status FROM orders o JOIN users u ON o.agent_id = u.id WHERE u.username = ?")) {
            pstmt.setString(1, username);
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

    private void uploadInvoiceDialog(JFrame frame) {
        String orderId = JOptionPane.showInputDialog(frame, "Enter Order ID:");
        if (orderId != null && !orderId.isEmpty()) {
            String invoice = JOptionPane.showInputDialog(frame, "Enter Invoice Details:");
            if (invoice != null && !invoice.isEmpty()) uploadInvoice(orderId, invoice);
        }
    }

    private void communicateDialog(JFrame frame) {
        String supermarketName = JOptionPane.showInputDialog(frame, "Enter Supermarket Name:");
        if (supermarketName != null && !supermarketName.isEmpty()) {
            String message = JOptionPane.showInputDialog(frame, "Enter Message:");
            if (message != null && !message.isEmpty()) communicate(supermarketName, message);
        }
    }
}