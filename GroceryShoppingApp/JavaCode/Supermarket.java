package GroceryShoppingApp;

class Supermarket {
    private String name;
    private Product[] products;
    private int productCount;
    private Order[] orders;
    private int orderCount;
    private static final int MAX_PRODUCTS = 50;
    private static final int MAX_ORDERS = 50;

    public Supermarket() {
        name = "";
        products = new Product[MAX_PRODUCTS];
        orders = new Order[MAX_ORDERS];
        productCount = 0;
        orderCount = 0;
    }

    public void setName(String n) { name = n; }
    public String getName() { return name; }
    public void addProduct(Product p) { if (productCount < MAX_PRODUCTS) products[productCount++] = p; }
    public void updateProductAvailability(String n, boolean a) {
        for (int i = 0; i < productCount; i++)
            if (products[i] != null && products[i].getName().equals(n)) { products[i].setAvailable(a); return; }
    }
    public void addOrder(Order o) { if (orderCount < MAX_ORDERS) orders[orderCount++] = o; }
    public Product[] getProducts() { return products; }
    public int getProductCount() { return productCount; }
    public Order[] getOrders() { return orders; }
    public int getOrderCount() { return orderCount; }
}