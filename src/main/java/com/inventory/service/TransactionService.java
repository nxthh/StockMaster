package com.inventory.service;

import com.inventory.exception.UnauthorizedActionException;
import com.inventory.model.Transaction;
import com.inventory.repository.TransactionFileRepository;
import com.inventory.util.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// business logic for viewing/deleting transaction history
public class TransactionService {

    private final TransactionFileRepository transactionFileRepository;

    public TransactionService(TransactionFileRepository transactionFileRepository) {
        this.transactionFileRepository = transactionFileRepository;
    }

    // admins see all sales, cashiers see only their own
    public List<Transaction> getVisibleTransactions() {
        List<Transaction> all = transactionFileRepository.loadTransactions();

        if (Session.isAdmin()) {
            return all;
        }

        String currentUser = Session.getCurrentUsername();
        List<Transaction> ownTransactions = new ArrayList<>();
        for (Transaction transaction : all) {
            if (transaction.getCashier().equalsIgnoreCase(currentUser)) {
                ownTransactions.add(transaction);
            }
        }
        return ownTransactions;
    }

    public Optional<Transaction> findTransaction(String transactionId) {
        return transactionFileRepository.findTransaction(transactionId);
    }

    public void deleteTransaction(String transactionId) {
        requireAdmin("delete transaction history");
        transactionFileRepository.deleteTransaction(transactionId);
    }

    public void clearAllHistory() {
        requireAdmin("clear transaction history");
        transactionFileRepository.deleteAllTransactions();
    }

    // guard: role check shared by admin-only actions
    private void requireAdmin(String actionDescription) {
        if (!Session.isAdmin()) {
            throw new UnauthorizedActionException("Only ADMIN users can " + actionDescription + ".");
        }
    }
}
