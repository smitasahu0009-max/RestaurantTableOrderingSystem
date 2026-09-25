import java.sql.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        String url = "jdbc:mysql://localhost:3306/restaurant_db";
        String username = "root";
        String password = System.getenv("DB_PASSWORD");

        Scanner sc = new Scanner(System.in);

        try {
            Connection con = DriverManager.getConnection(url, username, password);

            System.out.print("Enter Table Number: ");
            int tableNo = sc.nextInt();

            System.out.println("\n===== RESTAURANT MENU =====");

            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM menu");

            while (rs.next()) {
                System.out.println(
                        rs.getInt("id") + ". " +
                                rs.getString("item_name") +
                                " - ₹" + rs.getDouble("price")
                );
            }

            System.out.print("\nEnter Item ID: ");
            int itemId = sc.nextInt();

            System.out.print("Enter Quantity: ");
            int quantity = sc.nextInt();

            PreparedStatement ps = con.prepareStatement(
                    "SELECT item_name, price FROM menu WHERE id = ?"
            );

            ps.setInt(1, itemId);
            ResultSet item = ps.executeQuery();

            if (item.next()) {

                String itemName = item.getString("item_name");
                double price = item.getDouble("price");
                double total = price * quantity;

                PreparedStatement orderPs = con.prepareStatement(
                        "INSERT INTO orders (table_no, item_name, quantity, total) VALUES (?, ?, ?, ?)"
                );

                orderPs.setInt(1, tableNo);
                orderPs.setString(2, itemName);
                orderPs.setInt(3, quantity);
                orderPs.setDouble(4, total);

                orderPs.executeUpdate();

                System.out.println("\n===== ORDER CONFIRMED =====");
                System.out.println("Table No: " + tableNo);
                System.out.println("Item: " + itemName);
                System.out.println("Quantity: " + quantity);
                System.out.println("Total: ₹" + total);

            } else {
                System.out.println("Invalid Item ID!");
            }

            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}