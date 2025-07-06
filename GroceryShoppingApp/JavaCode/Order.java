package GroceryShoppingApp;

import java.io.*;

class Order {
    private Product[] products;
    private int itemCount;
    private double total;
    private String status;
    private String orderId; // Will store the database-generated ID as a String
    private String invoice;
    private Supermarket supermarket;
    private static final int MAX_ITEMS = 10;

    public Order() {
        products = new Product[MAX_ITEMS];
        itemCount = 0;
        total = 0.0;
        status = "Pending";
        orderId = null; // Will be set after database insertion
        invoice = "";
    }

    public void addProduct(Product p) {
        if (itemCount < MAX_ITEMS) {
            products[itemCount++] = p;
            total += p.getPrice();
        }
    }

    public double getTotal() {
        double deliveryFee = (supermarket != null ? 5.0 : 0.0) + (itemCount * 0.5); // $5 base + $0.5 per item
        return total + deliveryFee;
    }

    public String getStatus() { return status; }
    public void updateStatus(String s) { status = s; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String id) { this.orderId = id; }
    public void setInvoice(String i) { invoice = i; FileHandler.saveInvoice(orderId + ".txt", i); }
    public String getInvoice() { return invoice.isEmpty() ? "No invoice" : invoice; }
    public void setSupermarket(Supermarket s) { supermarket = s; }
}