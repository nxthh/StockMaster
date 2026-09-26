package com.inventory.repository;

import com.inventory.model.Admin;
import com.inventory.model.Cashier;
import com.inventory.model.User;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// file I/O layer: reads/writes users.txt
public class UserFileRepository {

    private static final String FILE_PATH = "data/users.txt";

    public UserFileRepository() {
        createFileWithDefaultAccountsIfMissing();
    }

    // read every user line into memory
    public List<User> loadAll() {
        List<User> users = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                try {
                    users.add(User.fromFileLine(line));
                } catch (IllegalArgumentException e) {

                    System.out.println("Skipping invalid user record: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Could not read user file: " + e.getMessage());
        }

        return users;
    }

    public Optional<User> findByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        for (User user : loadAll()) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    // append one new account without rewriting the file
    public void addUser(User user) {
        try {
            Path filePath = Path.of(FILE_PATH);
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            try (FileWriter writer = new FileWriter(FILE_PATH, true)) {
                writer.write(user.toFileLine());
                writer.write(System.lineSeparator());
            }
        } catch (IOException e) {
            System.out.println("Could not save new user account: " + e.getMessage());
        }
    }

    // overwrite the file with the full current list
    public void saveAll(List<User> users) {
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            for (User user : users) {
                writer.write(user.toFileLine());
                writer.write(System.lineSeparator());
            }
        } catch (IOException e) {
            System.out.println("Could not save user file: " + e.getMessage());
        }
    }

    public void deleteUser(String username) {
        List<User> users = loadAll();
        users.removeIf(user -> user.getUsername().equalsIgnoreCase(username));
        saveAll(users);
    }

    // first run: seed the data file with an admin + cashier login
    private void createFileWithDefaultAccountsIfMissing() {
        try {
            Path filePath = Path.of(FILE_PATH);

            if (Files.exists(filePath)) {
                return;
            }

            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }

            List<User> defaultUsers = new ArrayList<>();
            defaultUsers.add(new Admin("admin", "admin123"));
            defaultUsers.add(new Cashier("cashier", "cashier123"));

            try (FileWriter writer = new FileWriter(FILE_PATH)) {
                for (User user : defaultUsers) {
                    writer.write(user.toFileLine());
                    writer.write(System.lineSeparator());
                }
            }
        } catch (IOException e) {
            System.out.println("Could not create user file: " + e.getMessage());
        }
    }
}