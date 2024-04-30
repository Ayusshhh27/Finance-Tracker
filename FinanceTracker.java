import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.Locale;
import java.util.Vector;

public class FinanceTracker extends JFrame {
    private final DefaultTableModel tableModel;
    private final JTextField descriptionInput;
    private final JTextField amountInput;
    private final JComboBox<String> categoryInput;
    private final JTextField budgetInput;
    private final JLabel balanceLabel;
    private double budget;
    private double balance;
    private final Vector<String> categories = new Vector<>();
    private final JPanel mainPanel;
    private final JScrollPane scrollPane;
    private NumberFormat currencyFormat;
    private DateTimeFormatter dateTimeFormatter;

    public FinanceTracker() {
        setTitle("Personal Finance Tracker");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Initialize categories
        categories.add("Food");
        categories.add("Travel");
        categories.add("Education");
        categories.add("Other");

        // Currency format for displaying amounts
        currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

        // Date time format
        dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Table model
        tableModel = new DefaultTableModel();
        tableModel.addColumn("Description");
        tableModel.addColumn("Amount");
        tableModel.addColumn("Category");
        tableModel.addColumn("Date & Time");

        // Table
        JTable table = new JTable(tableModel);
        scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Transactions"));

        // Toggle for transaction summary
        JCheckBox toggleTransactions = new JCheckBox("Show Transactions", true);
        toggleTransactions.addActionListener(e -> scrollPane.setVisible(toggleTransactions.isSelected()));

        // Input and budget panels
        JPanel inputAndBudgetPanel = new JPanel(new GridLayout(2, 1));

        // Budget panel
        JPanel budgetPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        budgetInput = new JTextField(10);
        budgetInput.setFont(new Font("Arial", Font.PLAIN, 16));
        JButton setBudgetButton = new JButton("Set Budget");
        setBudgetButton.setFont(new Font("Arial", Font.BOLD, 14));
        balanceLabel = new JLabel("Balance: " + this.currencyFormat.format(0));
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        JButton newCategoryButton = new JButton("New Category");
        newCategoryButton.setFont(new Font("Arial", Font.BOLD, 14));
        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> resetApplication());

        budgetPanel.add(new JLabel("Monthly Budget:"));
        budgetPanel.add(budgetInput);
        budgetPanel.add(setBudgetButton);
        budgetPanel.add(balanceLabel);
        budgetPanel.add(newCategoryButton);
        budgetPanel.setBorder(BorderFactory.createTitledBorder("Budget Settings"));
        budgetPanel.setBackground(new Color(255, 228, 196)); // Peach background

        // Input panel
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        descriptionInput = new JTextField(10);
        amountInput = new JTextField(10);
        categoryInput = new JComboBox<>(categories);
        JButton addButton = new JButton("Add Transaction");

        inputPanel.add(new JLabel("Description:"));
        inputPanel.add(descriptionInput);
        inputPanel.add(new JLabel("Amount:"));
        inputPanel.add(amountInput);
        inputPanel.add(new JLabel("Category:"));
        inputPanel.add(categoryInput);
        inputPanel.add(addButton);
        budgetPanel.add(resetButton);
        inputPanel.setBorder(BorderFactory.createTitledBorder("Add New Transaction"));
        inputPanel.setBackground(new Color(204, 255, 255));

        // Combine budget and input panels
        inputAndBudgetPanel.add(budgetPanel);
        inputAndBudgetPanel.add(inputPanel);

        // Main panel setup
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(inputAndBudgetPanel, BorderLayout.NORTH);
        mainPanel.add(toggleTransactions, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        amountInput.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                "addTransaction");
        amountInput.getActionMap().put("addTransaction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addTransaction();
            }
        });

        // Set background colors for panels
        inputAndBudgetPanel.setBackground(new Color(240, 240, 240));
        mainPanel.setBackground(new Color(240, 240, 240));

        // Actions
        setBudgetButton.addActionListener(e -> setBudget());
        addButton.addActionListener(e -> addTransaction());
        newCategoryButton.addActionListener(e -> addNewCategory());

    }

    private void setBudget() {
        try {
            budget = Double.parseDouble(budgetInput.getText());
            if (budget <= 0) {
                throw new IllegalArgumentException("Budget must be a positive number.");
            }
            balance = budget;
            balanceLabel.setText("Balance: " + this.currencyFormat.format(balance));
            budgetInput.setText("");
            JOptionPane.showMessageDialog(null, "Budget set successfully.");
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(null, "Invalid budget amount. Please enter a valid number.");
        } catch (IllegalArgumentException iae) {
            JOptionPane.showMessageDialog(null, iae.getMessage());
        }
    }

    private void addTransaction() {
        String description = descriptionInput.getText().trim();
        String amountStr = amountInput.getText().trim();
        String category = (String) categoryInput.getSelectedItem();
        try {
            if (description.isEmpty() || amountStr.isEmpty()) {
                throw new IllegalArgumentException("Both description and amount must be provided.");
            }
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                throw new IllegalArgumentException("Amount must be a positive number.");
            }
            if ((balance - amount) < 0) {
                throw new IllegalArgumentException("Transaction amount exceeds budget!");
            }
            LocalDateTime dateTime = LocalDateTime.now();
            String formattedDateTime = dateTime.format(dateTimeFormatter);
            tableModel.addRow(new Object[] { formattedDateTime, description, currencyFormat.format(amount), category });
            updateBalance(-amount);
            descriptionInput.setText("");
            amountInput.setText("");
            JOptionPane.showMessageDialog(null, "Transaction added successfully.");
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(null, "Invalid amount format. Please enter a valid number.");
        } catch (IllegalArgumentException iae) {
            JOptionPane.showMessageDialog(null, iae.getMessage());
        }
    }

    private void updateBalance(double amount) {
        balance += amount;
        balanceLabel.setText("Balance: " + currencyFormat.format(balance));
    }

    private void addNewCategory() {
        String newCategory = JOptionPane.showInputDialog("Enter new category name:");
        if (newCategory != null && !newCategory.trim().isEmpty() && !categories.contains(newCategory)) {
            categoryInput.addItem(newCategory);
        }
    }

    private void resetApplication() {
        // Clear table data
        tableModel.setRowCount(0);

        // Reset budget and balance
        budget = 0.0;
        balance = 0.0;
        balanceLabel.setText("Balance: " + currencyFormat.format(balance));

        // Clear input fields
        descriptionInput.setText("");
        amountInput.setText("");
    }

    public static void main(String[] args) {
        Currency usd = Currency.getInstance(Locale.US);
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        currencyFormat.setCurrency(usd);
        SwingUtilities.invokeLater(() -> new FinanceTracker().setVisible(true));
    }
}
