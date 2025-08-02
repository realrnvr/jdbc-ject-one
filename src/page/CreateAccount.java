package page;

import model.Account;
import model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class CreateAccount {
    private final Connection CONNECTION;
    private final User USER;
    private final Scanner SC;

    public CreateAccount(Connection connection, User user, Scanner sc) {
        this.CONNECTION = connection;
        this.USER = user;
        this.SC = sc;
    }

    public Account newAccount() throws SQLException {
        System.out.println("New Account Creation!");
        System.out.println("Enter Initial Amount:");
        double balance = SC.nextDouble();
        SC.nextLine();

        String securityPin;
        while (true) {
            System.out.println("Enter Security Pin (4-6 digits recommended):");
            securityPin = SC.nextLine().trim();

            if (!securityPin.matches("\\d{4,6}")) {
                System.out.println("Security PIN must be 4-6 digits. Please try again.");
                continue;
            }
            break;
        }

        // Hash the security PIN using BCrypt
        String hashedPin = BCrypt.hashpw(securityPin, BCrypt.gensalt(12));

        String createQuery = "INSERT INTO account(balance, security_pin, email) VALUES(?, ?, ?);";
        PreparedStatement createPreparedStatement = CONNECTION.prepareStatement(createQuery);
        createPreparedStatement.setDouble(1, balance);
        createPreparedStatement.setString(2, hashedPin);
        createPreparedStatement.setString(3, USER.email);

        int createAffect = createPreparedStatement.executeUpdate();
        createPreparedStatement.close();
        if (createAffect <= 0) {
            System.out.println("Error creating bank account");
            return null;
        }

        String getQuery = "SELECT acc_number, balance FROM account WHERE email = ?;";
        PreparedStatement getPreparedStatement = CONNECTION.prepareStatement(getQuery);
        getPreparedStatement.setString(1, USER.email);

        ResultSet getRes = getPreparedStatement.executeQuery();

        int storedAccountNumber = 0;
        double storedBalance = 0;

        if (getRes.next()) {
            storedAccountNumber = getRes.getInt("acc_number");
            storedBalance = getRes.getDouble("balance");
        } else {
            System.out.println("Error fetching account details!");
            return null;
        }

        getPreparedStatement.close();
        getRes.close();
        System.out.println("Account creation successful!");

        return new Account(storedAccountNumber, storedBalance);
    }
}
