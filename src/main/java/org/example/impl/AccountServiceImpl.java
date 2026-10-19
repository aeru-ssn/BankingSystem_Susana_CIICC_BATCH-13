
package org.example.impl;

import org.example.config.DatabaseConnection;
import org.example.dao.AccountDAO;
import org.example.dao.TransactionDAO;
import org.example.exception.AccountNotFoundException;
import org.example.model.Account;
import org.example.model.Transaction;
import org.example.service.AccountService;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class AccountServiceImpl implements AccountService {

    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;

    public AccountServiceImpl(
            AccountDAO accountDAO,
            TransactionDAO transactionDAO
    ) {
        this.accountDAO = accountDAO;
        this.transactionDAO = transactionDAO;
    }

    @Override
    public Account createAccount(
            String accountName,
            BigDecimal initialDeposit
    ) throws SQLException {

        if (accountName == null || accountName.isBlank()) {
            throw new IllegalArgumentException(
                    "Account name cannot be empty."
            );
        }
        if (accountName.trim().length() > 100) {
            throw new IllegalArgumentException(
                    "Account name cannot exceed 100 characters."
            );
        }

        BigDecimal maxBalance =
                new BigDecimal("9999999999999.99");

        if (initialDeposit == null
                || initialDeposit.signum() < 0
                || initialDeposit.scale() > 2
                || initialDeposit.compareTo(maxBalance) > 0) {

            throw new IllegalArgumentException(
                    "Enter a valid initial deposit with a maximum of 2 decimal places."
            );
        }
        Account account;

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String accountNumber = accountDAO.findNextAccountNumber(conn);
                account = new Account(
                        null,
                        accountNumber,
                        accountName.trim(),
                        initialDeposit,
                        null
                );
                // Save the account and retrieve its generated ID.
                accountDAO.save(account, conn);
                // The account ID is now available.
                if (initialDeposit.signum() > 0) {

                    Transaction transaction = new Transaction(
                            null,
                            UUID.randomUUID().toString(),
                            account.getId(),
                            "INITIAL_DEPOSIT",
                            initialDeposit,
                            initialDeposit,
                            null,
                            null
                    );
                    transactionDAO.save(transaction, conn);
                }
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }
        }
        return account;
    }

    @Override
    public BigDecimal getBalance(Long accountId)
            throws SQLException {

        Account account = accountDAO.findById(accountId);

        if (account == null) {
            throw new AccountNotFoundException(
                    "Account not found: " + accountId
            );
        }

        return account.getBalance();
    }

    @Override
    public BigDecimal getBalance(String accountNumber)
            throws SQLException {

        Account account = accountDAO.findByAccountNumber(accountNumber);

        if (account == null) {
            throw new AccountNotFoundException(
                    "Account not found: " + accountNumber
            );
        }

        return account.getBalance();
    }

    @Override
    public List<Account> getAllAccounts()
            throws SQLException {

        return accountDAO.findAll();
    }
}
