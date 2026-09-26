package com.inventory.util;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;

// popup for viewing/printing a plain-text receipt
public class ReceiptDialog {

    private ReceiptDialog() {
    } // static-only: no instances

    public static void show(String title, String receiptText) {
        TextArea textArea = new TextArea(receiptText);
        textArea.setEditable(false);
        textArea.setWrapText(false);
        textArea.setPrefColumnCount(40);
        textArea.setPrefRowCount(24);
        textArea.setStyle("-fx-font-family: 'monospace';");

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.getDialogPane().setContent(textArea);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
}
