-- Banking System Database Schema
-- Database: BankingSystem_CIICC

CREATE DATABASE IF NOT EXISTS BankingSystem_CIICC;
USE BankingSystem_CIICC;

CREATE TABLE IF NOT EXISTS accounts (
    id             BIGINT          NOT NULL AUTO_INCREMENT,
    account_number VARCHAR(20)     NOT NULL UNIQUE,
    account_name   VARCHAR(100)    NOT NULL,
    balance        DECIMAL(15, 2)  NOT NULL DEFAULT 0.00,
    created_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS transactions (
    id                    BIGINT          NOT NULL AUTO_INCREMENT,
    transaction_reference VARCHAR(36)     NOT NULL UNIQUE,
    account_id            BIGINT          NOT NULL,
    transaction_type      VARCHAR(20)     NOT NULL,
    amount                DECIMAL(15, 2)  NOT NULL,
    balance               DECIMAL(15, 2)  NOT NULL,
    reference_account_id  BIGINT          NULL,
    created_at            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_transaction_account
        FOREIGN KEY (account_id) REFERENCES accounts (id),
    CONSTRAINT fk_reference_account
        FOREIGN KEY (reference_account_id) REFERENCES accounts (id)
);
