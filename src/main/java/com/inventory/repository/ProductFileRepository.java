package com.inventory.repository;

import com.inventory.model.Category;
import com.inventory.model.Product;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// file I/O layer: reads/writes products.txt (one repo per entity)
public class ProductFileRepository {

    private static final String FILE_PATH = "data/products.txt";

    public ProductFileRepository() {
        createFileWithSampleDataIfMissing();
    }

    // read every product line into memory
    public List<Product> loadAll() {
        List<Product> products = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                try {
                    products.add(Product.fromFileLine(line));
                } catch (RuntimeException e) {
                    // skip one bad line instead of failing the whole load
                    System.out.println("Skipping invalid product record: " + line + " (" + e.getMessage() + ")");
                }
            }
        } catch (IOException e) {
            System.out.println("Could not read product file: " + e.getMessage());
        }

        return products;
    }

    // overwrite the file with the full current list
    public void saveAll(List<Product> products) {
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            for (Product product : products) {
                writer.write(product.toFileLine());
                writer.write(System.lineSeparator());
            }
        } catch (IOException e) {
            System.out.println("Could not save product file: " + e.getMessage());
        }
    }

    public void add(Product product) {
        List<Product> products = loadAll();
        products.add(product);
        saveAll(products);
    }

    // find by id, replace, then rewrite the whole file
    public void update(Product updatedProduct) {
        List<Product> products = loadAll();
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId().equalsIgnoreCase(updatedProduct.getId())) {
                products.set(i, updatedProduct);
                break;
            }
        }
        saveAll(products);
    }

    public void delete(String productId) {
        List<Product> products = loadAll();
        products.removeIf(product -> product.getId().equalsIgnoreCase(productId));
        saveAll(products);
    }

    public Optional<Product> findById(String productId) {
        for (Product product : loadAll()) {
            if (product.getId().equalsIgnoreCase(productId)) {
                return Optional.of(product);
            }
        }
        return Optional.empty();
    }

    // first run: seed the data file with sample products
    private void createFileWithSampleDataIfMissing() {
        try {
            Path filePath = Path.of(FILE_PATH);

            if (Files.exists(filePath)) {
                return;
            }

            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }

            List<Product> sampleProducts = new ArrayList<>();
            sampleProducts.add(new Product("P001", "Coca Cola", new Category("Drink"), 1.50, 50, 10));
            sampleProducts.add(new Product("P002", "Pepsi", new Category("Drink"), 1.50, 30, 10));
            sampleProducts.add(new Product("P003", "Bread", new Category("Food"), 2.00, 15, 5));
            sampleProducts.add(new Product("P004", "Milk", new Category("Dairy"), 2.50, 20, 5));
            sampleProducts.add(new Product("P005", "Instant Noodles", new Category("Food"), 1.25, 40, 10));

            saveAll(sampleProducts);
        } catch (IOException e) {
            System.out.println("Could not create product file: " + e.getMessage());
        }
    }
}
