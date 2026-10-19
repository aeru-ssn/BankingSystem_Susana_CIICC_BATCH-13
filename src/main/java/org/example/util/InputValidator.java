
package org.example.util;

import java.math.BigDecimal;

public class InputValidator {

    private InputValidator() {
    }

    public static Long validateAccountId(String input) {
        try {
            long id = Long.parseLong(input.trim());

            if (id <= 0) {
                throw new NumberFormatException();
            }

            return id;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Account ID must be a positive number."
            );
        }
    }

    public static String validateAccountNumber(String input) {
        if (input == null || input.trim().isBlank()) {
            throw new IllegalArgumentException(
                    "Account number cannot be empty."
            );
        }
        return input.trim();
    }

    public static BigDecimal validateAmount(String input) {
        try {
            BigDecimal amount = new BigDecimal(input.trim());

            if (amount.signum() <= 0 ||
                    amount.scale() > 2 ||
                    amount.compareTo(
                            new BigDecimal("9999999999999.99")) > 0) {
                throw new IllegalArgumentException(
                        "Enter a valid positive amount with at most 2 decimal places."
                );
            }

            return amount;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid amount."
            );
        }
    }
}
