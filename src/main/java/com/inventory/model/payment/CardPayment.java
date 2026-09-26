package com.inventory.model.payment;

// card payment (polymorphism)
public class CardPayment extends Payment {

    public CardPayment(double amountDue) {
        super(amountDue);
    }

    @Override
    public void processPayment() {

        markSuccessful();
    }

    @Override
    public String getMethodName() {
        return "Card";
    }
}
