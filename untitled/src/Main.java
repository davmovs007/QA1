import controller.TaskController;
import service.TaskManager;
import ui.MainFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Устанавливаем глобальный обработчик необработанных исключений
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
                // 1. Инициализация слоя бизнес-логики (Model / Service)
                TaskManager taskManager = new TaskManager();

                // 2. Инициализация контроллера (Controller)
                TaskController taskController = new TaskController(taskManager);
                taskController.loadAutoSavedTasks(); // Загружаем данные до показа окна

                // 3. Инициализация представления (View) с передачей контроллера
                MainFrame frame = new MainFrame(taskController);
                taskController.setView(frame);
                
                // 4. Первичное отображение данных
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
