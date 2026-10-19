
package org.example.impl;

import org.example.config.DatabaseConnection;
import org.example.dao.AccountDAO;
import org.example.dao.TransactionDAO;
import org.example.exception.AccountNotFoundException;
import org.example.exception.InsufficientFundsException;
import org.example.model.Account;
import org.example.model.Transaction;
import org.example.service.TransactionService;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class TransactionServiceImpl implements TransactionService {

    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;

    public TransactionServiceImpl(
            AccountDAO accountDAO,
            TransactionDAO transactionDAO
    ) {
        this.accountDAO = accountDAO;
        this.transactionDAO = transactionDAO;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null ||
                amount.signum() <= 0 ||
                amount.scale() > 2 ||
                amount.compareTo(new BigDecimal("9999999999999.99")) > 0) {

            throw new IllegalArgumentException(
                    "Enter a valid positive amount (maximum 2 decimal places)."
            );
        }
    }

    private Account requireAccount(Long id, Connection conn)
            throws SQLException {

        Account account = accountDAO.findByIdForUpdate(id, conn);

        if (account == null) {
            throw new AccountNotFoundException(
                    "Account not found: " + id
            );
        }

        return account;
    }

    private Account requireAccountByNumber(String accountNumber, Connection conn)
            throws SQLException {

        Account account = accountDAO.findByAccountNumberForUpdate(accountNumber, conn);

        if (account == null) {
            throw new AccountNotFoundException(
                    "Account not found: " + accountNumber
            );
        }

        return account;
    }

    private void recordTransaction(
            Account account,
            String type,
            BigDecimal amount,
            BigDecimal newBalance,
            Long referenceAccountId,
            String reference,
            Connection conn
    ) throws SQLException {

        Transaction transaction = new Transaction(
                null,
                reference,
                account.getId(),
                type,
                amount,
                newBalance,
                referenceAccountId,
                null
        );

        transactionDAO.save(transaction, conn);
    }

    @Override
    public void deposit(Long accountId, BigDecimal amount)
            throws SQLException {

        validateAmount(amount);

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                Account account = requireAccount(accountId, conn);

                BigDecimal newBalance =
                        account.getBalance().add(amount);

                validateBalance(newBalance);

                accountDAO.updateBalance(
                        accountId, newBalance, conn
                );

                recordTransaction(
                        account, "DEPOSIT", amount,
                        newBalance, null,
                        UUID.randomUUID().toString(), conn
                );

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
    }

    @Override
    public void deposit(String accountNumber, BigDecimal amount)
            throws SQLException {

        validateAmount(amount);

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                Account account = requireAccountByNumber(accountNumber, conn);

                BigDecimal newBalance =
                        account.getBalance().add(amount);

                validateBalance(newBalance);

                accountDAO.updateBalance(
                        account.getId(), newBalance, conn
                );

                recordTransaction(
                        account, "DEPOSIT", amount,
                        newBalance, null,
                        UUID.randomUUID().toString(), conn
                );

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
    }

    @Override
    public void withdraw(Long accountId, BigDecimal amount)
            throws SQLException {

        validateAmount(amount);

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                Account account = requireAccount(accountId, conn);

                if (account.getBalance().compareTo(amount) < 0) {
                    throw new InsufficientFundsException(
                            "Insufficient funds."
                    );
                }

                BigDecimal newBalance =
                        account.getBalance().subtract(amount);

                accountDAO.updateBalance(
                        accountId, newBalance, conn
                );

                recordTransaction(
                        account, "WITHDRAW", amount,
                        newBalance, null,
                        UUID.randomUUID().toString(), conn
                );

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
    }

    @Override
    public void transfer(
            Long fromId, Long toId, BigDecimal amount
    ) throws SQLException {

        validateAmount(amount);

        if (fromId == null || toId == null) {
            throw new IllegalArgumentException(
                    "Account IDs cannot be null."
            );
        }

        if (fromId.equals(toId)) {
            throw new IllegalArgumentException(
                    "Cannot transfer to the same account."
            );
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // Lock accounts in consistent ID order.
                Long firstId = Math.min(fromId, toId);
                Long secondId = Math.max(fromId, toId);

                Account first = requireAccount(firstId, conn);
                Account second = requireAccount(secondId, conn);

                Account sender = fromId.equals(firstId)
                        ? first : second;

                Account receiver = toId.equals(firstId)
                        ? first : second;

                if (sender.getBalance().compareTo(amount) < 0) {
                    throw new InsufficientFundsException(
                            "Insufficient funds for transfer."
                    );
                }

                BigDecimal senderBalance =
                        sender.getBalance().subtract(amount);

                BigDecimal receiverBalance =
                        receiver.getBalance().add(amount);

                validateBalance(receiverBalance);

                accountDAO.updateBalance(
                        fromId, senderBalance, conn
                );

                accountDAO.updateBalance(
                        toId, receiverBalance, conn
                );

                recordTransaction(
                        sender, "TRANSFER_OUT", amount,
                        senderBalance,
                        receiver.getId(),
                        UUID.randomUUID().toString(), conn
                );

                recordTransaction(
                        receiver, "TRANSFER_IN", amount,
                        receiverBalance,
                        sender.getId(),
                        UUID.randomUUID().toString(), conn
                );

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
    }

    @Override
    public void withdraw(String accountNumber, BigDecimal amount)
            throws SQLException {

        validateAmount(amount);

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                Account account = requireAccountByNumber(accountNumber, conn);

                if (account.getBalance().compareTo(amount) < 0) {
                    throw new InsufficientFundsException(
                            "Insufficient funds."
                    );
                }

                BigDecimal newBalance =
                        account.getBalance().subtract(amount);

                accountDAO.updateBalance(
                        account.getId(), newBalance, conn
                );

                recordTransaction(
                        account, "WITHDRAW", amount,
                        newBalance, null,
                        UUID.randomUUID().toString(), conn
                );

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
    }

    @Override
    public void transfer(
            String fromAccountNumber, String toAccountNumber, BigDecimal amount
    ) throws SQLException {

        validateAmount(amount);

        if (fromAccountNumber == null || toAccountNumber == null ||
                fromAccountNumber.isBlank() || toAccountNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Account numbers cannot be empty."
            );
        }

        if (fromAccountNumber.trim().equalsIgnoreCase(toAccountNumber.trim())) {
            throw new IllegalArgumentException(
                    "Cannot transfer to the same account."
            );
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                String firstNum = fromAccountNumber.compareTo(toAccountNumber) < 0
                        ? fromAccountNumber : toAccountNumber;
                String secondNum = fromAccountNumber.compareTo(toAccountNumber) < 0
                        ? toAccountNumber : fromAccountNumber;

                Account first = requireAccountByNumber(firstNum, conn);
                Account second = requireAccountByNumber(secondNum, conn);

                Account sender = fromAccountNumber.trim().equalsIgnoreCase(first.getAccountNumber())
                        ? first : second;
                Account receiver = toAccountNumber.trim().equalsIgnoreCase(first.getAccountNumber())
                        ? first : second;

                if (sender.getBalance().compareTo(amount) < 0) {
                    throw new InsufficientFundsException(
                            "Insufficient funds for transfer."
                    );
                }

                BigDecimal senderBalance =
                        sender.getBalance().subtract(amount);

                BigDecimal receiverBalance =
                        receiver.getBalance().add(amount);

                validateBalance(receiverBalance);

                accountDAO.updateBalance(
                        sender.getId(), senderBalance, conn
                );

                accountDAO.updateBalance(
                        receiver.getId(), receiverBalance, conn
                );

                recordTransaction(
                        sender, "TRANSFER_OUT", amount,
                        senderBalance,
                        receiver.getId(),
                        UUID.randomUUID().toString(), conn
                );

                recordTransaction(
                        receiver, "TRANSFER_IN", amount,
                        receiverBalance,
                        sender.getId(),
                        UUID.randomUUID().toString(), conn
                );

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
    }

    @Override
    public List<Transaction> getTransactionHistory(Long accountId)
            throws SQLException {

        Account account = accountDAO.findById(accountId);

        if (account == null) {
            throw new AccountNotFoundException(
                    "Account not found: " + accountId
            );
        }

        return transactionDAO.findByAccountId(accountId);
    }

    @Override
    public List<Transaction> getTransactionHistory(String accountNumber)
            throws SQLException {

        Account account = accountDAO.findByAccountNumber(accountNumber);

        if (account == null) {
            throw new AccountNotFoundException(
                    "Account not found: " + accountNumber
            );
        }

        return transactionDAO.findByAccountId(account.getId());
    }


    private void validateBalance(BigDecimal balance) {
        BigDecimal maxBalance =
                new BigDecimal("9999999999999.99");

        if (balance.compareTo(maxBalance) > 0) {
            throw new IllegalArgumentException(
                    "Transaction exceeds maximum account balance."
            );
        }
    }

}
