package com.inventory.repository;

import com.inventory.model.Category;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// file I/O layer: reads/writes categories.txt
public class CategoryFileRepository {

    private static final String FILE_PATH = "data/categories.txt";

    public CategoryFileRepository() {
        createFileWithDefaultsIfMissing();
    }

    // read every category line into memory
    public List<Category> loadAll() {
        List<Category> categories = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                try {
                    categories.add(new Category(line));
                } catch (RuntimeException e) {
                    System.out.println("Skipping invalid category record: " + line + " (" + e.getMessage() + ")");
                }
            }
        } catch (IOException e) {
            System.out.println("Could not read category file: " + e.getMessage());
        }

        return categories;
    }

    // overwrite the file with the full current list
    public void saveAll(List<Category> categories) {
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            for (Category category : categories) {
                writer.write(category.toDisplayString());
                writer.write(System.lineSeparator());
            }
        } catch (IOException e) {
            System.out.println("Could not save category file: " + e.getMessage());
        }
    }

    public void add(Category category) {
        List<Category> categories = loadAll();
        categories.add(category);
        saveAll(categories);
    }

    // find by name, replace, then rewrite the whole file
    public void update(String currentName, Category updatedCategory) {
        List<Category> categories = loadAll();
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getName().equalsIgnoreCase(currentName)) {
                categories.set(i, updatedCategory);
                break;
            }
        }
        saveAll(categories);
    }

    public void delete(String name) {
        List<Category> categories = loadAll();
        categories.removeIf(category -> category.getName().equalsIgnoreCase(name));
        saveAll(categories);
    }

    public Optional<Category> findByName(String name) {
        for (Category category : loadAll()) {
            if (category.getName().equalsIgnoreCase(name)) {
                return Optional.of(category);
            }
        }
        return Optional.empty();
    }

    // first run: seed the data file with default categories
    private void createFileWithDefaultsIfMissing() {
        try {
            Path filePath = Path.of(FILE_PATH);

            if (Files.exists(filePath)) {
                return;
            }

            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }

            List<Category> defaultCategories = new ArrayList<>();
            defaultCategories.add(new Category("Drink"));
            defaultCategories.add(new Category("Food"));
            defaultCategories.add(new Category("Dairy"));
            defaultCategories.add(new Category("Other"));

            saveAll(defaultCategories);
        } catch (IOException e) {
            System.out.println("Could not create category file: " + e.getMessage());
        }
    }
}
