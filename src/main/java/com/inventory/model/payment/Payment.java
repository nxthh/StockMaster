package com.inventory.model.payment;

// abstract base for payment methods (abstraction)
public abstract class Payment {

    protected final double amountDue;
    protected double change;
    private boolean successful;

    protected Payment(double amountDue) {
        this.amountDue = amountDue;
    }

    public abstract void processPayment(); // overridden per method (polymorphism)

    public abstract String getMethodName();

    public double getAmountDue() {
        return amountDue;
    }

    public double getChange() {
        return change;
    }

    public boolean isSuccessful() {
        return successful;
    }

    protected void markSuccessful() {
        this.successful = true;
    }
}
