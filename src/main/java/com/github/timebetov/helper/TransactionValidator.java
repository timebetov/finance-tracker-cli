package com.github.timebetov.helper;

import com.github.timebetov.models.Transaction;

import java.awt.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.regex.Pattern;

public class TransactionValidator {

    private TransactionValidator() {}

    public static Transaction.TransactionType isValidType(String type) {

        if (type == null || type.isBlank()) return null;
        try {
            return Transaction.TransactionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException ex) {
            var values = Arrays.toString(Transaction.TransactionType.values());
            throw new IllegalArgumentException("Invalid transaction type: " + type + " Must be one of " + values);
        }
    }

    public static Transaction.Category isValidCategory(String category) {

        if (category == null || category.isBlank()) return null;
        try {
            return Transaction.Category.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException ex) {
            var values = Arrays.toString(Transaction.Category.values());
            throw new IllegalArgumentException("Invalid transaction category: " + category + " Must be on of " + values);
        }
    }

    public static BigDecimal isValidAmount(String amount) {

        if (amount == null || amount.isBlank()) return null;
        final Pattern AMOUNT_PATTERN = Pattern.compile("^\\d+(\\.\\d{1,2})?$");
        if (!AMOUNT_PATTERN.matcher(amount).matches())
            throw new IllegalArgumentException("Provided amount must be in format 00.00");
        try {
            return new BigDecimal(amount);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Provided wrong input");
        }
    }

    public static Instant isValidTime(String time) {

        if (time == null || time.isBlank()) return null;
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd HH:mm")
                .optionalStart()
                .appendPattern(":ss")
                .optionalEnd()
                .toFormatter();
        try {
            LocalDateTime localDateTime = LocalDateTime.parse(time, formatter);
            return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date format. Use 'yyyy-MM-dd HH:mm' or 'yyyy-MM-dd HH:mm:ss'");
        }
    }
}
