import controller.TaskController;
import service.TaskManager;
import ui.MainFrame;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        // Включаем сглаживание шрифтов
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
        } catch (Exception ignored) {
        }

        // Глобальный обработчик необработанных исключений
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            throwable.printStackTrace();
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(
                        null,
                        "Произошла непредвиденная ошибка в приложении:\n" + throwable.getMessage(),
                        "Критическая ошибка",
                        JOptionPane.ERROR_MESSAGE
                );
            });
        });

        SwingUtilities.invokeLater(() -> {
            try {
                TaskManager taskManager = new TaskManager();
                TaskController taskController = new TaskController(taskManager);
                MainFrame frame = new MainFrame(taskController);
                taskController.setView(frame);
                
                frame.applyFilters();
                frame.setVisible(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        null,
                        "Не удалось запустить приложение:\n" + ex.getMessage(),
                        "Ошибка запуска",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }

}
