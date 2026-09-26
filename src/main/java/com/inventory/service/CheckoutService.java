package com.inventory.service;

import com.inventory.exception.InsufficientStockException;
import com.inventory.exception.InvalidCartOperationException;
import com.inventory.model.Cart;
import com.inventory.model.CartItem;
import com.inventory.model.CheckoutTotals;
import com.inventory.model.Discount;
import com.inventory.model.Product;
import com.inventory.model.Transaction;
import com.inventory.model.payment.Payment;
import com.inventory.repository.TransactionFileRepository;
import com.inventory.util.Session;

import java.util.List;

// orchestrates a POS sale: totals, stock check, payment, receipt
public class CheckoutService {

    private final ProductService productService;
    private final InventoryService inventoryService;
    private final TaxCalculator taxCalculator;
    private final TransactionFileRepository transactionFileRepository;
    private final ReceiptService receiptService;

    public CheckoutService(ProductService productService, InventoryService inventoryService,
                            TaxCalculator taxCalculator, TransactionFileRepository transactionFileRepository,
                            ReceiptService receiptService) {
        this.productService = productService;
        this.inventoryService = inventoryService;
        this.taxCalculator = taxCalculator;
        this.transactionFileRepository = transactionFileRepository;
        this.receiptService = receiptService;
    }

    // subtotal -> discount -> tax -> total (uses Discount, polymorphism)
    public CheckoutTotals calculateTotals(Cart cart, Discount discount) {
        double subtotal = cart.getSubtotal();
        double discountAmount = (discount == null) ? 0 : discount.calculateDiscount(subtotal);
        double taxableAmount = subtotal - discountAmount;
        double taxAmount = taxCalculator.calculateTax(taxableAmount);
        double total = taxableAmount + taxAmount;
        return new CheckoutTotals(subtotal, discountAmount, taxAmount, total);
    }

    // full checkout flow: validate stock, pay, save, deduct stock
    public Transaction checkout(Cart cart, Discount discount, Payment payment) {
        if (cart.isEmpty()) {
            throw new InvalidCartOperationException("Cannot checkout an empty cart.");
        }

        List<CartItem> items = cart.getItems();

        for (CartItem item : items) { // re-check stock at checkout time
            Product currentProduct = productService.findById(item.getProduct().getId());
            if (item.getQuantity() > currentProduct.getQuantity()) {
                throw new InsufficientStockException(
                        "Not enough stock for " + currentProduct.getName() +
                                ". Available: " + currentProduct.getQuantity() +
                                ", in cart: " + item.getQuantity());
            }
        }

        CheckoutTotals totals = calculateTotals(cart, discount);

        payment.processPayment(); // polymorphic: cash/card/QR behave differently

        String transactionId = transactionFileRepository.generateNextTransactionId();
        String receiptId = receiptService.generateReceiptId();
        String cashier = Session.getCurrentUsername();
        Transaction transaction = new Transaction(transactionId, cashier, items, totals.getSubtotal(),
                totals.getDiscountAmount(), totals.getTaxAmount(), totals.getTotal(), payment, receiptId);

        transactionFileRepository.saveTransaction(transaction);

        receiptService.generateAndSaveReceipt(transaction);

        for (CartItem item : items) { // deduct sold quantities from stock
            inventoryService.stockOut(item.getProduct().getId(), item.getQuantity());
        }

        cart.clear();

        return transaction;
    }
}
