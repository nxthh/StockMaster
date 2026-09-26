package com.inventory.model;

public class Category {

    private final String name;

    public Category(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name cannot be empty.");
        }
        this.name = name.trim();
    }

    public String getName() {
        return name;
    }

    public static Category fromString(String text) {
        if (text == null || text.isBlank()) {
            return new Category("Other");
        }
        return new Category(text.trim());
    }

    public String toDisplayString() {
        return name;
    }

    @Override
    // equal by name, case-insensitive
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Category)) {
            return false;
        }
        Category that = (Category) other;
        return this.name.equalsIgnoreCase(that.name);
    }

    @Override
    public int hashCode() {
        return name.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
