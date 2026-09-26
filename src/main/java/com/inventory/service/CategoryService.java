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

/**
 * CategoryService contains the BUSINESS RULES for managing categories:
 * validation, uniqueness checks, and keeping products in sync when a
 * category is renamed or deleted.
 *
 * OOP concept: LAYERED ARCHITECTURE.
 * Controllers never talk to CategoryFileRepository or ProductFileRepository
 * directly - they only call this service. This service is allowed to use
 * BOTH repositories because renaming/deleting a category also affects any
 * product that uses it; keeping that coordination in one place (instead of
 * copy-pasted inside a controller) is exactly what a service layer is for.
 */
public class CategoryService {

    private final CategoryFileRepository categoryFileRepository;
    private final ProductFileRepository productFileRepository;

    public CategoryService(CategoryFileRepository categoryFileRepository,
                            ProductFileRepository productFileRepository) {
        this.categoryFileRepository = categoryFileRepository;
        this.productFileRepository = productFileRepository;
    }

    /**
     * Returns every category currently saved.
     */
    public List<Category> getAllCategories() {
        return categoryFileRepository.loadAll();
    }

    /**
     * Returns every category's name as plain text, for filling
     * ComboBoxes in the UI.
     */
    public List<String> getAllCategoryNames() {
        List<String> names = new ArrayList<>();
        for (Category category : getAllCategories()) {
            names.add(category.getName());
        }
        return names;
    }

    /**
     * Validates and adds a new category.
     *
     * @throws InvalidCategoryException   if the name is blank
     * @throws DuplicateCategoryException if a category with that name (ignoring case) already exists
     */
    public Category addCategory(String name) {
        String trimmedName = validateName(name);

        if (categoryFileRepository.findByName(trimmedName).isPresent()) {
            throw new DuplicateCategoryException("Category \"" + trimmedName + "\" already exists.");
        }

        Category category = new Category(trimmedName);
        categoryFileRepository.add(category);
        return category;
    }

    /**
     * Renames an existing category and updates every product currently
     * using it, so no product is left pointing at a category name that no
     * longer exists.
     *
     * @throws InvalidCategoryException   if the new name is blank
     * @throws CategoryNotFoundException  if the current category does not exist
     * @throws DuplicateCategoryException if another category already uses the new name
     */
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

    /**
     * Deletes a category, as long as no product currently uses it.
     *
     * @throws CategoryNotFoundException if the category does not exist
     * @throws CategoryInUseException    if one or more products still use this category
     */
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

    /**
     * Counts how many products currently use the given category, so the
     * "Manage Categories" screen can show it (and decide whether Delete
     * is safe) before the admin even tries.
     */
    public int countProductsUsingCategory(String name) {
        int count = 0;
        for (Product product : productFileRepository.loadAll()) {
            if (product.getCategory().getName().equalsIgnoreCase(name)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Updates every product using the old category name so it points at
     * the renamed Category instead.
     */
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
