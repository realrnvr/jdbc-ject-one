package model;

//        +----------+--------------+------+-----+---------+-------+
//        | Field    | Type         | Null | Key | Default | Extra |
//        +----------+--------------+------+-----+---------+-------+
//        | username | varchar(30)  | NO   | UNI | NULL    |       |
//        | password | varchar(255) | NO   |     | NULL    |       |
//        | email    | varchar(255) | NO   | PRI | NULL    |       |
//        +----------+--------------+------+-----+---------+-------+

public class User {
    public String email;
    public String username;

    public User(String email, String username) {
        this.email = email;
        this.username = username;
    }

    @Override
    public String toString() {
        return "email: " + this.email + ", " + "username: " + this.username;
    }
}
