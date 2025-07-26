package com.github.timebetov.service.implementation;

import com.github.timebetov.models.Transaction;
import com.github.timebetov.service.TransactionService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InMemoryTransactionService implements TransactionService {

    private final Map<UUID, Transaction> transactions = new LinkedHashMap<>();
    private final String username;

    public InMemoryTransactionService(String username) {
        this.username = username;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public void add(Transaction transaction) {

        if (transactions.containsKey(transaction.getId())) {
            throw new IllegalArgumentException("Transaction with ID: " + transaction.getId() + " already exists");
        }
        transactions.put(transaction.getId(), transaction);
    }

    @Override
    public List<Transaction> getTransactions(boolean isDeleted) {
        return transactions.values().stream()
                .filter(t -> t.isDeleted() == isDeleted)
                .sorted()
                .toList();
    }

    @Override
    public void update(String transactionId, Transaction transaction) {

        Transaction toUpdate = getById(transactionId);
        Transaction.updateTransaction(toUpdate, transaction);
    }

    @Override
    public void delete(String transactionId) {
        transactions.get(UUID.fromString(transactionId)).setDeleted(true);
    }

    @Override
    public void clear(boolean clearAll) {

        if (clearAll)
            transactions.clear();
        else {
            transactions.values().removeIf(Transaction::isDeleted);
        }
    }

    @Override
    public Transaction getById(String transactionId) {

        UUID id = UUID.fromString(transactionId);
        if (!transactions.containsKey(id) || (transactions.containsKey(id) && transactions.get(id).isDeleted()))
            throw new IllegalArgumentException("Transaction with ID: " + transactionId + " does not exists");
        return transactions.get(UUID.fromString(transactionId));
    }
}
