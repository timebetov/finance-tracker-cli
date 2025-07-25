package com.github.timebetov.service;

import com.github.timebetov.helper.AppConstant;
import com.github.timebetov.models.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;

public interface TransactionService {

    String getUsername();
    void add(Transaction transaction);
    List<Transaction> getTransactions(boolean isDeleted);
    void update(String transactionId, Transaction transaction);
    void delete(String transactionId);
    Transaction getById(String transactionId);

    default BigDecimal getBalance(List<Transaction> transactions) {
        return transactions.stream()
                .map(t -> t.getType() == Transaction.TransactionType.INCOME
                        ? t.getAmount()
                        : t.getAmount().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    default Map<String, String> getSummary(List<Transaction> transactions) {

        Map<String, String> summary = new LinkedHashMap<>();

        int totalTransactions = transactions.size();

        BigDecimal totalIncome = transactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = transactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Optional<Transaction> firstTransaction = transactions.stream().min(Comparator.naturalOrder());
        Optional<Transaction> lastTransaction = transactions.stream().max(Comparator.naturalOrder());

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
        summary.put("Current Balance", "$" + getBalance(transactions));
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
