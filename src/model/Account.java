package model;

//        +--------------+---------------+------+-----+---------+----------------+
//        | Field        | Type          | Null | Key | Default | Extra          |
//        +--------------+---------------+------+-----+---------+----------------+
//        | acc_number   | int           | NO   | PRI | NULL    | auto_increment |
//        | balance      | decimal(10,2) | NO   |     | NULL    |                |
//        | security_pin | varchar(4)    | NO   |     | NULL    |                |
//        | email        | varchar(255)  | YES  | MUL | NULL    |                |
//        +--------------+---------------+------+-----+---------+----------------+

public class Account {
    public int accountNumber;
    public double balance;

    public Account(int accountNumber, double balance) {
        this.accountNumber = accountNumber;
        this.balance = balance;
    }
}
