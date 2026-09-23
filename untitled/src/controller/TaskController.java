package controller;

import model.Priority;
import model.Task;
import model.TaskStatus;
import service.FileManager;
import service.TaskManager;
import ui.MainFrame;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class TaskController {
    private static final String AUTO_SAVE_FILE = FileManager.DEFAULT_FILE_NAME;
    
    private final TaskManager taskManager;
    private MainFrame mainFrame;

    public TaskController(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    public void setView(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    public void loadAutoSavedTasks() {
        File file = new File(AUTO_SAVE_FILE);
        if (file.exists()) {
            try {
                taskManager.loadFromCsv(file);
            } catch (Exception ex) {
                System.err.println("Не удалось выполнить автозагрузку задач: " + ex.getMessage());
            }
        }
    }

    public void autoSaveTasks() {
        try {
            File file = new File(AUTO_SAVE_FILE);
            taskManager.saveToCsv(file);
        } catch (Exception ex) {
            System.err.println("Не удалось выполнить автосохранение задач: " + ex.getMessage());
        }
    }

    public void addTask(Task t) {
        taskManager.addTask(t.getTitle(), t.getDescription(), t.getPriority(), t.getStatus(), t.getDueDate());
    }

    public void updateTask(String id, Task updatedTask) {
        taskManager.updateTask(id, updatedTask.getTitle(), updatedTask.getDescription(), 
                updatedTask.getPriority(), updatedTask.getStatus(), updatedTask.getDueDate());
    }

    public void deleteTask(String id) {
        taskManager.deleteTask(id);
    }

    public void saveToFile(File file) throws Exception {
        if (!file.getName().toLowerCase().endsWith(".csv") && !file.getName().toLowerCase().endsWith(".txt")) {
            file = new File(file.getAbsolutePath() + ".csv");
        }
        taskManager.saveToCsv(file);
    }

    public void loadFromFile(File file) throws Exception {
        taskManager.loadFromCsv(file);
    }

    /**
     * Логика фильтрации вынесена в контроллер
     */
    public List<Task> getFilteredTasks(String query, int statusIndex, int priorityIndex) {
        List<Task> result = taskManager.getAllTasks();

        if (query != null && !query.trim().isEmpty()) {
            String lowerQuery = query.trim().toLowerCase();
            result = result.stream()
                    .filter(t -> t.getTitle().toLowerCase().contains(lowerQuery) || 
                                 t.getDescription().toLowerCase().contains(lowerQuery))
                    .collect(Collectors.toList());
        }

        if (statusIndex > 0) { // 0 - "Все статусы"
            TaskStatus selectedStatus = TaskStatus.values()[statusIndex - 1];
            result = result.stream()
                    .filter(t -> t.getStatus() == selectedStatus)
                    .collect(Collectors.toList());
        }

        if (priorityIndex > 0) { // 0 - "Все приоритеты"
            Priority selectedPriority = Priority.values()[priorityIndex - 1];
            result = result.stream()
                    .filter(t -> t.getPriority() == selectedPriority)
                    .collect(Collectors.toList());
        }
        return result;
    }
}
