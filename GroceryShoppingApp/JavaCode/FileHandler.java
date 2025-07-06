package GroceryShoppingApp;

import javax.swing.*;
import java.io.*;

class FileHandler {
    public static void saveInvoice(String f, String c) {
        try (FileWriter w = new FileWriter(f)) {
            w.write(c);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }
}