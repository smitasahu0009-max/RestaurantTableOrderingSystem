import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class RestaurantGUI extends JFrame {

    JTextField tableField;
    JComboBox<String> itemBox;
    JSpinner quantitySpinner;
    JLabel totalLabel;
    JButton statusButton;

    String[] items;
    double[] prices;

    public RestaurantGUI() {

        setTitle("Restaurant Table Ordering System");
        setSize(500, 400);
        setResizable(false);
        getContentPane().setBackground(new Color(245, 245, 245));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        JLabel titleLabel = new JLabel("🍴 RESTAURANT TABLE ORDERING SYSTEM 🍴");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        add(titleLabel, BorderLayout.NORTH);

        JPanel panel = new JPanel(new GridLayout(7, 2, 8, 8));

        panel.add(new JLabel("Table Number:"));
        tableField = new JTextField();
        panel.add(tableField);

        panel.add(new JLabel("Select Item:"));

        loadMenu();

        itemBox = new JComboBox<>(items);
        panel.add(itemBox);

        panel.add(new JLabel("Quantity:"));
        quantitySpinner = new JSpinner(
                new SpinnerNumberModel(1, 1, 20, 1)
        );
        panel.add(quantitySpinner);

        panel.add(new JLabel("Total:"));
        totalLabel = new JLabel("₹0.0");
        panel.add(totalLabel);

        JButton calculateButton = new JButton("Calculate Total");
        panel.add(calculateButton);

        JButton orderButton = new JButton("Place Order");
        panel.add(orderButton);

        JButton viewButton = new JButton("View Orders");
        panel.add(viewButton);

        JButton clearButton = new JButton("Clear");
        panel.add(clearButton);

        JButton deleteButton = new JButton("Delete Order");
        panel.add(deleteButton);

        statusButton = new JButton("Update Status");
        panel.add(statusButton);

        panel.setBorder(BorderFactory.createTitledBorder("Order Details"));

        Font buttonFont = new Font("Arial", Font.BOLD, 14);

        calculateButton.setPreferredSize(new Dimension(180, 40));
        orderButton.setPreferredSize(new Dimension(180, 40));
        viewButton.setPreferredSize(new Dimension(180, 40));
        clearButton.setPreferredSize(new Dimension(180, 40));
        deleteButton.setPreferredSize(new Dimension(180, 40));

        calculateButton.setFont(new Font("Arial", Font.BOLD, 14));
        orderButton.setFont(new Font("Arial", Font.BOLD, 14));
        viewButton.setFont(new Font("Arial", Font.BOLD, 14));
        clearButton.setFont(new Font("Arial", Font.BOLD, 14));

        add(panel);

        calculateButton.addActionListener(e -> calculateTotal());

        orderButton.addActionListener(e -> placeOrder());

        viewButton.addActionListener(e -> viewOrders());

        clearButton.addActionListener(e -> {

            tableField.setText("");
            itemBox.setSelectedIndex(0);
            quantitySpinner.setValue(1);
            totalLabel.setText("₹0.0");
        });

        deleteButton.addActionListener(e -> deleteOrder());

        statusButton.addActionListener(e -> updateStatus());

    }
    void viewOrders() {
        String url = "jdbc:mysql://localhost:3306/restaurant_db";
        String username = "root";
        String password = System.getenv("DB_PASSWORD");
        try {
            Connection con = DriverManager.getConnection(url, username, password);

            String query = "SELECT * FROM orders";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(query);

            String[] columns = {
                    "ID", "Table No.", "Item", "Quantity", "Total", "Status"
            };

            javax.swing.table.DefaultTableModel model =
                    new javax.swing.table.DefaultTableModel(columns, 0);

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getInt("table_no"),
                        rs.getString("item_name"),
                        rs.getInt("quantity"),
                        rs.getDouble("total"),
                        rs.getString("status")
                });
            }

            con.close();

            JTable table = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(table);

            JFrame frame = new JFrame("All Orders");
            frame.add(scrollPane);
            frame.setSize(700, 400);
            frame.setLocationRelativeTo(this);
            frame.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Database Error: " + e.getMessage()
            );
        }
    }
    void deleteOrder() {
        String input = JOptionPane.showInputDialog(this,
                "Enter Order ID to delete:");

        if (input == null || input.isEmpty()) {
            return;
        }

        try {
            int orderId = Integer.parseInt(input);

            String url = "jdbc:mysql://localhost:3306/restaurant_db";
            String username = "root";
            String password = System.getenv("DB_PASSWORD");

            Connection con = DriverManager.getConnection(url, username, password);

            String query = "DELETE FROM orders WHERE id = ?";
            PreparedStatement ps = con.prepareStatement(query);

            ps.setInt(1, orderId);

            int result = ps.executeUpdate();

            con.close();

            if (result > 0) {
                JOptionPane.showMessageDialog(this,
                        "Order deleted successfully!");
            } else {
                JOptionPane.showMessageDialog(this,
                        "Order ID not found!");
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid Order ID!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Database Error: " + e.getMessage());
        }
    }
    void updateStatus() {
        String input = JOptionPane.showInputDialog(this, "Enter Order ID:");

        if (input == null || input.isEmpty()) {
            return;
        }

        try {
            int id = Integer.parseInt(input);

            String[] options = {"Pending", "Preparing", "Served"};

            String status = (String) JOptionPane.showInputDialog(
                    this,
                    "Select Status:",
                    "Update Order Status",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            if (status == null) {
                return;
            }

            String url = "jdbc:mysql://localhost:3306/restaurant_db";
            String username = "root";
            String password = System.getenv("DB_PASSWORD");

            Connection con = DriverManager.getConnection(url, username, password);

            String query = "UPDATE orders SET status = ? WHERE id = ?";
            PreparedStatement ps = con.prepareStatement(query);

            ps.setString(1, status);
            ps.setInt(2, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Order status updated!");
            } else {
                JOptionPane.showMessageDialog(this, "Order ID not found!");
            }

            con.close();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    void loadMenu() {

        String url = "jdbc:mysql://localhost:3306/restaurant_db";
        String username = "root";
        String password = System.getenv("DB_PASSWORD");

        try {
            Connection con = DriverManager.getConnection(url, username, password);

            Statement st = con.createStatement();

            ResultSet rs = st.executeQuery(
                    "SELECT item_name, price FROM menu"
            );

            java.util.ArrayList<String> itemList = new java.util.ArrayList<>();
            java.util.ArrayList<Double> priceList = new java.util.ArrayList<>();

            while (rs.next()) {
                itemList.add(rs.getString("item_name"));
                priceList.add(rs.getDouble("price"));
            }

            items = itemList.toArray(new String[0]);

            prices = new double[priceList.size()];

            for (int i = 0; i < priceList.size(); i++) {
                prices[i] = priceList.get(i);
            }

            con.close();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Menu Database Error: " + e.getMessage()
            );
        }
    }

    void calculateTotal() {
        int index = itemBox.getSelectedIndex();
        int quantity = (int) quantitySpinner.getValue();

        double total = prices[index] * quantity;

        totalLabel.setText("₹" + total);
    }

    void placeOrder() {

        if (tableField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter table number!");
            return;
        }

        int tableNo;

        try {
            tableNo = Integer.parseInt(tableField.getText());
            if (tableNo <= 0) {
                JOptionPane.showMessageDialog(this, "Please enter a valid positive table number!");
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid table number!");
            return;
        }
        int index = itemBox.getSelectedIndex();
        int quantity = (int) quantitySpinner.getValue();

        String itemName = items[index].split(" - ")[0];
        double total = prices[index] * quantity;

        String url = "jdbc:mysql://localhost:3306/restaurant_db";
        String username = "root";
        String password = System.getenv("DB_PASSWORD");

        try {
            Connection con = DriverManager.getConnection(
                    url, username, password
            );

            String query =
                    "INSERT INTO orders " +
                            "(table_no, item_name, quantity, total) " +
                            "VALUES (?, ?, ?, ?)";

            PreparedStatement ps = con.prepareStatement(query);

            ps.setInt(1, tableNo);
            ps.setString(2, itemName);
            ps.setInt(3, quantity);
            ps.setDouble(4, total);

            ps.executeUpdate();

            con.close();

            String bill =
                    "===== RESTAURANT BILL =====\n\n" +
                            "Table No. : " + tableNo + "\n" +
                            "Item      : " + itemName + "\n" +
                            "Quantity  : " + quantity + "\n" +
                            "--------------------------\n" +
                            "Total     : ₹" + total + "\n\n" +
                            "Order Placed Successfully!\n" +
                            "Thank You! Visit Again.";

            JOptionPane.showMessageDialog(this, bill, "Order Receipt",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Database Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new RestaurantGUI().setVisible(true);
    }
}