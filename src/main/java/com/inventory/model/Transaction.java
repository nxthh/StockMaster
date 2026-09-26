package com.inventory.model;

import com.inventory.model.payment.CashPayment;
import com.inventory.model.payment.Payment;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

// receipt record: one transaction + its line items
public class Transaction {

    private final String transactionId;
    private final LocalDateTime dateTime;
    private final String cashier;
    private final List<CartItem> items;
    private final double subtotal;
    private final double discountAmount;
    private final double taxAmount;
    private final double total;
    private final String paymentMethod;
    private final double amountPaid;
    private final double change;
    private final String receiptId;

    public Transaction(String transactionId, LocalDateTime dateTime, String cashier,
                        List<CartItem> items, double subtotal, double discountAmount,
                        double taxAmount, double total, String paymentMethod,
                        double amountPaid, double change, String receiptId) {
        this.transactionId = transactionId;
        this.dateTime = dateTime;
        this.cashier = cashier;
        this.items = items;
        this.subtotal = subtotal;
        this.discountAmount = discountAmount;
        this.taxAmount = taxAmount;
        this.total = total;
        this.paymentMethod = paymentMethod;
        this.amountPaid = amountPaid;
        this.change = change;
        this.receiptId = (receiptId == null) ? "" : receiptId;
    }

    // convenience constructor built from a Payment (polymorphism)
    public Transaction(String transactionId, String cashier, List<CartItem> items,
                        double subtotal, double discountAmount, double taxAmount,
                        double total, Payment payment, String receiptId) {
        this(transactionId, LocalDateTime.now(), cashier, items, subtotal, discountAmount,
                taxAmount, total, payment.getMethodName(),
                (payment instanceof CashPayment cashPayment) ? cashPayment.getAmountPaid() : total,
                payment.getChange(), receiptId);
    }

    public String getTransactionId() {
        return transactionId;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getCashier() {
        return cashier;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public double getTaxAmount() {
        return taxAmount;
    }

    public double getTotal() {
        return total;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public double getChange() {
        return change;
    }

    public String getReceiptId() {
        return receiptId;
    }

    public boolean hasReceipt() {
        return receiptId != null && !receiptId.isBlank();
    }

    // serialize: TRANSACTION header line + one ITEM line per product + END
    public List<String> toFileLines() {
        List<String> lines = new ArrayList<>();

        lines.add(String.join(",",
                "TRANSACTION",
                transactionId,
                dateTime.toString(),
                cashier,
                String.valueOf(subtotal),
                String.valueOf(discountAmount),
                String.valueOf(taxAmount),
                String.valueOf(total),
                paymentMethod,
                String.valueOf(amountPaid),
                String.valueOf(change),
                receiptId));

        for (CartItem item : items) {
            Product product = item.getProduct();
            lines.add(String.join(",",
                    "ITEM",
                    product.getId(),
                    product.getName(),
                    product.getCategory().toDisplayString(),
                    String.valueOf(product.getPrice()),
                    String.valueOf(item.getQuantity())));
        }

        lines.add("END");
        return lines;
    }

    // deserialize: rebuild a Transaction from its stored block of lines
    public static Transaction fromFileLines(List<String> blockLines) {
        if (blockLines == null || blockLines.isEmpty()) {
            throw new IllegalArgumentException("Empty transaction block.");
        }

        String header = blockLines.get(0);
        if (!header.startsWith("TRANSACTION,")) {
            throw new IllegalArgumentException("Transaction block must start with a TRANSACTION line.");
        }

        String[] parts = header.split(",", -1);

        if (parts.length != 11 && parts.length != 12) {
            throw new IllegalArgumentException("Malformed TRANSACTION line: " + header);
        }

        String transactionId = parts[1].trim();
        LocalDateTime dateTime;
        try {
            dateTime = LocalDateTime.parse(parts[2].trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date/time in transaction " + transactionId, e);
        }
        String cashier = parts[3].trim();
        double subtotal = parseDouble(parts[4], "subtotal", transactionId);
        double discountAmount = parseDouble(parts[5], "discount", transactionId);
        double taxAmount = parseDouble(parts[6], "tax", transactionId);
        double total = parseDouble(parts[7], "total", transactionId);
        String paymentMethod = parts[8].trim();
        double amountPaid = parseDouble(parts[9], "amountPaid", transactionId);
        double change = parseDouble(parts[10], "change", transactionId);
        String receiptId = (parts.length == 12) ? parts[11].trim() : "";

        if (transactionId.isEmpty()) {
            throw new IllegalArgumentException("Transaction is missing its ID.");
        }

        List<CartItem> items = new ArrayList<>();
        boolean sawEnd = false;

        for (int i = 1; i < blockLines.size(); i++) {
            String line = blockLines.get(i);
            if (line.equals("END")) {
                sawEnd = true;
                break;
            }
            if (!line.startsWith("ITEM,")) {
                throw new IllegalArgumentException("Unexpected line in transaction " + transactionId + ": " + line);
            }
            items.add(parseItemLine(line, transactionId));
        }

        if (!sawEnd) {
            throw new IllegalArgumentException("Transaction " + transactionId + " is missing its END line.");
        }

        return new Transaction(transactionId, dateTime, cashier, items, subtotal,
                discountAmount, taxAmount, total, paymentMethod, amountPaid, change, receiptId);
    }

    // parse a single ITEM line back into a CartItem
    private static CartItem parseItemLine(String line, String transactionId) {
        String[] parts = line.split(",", -1);
        if (parts.length != 6) {
            throw new IllegalArgumentException("Malformed ITEM line in transaction " + transactionId + ": " + line);
        }
        String productId = parts[1].trim();
        String productName = parts[2].trim();
        Category category = Category.fromString(parts[3].trim());
        double price = parseDouble(parts[4], "item price", transactionId);
        int quantity;
        try {
            quantity = Integer.parseInt(parts[5].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid item quantity in transaction " + transactionId, e);
        }
        if (productId.isEmpty()) {
            throw new IllegalArgumentException("Item is missing a product ID in transaction " + transactionId);
        }

        Product snapshotProduct = new Product(productId, productName, category, price, quantity, 0);
        return new CartItem(snapshotProduct, quantity);
    }

    private static double parseDouble(String text, String fieldName, String transactionId) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid " + fieldName + " in transaction " + transactionId, e);
        }
    }

    @Override
    public String toString() {
        return "Transaction{id=" + transactionId + ", dateTime=" + dateTime + ", cashier=" + cashier +
                ", total=" + total + ", paymentMethod=" + paymentMethod + "}";
    }
}
