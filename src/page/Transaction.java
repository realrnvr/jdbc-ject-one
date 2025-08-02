package page;

import model.Account;
import model.User;
import util.HashUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Transaction {
    private Account ACCOUNT;
    private final Scanner SC;
    private final Connection CONNECTION;
    private User USER;

    public Transaction(Account account, Scanner sc, Connection connection, User user) {
        this.ACCOUNT = account;
        this.SC = sc;
        this.CONNECTION = connection;
        this.USER = user;
    }

    public void screen() throws SQLException {
        boolean isSystemRunning = true;

        while(isSystemRunning) {
            System.out.println("Welcome to Transaction page:");
            System.out.println("1. Debit Money");
            System.out.println("2. Credit Money");
            System.out.println("3. Transfer Money");
            System.out.println("4. Check Balance");
            System.out.println("5. Log out");
            System.out.println("Enter a choice:");
            int choice = this.SC.nextInt();
            this.SC.nextLine();

            switch (choice) {
                case 1: {
                    // debit money
                    debitMoney();
                    break;
                }
                case 2: {
                    // credit money
                    creditMoney();
                    break;
                }
                case 3: {
                    // transfer money
                    transferMoney();
                    break;
                }
                case 4: {
                    // check balance
                    checkBalance();
                    break;
                }
                case 5: {
                    isSystemRunning = false;
                    this.ACCOUNT = null;
                    this.USER = null;
                    System.gc();
                    System.out.println("...");
                    break;
                }
            }
        }
    }

    private void debitMoney() throws SQLException {
        if(!isSecurityPinCorrect()) return;

        System.out.println("Enter the amount you want to debit:");
        double currAmount = this.SC.nextDouble();
        this.SC.nextLine();

        String storedMoneyQuery = "select balance from account where acc_number = ? and email = ?;";
        PreparedStatement storedMoneyStatement = this.CONNECTION.prepareStatement(storedMoneyQuery);
        storedMoneyStatement.setInt(1, this.ACCOUNT.accountNumber);
        storedMoneyStatement.setString(2, this.USER.email);

        ResultSet storedMoneyRes = storedMoneyStatement.executeQuery();

        double storedMoney = 0;
        if(storedMoneyRes.next()) {
            storedMoney = storedMoneyRes.getDouble("balance");
        } else {
            System.out.println("Error fetching Account details!");
            return;
        }

        if(currAmount > storedMoney) {
            System.out.println("Insufficient funds!");
            return;
        }

        String debitAmountQuery = "update account set balance = balance - ? where acc_number = ? and email = ?;";
        this.CONNECTION.setAutoCommit(false);
        PreparedStatement debitAmountStatement = this.CONNECTION.prepareStatement(debitAmountQuery);
        debitAmountStatement.setDouble(1, currAmount);
        debitAmountStatement.setInt(2, this.ACCOUNT.accountNumber);
        debitAmountStatement.setString(3, this.USER.email);

        int affect = debitAmountStatement.executeUpdate();

        if(affect <= 0) {
            this.CONNECTION.rollback();
            this.CONNECTION.setAutoCommit(true);
            System.out.println("Error Debiting amount!");
            return;
        }

        this.CONNECTION.commit();
        this.CONNECTION.setAutoCommit(true);
        System.out.println("Amount debited successfully!");

        updateState();
    }

    private void creditMoney() throws SQLException {
        if(!isSecurityPinCorrect()) return;
        System.out.println("Enter the money you want to credit:");
        double currAmount = this.SC.nextDouble();
        this.SC.nextLine();

        String query = "update account set balance = balance + ? where acc_number = ? and email = ?;";
        this.CONNECTION.setAutoCommit(false);
        PreparedStatement preparedStatement = this.CONNECTION.prepareStatement(query);
        preparedStatement.setDouble(1, currAmount);
        preparedStatement.setInt(2, this.ACCOUNT.accountNumber);
        preparedStatement.setString(3, this.USER.email);

        int affect = preparedStatement.executeUpdate();
        if(affect <= 0) {
            this.CONNECTION.rollback();
            this.CONNECTION.setAutoCommit(true);
            System.out.println("Error crediting amount!");
            return;
        }

        this.CONNECTION.commit();
        this.CONNECTION.setAutoCommit(true);
        System.out.println("Amount credited!");

        updateState();
    }

    private boolean isSecurityPinCorrect() throws SQLException {
        System.out.println("Enter the security pin:");
        String enteredSecurityPin = this.SC.nextLine();

        String query = "SELECT security_pin FROM account WHERE acc_number = ?;";
        PreparedStatement preparedStatement = this.CONNECTION.prepareStatement(query);
        preparedStatement.setInt(1, this.ACCOUNT.accountNumber);

        ResultSet res = preparedStatement.executeQuery();
        String storedHashedPin = "";

        if (res.next()) {
            storedHashedPin = res.getString("security_pin");
            System.out.println("Security pin fetched!");
        } else {
            System.out.println("Error fetching security pin!");
            return false;
        }

        preparedStatement.close();
        res.close();

        // Use HashUtil to check hashed PIN
        if (!HashUtil.check(enteredSecurityPin, storedHashedPin)) {
            System.out.println("Entered pin is incorrect!");
            return false;
        }

        System.out.println("Entered pin is correct!");
        return true;
    }


    private void updateState() throws SQLException {
        String query = "select balance from account where acc_number = ? and email = ?;";
        PreparedStatement preparedStatement = this.CONNECTION.prepareStatement(query);
        preparedStatement.setInt(1, ACCOUNT.accountNumber);
        preparedStatement.setString(2, USER.email);
        ResultSet res = preparedStatement.executeQuery();

        if(res.next()) {
            this.ACCOUNT.balance = res.getDouble("balance");
            System.out.println("Amount updated successfully!");
        } else {
            System.out.println("Error updating amount!");
            return;
        }

        System.out.println("Current Amount: " + this.ACCOUNT.balance);
    }

    private boolean isSufficientAmount(double amount) throws SQLException {
        String storedMoneyQuery = "select balance from account where acc_number = ? and email = ?;";
        PreparedStatement storedMoneyStatement = this.CONNECTION.prepareStatement(storedMoneyQuery);
        storedMoneyStatement.setInt(1, this.ACCOUNT.accountNumber);
        storedMoneyStatement.setString(2, this.USER.email);

        ResultSet storedMoneyRes = storedMoneyStatement.executeQuery();

        double storedMoney = 0;
        if(storedMoneyRes.next()) {
            storedMoney = storedMoneyRes.getDouble("balance");
        } else {
            System.out.println("Error fetching Account details!");
            return false;
        }

        if(amount > storedMoney) {
            System.out.println("Insufficient funds!");
            return false;
        }

        return true;
    }

    private void transferMoney() throws SQLException {
        if(!isSecurityPinCorrect()) return;

        System.out.println("Enter Account number of the recipient:");
        int accountNumber = this.SC.nextInt();
        System.out.println("Enter the Amount to transfer:");
        double currAmount = this.SC.nextDouble();

        if(accountNumber == this.ACCOUNT.accountNumber) {
            System.out.println("Can not transfer money to same account!");
            return;
        }

        String checkAccountQuery = "select balance from account where acc_number = ?;";
        PreparedStatement checkAccountStatement = this.CONNECTION.prepareStatement(checkAccountQuery);
        checkAccountStatement.setInt(1, accountNumber);

        ResultSet checkAccountRes = checkAccountStatement.executeQuery();
        if(checkAccountRes.next()) {
            System.out.println("Recipient Account found!");
        } else {
            System.out.println("No account exists with the provided account number");
            return;
        }

        if(!isSufficientAmount(currAmount)) return;

        String subAmountQuery = "update account set balance = balance - ? where acc_number = ?;";
        this.CONNECTION.setAutoCommit(false);
        PreparedStatement subAmountStatement = this.CONNECTION.prepareStatement(subAmountQuery);
        subAmountStatement.setDouble(1, currAmount);
        subAmountStatement.setInt(2, this.ACCOUNT.accountNumber);

        int subAffect = subAmountStatement.executeUpdate();
        if(subAffect <= 0) {
            this.CONNECTION.rollback();
            this.CONNECTION.setAutoCommit(true);
            System.out.println("Error debiting Amount!");
            return;
        }

        this.CONNECTION.commit();
        System.out.println("Amount debited successfully!");

        System.out.println("Transferring amount...");

        String addAmountQuery = "update account set balance = balance + ? where acc_number = ?;";
        PreparedStatement addAmountStatement = this.CONNECTION.prepareStatement(addAmountQuery);
        addAmountStatement.setDouble(1, currAmount);
        addAmountStatement.setInt(2, accountNumber);

        int addAffect = addAmountStatement.executeUpdate();
        if(addAffect <= 0) {
            this.CONNECTION.rollback();
            this.CONNECTION.setAutoCommit(true);
            System.out.println("Error Transferring Amount!");
            return;
        }

        this.CONNECTION.commit();
        this.CONNECTION.setAutoCommit(true);
        System.out.println("Amount Transferred successfully!");

        updateState();
    }

    public void checkBalance() throws SQLException {
        if(!isSecurityPinCorrect()) return;

        System.out.println("Current bank balance is: " + this.ACCOUNT.balance);
    }
}
