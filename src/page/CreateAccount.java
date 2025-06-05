package page;

import model.Account;
import model.User;

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
        System.out.println("Enter Security Pin:");
        String securityPin = SC.nextLine();

        String createQuery = "insert into account(balance, security_pin, email) values(?, ?, ?);";
        PreparedStatement createPreparedStatement = CONNECTION.prepareStatement(createQuery);
        createPreparedStatement.setDouble(1, balance);
        createPreparedStatement.setString(2, securityPin);
        createPreparedStatement.setString(3, USER.email);

        int createAffect = createPreparedStatement.executeUpdate();
        createPreparedStatement.close();
        if(createAffect <= 0) {
            System.out.println("Error creating bank account");
            return null;
        }

        String getQuery = "select acc_number, balance from account where email = ?;";
        PreparedStatement getPreparedStatement = CONNECTION.prepareStatement(getQuery);
        getPreparedStatement.setString(1, USER.email);

        ResultSet getRes = getPreparedStatement.executeQuery();

        int storedAccountNumber = 0;
        double storedBalance = 0;

        if(getRes.next()) {
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
