package auth;

import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Process {
    private final Connection CONNECTION;
    private final Scanner SC;

    public Process(Connection connection, Scanner sc) {
        this.CONNECTION = connection;
        this.SC = sc;
    }

    public User register() throws SQLException {
        System.out.println("Enter username:");
        String username = SC.nextLine();
        System.out.println("Enter Email:");
        String email = SC.nextLine();
        System.out.println("Enter password:");
        String password = SC.nextLine();

        if(isUserExists(email)) {
            System.out.println("User Already Exists, Please login!");
            return null;
        }

        while(isUsernameExists(username)) {
            System.out.println("Username already exists, please use a different one!");
            System.out.println("Enter username:");
            username = SC.nextLine();
        }

        String query = "insert into user(username, password, email) values(?, ?, ?);";
        PreparedStatement preparedStatement = CONNECTION.prepareStatement(query);
        preparedStatement.setString(1, username);
        preparedStatement.setString(2, password);
        preparedStatement.setString(3, email);

        int affect = preparedStatement.executeUpdate();
        if(affect <= 0) {
            System.out.println("Error registering user!");
            return null;
        }

        System.out.println("User registered successfully!");
        preparedStatement.close();

        return new User(email, username);
    }

    public User login() throws SQLException {
        System.out.println("Enter email:");
        String email = SC.nextLine();
        System.out.println("Enter password:");
        String password = SC.nextLine();

        if(!isUserExists(email)) {
            System.out.println("User does not exists, Please register!");
            return null;
        }

        String query = "select * from user where email = ?;";
        PreparedStatement preparedStatement = CONNECTION.prepareStatement(query);
        preparedStatement.setString(1, email);
        ResultSet res = preparedStatement.executeQuery();

        String storedPassword = "";
        String storedEmail = "";
        String storedUsername = "";

        if(res.next()) {
            storedPassword = res.getString("password");
            storedEmail = res.getString("email");
            storedUsername = res.getString("username");
        } else {
            System.out.println("Error fetching user details!");
            return null;
        }

        if(!password.equals(storedPassword)) {
            System.out.println("Incorrect password!");
            return null;
        }

        System.out.println("Login successful!");
        preparedStatement.close();
        res.close();

        return new User(storedEmail, storedUsername);
    }

    public boolean isUserExists(String email) throws SQLException {
        String query = "select email from user where email = ?;";
        PreparedStatement preparedStatement = CONNECTION.prepareStatement(query);
        preparedStatement.setString(1, email);

        ResultSet res = preparedStatement.executeQuery();
        boolean isNext = res.next();

        preparedStatement.close();
        res.close();
        return isNext;
    }

    public boolean isUsernameExists(String username) throws SQLException {
        String query = "select username from user where username = ?;";
        PreparedStatement preparedStatement = CONNECTION.prepareStatement(query);
        preparedStatement.setString(1, username);

        ResultSet res = preparedStatement.executeQuery();
        boolean isNext = res.next();

        preparedStatement.close();
        res.close();

        return isNext;
    }
}
