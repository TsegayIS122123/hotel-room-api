package GroceryShoppingApp;

import javax.swing.*;

abstract class User {
    protected String username;
    protected String password;

    public User() {
        this.username = "";
        this.password = "";
    }

    public boolean authenticate(String uname, String pwd) {
        return username.equals(uname) && password.equals(pwd);
    }

    public void setCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public abstract void showMenu(JFrame parentFrame);
}