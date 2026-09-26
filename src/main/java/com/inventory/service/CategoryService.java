package com.inventory.service;

import com.inventory.exception.CategoryInUseException;
import com.inventory.exception.CategoryNotFoundException;
import com.inventory.exception.DuplicateCategoryException;
import com.inventory.exception.InvalidCategoryException;
import com.inventory.model.Category;
import com.inventory.model.Product;
import com.inventory.repository.CategoryFileRepository;
import com.inventory.repository.ProductFileRepository;

import java.util.ArrayList;
import java.util.List;

// business logic + validation for categories
public class CategoryService {

    private final CategoryFileRepository categoryFileRepository;
    private final ProductFileRepository productFileRepository;

    public CategoryService(CategoryFileRepository categoryFileRepository,
                            ProductFileRepository productFileRepository) {
        this.categoryFileRepository = categoryFileRepository;
        this.productFileRepository = productFileRepository;
    }

    public List<Category> getAllCategories() {
        return categoryFileRepository.loadAll();
    }

    public List<String> getAllCategoryNames() {
        List<String> names = new ArrayList<>();
        for (Category category : getAllCategories()) {
            names.add(category.getName());
        }
        return names;
    }

    // validate, reject duplicates, then persist
    public Category addCategory(String name) {
        String trimmedName = validateName(name);

        if (categoryFileRepository.findByName(trimmedName).isPresent()) {
            throw new DuplicateCategoryException("Category \"" + trimmedName + "\" already exists.");
        }

        Category category = new Category(trimmedName);
        categoryFileRepository.add(category);
        return category;
    }

    // rename + cascade the new name onto every product using it
    public void renameCategory(String currentName, String newName) {
        String trimmedNewName = validateName(newName);

        Category existing = categoryFileRepository.findByName(currentName)
                .orElseThrow(() -> new CategoryNotFoundException("No category found named: " + currentName));

        boolean nameActuallyChanging = !existing.getName().equalsIgnoreCase(trimmedNewName);
        if (nameActuallyChanging && categoryFileRepository.findByName(trimmedNewName).isPresent()) {
            throw new DuplicateCategoryException("Category \"" + trimmedNewName + "\" already exists.");
        }

        Category renamed = new Category(trimmedNewName);
        categoryFileRepository.update(currentName, renamed);

        if (nameActuallyChanging) {
            renameCategoryOnProducts(currentName, renamed);
        }
    }

    // guard: block deleting a category still in use
    public void deleteCategory(String name) {
        categoryFileRepository.findByName(name)
                .orElseThrow(() -> new CategoryNotFoundException("No category found named: " + name));

        int productsUsingIt = countProductsUsingCategory(name);
        if (productsUsingIt > 0) {
            throw new CategoryInUseException(
                    "Cannot delete \"" + name + "\" - " + productsUsingIt +
                            " product(s) still use it. Re-categorize or remove them first.");
        }

        categoryFileRepository.delete(name);
    }

    // how many products currently use this category
    public int countProductsUsingCategory(String name) {
        int count = 0;
        for (Product product : productFileRepository.loadAll()) {
            if (product.getCategory().getName().equalsIgnoreCase(name)) {
                count++;
            }
        }
        return count;
    }

    // update every matching product's category, then save once
    private void renameCategoryOnProducts(String oldName, Category renamed) {
        List<Product> products = productFileRepository.loadAll();
        boolean changedAny = false;

        for (Product product : products) {
            if (product.getCategory().getName().equalsIgnoreCase(oldName)) {
                product.setCategory(renamed);
                changedAny = true;
            }
        }

        if (changedAny) {
            productFileRepository.saveAll(products);
        }
    }

    private String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidCategoryException("Category name cannot be empty.");
        }
        return name.trim();
    }
}
