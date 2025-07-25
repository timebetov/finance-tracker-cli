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
            "GET | Get transaction by ID",
            "UPDATE | Update transaction by ID",
            "DELETE | Delete transaction by ID",
            "BALANCE | View current Balance",
            "SUMMARY | Get full summary report",
            "TRASH | Get all deleted transactions",
            "EXIT | Close application"
    );

    public AppRunner(TransactionService service) {
        this.service = service;
    }

    public void start() {

        greeting();
        showMenu();
        while(true) {
            String input = AppUtilities.getInput(scanner, "Please choose a command or type 'menu' to show menu", false);
            if (input.equalsIgnoreCase("EXIT")) return;
            action(input);
        }
    }

    private void action(String input) {
        switch (input.toUpperCase()) {
            case "ADD" -> addTransaction();
            case "SHOW" -> showTransactions(false);
            case "GET" -> getTransaction();
            case "UPDATE" -> updateTransaction();
            case "DELETE" -> deleteTransaction();
            case "BALANCE" -> getBalance();
            case "SUMMARY" -> showTransactionSummary();
            case "TRASH" -> showTransactions(true);
            case "MENU" -> showMenu();
            default -> System.out.println("Please choose right choice");
        }
    }

    private Transaction getTransactionDetails(boolean allowBlank) {

        var types = Arrays.toString(Transaction.TransactionType.values());
        var categories = Arrays.toString(Transaction.Category.values());

        String type = getInput(scanner, "Please provide type of transaction " + types, allowBlank);
        String category = getInput(scanner, "Please provide category of transaction " + categories, allowBlank);
        String amount = getInput(scanner, "Please provide amount of transaction", allowBlank);
        String description = getInput(scanner, "Please provide description of transaction", allowBlank);
        String time = getInput(scanner, "Please provide transaction time in format: " +
                LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()).format(AppConstant.TIME_FORMAT), allowBlank);

        var validType = TransactionValidator.isValidType(type);
        var validCat = TransactionValidator.isValidCategory(category);
        var validAmount = TransactionValidator.isValidAmount(amount);
        var validTime = TransactionValidator.isValidTime(time);

        return new Transaction(validType, validCat, validAmount, description, validTime);
    }

    private void addTransaction() {

        Transaction transaction = getTransactionDetails(false);
        if (transaction == null) {
            showResponse("Transaction NOT ADDED");
            return;
        }

        service.add(transaction);
        showResponse("Transaction with ID: " + transaction.getId() + " added successfully");
    }

    private void showTransactions(boolean isDeleted) {

        final List<Transaction> transactions = service.getTransactions(isDeleted);
        displayTransactions(transactions);
    }

    private void getTransaction() {

        String transactionId = getInput(scanner, "Pleas provide transaction id you want to view", true);
        if (transactionId.isBlank()) return;

        try {
            Transaction transaction = service.getById(transactionId);
            displayTransactions(List.of(transaction));
        } catch (Exception e) {
            showResponse(e.getMessage());
        }
    }

    private void updateTransaction() {

        String transactionId = getInput(scanner, "Please provide transaction id you want to update", true);
        if (transactionId.isBlank()) return;

        try {
            service.getById(transactionId);
            Transaction transaction = getTransactionDetails(true);
            if (transaction == null) {
                showResponse("Some issues occurred. Please provide valid data.");
                return;
            }
            service.update(transactionId, transaction);
            showResponse("Transaction updated successfully");
        } catch (IllegalArgumentException ex) {
            showResponse(ex.getMessage());
        }
    }

    private void deleteTransaction() {

        String transactionId = getInput(scanner, "Please provide transaction id to delete", true);
        if (transactionId.isBlank()) return;

        try {
            service.getById(transactionId);
            service.delete(transactionId);
            showResponse("Transaction deleted successfully");
        } catch (IllegalArgumentException ex) {
            showResponse(ex.getMessage());
        }
    }

    private void showTransactionSummary() {

        Map<String, String> summary = service.getSummary(service.getTransactions(false));
        System.out.println("\n📊 Summary Report");
        System.out.println("‒".repeat(61));
        String format = "| %-30s | %-25s |%n";
        System.out.printf(format, "Metric", "Value");
        System.out.println("‒".repeat(61));
        summary.forEach((key, value) -> System.out.printf(format, key, value));
        System.out.println("‒".repeat(61));
    }

    private void getBalance() {

        BigDecimal balance = service.getBalance(service.getTransactions(false));
        String sign = balance.signum() >= 0 ? "" : "-";
        showResponse("BALANCE: " + sign + "$" + balance.abs());
    }

    private void displayTransactions(List<Transaction> transactions) {

        final String displayFormat = "| %-5s " + AppConstant.DISPLAY_FORMAT;

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

    private void greeting() {

        String greetingText = """
                Hey! Welcome to `MoneyWise` — your personal finance tracker by Timebetov.
                I really appreciate you trying it out.
                Mr.%s
                Here you can easily manage all your transactions.%n""";

        System.out.printf(greetingText, service.getUsername());
    }
}
