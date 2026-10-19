# BankingSystem – UML & Documentation Index

| File | Contents |
|------|----------|
| [01_class_diagram.md](01_class_diagram.md) | Full class diagram — all classes, interfaces, fields, methods, and relationships |
| [02_package_diagram.md](02_package_diagram.md) | Package / component diagram — inter-package dependency graph |
| [03_sequence_diagrams.md](03_sequence_diagrams.md) | Sequence diagrams for: Create Account, Deposit, Withdraw, Transfer, Transaction History |
| [04_database_layout.md](04_database_layout.md) | ER diagram, table column definitions, transaction type reference, recommended indexes |

## Architecture Overview

```
┌──────────────────────────────────────────────────────────┐
│  Presentation Layer                                       │
│  org.example.ui.BankingConsole                           │
│  org.example.util.InputValidator                         │
└────────────────────────┬─────────────────────────────────┘
                         │ uses (interfaces)
┌────────────────────────▼─────────────────────────────────┐
│  Service Layer                                            │
│  org.example.service.AccountService                      │
│  org.example.service.TransactionService                  │
│       implemented by org.example.impl.*ServiceImpl        │
└────────────────────────┬─────────────────────────────────┘
                         │ uses (interfaces)
┌────────────────────────▼─────────────────────────────────┐
│  DAO Layer                                                │
│  org.example.dao.AccountDAO                              │
│  org.example.dao.TransactionDAO                          │
│       implemented by org.example.impl.*DAOImpl            │
└────────────────────────┬─────────────────────────────────┘
                         │ JDBC
┌────────────────────────▼─────────────────────────────────┐
│  Infrastructure                                           │
│  org.example.config.DatabaseConnection  (MySQL JDBC)     │
│  org.example.model.Account                               │
│  org.example.model.Transaction                           │
│  org.example.exception.*                                 │
└──────────────────────────────────────────────────────────┘
```
