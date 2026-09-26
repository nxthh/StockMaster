package com.inventory.model;

// concrete role (inheritance + polymorphism via getRole())
public class Cashier extends User {

    public Cashier(String username, String password) {
        super(username, password);
    }

    @Override
    public Role getRole() {
        return Role.CASHIER;
    }
}
