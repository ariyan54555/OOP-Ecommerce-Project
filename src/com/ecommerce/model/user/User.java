package com.ecommerce.model.user;

/**
 * Abstract base class for all system users.
 * Demonstrates: Abstraction, Encapsulation (private/protected fields + accessors).
 */
public abstract class User {

    protected String username;
    protected String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean authenticate(String password) {
        return this.password.equals(password);
    }

    /** Polymorphic - each subclass returns its own role string. */
    public abstract String getRole();

    /** Used when persisting to users.txt : username,password,ROLE */
    @Override
    public String toString() {
        return username + "," + password + "," + getRole();
    }
}
