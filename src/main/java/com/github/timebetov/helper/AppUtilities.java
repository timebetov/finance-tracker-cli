package com.github.timebetov.helper;

import java.util.Scanner;

public class AppUtilities {

    private AppUtilities() {}

    protected static String getInput(Scanner scanner, String promptMsg) {

        System.out.print(promptMsg + " >>> ");
        String input = scanner.nextLine().trim();
        if (input.isBlank()) {
            input = getInput(scanner, promptMsg);
        }
        return input;
    }

    protected static void showResponse(String response) {
        System.out.println("‒".repeat(50));
        System.out.println(response);
        System.out.println("‒".repeat(50));
    }
}
