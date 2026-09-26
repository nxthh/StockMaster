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

/**
 * CategoryFileRepository is responsible for ALL reading and writing of
 * category data to and from the text file data/categories.txt (one
 * category name per line).
 *
 * OOP concept: SEPARATION OF RESPONSIBILITIES / LAYERED ARCHITECTURE.
 * This class follows the exact same shape as ProductFileRepository: it
 * is the ONLY class that touches data/categories.txt directly. Services
 * and controllers never open this file themselves - they go through
 * CategoryService, which goes through this repository.
 */
public class CategoryFileRepository {

    private static final String FILE_PATH = "data/categories.txt";

    /**
     * Creates the repository and makes sure data/categories.txt exists.
     * If it is missing, it is created with the four categories the
     * products already saved in data/products.txt were using before
     * categories became editable, so existing data keeps working.
     */
    public CategoryFileRepository() {
        createFileWithDefaultsIfMissing();
    }

    /**
     * Reads every line from data/categories.txt and converts each line
     * into a Category object.
     */
    public List<Category> loadAll() {
        List<Category> categories = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue; // skip blank lines
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

    /**
     * Writes the given list of categories to data/categories.txt,
     * replacing whatever was there before.
     */
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

    /**
     * Adds a new category and immediately saves the updated list to disk.
     */
    public void add(Category category) {
        List<Category> categories = loadAll();
        categories.add(category);
        saveAll(categories);
    }

    /**
     * Renames an existing category (matched by its current name) and
     * saves the change to disk.
     */
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

    /**
     * Removes the category with the given name and saves the change to disk.
     */
    public void delete(String name) {
        List<Category> categories = loadAll();
        categories.removeIf(category -> category.getName().equalsIgnoreCase(name));
        saveAll(categories);
    }

    /**
     * Searches the saved categories for one matching the given name
     * (case-insensitive).
     */
    public Optional<Category> findByName(String name) {
        for (Category category : loadAll()) {
            if (category.getName().equalsIgnoreCase(name)) {
                return Optional.of(category);
            }
        }
        return Optional.empty();
    }

    /**
     * Creates the data folder and categories.txt file with the four
     * starter categories if they do not already exist. This runs once,
     * the first time the application starts.
     */
    private void createFileWithDefaultsIfMissing() {
        try {
            Path filePath = Path.of(FILE_PATH);

            if (Files.exists(filePath)) {
                return; // file already exists, nothing to do
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
