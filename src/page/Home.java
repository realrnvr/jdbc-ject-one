package page;

import model.Account;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Home {
    private final User USER;
    private final Connection CONNECTION;
    private final Scanner SC;
    private Account account;

    public Home(User user, Connection connection, Scanner sc) {
        this.USER = user;
        this.CONNECTION = connection;
        this.SC = sc;
    }

    public void screen() throws SQLException {
        System.out.println("Welcome to the home page!");
        System.out.println("Username: " + USER.username);
        System.out.println("Email: " + USER.email);

        if(isAccountExists()) {
            accountScreen();
            // give a user option to go to transaction page
            Transaction transaction = new Transaction(this.account, this.SC, this.CONNECTION, this.USER);
            transaction.screen();
        } else {
            noAccountScreen();
        }
    }

    private void noAccountScreen() throws SQLException {
        CreateAccount createAccount = new CreateAccount(CONNECTION, USER, SC);
        boolean isSystemRunning = true;

        while(isSystemRunning) {
            System.out.println("Looks like you dont have any accounts...");
            System.out.println("1. Create new Account");
            System.out.println("2. Exit");
            int choice = SC.nextInt();
            SC.nextLine();

            switch (choice) {
                case 1: {
                    // create a new account
                    this.account = createAccount.newAccount();
                    if(this.account == null) break;

                    // do something
                    accountScreen();
                    isSystemRunning = false;

                    // send user to transaction page
                    Transaction transaction = new Transaction(this.account, this.SC, this.CONNECTION, this.USER);
                    transaction.screen();
                    break;
                }
                case 2: {
                    isSystemRunning = false;
                    System.out.println("Exiting...");
                    break;
                }
                default: {
                    System.out.println("Please select a valid choice");
                    break;
                }
            }
        }
    }

    private void accountScreen() throws SQLException {
        if(this.account == null) {
            String query = "select acc_number, balance from account where email = ?;";
            PreparedStatement preparedStatement = CONNECTION.prepareStatement(query);
            preparedStatement.setString(1, USER.email);

            ResultSet res = preparedStatement.executeQuery();
            if(res.next()) {
                this.account = new Account(res.getInt("acc_number"), res.getDouble("balance"));
            } else {
                System.out.println("Error fetching Account details!");
            }

            preparedStatement.close();
            res.close();
        }

        System.out.println("Account details: ");
        System.out.println("Account Number: " + this.account.accountNumber);
        System.out.println("Account Balance: " + this.account.balance);
    }

    private boolean isAccountExists() throws SQLException {
        String query = "select acc_number from account where email = ?;";
        PreparedStatement preparedStatement = CONNECTION.prepareStatement(query);
        preparedStatement.setString(1, USER.email);

        ResultSet res = preparedStatement.executeQuery();
        boolean isNext = res.next();

        preparedStatement.close();
        res.close();

        return isNext;
    }
}
