package com.github.timebetov.service;

import com.github.timebetov.models.Transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface TransactionService {

    void add(Transaction transaction);
    List<Transaction> getTransactions();
    BigDecimal getBalance();
    void update(String transactionId, Transaction transaction);
    void delete(String transactionId);
    Transaction getById(String transactionId);
    Map<String, String> getSummary();
}
