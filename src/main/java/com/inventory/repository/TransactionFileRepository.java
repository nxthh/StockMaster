package com.inventory.repository;

import com.inventory.model.Transaction;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// file I/O layer: reads/writes transactions.txt (block-based format)
public class TransactionFileRepository {

    private static final String FILE_PATH = "data/transactions.txt";

    public TransactionFileRepository() {
        createFileIfMissing();
    }

    // read TRANSACTION/ITEM/END blocks into Transaction objects
    public List<Transaction> loadTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        Path path = Path.of(FILE_PATH);

        if (!Files.exists(path)) {
            return transactions;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            List<String> currentBlock = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                if (line.startsWith("TRANSACTION,")) { // start of a new block
                    currentBlock = new ArrayList<>();
                    currentBlock.add(line);
                } else if (line.startsWith("ITEM,")) {
                    currentBlock.add(line);
                } else if (line.equals("END")) {
                    currentBlock.add(line);
                    addParsedTransaction(transactions, currentBlock);
                    currentBlock = new ArrayList<>();
                } else {
                    System.out.println("Skipping unrecognized line in transaction file: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Could not read transaction file: " + e.getMessage());
        }

        return transactions;
    }

    private void addParsedTransaction(List<Transaction> transactions, List<String> block) {
        try {
            transactions.add(Transaction.fromFileLines(block));
        } catch (IllegalArgumentException e) {
            System.out.println("Skipping invalid transaction record: " + e.getMessage());
        }
    }

    // append one transaction's block without rewriting the file
    public void saveTransaction(Transaction transaction) {
        try {
            Path path = Path.of(FILE_PATH);
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }

            try (FileWriter writer = new FileWriter(FILE_PATH, true)) {
                for (String line : transaction.toFileLines()) {
                    writer.write(line);
                    writer.write(System.lineSeparator());
                }
            }
        } catch (IOException e) {
            System.out.println("Could not save transaction " + transaction.getTransactionId()
                    + ": " + e.getMessage());
        }
    }

    public void deleteTransaction(String transactionId) {
        List<Transaction> transactions = loadTransactions();
        transactions.removeIf(transaction -> transaction.getTransactionId().equalsIgnoreCase(transactionId));
        rewriteFile(transactions);
    }

    public void deleteAllTransactions() {
        rewriteFile(new ArrayList<>());
    }

    // overwrite the file with the full current list
    private void rewriteFile(List<Transaction> transactions) {
        try {
            Path path = Path.of(FILE_PATH);
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            try (FileWriter writer = new FileWriter(FILE_PATH)) {
                for (Transaction transaction : transactions) {
                    for (String line : transaction.toFileLines()) {
                        writer.write(line);
                        writer.write(System.lineSeparator());
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Could not save transaction file: " + e.getMessage());
        }
    }

    public Optional<Transaction> findTransaction(String transactionId) {
        if (transactionId == null) {
            return Optional.empty();
        }
        for (Transaction transaction : loadTransactions()) {
            if (transaction.getTransactionId().equalsIgnoreCase(transactionId)) {
                return Optional.of(transaction);
            }
        }
        return Optional.empty();
    }

    // scan existing ids and return the next Txxxx id
    public String generateNextTransactionId() {
        int highestNumber = 0;

        for (Transaction transaction : loadTransactions()) {
            int number = extractNumber(transaction.getTransactionId());
            if (number > highestNumber) {
                highestNumber = number;
            }
        }

        int nextNumber = highestNumber + 1;
        return String.format("T%04d", nextNumber);
    }

    private int extractNumber(String transactionId) {
        if (transactionId == null || !transactionId.startsWith("T")) {
            return 0;
        }
        try {
            return Integer.parseInt(transactionId.substring(1));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // first run: create an empty transaction file
    private void createFileIfMissing() {
        try {
            Path path = Path.of(FILE_PATH);
            if (Files.exists(path)) {
                return;
            }
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.createFile(path);
        } catch (IOException e) {
            System.out.println("Could not create transaction file: " + e.getMessage());
        }
    }
}
