package com.github.timebetov;

import com.github.timebetov.helper.AppConstant;
import com.github.timebetov.helper.AppRunner;
import com.github.timebetov.service.TransactionService;
import com.github.timebetov.service.implementation.InFilesTransactionService;

import java.time.LocalDateTime;
import java.util.UUID;

public class App {

    public static void main(String[] args) {

        System.out.println(UUID.randomUUID());
        String username = getUsername(args);
        TransactionService service = new InFilesTransactionService(username);
        new AppRunner(service).start();
    }

    private static String getUsername(String[] args) {

        String username = null;
        for (int i = 0; i < args.length; i++) {
            if ("-U".equals(args[i]) && i + 1 < args.length) {
                username = args[i + 1];
                break;
            }
        }

        if (username == null) {
            System.err.println("flag -U and username not provided");
            System.exit(1);
        }

        System.out.println(LocalDateTime.now().format(AppConstant.TIME_FORMAT) + " INFO: Logged in as: " + username);
        System.out.println("‒".repeat(50));
        return username;
    }
}
