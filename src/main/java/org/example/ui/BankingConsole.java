
package org.example.ui;

import org.example.exception.AccountNotFoundException;
import org.example.exception.InsufficientFundsException;
import org.example.model.Account;
import org.example.model.Transaction;
import org.example.service.AccountService;
import org.example.service.TransactionService;
import org.example.util.InputValidator;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class BankingConsole {

    private final AccountService accountService;
    private final TransactionService transactionService;
    private final Scanner scanner = new Scanner(System.in);

    public BankingConsole(
            AccountService accountService,
            TransactionService transactionService
    ) {
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    private String readAccountNumber(String prompt) {
        System.out.print(prompt);
        return InputValidator.validateAccountNumber(scanner.nextLine());
    }

    private Long readAccountId(String prompt) {
        System.out.print(prompt);
        return InputValidator.validateAccountId(scanner.nextLine());
    }

    private BigDecimal readAmount(String prompt) {
        System.out.print(prompt);
        return InputValidator.validateAmount(scanner.nextLine());
    }

    private void showMenu() {
        System.out.println("\n================================");
        System.out.println("          BANKING SYSTEM");
        System.out.println("================================");
        System.out.println("[1] Create Account");
        System.out.println("[2] Balance Inquiry");
        System.out.println("[3] List Accounts");
        System.out.println("[4] Deposit");
        System.out.println("[5] Withdraw");
        System.out.println("[6] Transfer");
        System.out.println("[7] Transaction History");
        System.out.println("[0] Exit");
        System.out.println("================================");
        System.out.print("Enter choice: ");
    }

    public void start() {
        boolean running = true;

        while (running) {
            showMenu();
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> createAccount();
                    case "2" -> balanceInquiry();
                    case "3" -> listAccounts();
                    case "4" -> deposit();
                    case "5" -> withdraw();
                    case "6" -> transfer();
                    case "7" -> transactionHistory();

                    case "0" -> {
                        System.out.println("Thank you for using Banking System!");
                        running = false;
                    }

                    default -> System.out.println("Invalid choice.");
                }

            } catch (SQLException e) {
                System.out.println(
                        "Database error: " + e.getMessage()
                );

            } catch (IllegalArgumentException |
                     AccountNotFoundException |
                     InsufficientFundsException e) {
                System.out.println(
                        "Error: " + e.getMessage()
                );
            }
        }
    }


    private void createAccount() throws SQLException {

        System.out.println("\n========== CREATE ACCOUNT ==========");

        System.out.print("Enter Account Name: ");
        String name = scanner.nextLine();

        BigDecimal initialDeposit =
                readAmount("Enter Initial Deposit: ");

        Account account = accountService.createAccount(
                name,
                initialDeposit
        );

        System.out.println("\nAccount created successfully!");
        System.out.println("Account Number: " + account.getAccountNumber());
        System.out.println("Account Name: " + account.getAccountName());
        System.out.println("Initial Balance: PHP " + account.getBalance());
    }


    private void balanceInquiry() throws SQLException {
        String accountNumber = readAccountNumber("Enter Account Number: ");

        BigDecimal balance = accountService.getBalance(accountNumber);

        System.out.println("Balance: PHP " + balance);
    }

    private void listAccounts() throws SQLException {
        List<Account> accounts = accountService.getAllAccounts();

        if (accounts.isEmpty()) {
            System.out.println("No accounts found.");
            return;
        }

        System.out.println("\n========== ACCOUNTS ==========");

        for (Account account : accounts) {
            System.out.println("------------------------------");
            System.out.println("Account Number: " + account.getAccountNumber());
            System.out.println("Name: " + account.getAccountName());
            System.out.println("Balance: PHP " + account.getBalance());
        }
    }

    private void deposit() throws SQLException {
        String accountNumber = readAccountNumber("Enter Account Number: ");
        BigDecimal amount = readAmount("Enter deposit amount: ");

        transactionService.deposit(accountNumber, amount);

        System.out.println("Deposit successful!");
        System.out.println("New Balance: PHP " +
                accountService.getBalance(accountNumber));
    }

    private void withdraw() throws SQLException {
        String accountNumber = readAccountNumber("Enter Account Number: ");
        BigDecimal amount = readAmount("Enter withdrawal amount: ");

        transactionService.withdraw(accountNumber, amount);

        System.out.println("Withdrawal successful!");
        System.out.println("New Balance: PHP " +
                accountService.getBalance(accountNumber));
    }

    private void transfer() throws SQLException {
        String fromAccountNumber = readAccountNumber("Sender Account Number: ");
        String toAccountNumber = readAccountNumber("Receiver Account Number: ");
        BigDecimal amount = readAmount("Enter transfer amount: ");

        transactionService.transfer(fromAccountNumber, toAccountNumber, amount);

        System.out.println("Transfer successful!");
        System.out.println("Sender Balance: PHP " +
                accountService.getBalance(fromAccountNumber));
    }

    private void transactionHistory() throws SQLException {
        String accountNumber = readAccountNumber("Enter Account Number: ");

        List<Transaction> transactions =
                transactionService.getTransactionHistory(accountNumber);

        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }

        System.out.println("\n======= TRANSACTION HISTORY =======");

        for (Transaction transaction : transactions) {
            System.out.println("-----------------------------------");
            System.out.println("Reference: " +
                    transaction.getTransactionReference());
            System.out.println("Type: " +
                    transaction.getTransactionType());
            System.out.println("Amount: PHP " +
                    transaction.getAmount());
            System.out.println("Balance After: PHP " +
                    transaction.getBalance());

            if (transaction.getReferenceAccountNumber() != null) {
                System.out.println("Related Account: " +
                        transaction.getReferenceAccountNumber());
            }

            System.out.println("Date: " +
                    transaction.getCreatedAt());
        }
    }
}
