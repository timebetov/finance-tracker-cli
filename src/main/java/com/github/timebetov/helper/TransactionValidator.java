package com.github.timebetov.helper;

import com.github.timebetov.models.Transaction;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.regex.Pattern;

public class TransactionValidator {

    private TransactionValidator() {}

    public static Transaction.TransactionType isValidType(String type) {

        try {
            return Transaction.TransactionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException ex) {
            var values = Arrays.toString(Transaction.TransactionType.values());
            throw new IllegalArgumentException("Invalid transaction type: " + type + " Must be one of " + values);
        }
    }

    public static Transaction.Category isValidCategory(String category) {

        try {
            return Transaction.Category.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException ex) {
            var values = Arrays.toString(Transaction.Category.values());
            throw new IllegalArgumentException("Invalid transaction category: " + category + " Must be on of " + values);
        }
    }

    public static BigDecimal isValidAmount(String amount) {

        final Pattern AMOUNT_PATTERN = Pattern.compile("^\\d+(\\.\\d{1,2})?$");
        if (!AMOUNT_PATTERN.matcher(amount).matches())
            throw new IllegalArgumentException("Provided amount must be in format 00.00");
        try {
            return new BigDecimal(amount);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Provided wrong input");
        }
    }
}
