
package org.example.impl;

import org.example.config.DatabaseConnection;
import org.example.dao.AccountDAO;
import org.example.model.Account;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAOImpl implements AccountDAO {

    private Account mapAccount(ResultSet rs) throws SQLException {
        Timestamp timestamp = rs.getTimestamp("created_at");

        return new Account(
                rs.getLong("id"),
                rs.getString("account_number"),
                rs.getString("account_name"),
                rs.getBigDecimal("balance"),
                timestamp == null ? null : timestamp.toLocalDateTime()
        );
    }

    @Override
    public void save(Account account, Connection conn)
            throws SQLException {

        String sql = """
            INSERT INTO accounts (account_number, account_name, balance)
            VALUES (?, ?, ?)
            """;

        try (PreparedStatement stmt = conn.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, account.getAccountNumber());
            stmt.setString(2, account.getAccountName());
            stmt.setBigDecimal(3, account.getBalance());

            stmt.executeUpdate();

            // Find the generated ID here
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    account.setId(keys.getLong(1));
                } else {
                    throw new SQLException(
                            "Account ID was not generated."
                    );
                }
            }
        }
    }


    @Override
    public Account findById(Long id) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapAccount(rs) : null;
            }
        }
    }

    @Override
    public Account findByAccountNumber(String accountNumber) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE account_number = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accountNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapAccount(rs) : null;
            }
        }
    }

    @Override
    public List<Account> findAll() throws SQLException {
        List<Account> accounts = new ArrayList<>();

        String sql = "SELECT * FROM accounts ORDER BY id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                accounts.add(mapAccount(rs));
            }
        }

        return accounts;
    }

    @Override
    public Account findByIdForUpdate(Long id, Connection conn)
            throws SQLException {

        String sql = "SELECT * FROM accounts WHERE id = ? FOR UPDATE";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapAccount(rs) : null;
            }
        }
    }

    @Override
    public Account findByAccountNumberForUpdate(String accountNumber, Connection conn)
            throws SQLException {

        String sql = "SELECT * FROM accounts WHERE account_number = ? FOR UPDATE";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, accountNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapAccount(rs) : null;
            }
        }
    }

    @Override
    public void updateBalance(
            Long id, BigDecimal balance, Connection conn
    ) throws SQLException {

        String sql = "UPDATE accounts SET balance = ? WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, balance);
            stmt.setLong(2, id);

            if (stmt.executeUpdate() != 1) {
                throw new SQLException("Balance update failed.");
            }
        }
    }

    @Override
    public String findNextAccountNumber(Connection conn) throws SQLException {
        String sql = "SELECT MAX(CAST(account_number AS UNSIGNED)) FROM accounts";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                long max = rs.getLong(1);
                if (!rs.wasNull() && max >= 100000L) {
                    return String.valueOf(max + 1);
                }
            }

            return "100000";
        }
    }
}

