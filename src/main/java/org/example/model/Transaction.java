
package org.example.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class Transaction {

    private Long id;
    private String transactionReference;
    private Long accountId;
    private String transactionType;
    private BigDecimal amount;
    private BigDecimal balance;
    private Long referenceAccountId;
    private LocalDateTime createdAt;
    private String referenceAccountNumber;

    public Transaction(
            Long id,
            String transactionReference,
            Long accountId,
            String transactionType,
            BigDecimal amount,
            BigDecimal balance,
            Long referenceAccountId,
            LocalDateTime createdAt
    ) {
        this(id, transactionReference, accountId, transactionType, amount, balance, referenceAccountId, createdAt, null);
    }
}

