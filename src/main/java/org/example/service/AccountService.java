
package org.example.service;

import org.example.model.Account;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public interface AccountService {

    Account createAccount(
            String accountName,
            BigDecimal initialDeposit
    ) throws SQLException;

    BigDecimal getBalance(Long accountId) throws SQLException;

    BigDecimal getBalance(String accountNumber) throws SQLException;

    List<Account> getAllAccounts() throws SQLException;
}
