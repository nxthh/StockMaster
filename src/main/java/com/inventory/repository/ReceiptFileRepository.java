package com.inventory.repository;

import com.inventory.exception.ReceiptException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// file I/O layer: one .txt file per receipt in data/receipts
public class ReceiptFileRepository {

    private static final String RECEIPTS_FOLDER = "data/receipts";
    private static final Pattern RECEIPT_ID_PATTERN = Pattern.compile("R(\\d+)\\.txt", Pattern.CASE_INSENSITIVE);

    public ReceiptFileRepository() {
        createFolderIfMissing();
    }

    // scan receipt files and return the next Rxxxx id
    public String generateNextReceiptId() {
        int highestNumber = 0;
        Path folder = Path.of(RECEIPTS_FOLDER);

        if (Files.isDirectory(folder)) {
            try (DirectoryStream<Path> files = Files.newDirectoryStream(folder, "*.txt")) {
                for (Path file : files) {
                    int number = extractNumber(file.getFileName().toString());
                    if (number > highestNumber) {
                        highestNumber = number;
                    }
                }
            } catch (IOException e) {
                throw new ReceiptException("Could not read the receipts folder to generate a new receipt ID.", e);
            }
        }

        int nextNumber = highestNumber + 1;
        return String.format("R%04d", nextNumber);
    }

    // write one receipt's text to its own file
    public void saveReceipt(String receiptId, String content) {
        if (receiptId == null || receiptId.isBlank()) {
            throw new ReceiptException("Cannot save a receipt without a receipt ID.");
        }
        try {
            createFolderIfMissing();
            Path path = Path.of(RECEIPTS_FOLDER, receiptId + ".txt");
            Files.writeString(path, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ReceiptException("Could not save receipt " + receiptId + ": " + e.getMessage(), e);
        }
    }

    public String loadReceipt(String receiptId) {
        if (receiptId == null || receiptId.isBlank()) {
            throw new ReceiptException("No receipt ID was provided.");
        }
        Path path = Path.of(RECEIPTS_FOLDER, receiptId + ".txt");
        if (!Files.exists(path)) {
            throw new ReceiptException("Receipt " + receiptId + " was not found.");
        }
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ReceiptException("Could not read receipt " + receiptId + ": " + e.getMessage(), e);
        }
    }

    public void deleteReceipt(String receiptId) {
        if (receiptId == null || receiptId.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(Path.of(RECEIPTS_FOLDER, receiptId + ".txt"));
        } catch (IOException e) {
            System.out.println("Could not delete receipt " + receiptId + ": " + e.getMessage());
        }
    }

    public boolean receiptExists(String receiptId) {
        if (receiptId == null || receiptId.isBlank()) {
            return false;
        }
        return Files.exists(Path.of(RECEIPTS_FOLDER, receiptId + ".txt"));
    }

    private int extractNumber(String fileName) {
        Matcher matcher = RECEIPT_ID_PATTERN.matcher(fileName);
        if (!matcher.matches()) {
            return 0;
        }
        try {
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // first run: ensure the receipts folder exists
    private void createFolderIfMissing() {
        try {
            Files.createDirectories(Path.of(RECEIPTS_FOLDER));
        } catch (IOException e) {
            throw new ReceiptException("Could not create the data/receipts folder: " + e.getMessage(), e);
        }
    }
}
