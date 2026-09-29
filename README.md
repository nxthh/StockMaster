# StockMaster

**StockMaster** is a JavaFX desktop Point of Sale (POS) and Inventory Management System designed with a lightweight, file-backed architecture. It provides retail management features including role-based user authentication, product and editable category management, dynamic POS checkout with receipt generation, transaction history tracking, and real-time sales reporting.

## Tech Stack

* **Language**: Java 21


* **UI Framework**: JavaFX


* **Build Tool**: Maven


* **Packaging**: Windows Batch (`build.bat`) using JDK `jpackage`
* **Data Persistence**: Standard Java File I/O (`.txt` files, no SQL database or Spring/Hibernate dependencies)



## Core Features

* **Role-Based Authentication**: Built-in Admin and Cashier roles. Admins manage Cashier accounts, inventory items, category lists, reports, and transaction deletions, while Cashiers access checkout and personal transaction history.


* **Inventory & Dynamic Categories**: Full product CRUD, stock in/out controls, low-stock detection, and custom editable categories with delete-protection and cascading renames.


* **Point of Sale (POS) & Checkout**: Product search and category filtering, live cart subtotal calculations, percentage discount processing, tax calculation, payment methods (Cash with change computation, simulated Card and QR), and stock deduction upon successful sale.


* **Receipt Generation**: Formats and persists itemized plain-text receipts (`.txt`) under `data/receipts/` for every transaction.


* **Transaction Management**: Append-only transaction logging with row-level itemized breakdown viewing, receipt re-opening, and Admin-only history clearing.


* **Live Dashboard & Business Reports**: Real-time business metrics (Total Revenue, Inventory Value, Low Stock Count) alongside date-range filtered sales reports.



## Architecture

StockMaster uses a clean, layered architecture separating UI, business rules, and storage:

```text
JavaFX UI (FXML / CSS)
        ↓
   Controllers
        ↓
    Services
        ↓
  Repositories
        ↓
    File I/O

```

## Default Credentials

The system auto-creates default accounts in `data/users.txt` on first run:

| Role | Username | Password |
| --- | --- | --- |
| Admin | `admin`<br> | `admin123`<br> |
| Cashier | `cashier`<br> | `cashier123`<br> |

## Getting Started

### Prerequisites

* Java Development Kit (JDK) 21 or higher


* Apache Maven



### Running via Maven

To run the application directly from source:

```bash
mvn clean javafx:run

```

All data files (`products.txt`, `categories.txt`, `users.txt`, `transactions.txt`, and receipts) will automatically initialize inside the `data/` folder if missing.

### Building Portable Executable (`jpackage`)

To package the application into a standalone portable distribution:

1. Execute the `build.bat` script in the project root:
```cmd
build.bat

```


2. The script runs Maven to build the application binaries and invokes `jpackage` to bundle a custom Java runtime environment.
3. Locate the generated portable application folder inside the `dist/` directory.
4. Launch the `.exe` directly without needing Maven or a system-wide JDK installation.
