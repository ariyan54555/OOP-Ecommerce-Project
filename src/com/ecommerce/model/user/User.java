package com.ecommerce.model.user;

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

  
    public abstract String getRole();

   
    @Override
    public String toString() {
        return username + "," + password + "," + getRole();
    }
}
