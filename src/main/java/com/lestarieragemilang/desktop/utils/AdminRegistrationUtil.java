package com.lestarieragemilang.desktop.utils;

import com.lestarieragemilang.desktop.model.User;
import com.lestarieragemilang.desktop.service.UserService;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Scanner;
import java.util.UUID;

public class AdminRegistrationUtil {
    private static final UserService userService = new UserService();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== Admin Registration Utility ===");
        System.out.println("Please enter the required information:");

        // Get user input
        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Username: ");
        String username = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        // Validate input
        if (validateInput(email, username, password)) {
            registerAdmin(email, username, password);
        }

        scanner.close();
    }

    private static void registerAdmin(String email, String username, String password) {
        try {
            User user = new User();
            user.setEmail(email);
            user.setUsername(username);
            user.setName(username);

            String salt = UUID.randomUUID().toString().substring(0, 32);
            user.setSalt(salt);
            user.setPasswordHash(BCrypt.hashpw(password + salt, BCrypt.gensalt()));

            userService.save(user);
            System.out.println("Admin registered successfully!");
            System.out.println("Username: " + username);
            
        } catch (Exception e) {
            System.err.println("Error registering admin: " + e.getMessage());
        }
    }

    private static boolean validateInput(String email, String username, String password) {
        if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            System.err.println("Error: All fields must be filled");
            return false;
        }

        if (userService.isUsernameExists(username)) {
            System.err.println("Error: Username already exists");
            return false;
        }

        if (userService.isEmailExists(email)) {
            System.err.println("Error: Email already exists");
            return false;
        }

        return true;
    }
}
