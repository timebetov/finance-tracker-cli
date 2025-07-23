package com.github.timebetov.helper;

import com.github.timebetov.models.Transaction;
import com.github.timebetov.service.TransactionService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static com.github.timebetov.helper.AppUtilities.showResponse;
import static com.github.timebetov.helper.AppUtilities.getInput;

public class AppRunner {

    private final TransactionService service;
    private final Scanner scanner = new Scanner(System.in);
    private final List<String> menuItems = List.of(
            "ADD | Add a new transaction",
            "SHOW | Show all transactions",
            "DELETE | Delete transaction by ID",
            "BALANCE | View current Balance",
            "SUMMARY | Get full summary report",
            "EXIT | Close application"
    );

    public AppRunner(TransactionService service) {
        this.service = service;
    }

    public void start() {

        greeting();
        showMenu();
        while(true) {
            String input = AppUtilities.getInput(scanner, "Please choose a command or type 'menu' to show menu");
            if (input.equalsIgnoreCase("EXIT")) return;
            action(input);
        }
    }

    private void action(String input) {
        switch (input.toUpperCase()) {
            case "ADD" -> addTransaction();
            case "SHOW" -> showTransactions();
            case "DELETE" -> deleteTransaction();
            case "SUMMARY" -> showTransactionSummary();
            case "BALANCE" -> getBalance();
            case "MENU" -> showMenu();
            default -> System.out.println("Please choose right choice");
        }
    }

    private void addTransaction() {

        var types = Arrays.toString(Transaction.TransactionType.values());
        var categories = Arrays.toString(Transaction.Category.values());

        String type = getInput(scanner, "Please provide type of transaction " + types);
        String category = getInput(scanner, "Please provide category of transaction " + categories);
        String amount = getInput(scanner, "Please provide amount of transaction");
        String description = getInput(scanner, "Please provide description of transaction");

        try {
            var validType = TransactionValidator.isValidType(type);
            var validCat = TransactionValidator.isValidCategory(category);
            var validAmount = TransactionValidator.isValidAmount(amount);

            Transaction transaction = new Transaction(validType, validCat, validAmount, description, Instant.now());
            service.add(transaction);
            showResponse("Transaction added successfully");
        } catch (RuntimeException ex) {
            showResponse(ex.getMessage());
        }

    }

    private void showTransactions() {

        final List<Transaction> transactions = service.getTransactions();
        final String displayFormat = "| %-5s | %-36s | %-7s | %10s | %-13s | %-20s | %-20s |";

        // Case: If there are no transactions
        if (transactions.isEmpty()) {
            showResponse("There are no transactions yet");
            return;
        }

        System.out.println("‒".repeat(135));
        System.out.printf((displayFormat) + "%n", "#", "ID", "TYPE", "AMOUNT", "CATEGORY", "DESCRIPTION", "DATE");
        System.out.println("‒".repeat(135));
        for (int i = 0; i < transactions.size(); i++) {
            var transaction = transactions.get(i);
            LocalDateTime transactionTime = LocalDateTime.ofInstant(transaction.getTransactionTime(), ZoneId.systemDefault());
            System.out.printf((displayFormat) + "%n", i+1, transaction.getId(), transaction.getType(),
                    transaction.getAmount(), transaction.getCategory(), transaction.getDescription(),
                    transactionTime.format(AppConstant.TIME_FORMAT));
        }
        System.out.println("‒".repeat(135));
    }

    private void deleteTransaction() {

        String transactionId = getInput(scanner, "Please provide transaction id to delete");
        if (service.getById(transactionId) == null)
            showResponse("Transaction not found with ID: " + transactionId);
        else {
            service.delete(transactionId);
            showResponse("Transaction deleted successfully");
        }
    }

    private void showTransactionSummary() {

        Map<String, String> summary = service.getSummary();
        System.out.println("\n📊 Summary Report");
        System.out.println("‒".repeat(61));
        String format = "| %-30s | %-25s |%n";
        System.out.printf(format, "Metric", "Value");
        System.out.println("‒".repeat(61));
        summary.forEach((key, value) -> System.out.printf(format, key, value));
        System.out.println("‒".repeat(61));
    }

    private void getBalance() {

        BigDecimal balance = service.getBalance();
        String sign = balance.signum() >= 0 ? "" : "-";
        showResponse("BALANCE: " + sign + "$" + balance.abs());
    }

    private void showMenu() {

        System.out.println("‒".repeat(30));
        String format = "%-10s | %-15s%n";
        System.out.printf(format, "Command", "Description");
        System.out.println("‒".repeat(30));
        for (var item : menuItems) {
            String command = item.substring(0, item.indexOf(" "));
            String description = item.substring(command.length() + 3);
            System.out.printf(format, command, description);
        }
        System.out.println("‒".repeat(30));
    }

    private String greeting() {

        String greetingText = """
                Hey! Welcome to `MoneyWise` — your personal finance tracker by Timebetov.
                I really appreciate you trying it out.
                First things first - what's your name?""";

        String name = getInput(scanner, greetingText);
        name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
        String welcomeText = """
                Nice to meet you, Mr.%s! \s
                Here you can easily manage all your transactions.%n""";
        System.out.printf(welcomeText, name);
        return name;
    }
}
