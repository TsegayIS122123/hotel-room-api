package GroceryShoppingApp;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class GroceryDeliveryApp {
    // Declare currentPanel as a static field
    private static JPanel currentPanel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                DatabaseConnection.initializeDatabase();
                initializeDefaultUsers();
                showLoginWindow();
            }
        });
    }

    private static void initializeDefaultUsers() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            Customer customer1 = new Customer();
            customer1.register("customer1", "custpass1", "Robel Mokenen", "0987657456", "Addis Ababa", "Meganegna", 7000.0);
            registerCustomer(customer1);

            Agent agent1 = new Agent();
            agent1.register("agent1", "agentpass1", "Hana Tesfu", "0989567845");
            registerAgent(agent1);

            Supermarket superMart = new Supermarket();
            superMart.setName("SuperMart");
            superMart.addProduct(new Product("Apple", 50, true));
            superMart.addProduct(new Product("Bread", 10, true));
            SupermarketOwner owner1 = new SupermarketOwner();
            owner1.register("owner1", "ownerpass1", "Asmelash Yared", "0967895678", "SuperMart");
            registerSupermarket(superMart, owner1);
        } catch (SQLException e) {
            System.out.println("Error initializing default users: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Failed to initialize default users. Please check the database connection and try again.");
            System.exit(1);
        }
    }

    public static void registerCustomer(Customer c) {
        String sqlUser = "INSERT INTO users (username, password, user_type) VALUES (?, ?, ?)";
        String sqlCustomer = "INSERT INTO customers (id, name, phone, address, pickup_location, wallet_balance) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt1 = conn.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement pstmt2 = conn.prepareStatement(sqlCustomer)) {
            pstmt1.setString(1, c.getUsername());
            pstmt1.setString(2, c.getPassword());
            pstmt1.setString(3, "Customer");
            pstmt1.executeUpdate();
            ResultSet rs = pstmt1.getGeneratedKeys();
            if (rs.next()) {
                int id = rs.getInt(1);
                pstmt2.setInt(1, id);
                pstmt2.setString(2, c.getName());
                pstmt2.setString(3, c.getPhone());
                pstmt2.setString(4, c.getAddress());
                pstmt2.setString(5, c.getPickupLocation());
                pstmt2.setDouble(6, c.getWallet().getBalance());
                pstmt2.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("Error registering customer: " + e.getMessage());
        }
    }

    public static void registerAgent(Agent a) {
        String sqlUser = "INSERT INTO users (username, password, user_type) VALUES (?, ?, ?)";
        String sqlAgent = "INSERT INTO agents (id, name, phone) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt1 = conn.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement pstmt2 = conn.prepareStatement(sqlAgent)) {
            pstmt1.setString(1, a.getUsername());
            pstmt1.setString(2, a.getPassword());
            pstmt1.setString(3, "Agent");
            pstmt1.executeUpdate();
            ResultSet rs = pstmt1.getGeneratedKeys();
            if (rs.next()) {
                int id = rs.getInt(1);
                pstmt2.setInt(1, id);
                pstmt2.setString(2, a.getName());
                pstmt2.setString(3, a.getPhone());
                pstmt2.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("Error registering agent: " + e.getMessage());
        }
    }

    public static void registerSupermarket(Supermarket s, SupermarketOwner o) {
        String sqlUser = "INSERT INTO users (username, password, user_type) VALUES (?, ?, ?)";
        String sqlSupermarket = "INSERT INTO supermarkets (name, location) VALUES (?, ?)";
        String sqlOwner = "INSERT INTO supermarket_owners (id, name, phone, supermarket_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt1 = conn.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement pstmt2 = conn.prepareStatement(sqlSupermarket, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement pstmt3 = conn.prepareStatement(sqlOwner)) {
            pstmt1.setString(1, o.getUsername());
            pstmt1.setString(2, o.getPassword());
            pstmt1.setString(3, "SupermarketOwner");
            pstmt1.executeUpdate();
            ResultSet rs1 = pstmt1.getGeneratedKeys();
            if (rs1.next()) {
                int ownerId = rs1.getInt(1);

                pstmt2.setString(1, s.getName());
                pstmt2.setString(2, "Downtown");
                pstmt2.executeUpdate();
                ResultSet rs2 = pstmt2.getGeneratedKeys();
                if (rs2.next()) {
                    int supermarketId = rs2.getInt(1);

                    pstmt3.setInt(1, ownerId);
                    pstmt3.setString(2, o.getName());
                    pstmt3.setString(3, o.getPhone());
                    pstmt3.setInt(4, supermarketId);
                    pstmt3.executeUpdate();

                    try (PreparedStatement pstmtUpdate = conn.prepareStatement("UPDATE supermarkets SET owner_id = ? WHERE id = ?")) {
                        pstmtUpdate.setInt(1, ownerId);
                        pstmtUpdate.setInt(2, supermarketId);
                        pstmtUpdate.executeUpdate();
                    }

                    for (int i = 0; i < s.getProductCount(); i++) {
                        Product p = s.getProducts()[i];
                        if (p != null) {
                            String sqlProduct = "INSERT INTO products (name, price, available, stock_quantity, supermarket_id) VALUES (?, ?, ?, ?, ?)";
                            try (PreparedStatement pstmt4 = conn.prepareStatement(sqlProduct)) {
                                pstmt4.setString(1, p.getName());
                                pstmt4.setDouble(2, p.getPrice());
                                pstmt4.setBoolean(3, p.isAvailable());
                                pstmt4.setInt(4, 100);
                                pstmt4.setInt(5, supermarketId);
                                pstmt4.executeUpdate();
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error registering supermarket: " + e.getMessage());
        }
    }

    public static boolean authenticate(String u, String p) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, u);
            pstmt.setString(2, p);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println("Error during authentication: " + e.getMessage());
            return false;
        }
    }

    public static ResultSet getCustomers() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = stmt.executeQuery("SELECT * FROM customers JOIN users ON customers.id = users.id");
        return rs;
    }

    public static ResultSet getAgents() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = stmt.executeQuery("SELECT * FROM agents JOIN users ON agents.id = users.id");
        return rs;
    }

    public static ResultSet getSupermarkets() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = stmt.executeQuery("SELECT * FROM supermarkets");
        return rs;
    }

    public static ResultSet getOwners() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = stmt.executeQuery("SELECT * FROM supermarket_owners JOIN users ON supermarket_owners.id = users.id");
        return rs;
    }

    private static void showLoginWindow() {
        JFrame frame = new JFrame("Login - Grocery Delivery System");
        frame.setSize(450, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Grocery Delivery System", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        frame.add(titleLabel, gbc);

        // Username Label and Field
        gbc.gridwidth = 1; // Reset gridwidth
        gbc.fill = GridBagConstraints.NONE; // Ensure no stretching
        JLabel usernameLabel = new JLabel("Username:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        frame.add(usernameLabel, gbc);

        JTextField usernameField = new JTextField(20);
        usernameField.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        frame.add(usernameField, gbc);

        // Password Label and Field
        JLabel passwordLabel = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        frame.add(passwordLabel, gbc);

        JPasswordField passwordField = new JPasswordField(20);
        passwordField.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        frame.add(passwordField, gbc);

        JButton loginButton = new JButton("Login");
        loginButton.setPreferredSize(new Dimension(100, 30));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        loginButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                String uname = usernameField.getText().trim();
                String pwd = new String(passwordField.getPassword()).trim();
                if (uname.isEmpty() || pwd.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Please enter both username and password.");
                    return;
                }
                if (authenticate(uname, pwd)) {
                    try {
                        ResultSet rsCustomers = getCustomers();
                        while (rsCustomers.next()) {
                            if (rsCustomers.getString("username").equals(uname) && rsCustomers.getString("password").equals(pwd)) {
                                Customer customer = new Customer();
                                customer.setCredentials(uname, pwd);
                                customer.setName(rsCustomers.getString("name"));
                                customer.setPhone(rsCustomers.getString("phone"));
                                customer.setAddress(rsCustomers.getString("address"));
                                customer.setPickupLocation(rsCustomers.getString("pickup_location"));
                                customer.setWallet(new DigitalWallet(rsCustomers.getDouble("wallet_balance")));
                                customer.showMenu(frame);
                                frame.dispose();
                                rsCustomers.close();
                                rsCustomers.getStatement().getConnection().close();
                                return;
                            }
                        }
                        rsCustomers.close();
                        rsCustomers.getStatement().getConnection().close();

                        ResultSet rsAgents = getAgents();
                        while (rsAgents.next()) {
                            if (rsAgents.getString("username").equals(uname) && rsAgents.getString("password").equals(pwd)) {
                                Agent agent = new Agent();
                                agent.setCredentials(uname, pwd);
                                agent.setName(rsAgents.getString("name"));
                                agent.setPhone(rsAgents.getString("phone"));
                                agent.showMenu(frame);
                                frame.dispose();
                                rsAgents.close();
                                rsAgents.getStatement().getConnection().close();
                                return;
                            }
                        }
                        rsAgents.close();
                        rsAgents.getStatement().getConnection().close();

                        ResultSet rsOwners = getOwners();
                        while (rsOwners.next()) {
                            if (rsOwners.getString("username").equals(uname) && rsOwners.getString("password").equals(pwd)) {
                                SupermarketOwner owner = new SupermarketOwner();
                                owner.setCredentials(uname, pwd);
                                owner.setName(rsOwners.getString("name"));
                                owner.setPhone(rsOwners.getString("phone"));
                                try (Connection conn = DatabaseConnection.getConnection();
                                     PreparedStatement pstmt = conn.prepareStatement("SELECT name FROM supermarkets WHERE id = (SELECT supermarket_id FROM supermarket_owners WHERE id = ?)")) {
                                    pstmt.setInt(1, rsOwners.getInt("id"));
                                    ResultSet rsSupermarket = pstmt.executeQuery();
                                    if (rsSupermarket.next()) {
                                        owner.setSupermarketName(rsSupermarket.getString("name"));
                                        Supermarket supermarket = new Supermarket();
                                        supermarket.setName(rsSupermarket.getString("name"));
                                        owner.setSupermarket(supermarket);
                                        owner.showMenu(frame);
                                        frame.dispose();
                                        rsOwners.close();
                                        rsOwners.getStatement().getConnection().close();
                                        return;
                                    }
                                }
                            }
                        }
                        rsOwners.close();
                        rsOwners.getStatement().getConnection().close();
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(frame, "Error accessing database: " + ex.getMessage());
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Invalid credentials.");
                }
            }
        });
        frame.add(loginButton, gbc);

        JButton registerButton = new JButton("Register");
        registerButton.setPreferredSize(new Dimension(100, 30));
        gbc.gridy = 4;
        registerButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                showRegistrationForm(frame);
            }
        });
        frame.add(registerButton, gbc);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void showRegistrationForm(JFrame parentFrame) {
        JFrame regFrame = new JFrame("Register - Grocery Delivery System");
        regFrame.setSize(450, 650);
        regFrame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel titleLabel = new JLabel("Register", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.gridwidth = 2;
        regFrame.add(titleLabel, gbc);
        gbc.gridwidth = 1;
        gbc.gridy++;

        JLabel typeLabel = new JLabel("User Type:");
        regFrame.add(typeLabel, gbc);
        gbc.gridx = 1;

        String[] types = {"Customer", "Agent", "Supermarket Owner"};
        JComboBox<String> typeCombo = new JComboBox<>(types);
        regFrame.add(typeCombo, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        JLabel unameLabel = new JLabel("Username:");
        regFrame.add(unameLabel, gbc);
        gbc.gridx = 1;
        JTextField unameField = new JTextField(20);
        unameField.setPreferredSize(new Dimension(200, 30));
        regFrame.add(unameField, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        JLabel pwdLabel = new JLabel("Password:");
        regFrame.add(pwdLabel, gbc);
        gbc.gridx = 1;
        JPasswordField pwdField = new JPasswordField(20);
        pwdField.setPreferredSize(new Dimension(200, 30));
        regFrame.add(pwdField, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        JLabel nameLabel = new JLabel("Name:");
        regFrame.add(nameLabel, gbc);
        gbc.gridx = 1;
        JTextField nameField = new JTextField(20);
        nameField.setPreferredSize(new Dimension(200, 30));
        regFrame.add(nameField, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        JLabel phoneLabel = new JLabel("Phone:");
        regFrame.add(phoneLabel, gbc);
        gbc.gridx = 1;
        JTextField phoneField = new JTextField(20);
        phoneField.setPreferredSize(new Dimension(200, 30));
        regFrame.add(phoneField, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        JPanel customerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints panelGbc = new GridBagConstraints();
        panelGbc.insets = new Insets(5, 5, 5, 5);
        panelGbc.fill = GridBagConstraints.HORIZONTAL;
        panelGbc.gridx = 0;

        JLabel addressLabel = new JLabel("Address:");
        JTextField addressField = new JTextField(20);
        addressField.setPreferredSize(new Dimension(200, 30));
        panelGbc.gridy = 0;
        customerPanel.add(addressLabel, panelGbc);
        panelGbc.gridx = 1;
        customerPanel.add(addressField, panelGbc);
        panelGbc.gridx = 0;
        panelGbc.gridy++;

        JLabel pickupLabel = new JLabel("Pickup Location:");
        JTextField pickupField = new JTextField(20);
        pickupField.setPreferredSize(new Dimension(200, 30));
        customerPanel.add(pickupLabel, panelGbc);
        panelGbc.gridx = 1;
        customerPanel.add(pickupField, panelGbc);
        panelGbc.gridx = 0;
        panelGbc.gridy++;

        JLabel balanceLabel = new JLabel("Initial Balance:");
        JTextField balanceField = new JTextField(20);
        balanceField.setPreferredSize(new Dimension(200, 30));
        customerPanel.add(balanceLabel, panelGbc);
        panelGbc.gridx = 1;
        customerPanel.add(balanceField, panelGbc);

        JPanel ownerPanel = new JPanel(new GridBagLayout());
        panelGbc.gridx = 0;
        panelGbc.gridy = 0;
        JLabel supermarketLabel = new JLabel("Supermarket Name:");
        JTextField supermarketField = new JTextField(20);
        supermarketField.setPreferredSize(new Dimension(200, 30));
        ownerPanel.add(supermarketLabel, panelGbc);
        panelGbc.gridx = 1;
        ownerPanel.add(supermarketField, panelGbc);

        gbc.gridy++;
        gbc.gridwidth = 2;
        currentPanel = customerPanel; // Initialize with customer panel
        regFrame.add(currentPanel, gbc);

        typeCombo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                try {
                    String selectedType = (String) typeCombo.getSelectedItem();
                    if (currentPanel != null) {
                        regFrame.remove(currentPanel);
                    }
                    if ("Customer".equals(selectedType)) {
                        regFrame.add(customerPanel, gbc);
                        currentPanel = customerPanel;
                    } else if ("Supermarket Owner".equals(selectedType)) {
                        regFrame.add(ownerPanel, gbc);
                        currentPanel = ownerPanel;
                    } else {
                        currentPanel = null;
                    }
                    regFrame.revalidate();
                    regFrame.repaint();
                } catch (Exception ex) {
                    System.err.println("Error switching panels: " + ex.getMessage());
                    JOptionPane.showMessageDialog(regFrame, "An error occurred while updating the form. Please try again or restart the application.");
                    if (currentPanel == null) {
                        regFrame.add(customerPanel, gbc);
                        currentPanel = customerPanel;
                        regFrame.revalidate();
                        regFrame.repaint();
                    }
                }
            }
        });

        gbc.gridy++;
        JButton submitButton = new JButton("Submit");
        submitButton.setPreferredSize(new Dimension(100, 30));
        submitButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                try {
                    String type = (String) typeCombo.getSelectedItem();
                    String username = unameField.getText().trim();
                    String password = new String(pwdField.getPassword()).trim();
                    String name = nameField.getText().trim();
                    String phone = phoneField.getText().trim();
                    String address = addressField.getText().trim();
                    String pickupLocation = pickupField.getText().trim();
                    String balanceStr = balanceField.getText().trim();
                    String supermarketName = supermarketField.getText().trim();

                    if (username.isEmpty() || password.isEmpty() || name.isEmpty() || phone.isEmpty()) {
                        JOptionPane.showMessageDialog(regFrame, "Please fill in all required fields.");
                        return;
                    }

                    if (!phone.matches("\\d{10}")) {
                        JOptionPane.showMessageDialog(regFrame, "Phone number must be a 10-digit number (e.g., 0987654321).");
                        return;
                    }

                    if ("Customer".equals(type)) {
                        if (address.isEmpty() || pickupLocation.isEmpty() || balanceStr.isEmpty()) {
                            JOptionPane.showMessageDialog(regFrame, "Please fill in Address, Pickup Location, and Initial Balance.");
                            return;
                        }
                        double initialBalance = Double.parseDouble(balanceStr);
                        if (initialBalance < 0) {
                            JOptionPane.showMessageDialog(regFrame, "Initial Balance must be non-negative.");
                            return;
                        }
                        Customer customer = new Customer();
                        customer.register(username, password, name, phone, address, pickupLocation, initialBalance);
                        registerCustomer(customer);
                        JOptionPane.showMessageDialog(regFrame, "Customer registered!");
                    } else if ("Agent".equals(type)) {
                        Agent agent = new Agent();
                        agent.register(username, password, name, phone);
                        registerAgent(agent);
                        JOptionPane.showMessageDialog(regFrame, "Agent registered!");
                    } else if ("Supermarket Owner".equals(type)) {
                        if (supermarketName.isEmpty()) {
                            JOptionPane.showMessageDialog(regFrame, "Please enter Supermarket Name.");
                            return;
                        }
                        Supermarket supermarket = new Supermarket();
                        supermarket.setName(supermarketName);
                        SupermarketOwner owner1 = new SupermarketOwner();
                        owner1.register(username, password, name, phone, supermarketName);
                        registerSupermarket(supermarket, owner1);
                        JOptionPane.showMessageDialog(regFrame, "Supermarket Owner registered!");
                    }
                    regFrame.dispose();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(regFrame, "Invalid number format: " + ex.getMessage());
                } catch (Exception ex) {
                    System.err.println("Error during submission: " + ex.getMessage());
                    JOptionPane.showMessageDialog(regFrame, "An error occurred during registration. Please try again.");
                }
            }
        });
        regFrame.add(submitButton, gbc);

        regFrame.setLocationRelativeTo(null);
        regFrame.setVisible(true);
    }
}