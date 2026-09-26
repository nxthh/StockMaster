package com.inventory.service;

// simple flat-rate tax calculation
public class TaxCalculator {

    private final double taxRate;

    public TaxCalculator(double taxRate) {
        if (taxRate < 0) {
            throw new IllegalArgumentException("Tax rate cannot be negative.");
        }
        this.taxRate = taxRate;
    }

    public double getTaxRate() {
        return taxRate;
    }

    public double calculateTax(double taxableAmount) {
        return taxableAmount * taxRate;
    }
}
