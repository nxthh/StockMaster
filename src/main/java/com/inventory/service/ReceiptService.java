package com.inventory.service;

import com.inventory.model.CartItem;
import com.inventory.model.Transaction;
import com.inventory.repository.ReceiptFileRepository;

import java.time.format.DateTimeFormatter;

// builds a plain-text, printer-width receipt and saves it via the repository
public class ReceiptService {

    private static final String STORE_NAME = "MY STORE";
    private static final int LINE_WIDTH = 32;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ReceiptFileRepository receiptFileRepository;

    public ReceiptService(ReceiptFileRepository receiptFileRepository) {
        this.receiptFileRepository = receiptFileRepository;
    }

    public String generateReceiptId() {
        return receiptFileRepository.generateNextReceiptId();
    }

    public void generateAndSaveReceipt(Transaction transaction) {
        String content = buildReceiptText(transaction);
        receiptFileRepository.saveReceipt(transaction.getReceiptId(), content);
    }

    public String loadReceiptText(String receiptId) {
        return receiptFileRepository.loadReceipt(receiptId);
    }

    // assembles the full receipt text section by section
    private String buildReceiptText(Transaction transaction) {
        StringBuilder receipt = new StringBuilder();

        String border = "=".repeat(LINE_WIDTH);
        String divider = "-".repeat(LINE_WIDTH);

        receipt.append(border).append(System.lineSeparator());
        receipt.append(centerText(STORE_NAME)).append(System.lineSeparator());
        receipt.append(border).append(System.lineSeparator());
        receipt.append(System.lineSeparator());

        receipt.append("Receipt ID: ").append(transaction.getReceiptId()).append(System.lineSeparator());
        receipt.append("Date: ").append(transaction.getDateTime().format(DATE_FORMAT)).append(System.lineSeparator());
        receipt.append("Cashier: ").append(transaction.getCashier()).append(System.lineSeparator());
        receipt.append(System.lineSeparator());

        receipt.append(divider).append(System.lineSeparator());
        receipt.append(String.format("%-14s%-8s%10s", "Product", "Qty", "Total")).append(System.lineSeparator());
        receipt.append(divider).append(System.lineSeparator());

        for (CartItem item : transaction.getItems()) { // one line per item
            String name = truncate(item.getProduct().getName(), 13);
            String itemTotal = money(item.getSubtotal());
            receipt.append(String.format("%-14s%-8d%10s", name, item.getQuantity(), itemTotal))
                    .append(System.lineSeparator());
        }
        receipt.append(divider).append(System.lineSeparator());
        receipt.append(System.lineSeparator());

        receipt.append(moneyLine("Subtotal:", transaction.getSubtotal())).append(System.lineSeparator());
        receipt.append(moneyLine("Discount:", transaction.getDiscountAmount())).append(System.lineSeparator());
        receipt.append(moneyLine("Tax:", transaction.getTaxAmount())).append(System.lineSeparator());
        receipt.append(divider).append(System.lineSeparator());
        receipt.append(moneyLine("TOTAL:", transaction.getTotal())).append(System.lineSeparator());
        receipt.append(System.lineSeparator());

        receipt.append(String.format("%-18s%14s", "Payment:", transaction.getPaymentMethod()))
                .append(System.lineSeparator());
        if ("Cash".equals(transaction.getPaymentMethod())) { // show amount paid + change only for cash
            receipt.append(moneyLine("Amount Paid:", transaction.getAmountPaid())).append(System.lineSeparator());
            receipt.append(moneyLine("Change:", transaction.getChange())).append(System.lineSeparator());
        }
        receipt.append(System.lineSeparator());

        receipt.append(border).append(System.lineSeparator());
        receipt.append(centerText("Thank You!")).append(System.lineSeparator());
        receipt.append(border).append(System.lineSeparator());

        return receipt.toString();
    }

    private String moneyLine(String label, double amount) {
        return String.format("%-18s%14s", label, money(amount));
    }

    private String money(double amount) {
        return String.format("$%.2f", amount);
    }

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }

    // pad text so it's centered within LINE_WIDTH
    private String centerText(String text) {
        int padding = Math.max(0, (LINE_WIDTH - text.length()) / 2);
        return " ".repeat(padding) + text;
    }
}
