package auth;

import model.User;
import util.HashUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Process {
    private final Connection CONNECTION;
    private final Scanner SC;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    public Process(Connection connection, Scanner sc) {
        this.CONNECTION = connection;
        this.SC = sc;
    }

    public User register() throws SQLException {
        String username;
        String email;
        String password;

        // Username validation
        while (true) {
            System.out.println("Enter username (must be at least 5 characters):");
            username = SC.nextLine().trim();
            if (username.length() < 5) {
                System.out.println("Username must be at least 5 characters long.");
                continue;
            }
            if (isUsernameExists(username)) {
                System.out.println("Username already exists, please use a different one.");
                continue;
            }
            break;
        }

        // Email validation
        while (true) {
            System.out.println("Enter Email:");
            email = SC.nextLine().trim();
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                System.out.println("Invalid email format. Please try again.");
                continue;
            }
            if (isUserExists(email)) {
                System.out.println("User with this email already exists, please login.");
                return null;
            }
            break;
        }

        // Password validation
        while (true) {
            System.out.println("Enter password (must be at least 8 characters):");
            password = SC.nextLine();
            if (password.length() < 8) {
                System.out.println("Password must be at least 8 characters long.");
                continue;
            }
            break;
        }

        // Hash password using HashUtil
        String hashedPassword = HashUtil.hash(password);

        // Store in database
        String query = "INSERT INTO user(username, password, email) VALUES(?, ?, ?);";
        PreparedStatement preparedStatement = CONNECTION.prepareStatement(query);
        preparedStatement.setString(1, username);
        preparedStatement.setString(2, hashedPassword);
        preparedStatement.setString(3, email);

        int affect = preparedStatement.executeUpdate();
        preparedStatement.close();

        if (affect <= 0) {
            System.out.println("Error registering user.");
            return null;
        }

        System.out.println("User registered successfully.");
        return new User(email, username);
    }

    public User login() throws SQLException {
        System.out.println("Enter email:");
        String email = SC.nextLine().trim();
        if (email.isEmpty()) {
            System.out.println("Email cannot be empty.");
            return null;
        }

        System.out.println("Enter password:");
        String password = SC.nextLine();
        if (password.isEmpty()) {
            System.out.println("Password cannot be empty.");
            return null;
        }

        if (!isUserExists(email)) {
            System.out.println("User does not exist, please register.");
            return null;
        }

        String query = "SELECT * FROM user WHERE email = ?;";
        PreparedStatement preparedStatement = CONNECTION.prepareStatement(query);
        preparedStatement.setString(1, email);
        ResultSet res = preparedStatement.executeQuery();

        String storedPassword = "";
        String storedEmail = "";
        String storedUsername = "";

        if (res.next()) {
            storedPassword = res.getString("password");
            storedEmail = res.getString("email");
            storedUsername = res.getString("username");
        } else {
            System.out.println("Error fetching user details.");
            return null;
        }

        // Check hashed password using HashUtil
        if (!HashUtil.check(password, storedPassword)) {
            System.out.println("Incorrect password.");
            return null;
        }

        System.out.println("Login successful.");
        preparedStatement.close();
        res.close();

        return new User(storedEmail, storedUsername);
    }

    public boolean isUserExists(String email) throws SQLException {
        String query = "SELECT email FROM user WHERE email = ?;";
        PreparedStatement preparedStatement = CONNECTION.prepareStatement(query);
        preparedStatement.setString(1, email);

        ResultSet res = preparedStatement.executeQuery();
        boolean isNext = res.next();

        preparedStatement.close();
        res.close();
        return isNext;
    }

    public boolean isUsernameExists(String username) throws SQLException {
        String query = "SELECT username FROM user WHERE username = ?;";
        PreparedStatement preparedStatement = CONNECTION.prepareStatement(query);
        preparedStatement.setString(1, username);

        ResultSet res = preparedStatement.executeQuery();
        boolean isNext = res.next();

        preparedStatement.close();
        res.close();

        return isNext;
    }
}
