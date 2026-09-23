import controller.TaskController;
import service.TaskManager;
import ui.MainFrame;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.util.Enumeration;

public class Main {
    public static void main(String[] args) {
        // Включаем сглаживание шрифтов
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Восстанавливаем стандартный системный шрифт для корректного отображения ВСЕХ языков 
            // (особенно кириллицы), чтобы вместо текста не было символов/квадратиков.
            // "Dialog" / "SansSerif" — это умные составные шрифты Java, которые автоматически 
            // переключаются на Segoe UI для букв и на Emoji-шрифты для самих иконок.
            setUIFont(new FontUIResource("SansSerif", Font.PLAIN, 13));
            
            // Принудительно делаем везде текст черным
            setUIForeground(Color.BLACK);
            
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

    private static void setUIFont(FontUIResource font) {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, font);
            }
        }
    }
    
    private static void setUIForeground(Color color) {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            if (key != null && key.toString().endsWith(".foreground")) {
                UIManager.put(key, color);
            }
        }
    }
}
