package com.inventory.model;

/**
 * Category represents the type of a product (e.g. "Drink", "Food").
 *
 * OOP concept: ENCAPSULATION.
 * The category name is kept in a private field and can only be read
 * through getName()/toDisplayString(). Category used to be a fixed Java
 * ENUM (DRINK, FOOD, DAIRY, OTHER and nothing else), which is great when
 * the list of options never changes. This project now lets an Admin
 * create brand new categories at runtime (e.g. "Snacks"), and an enum's
 * set of values is fixed at compile time - it can never grow while the
 * program is running. So Category is a normal class instead: every
 * category (built-in or admin-created) is simply a Category object that
 * wraps a name, and the full list of allowed names now lives in
 * data/categories.txt, managed by CategoryFileRepository/CategoryService,
 * the exact same "file + repository + service" pattern already used for
 * Product and User.
 */
public class Category {

    private final String name;

    /**
     * OOP concept: CONSTRUCTOR + VALIDATION.
     * Creating a Category always requires a real name - this keeps a
     * "blank" or null category from ever accidentally being created.
     */
    public Category(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name cannot be empty.");
        }
        this.name = name.trim();
    }

    public String getName() {
        return name;
    }

    /**
     * Converts text from a data file (e.g. "Drink") into a Category
     * object. This is needed because text files store the category as a
     * plain word, not as a Java object.
     *
     * If the text is missing or blank, we default to "Other" instead of
     * crashing the whole application.
     */
    public static Category fromString(String text) {
        if (text == null || text.isBlank()) {
            return new Category("Other");
        }
        return new Category(text.trim());
    }

    /**
     * Converts this Category back into text for saving to a file, e.g.
     * a Category named "Drink" -> "Drink".
     */
    public String toDisplayString() {
        return name;
    }

    /**
     * OOP concept: OVERRIDING equals()/hashCode().
     * Two Category objects are considered "the same category" when their
     * names match, ignoring letter case ("Drink" equals "drink"). Without
     * this, two separate `new Category("Drink")` objects would never be
     * seen as equal, since Java would otherwise only compare object
     * references, not their actual data.
     */
    @Override
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
