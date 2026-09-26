package com.inventory.service;

import com.inventory.exception.InvalidLoginException;
import com.inventory.model.User;
import com.inventory.repository.UserFileRepository;

import java.util.Optional;

// business logic: validate credentials against stored users
public class AuthService {

    private final UserFileRepository userFileRepository;

    public AuthService(UserFileRepository userFileRepository) {
        this.userFileRepository = userFileRepository;
    }

    // validate input, look up user, check password
    public User login(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new InvalidLoginException("Please enter a username.");
        }
        if (password == null || password.isBlank()) {
            throw new InvalidLoginException("Please enter a password.");
        }

        Optional<User> foundUser = userFileRepository.findByUsername(username.trim());
        if (foundUser.isEmpty()) {
            throw new InvalidLoginException("Invalid username or password.");
        }

        User user = foundUser.get();
        if (!user.checkPassword(password)) {

            throw new InvalidLoginException("Invalid username or password.");
        }

        return user;
    }
}
