package modelviewer.ui;

import javax.swing.*;
import modelviewer.io.ObjReaderException;

public class ErrorDialog {

    public static void showError(String message, String title) {
        JOptionPane.showMessageDialog(
                null,
                formatMessage(message),
                title,
                JOptionPane.ERROR_MESSAGE
        );
    }

    public static void showWarning(String message, String title) {
        JOptionPane.showMessageDialog(
                null,
                formatMessage(message),
                title,
                JOptionPane.WARNING_MESSAGE
        );
    }

    public static void showInfo(String message, String title) {
        JOptionPane.showMessageDialog(
                null,
                formatMessage(message),
                title,
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public static void showObjReaderError(ObjReaderException e) {
        String message = "Error loading OBJ file:\n";
        message += e.getMessage();

        showError(message, "OBJ Loading Error");
    }

    public static boolean confirmAction(String message, String title) {
        int result = JOptionPane.showConfirmDialog(
                null,
                formatMessage(message),
                title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        return result == JOptionPane.YES_OPTION;
    }

    private static String formatMessage(String message) {
        return "<html><body style='width: 300px;'>" +
                message.replace("\n", "<br>") +
                "</body></html>";
    }
}