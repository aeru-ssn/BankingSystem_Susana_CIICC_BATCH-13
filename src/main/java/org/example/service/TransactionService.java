
package org.example.service;

import org.example.model.Transaction;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public interface TransactionService {

    void deposit(Long accountId, BigDecimal amount)
            throws SQLException;

    void deposit(String accountNumber, BigDecimal amount)
            throws SQLException;

    void withdraw(Long accountId, BigDecimal amount)
            throws SQLException;

    void withdraw(String accountNumber, BigDecimal amount)
            throws SQLException;

    void transfer(Long fromId, Long toId, BigDecimal amount)
            throws SQLException;

    void transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount)
            throws SQLException;

    List<Transaction> getTransactionHistory(Long accountId)
            throws SQLException;

    List<Transaction> getTransactionHistory(String accountNumber)
            throws SQLException;
}
