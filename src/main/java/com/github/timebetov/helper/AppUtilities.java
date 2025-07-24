package com.github.timebetov.helper;

import java.util.Scanner;

public class AppUtilities {

    private AppUtilities() {}

    protected static String getInput(Scanner scanner, String promptMsg, boolean allowBlank) {

        final String postFixMsgIfAllowed = " (or leave the field blank)";
        System.out.print(promptMsg + (allowBlank ? postFixMsgIfAllowed : "") + " >>> ");
        String input = scanner.nextLine().trim();
        if (!allowBlank && input.isBlank()) {
            input = getInput(scanner, promptMsg, false);
        }
        return input;
    }

    protected static void showResponse(String response) {
        System.out.println("‒".repeat(50));
        System.out.println(response);
        System.out.println("‒".repeat(50));
    }
}
