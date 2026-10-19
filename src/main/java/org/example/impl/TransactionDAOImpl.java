
package org.example.impl;

import org.example.config.DatabaseConnection;
import org.example.dao.TransactionDAO;
import org.example.model.Transaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAOImpl implements TransactionDAO {

    @Override
    public void save(Transaction transaction, Connection conn)
            throws SQLException {

        String sql = """
                INSERT INTO transactions
                (transaction_reference, account_id, transaction_type,
                 amount, balance, reference_account_id)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement preparedStatement =
                     conn.prepareStatement(sql)) {

            preparedStatement.setString(
                    1, transaction.getTransactionReference());

            preparedStatement.setLong(
                    2, transaction.getAccountId());

            preparedStatement.setString(
                    3, transaction.getTransactionType());

            preparedStatement.setBigDecimal(
                    4, transaction.getAmount());

            preparedStatement.setBigDecimal(
                    5, transaction.getBalance());

            if (transaction.getReferenceAccountId() != null) {
                preparedStatement.setLong(
                        6, transaction.getReferenceAccountId());
            } else {
                preparedStatement.setNull(6, Types.BIGINT);
            }

            preparedStatement.executeUpdate();
        }
    }

    @Override
    public List<Transaction> findByAccountId(Long accountId)
            throws SQLException {

        List<Transaction> transactions = new ArrayList<>();

        String sql = """
                SELECT t.id, t.transaction_reference, t.account_id,
                       t.transaction_type, t.amount, t.balance,
                       t.reference_account_id, t.created_at,
                       ref.account_number AS reference_account_number
                FROM transactions t
                LEFT JOIN accounts ref ON t.reference_account_id = ref.id
                WHERE t.account_id = ?
                ORDER BY t.created_at DESC, t.id DESC
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, accountId);

            try (ResultSet resultSet =
                         preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    transactions.add(new Transaction(
                            resultSet.getLong("id"),
                            resultSet.getString("transaction_reference"),
                            resultSet.getLong("account_id"),
                            resultSet.getString("transaction_type"),
                            resultSet.getBigDecimal("amount"),
                            resultSet.getBigDecimal("balance"),
                            getNullableLong(
                                    resultSet, "reference_account_id"),
                            resultSet.getTimestamp("created_at")
                                    .toLocalDateTime(),
                            resultSet.getString("reference_account_number")
                    ));
                }
            }
        }

        return transactions;
    }

    private Long getNullableLong(ResultSet resultSet, String column)
            throws SQLException {

        long value = resultSet.getLong(column);

        return resultSet.wasNull() ? null : value;
    }
}
