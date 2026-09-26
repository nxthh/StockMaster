package com.inventory.service;

import com.inventory.exception.DuplicateUsernameException;
import com.inventory.exception.InvalidUserException;
import com.inventory.model.Cashier;
import com.inventory.model.Role;
import com.inventory.model.User;
import com.inventory.repository.UserFileRepository;

import java.util.List;
import java.util.Optional;

// business logic for managing cashier accounts (admin only)
public class UserService {

    private final UserFileRepository userFileRepository;

    public UserService(UserFileRepository userFileRepository) {
        this.userFileRepository = userFileRepository;
    }

    public List<User> getAllUsers() {
        return userFileRepository.loadAll();
    }

    // validate fields, reject duplicate usernames, then persist
    public Cashier createCashierAccount(String username, String password, String confirmPassword) {
        if (username == null || username.isBlank()) {
            throw new InvalidUserException("Please enter a username.");
        }
        if (password == null || password.isBlank()) {
            throw new InvalidUserException("Please enter a password.");
        }
        if (!password.equals(confirmPassword)) {
            throw new InvalidUserException("Password and Confirm Password do not match.");
        }

        String trimmedUsername = username.trim();
        if (userFileRepository.findByUsername(trimmedUsername).isPresent()) {
            throw new DuplicateUsernameException("Username \"" + trimmedUsername + "\" is already taken.");
        }

        Cashier cashier = new Cashier(trimmedUsername, password);
        userFileRepository.addUser(cashier);
        return cashier;
    }

    // guard: admin accounts cannot be deleted from here
    public void deleteCashierAccount(String username) {
        if (username == null || username.isBlank()) {
            throw new InvalidUserException("Please select an account to delete.");
        }

        Optional<User> existing = userFileRepository.findByUsername(username.trim());
        if (existing.isEmpty()) {
            throw new InvalidUserException("Account \"" + username + "\" was not found.");
        }
        if (existing.get().getRole() == Role.ADMIN) {
            throw new InvalidUserException("Admin accounts cannot be deleted here.");
        }

        userFileRepository.deleteUser(username.trim());
    }
}