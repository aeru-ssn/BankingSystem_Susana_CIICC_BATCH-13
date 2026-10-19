
package org.example.app;

import org.example.dao.AccountDAO;
import org.example.dao.TransactionDAO;
import org.example.impl.AccountDAOImpl;
import org.example.impl.TransactionDAOImpl;
import org.example.service.AccountService;
import org.example.service.TransactionService;
import org.example.impl.AccountServiceImpl;
import org.example.impl.TransactionServiceImpl;
import org.example.ui.BankingConsole;

public class Main {

    public static void main(String[] args) {

        AccountDAO accountDAO = new AccountDAOImpl();
        TransactionDAO transactionDAO = new TransactionDAOImpl();

        AccountService accountService =
                new AccountServiceImpl(accountDAO, transactionDAO);

        TransactionService transactionService =
                new TransactionServiceImpl(
                        accountDAO,
                        transactionDAO
                );

        BankingConsole console = new BankingConsole(
                accountService,
                transactionService
        );

        console.start();
    }
}
