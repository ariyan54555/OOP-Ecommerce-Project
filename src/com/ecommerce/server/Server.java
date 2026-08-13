package com.ecommerce.server;

import com.ecommerce.manager.OrderManager;
import com.ecommerce.manager.ProductManager;
import com.ecommerce.manager.UserManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private final int port;
    private ServerSocket serverSocket;

    private final UserManager userManager;
    private final ProductManager productManager;
    private final OrderManager orderManager;

    public Server(int port) {
        this.port = port;
        this.userManager = new UserManager();
        this.productManager = new ProductManager();
        this.orderManager = new OrderManager();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("========================================");
            System.out.println(" E-Commerce Server started on port " + port);
            System.out.println(" Loaded " + productManager.getAllProducts().size() + " product(s) from file.");
            System.out.println(" Waiting for client connections...");
            System.out.println("========================================");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[+] Client connected: " + clientSocket.getInetAddress().getHostAddress());
                ClientHandler handler = new ClientHandler(clientSocket, userManager, productManager, orderManager);
                Thread t = new Thread(handler);
                t.start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        int port = 5000;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
                System.out.println("Invalid port argument, using default 5000.");
            }
        }
        new Server(port).start();
    }
}
