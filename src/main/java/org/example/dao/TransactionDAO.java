
package org.example.dao;

import org.example.model.Transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface TransactionDAO {

    void save(Transaction transaction, Connection conn)
            throws SQLException;

    List<Transaction> findByAccountId(Long accountId)
            throws SQLException;
}
