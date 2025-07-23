package com.github.timebetov;

import com.github.timebetov.helper.AppRunner;
import com.github.timebetov.service.TransactionService;
import com.github.timebetov.service.implementation.InMemoryTransactionService;

public class App {

    public static void main(String[] args) {

        TransactionService service = new InMemoryTransactionService();
        new AppRunner(service).start();
    }
}
