
package org.example.dao;

import org.example.model.Account;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface AccountDAO {

    void save(Account account, Connection connection)
            throws SQLException;

    Account findById(Long id) throws SQLException;

    Account findByAccountNumber(String accountNumber) throws SQLException;

    List<Account> findAll() throws SQLException;

    Account findByIdForUpdate(
            Long id, Connection connection
    ) throws SQLException;

    Account findByAccountNumberForUpdate(
            String accountNumber, Connection connection
    ) throws SQLException;

    void updateBalance(
            Long id,
            BigDecimal balance,
            Connection connection
    ) throws SQLException;

    String findNextAccountNumber(Connection connection) throws SQLException;
}
