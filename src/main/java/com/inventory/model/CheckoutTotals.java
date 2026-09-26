package com.inventory.model;

public class CheckoutTotals {

    private final double subtotal;
    private final double discountAmount;
    private final double taxAmount;
    private final double total;

    public CheckoutTotals(double subtotal, double discountAmount, double taxAmount, double total) {
        this.subtotal = subtotal;
        this.discountAmount = discountAmount;
        this.taxAmount = taxAmount;
        this.total = total;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public double getTaxAmount() {
        return taxAmount;
    }

    public double getTotal() {
        return total;
    }
}
