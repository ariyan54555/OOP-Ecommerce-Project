# E-Commerce Management System

A Java-based **E-Commerce Management System** developed as an
Object-Oriented Programming (OOP) project. The system uses a
**Client-Server architecture** to support customer and seller
operations, with socket communication, multithreading, synchronization,
and file-based data storage.

## Project Overview

The system provides a simple e-commerce environment where:

-   Customers can register, log in, browse and search products.
-   Customers can manage their cart and place orders.
-   Customers can view their order history and track order status.
-   Sellers can add and manage their products.
-   Sellers can update product information and stock.
-   Sellers can view relevant orders and update order status.
-   Multiple clients can communicate with the server concurrently.
-   User, product, and order data are persisted in text files.

## Main Features

-   Customer and Seller registration/login
-   Product browsing and searching
-   Shopping cart management
-   Product purchasing and stock management
-   Order placement and order history
-   Seller product management
-   Seller order management
-   Order status lifecycle:
    -   Pending
    -   Confirmed
    -   Packed
    -   Shipped
    -   Delivered
-   Client-Server communication using sockets
-   Multithreaded client handling
-   Synchronization for shared data
-   File-based data persistence
-   Input validation and exception handling

## System Architecture

The project follows a **Client-Server architecture**:

``` text
Customer / Seller
       |
       v
   ClientApp
       |
       | Socket Communication
       v
     Server
       |
       v
  ClientHandler
       |
       +-------------------+
       |         |         |
       v         v         v
 UserManager ProductManager OrderManager
       |         |         |
       v         v         v
   users.txt products.txt orders.txt
```

Each connected client is handled by a separate `ClientHandler` thread.
The Manager classes are responsible for the main business logic and data
operations.

## OOP Concepts Used

### Encapsulation

Important attributes are kept private inside classes and are accessed or
modified through public methods.

Examples include the private fields of:

-   `Product`
-   `Order`
-   `OrderItem`
-   `User`

### Inheritance

`Customer` and `Seller` extend the abstract `User` class.

``` text
User
├── Customer
└── Seller
```

### Polymorphism

The project demonstrates:

-   **Method overloading:** `Customer.addToCart(...)` has different
    parameter lists.
-   **Method overriding:** `Customer` and `Seller` provide their own
    implementation of `User.getRole()`.
-   **Constructor overloading:** `Order` and `OrderItem` provide
    constructors with different parameter lists.

### Abstraction

`User` is an abstract class containing common user information and
behavior. Its abstract `getRole()` method is implemented by `Customer`
and `Seller`.

### Interface

`ClientHandler` implements Java's `Runnable` interface so that each
client connection can run in its own thread.

## Technology Stack

-   **Language:** Java
-   **IDE:** IntelliJ IDEA
-   **Networking:** `Socket`, `ServerSocket`
-   **Concurrency:** Java Threads and `Runnable`
-   **Data Storage:** Text files
-   **Collections:** `ArrayList`, `List`
-   **OOP:** Encapsulation, Inheritance, Polymorphism, Abstraction,
    Interface
-   **Exception Handling:** Java exception handling

## Project Structure

``` text
OOP-Project/
├── data/
│   ├── users.txt
│   ├── products.txt
│   └── orders.txt
│
├── src/
│   └── com/
│       └── ecommerce/
│           ├── client/
│           │   ├── Client.java
│           │   └── ClientApp.java
│           │
│           ├── manager/
│           │   ├── UserManager.java
│           │   ├── ProductManager.java
│           │   └── OrderManager.java
│           │
│           ├── model/
│           │   ├── commerce/
│           │   │   ├── Product.java
│           │   │   ├── Order.java
│           │   │   └── OrderItem.java
│           │   │
│           │   └── user/
│           │       ├── User.java
│           │       ├── Customer.java
│           │       └── Seller.java
│           │
│           └── server/
│               ├── Server.java
│               └── ClientHandler.java
```

## Data Storage

The application uses text files for persistent storage:

  File                  Purpose
  --------------------- ------------------------------------------
  `data/users.txt`      Stores user information
  `data/products.txt`   Stores product and inventory information
  `data/orders.txt`     Stores order information

The Manager classes load the data when they are initialized and save
updates back to the corresponding files.

## Multithreading and Synchronization

The server creates a separate `ClientHandler` thread for each connected
client.

The Manager classes use synchronized methods for shared data operations.
In particular, product purchasing is synchronized so that stock
checking, stock updating, and saving the updated inventory are performed
safely when multiple clients access the system.

This helps prevent inconsistent inventory updates and product
overselling during concurrent purchases.

## How to Run

### 1. Open the project

Open the `OOP-Project` folder in IntelliJ IDEA.

### 2. Start the server

Run:

``` text
com.ecommerce.server.Server
```

The default server port is:

``` text
5000
```

A different port can also be supplied as a command-line argument.

### 3. Start the client

Run:

``` text
com.ecommerce.client.ClientApp
```

The client application connects to the server and provides the available
customer/seller operations through the console.

### 4. Multiple Clients

Run multiple client instances to test concurrent client-server
interaction.

## Team Members

-   Arnab Kumar Ghosh
-   Arian Zaman
-   Apurba Das Arpan
-   Niloy Paul
-   Ariful Islam Emon

## Contribution

This project was developed as a team project. Contributions included:

-   System design
-   UML design
-   Coding and implementation
-   Testing and debugging
-   Documentation

### Niloy Paul

Niloy mainly contributed to:

-   Designing the Idea UML and Current UML
-   Developing `UserManager`
-   Developing `ProductManager`
-   Developing `OrderManager`
-   Implementing user, product, and order management operations
-   Testing and debugging
-   Documentation

## Academic Project

**Course:** Object Oriented Programming (OOP)\
**Project:** E-Commerce Management System\
**Department:** Computer Science and Engineering, Daffodil International
University

------------------------------------------------------------------------

## Authors

Developed as an academic OOP project by the team members listed above.
