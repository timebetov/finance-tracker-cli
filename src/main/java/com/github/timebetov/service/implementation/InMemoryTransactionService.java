package com.github.timebetov.service.implementation;

import com.github.timebetov.helper.AppConstant;
import com.github.timebetov.models.Transaction;
import com.github.timebetov.service.TransactionService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;

public class InMemoryTransactionService implements TransactionService {

    private final Map<UUID, Transaction> transactions = new LinkedHashMap<>();

    @Override
    public void add(Transaction transaction) {

        if (transactions.containsKey(transaction.getId())) {
            throw new IllegalArgumentException("Transaction with ID: " + transaction.getId() + " already exists");
        }
        transactions.put(transaction.getId(), transaction);
    }

    @Override
    public List<Transaction> getTransactions() {
        return transactions.values().stream()
                .sorted()
                .toList();
    }

    @Override
    public BigDecimal getBalance() {
        return transactions.values().stream()
                .map(t -> t.getType() == Transaction.TransactionType.INCOME
                        ? t.getAmount()
                        : t.getAmount().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public void update(String transactionId, Transaction transaction) {

        Transaction toUpdate = getById(transactionId);

        if (transaction.getType() != null)
            toUpdate.setType(transaction.getType());
        if (transaction.getCategory() != null)
            toUpdate.setCategory(transaction.getCategory());
        if (transaction.getDescription() != null && !(transaction.getDescription().isBlank()))
            toUpdate.setDescription(transaction.getDescription());
        if (transaction.getAmount() != null)
            toUpdate.setAmount(transaction.getAmount());
        if (transaction.getTransactionTime() != null)
            toUpdate.setTransactionTime(transaction.getTransactionTime());
    }

    @Override
    public void delete(String transactionId) {
        transactions.remove(UUID.fromString(transactionId));
    }

    @Override
    public Transaction getById(String transactionId) {

        if (!transactions.containsKey(UUID.fromString(transactionId)))
            throw new IllegalArgumentException("Transaction with ID: " + transactionId + " does not exists");
        return transactions.get(UUID.fromString(transactionId));
    }

    @Override
    public Map<String, String> getSummary() {

        Map<String, String> summary = new LinkedHashMap<>();

        int totalTransactions = transactions.size();

        BigDecimal totalIncome = transactions.values().stream()
                .filter(t -> t.getType() == Transaction.TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = transactions.values().stream()
                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Optional<Transaction> firstTransaction = transactions.values().stream().min(Comparator.naturalOrder());
        Optional<Transaction> lastTransaction = transactions.values().stream().max(Comparator.naturalOrder());

        Period periodSinceFirstTransaction = firstTransaction
                .map(t -> Period.between(
                        t.getTransactionTime().atZone(ZoneId.systemDefault()).toLocalDate(),
                        Instant.now().atZone(ZoneId.systemDefault()).toLocalDate()
                )).orElse(Period.ZERO);

        Duration durationSinceLastTransaction = lastTransaction
                .map(t -> Duration.between(t.getTransactionTime(), Instant.now()))
                .orElse(Duration.ZERO);

        summary.put("Total transactions", String.valueOf(totalTransactions));
        summary.put("Total Income", "$"+totalIncome.setScale(2, RoundingMode.HALF_UP).toString());
        summary.put("Total Expense", "$"+totalExpense.setScale(2, RoundingMode.HALF_UP).toString());
        summary.put("Current Balance", "$"+getBalance());
        summary.put("First Transaction", firstTransaction.map(t ->
                LocalDateTime.ofInstant(t.getTransactionTime(), ZoneId.systemDefault()).format(AppConstant.TIME_FORMAT)).orElse("-"));
        summary.put("Time Since First Transaction", String.format("%d years, %d months, %d days",
                periodSinceFirstTransaction.getYears(), periodSinceFirstTransaction.getMonths(), periodSinceFirstTransaction.getDays()));
        summary.put("Last Transaction", lastTransaction.map(t ->
                LocalDateTime.ofInstant(t.getTransactionTime(), ZoneId.systemDefault()).format(AppConstant.TIME_FORMAT)).orElse("-"));
        summary.put("Time Since Last Transaction", durationSinceLastTransaction.isZero() ? "-"
                : durationSinceLastTransaction.toMinutes() + " minutes");
        return summary;
    }
}
