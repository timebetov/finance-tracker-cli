package com.github.timebetov.models;

import com.github.timebetov.helper.AppConstant;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;

@Getter @Setter
public class Transaction implements Comparable<Transaction> {

    public enum TransactionType { INCOME, EXPENSE };
    public enum Category { FOOD, SALARY, RENT, TRANSPORT, ENTERTAINMENT, OTHER };

    private UUID id;
    private TransactionType type;
    private Category category;
    private BigDecimal amount;
    private String description;
    private Instant transactionTime;
    private boolean isDeleted;

    public Transaction(TransactionType type, Category category, BigDecimal amount, String description, Instant transactionTime) {
        this.id = UUID.randomUUID();
        this.type = type;
        this.category = category;
        this.amount = amount;
        if (amount != null)
            this.amount = this.amount.setScale(2, RoundingMode.HALF_UP);
        this.description = description;
        this.transactionTime = transactionTime;
        this.isDeleted = false;
    }

    public static void updateTransaction(Transaction initial, Transaction transaction) {

        // Like a mapper
        if (transaction.getType() != null)
            initial.setType(transaction.getType());
        if (transaction.getCategory() != null)
            initial.setCategory(transaction.getCategory());
        if (transaction.getDescription() != null && !(transaction.getDescription().isBlank()))
            initial.setDescription(transaction.getDescription());
        if (transaction.getAmount() != null)
            initial.setAmount(transaction.getAmount());
        if (transaction.getTransactionTime() != null)
            initial.setTransactionTime(transaction.getTransactionTime());
    }

    @Override
    public int compareTo(Transaction o) {
        return transactionTime.compareTo(o.transactionTime);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Transaction that)) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return String.format(
                AppConstant.DISPLAY_FORMAT,
                id,
                type,
                amount,
                category,
                description,
                LocalDateTime.ofInstant(transactionTime, ZoneId.systemDefault()).format(AppConstant.TIME_FORMAT)
        );
    }
}
