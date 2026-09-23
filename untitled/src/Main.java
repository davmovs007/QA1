import ui.MainFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Устанавливаем глобальный обработчик необработанных исключений для предотвращения падений приложения
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
                MainFrame frame = new MainFrame();
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
