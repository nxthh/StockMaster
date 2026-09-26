package com.inventory.model;

import com.inventory.exception.InvalidDiscountException;

// discount by percentage (interface implementation)
public class PercentageDiscount implements Discount {

    private final double percentage;

    public PercentageDiscount(double percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new InvalidDiscountException("Discount percentage must be between 0 and 100.");
        }
        this.percentage = percentage;
    }

    public double getPercentage() {
        return percentage;
    }

    @Override
    public double calculateDiscount(double amount) {
        return amount * (percentage / 100.0);
    }
}
