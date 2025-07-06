
-- Drop the existing database if it exists
IF EXISTS (SELECT name FROM sys.databases WHERE name = 'GroceryDB')
BEGIN
    ALTER DATABASE GroceryDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE GroceryDB;
END
GO

-- Create the database
CREATE DATABASE GroceryDB;
GO

-- Use the database
USE GroceryDB;
GO

-- Create Users table (base table for all users)
CREATE TABLE users (
    id INT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) UNIQUE NOT NULL,
    password NVARCHAR(50) NOT NULL,
    user_type NVARCHAR(20) NOT NULL CHECK (user_type IN ('Customer', 'Agent', 'SupermarketOwner'))
);

-- Create Customers table (extends Users)
CREATE TABLE customers (
    id INT PRIMARY KEY,
    name NVARCHAR(100) NOT NULL,
    phone NVARCHAR(15) NOT NULL,
    address NVARCHAR(255) NOT NULL,
    pickup_location NVARCHAR(255),
    wallet_balance FLOAT DEFAULT 0.0,
    FOREIGN KEY (id) REFERENCES users(id)
);

-- Create Agents table (extends Users)
CREATE TABLE agents (
    id INT PRIMARY KEY,
    name NVARCHAR(100) NOT NULL,
    phone NVARCHAR(15) NOT NULL,
    FOREIGN KEY (id) REFERENCES users(id)
);

-- Create Supermarkets table
CREATE TABLE supermarkets (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL,
    owner_id INT,
    location NVARCHAR(255) NOT NULL,
    FOREIGN KEY (owner_id) REFERENCES users(id)
);

-- Create SupermarketOwners table (extends Users)
CREATE TABLE supermarket_owners (
    id INT PRIMARY KEY,
    name NVARCHAR(100) NOT NULL,
    phone NVARCHAR(15) NOT NULL,
    supermarket_id INT NOT NULL,
    FOREIGN KEY (id) REFERENCES users(id),
    FOREIGN KEY (supermarket_id) REFERENCES supermarkets(id)
);

-- Create Products table
CREATE TABLE products (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL,
    price FLOAT NOT NULL,
    available BIT NOT NULL DEFAULT 1,
    stock_quantity INT NOT NULL DEFAULT 0,
    supermarket_id INT NOT NULL,
    FOREIGN KEY (supermarket_id) REFERENCES supermarkets(id)
);

-- Create Orders table
CREATE TABLE orders (
    id INT IDENTITY(1,1) PRIMARY KEY,
    customer_id INT NOT NULL,
    agent_id INT,
    supermarket_id INT NOT NULL,
    total FLOAT NOT NULL,
    status NVARCHAR(20) NOT NULL DEFAULT 'Pending',
    order_date DATETIME NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (agent_id) REFERENCES agents(id),
    FOREIGN KEY (supermarket_id) REFERENCES supermarkets(id)
);

-- Create OrderItems table
CREATE TABLE order_items (
    id INT IDENTITY(1,1) PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    subtotal FLOAT NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Create Invoices table
CREATE TABLE invoices (
    id INT IDENTITY(1,1) PRIMARY KEY,
    order_id INT NOT NULL,
    supermarket_id INT NOT NULL,
    amount FLOAT NOT NULL,
    invoice_details TEXT,
    upload_date DATETIME NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (supermarket_id) REFERENCES supermarkets(id)
);

-- Create Deliveries table
CREATE TABLE deliveries (
    id INT IDENTITY(1,1) PRIMARY KEY,
    order_id INT NOT NULL,
    agent_id INT NOT NULL,
    delivery_status NVARCHAR(20) NOT NULL DEFAULT 'Pending',
    delivery_date DATETIME,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (agent_id) REFERENCES agents(id)
);

-- Insert sample data
INSERT INTO users (username, password, user_type) VALUES ('customer1', 'custpass1', 'Customer');
INSERT INTO customers (id, name, phone, address, pickup_location, wallet_balance)
VALUES (1, 'Robel Mokenen', '0987657456', 'Addis Ababa', 'Meganegna', 7000.00);

INSERT INTO users (username, password, user_type) VALUES ('agent1', 'agentpass1', 'Agent');
INSERT INTO agents (id, name, phone) VALUES (2, 'Hana Tesfu', '0989567845');

INSERT INTO supermarkets (name, location) VALUES ('SuperMart', 'Downtown');
INSERT INTO users (username, password, user_type) VALUES ('owner1', 'ownerpass1', 'SupermarketOwner');
INSERT INTO supermarket_owners (id, name, phone, supermarket_id) VALUES (3, 'Asmelash Yared', '0967895678', 1);
UPDATE supermarkets SET owner_id = 3 WHERE id = 1;

INSERT INTO products (supermarket_id, name, price, stock_quantity) VALUES (1, 'Apple', 50.00, 100);
INSERT INTO products (supermarket_id, name, price, stock_quantity) VALUES (1, 'Bread', 10.00, 50);

-- Verify the Customers table
IF EXISTS (SELECT * FROM sysobjects WHERE name='customers' AND xtype='U')
BEGIN
    SELECT * FROM customers;
END
ELSE
BEGIN
    PRINT 'Error: The customers table does not exist.';
END