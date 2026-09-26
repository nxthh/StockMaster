package com.inventory.model.payment;

// QR payment (polymorphism)
public class QRPayment extends Payment {

    public QRPayment(double amountDue) {
        super(amountDue);
    }

    @Override
    public void processPayment() {

        markSuccessful();
    }

    @Override
    public String getMethodName() {
        return "QR";
    }
}
