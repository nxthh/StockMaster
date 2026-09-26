package com.inventory.util;

import com.inventory.model.Role;
import com.inventory.model.User;

// global logged-in user state, shared across all controllers
public class Session {

    private static Role currentRole = Role.ADMIN;

    private static String currentUsername = "Cashier";

    private Session() {
    } // static-only: no instances

    public static Role getCurrentRole() {
        return currentRole;
    }

    public static void setCurrentRole(Role role) {
        currentRole = role;
    }

    public static boolean isAdmin() {
        return currentRole == Role.ADMIN;
    }

    public static String getCurrentUsername() {
        return currentUsername;
    }

    public static void setCurrentUsername(String username) {
        currentUsername = (username == null || username.isBlank()) ? "Cashier" : username.trim();
    }

    public static void login(User user) {
        setCurrentRole(user.getRole());
        setCurrentUsername(user.getUsername());
    }
}
