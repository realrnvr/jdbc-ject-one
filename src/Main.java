import auth.Process;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Scanner;
import model.User;
import page.Home;
import static util.Config.get;

public class Main {
    private final static String SQL_URI = get("SQL_URI");
    private final static String SQL_USERNAME = get("SQL_USERNAME");
    private final static String SQL_PASSWORD = get("SQL_PASSWORD");

    public static void main(String[] args) {
        try {
            Connection connection = DriverManager.getConnection(SQL_URI, SQL_USERNAME, SQL_PASSWORD);
            Scanner sc = new Scanner(System.in);
            Process process = new Process(connection, sc);

            boolean isSystemRunning = true;

            while (isSystemRunning) {
                System.out.println("Welcome to Banking System:");
                System.out.println("1. Register");
                System.out.println("2. Login");
                System.out.println("3. Exit");
                System.out.println("Select an option:");
                int choice = sc.nextInt();
                sc.nextLine();

                switch (choice) {
                    case 1 -> {
                        // register
                        User user = process.register();
                        if (user == null)
                            break;

                        // do something
                        Home home = new Home(user, connection, sc);
                        home.screen();

                    }
                    case 2 -> {
                        // login
                        User user = process.login();
                        if (user == null)
                            break;

                        // do something
                        Home home = new Home(user, connection, sc);
                        home.screen();

                    }
                    case 3 -> {
                        isSystemRunning = false;
                        System.out.println("Exiting...");
                    }
                    default -> {
                        System.out.println("Please select a valid option!");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}