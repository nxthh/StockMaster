package com.inventory.model;

// concrete role (inheritance + polymorphism via getRole())
public class Admin extends User {

    public Admin(String username, String password) {
        super(username, password);
    }

    @Override
    public Role getRole() {
        return Role.ADMIN;
    }
}
