package com.inventory.model.payment;

import com.inventory.exception.PaymentException;

// cash payment, tracks change (polymorphism)
public class CashPayment extends Payment {

    private final double amountPaid;

    public CashPayment(double amountDue, double amountPaid) {
        super(amountDue);
        if (amountPaid < 0) {
            throw new PaymentException("Amount paid cannot be negative.");
        }
        this.amountPaid = amountPaid;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    @Override
    public void processPayment() {
        if (amountPaid < amountDue) { // guard: reject underpayment
            throw new PaymentException(String.format(
                    "Insufficient cash. Total is $%.2f but only $%.2f was paid.", amountDue, amountPaid));
        }
        change = amountPaid - amountDue;
        markSuccessful();
    }

    @Override
    public String getMethodName() {
        return "Cash";
    }
}
