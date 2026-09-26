package com.inventory.model;

// abstract base for Admin/Cashier (abstraction)
public abstract class User {

    private final String username;
    private final String password;

    protected User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public abstract Role getRole(); // overridden per role (polymorphism)

    public boolean checkPassword(String candidatePassword) {
        return password != null && password.equals(candidatePassword);
    }

    // serialize: one user per line for file storage
    public String toFileLine() {
        return username + "," + password + "," + getRole();
    }

    // deserialize: rebuild the right subclass from a stored line
    public static User fromFileLine(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Malformed user line: " + line);
        }

        String username = parts[0].trim();
        String password = parts[1].trim();
        String roleText = parts[2].trim().toUpperCase();

        if (username.isEmpty()) {
            throw new IllegalArgumentException("User line is missing a username: " + line);
        }

        Role role;
        try {
            role = Role.valueOf(roleText);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown role \"" + parts[2].trim() + "\" in user line: " + line);
        }

        return switch (role) {
            case ADMIN -> new Admin(username, password);
            case CASHIER -> new Cashier(username, password);
        };
    }

    @Override
    public String toString() {
        return "User{username=" + username + ", role=" + getRole() + "}";
    }
}
