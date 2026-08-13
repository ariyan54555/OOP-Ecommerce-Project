package com.ecommerce.manager;

import com.ecommerce.model.user.Customer;
import com.ecommerce.model.user.Seller;
import com.ecommerce.model.user.User;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UserManager {

    private static final String FILE_PATH = "data/users.txt";
    private List<User> users;

    public UserManager() {
        users = new ArrayList<>();
        loadUsers();
    }

    public synchronized void loadUsers() {
        users.clear();
        File file = new File(FILE_PATH);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", 3);
                String username = parts[0];
                String password = parts[1];
                String role = parts[2];
                User u = role.equalsIgnoreCase("SELLER")
                        ? new Seller(username, password)
                        : new Customer(username, password);
                users.add(u);
            }
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }

    public synchronized void saveUsers() {
        File file = new File(FILE_PATH);
        file.getParentFile().mkdirs();
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            for (User u : users) {
                pw.println(u.toString());
            }
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    public synchronized boolean userExists(String username) {
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username)) return true;
        }
        return false;
    }

    /** Corner case: duplicate username is rejected before account creation. */
    public synchronized String register(String username, String password, String role) {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            return "FAIL:Username and password cannot be empty";
        }
        if (userExists(username)) {
            return "FAIL:Username already exists";
        }
        if (!role.equalsIgnoreCase("CUSTOMER") && !role.equalsIgnoreCase("SELLER")) {
            return "FAIL:Role must be CUSTOMER or SELLER";
        }
        User u = role.equalsIgnoreCase("SELLER")
                ? new Seller(username, password)
                : new Customer(username, password);
        users.add(u);
        saveUsers();
        return "SUCCESS:" + u.getRole();
    }

   
    public synchronized User login(String username, String password) {
        for (User u : users) {
            if (u.getUsername().equals(username) && u.authenticate(password)) {
                return u;
            }
        }
        return null;
    }
}
