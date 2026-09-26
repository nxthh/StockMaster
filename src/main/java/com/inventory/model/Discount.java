package com.inventory.model;

// contract for discount strategies (abstraction)
public interface Discount {

    double calculateDiscount(double amount);
}
