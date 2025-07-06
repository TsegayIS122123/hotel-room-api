package GroceryShoppingApp;

class OrderItem {
    private int productId;
    private int supermarketId;
    private int quantity;
    private double subtotal;

    public OrderItem(Product p) {
        this.productId = -1;
        this.supermarketId = -1;
        this.quantity = 1;
        this.subtotal = p.getPrice();
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getSupermarketId() {
        return supermarketId;
    }

    public void setSupermarketId(int supermarketId) {
        this.supermarketId = supermarketId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }
}
