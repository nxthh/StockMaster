package com.inventory.model;

// product record + file (de)serialization (encapsulation)
public class Product {

    private String id;
    private String name;
    private Category category;
    private double price;
    private int quantity;
    private int minimumStock;

    public Product(String id, String name, Category category, double price, int quantity, int minimumStock) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
        this.minimumStock = minimumStock;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Category getCategory() {
        return category;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getMinimumStock() {
        return minimumStock;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setMinimumStock(int minimumStock) {
        this.minimumStock = minimumStock;
    }

    // serialize: one product per line
    public String toFileLine() {
        return id + "," + name + "," + category.toDisplayString() + "," + price + "," + quantity + "," + minimumStock;
    }

    // deserialize: parse a stored line back into a Product
    public static Product fromFileLine(String line) {
        String[] parts = line.split(",");
        String id = parts[0].trim();
        String name = parts[1].trim();
        Category category = Category.fromString(parts[2].trim());
        double price = Double.parseDouble(parts[3].trim());
        int quantity = Integer.parseInt(parts[4].trim());
        int minimumStock = Integer.parseInt(parts[5].trim());
        return new Product(id, name, category, price, quantity, minimumStock);
    }

    @Override
    public String toString() {
        return "Product{id=" + id + ", name=" + name + ", category=" + category +
                ", price=" + price + ", quantity=" + quantity + ", minimumStock=" + minimumStock + "}";
    }
}
