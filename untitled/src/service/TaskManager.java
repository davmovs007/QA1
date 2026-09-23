package service;

import model.Priority;
import model.Task;
import model.TaskStatus;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Класс управления задачами (бизнес-логика, фильтрация, сортировка, хранение).
 */
public class TaskManager {
    private final List<Task> tasks;

    public TaskManager() {
        this.tasks = new ArrayList<>();
    }

    // ==================== CRUD Операции ====================

    /**
     * Создает и добавляет новую задачу.
     */
    public Task addTask(String title, String description, Priority priority, TaskStatus status, java.time.LocalDateTime dueDate) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        Task task = new Task(id, title, description, priority, status, dueDate);
        tasks.add(task);
        return task;
    }

    /**
     * Добавляет уже созданный объект задачи.
     */
    public void addTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Задача не может быть null.");
        }
        if (getTaskById(task.getId()).isPresent()) {
            throw new IllegalArgumentException("Задача с ID " + task.getId() + " уже существует.");
        }
        tasks.add(task);
    }

    /**
     * Устанавливает новый список задач, заменяя текущий.
     */
    public void setTasks(List<Task> newTasks) {
        tasks.clear();
        if (newTasks != null) {
            tasks.addAll(newTasks);
        }
    }

    /**
     * Поиск задачи по идентификатору.
     */
    public Optional<Task> getTaskById(String id) {
        if (id == null) return Optional.empty();
        return tasks.stream()
                .filter(t -> t.getId().equalsIgnoreCase(id))
                .findFirst();
    }

    /**
     * Обновление существующей задачи.
     */
    public boolean updateTask(String id, String newTitle, String newDescription, Priority newPriority, TaskStatus newStatus, java.time.LocalDateTime newDueDate) {
        Optional<Task> optionalTask = getTaskById(id);
        if (optionalTask.isPresent()) {
            Task task = optionalTask.get();
            task.setTitle(newTitle);
            task.setDescription(newDescription);
            task.setPriority(newPriority);
            task.setStatus(newStatus);
            task.setDueDate(newDueDate);
            return true;
        }
        return false;
    }

    /**
     * Удаление задачи по ID.
     */
    public boolean deleteTask(String id) {
        return tasks.removeIf(task -> task.getId().equalsIgnoreCase(id));
    }

    /**
     * Получить список всех задач (немодифицируемый вид).
     */
    public List<Task> getAllTasks() {
        return Collections.unmodifiableList(tasks);
    }

    /**
     * Очистить весь список задач.
     */
    public void clearAllTasks() {
        tasks.clear();
    }

    /**
     * Получить количество задач.
     */
    public int getTaskCount() {
        return tasks.size();
    }

    // ==================== Поиск и Фильтрация ====================

    /**
     * Фильтрация задач по статусу.
     */
    public List<Task> filterByStatus(TaskStatus status) {
        if (status == null) return getAllTasks();
        return tasks.stream()
                .filter(t -> t.getStatus() == status)
                .collect(Collectors.toList());
    }

    /**
     * Фильтрация задач по приоритету.
     */
    public List<Task> filterByPriority(Priority priority) {
        if (priority == null) return getAllTasks();
        return tasks.stream()
                .filter(t -> t.getPriority() == priority)
                .collect(Collectors.toList());
    }

    /**
     * Поиск по ключевому слову в названии или описании.
     */
    public List<Task> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllTasks();
        }
        String lowerQuery = query.toLowerCase().trim();
        return tasks.stream()
                .filter(t -> t.getTitle().toLowerCase().contains(lowerQuery) ||
                             t.getDescription().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
    }

    // ==================== Сортировка ====================

    /**
     * Сортировка по дате создания (новые сначала или старые сначала).
     */
    public List<Task> sortByCreatedAt(boolean ascending) {
        Comparator<Task> comparator = Comparator.comparing(Task::getCreatedAt);
        if (!ascending) {
            comparator = comparator.reversed();
        }
        return tasks.stream().sorted(comparator).collect(Collectors.toList());
    }

    /**
     * Сортировка по дедлайну (задачи без срока помещаются в конец).
     */
    public List<Task> sortByDueDate(boolean ascending) {
        Comparator<Task> comparator = Comparator.comparing(
                Task::getDueDate,
                Comparator.nullsLast(Comparator.naturalOrder())
        );
        if (!ascending) {
            comparator = comparator.reversed();
        }
        return tasks.stream().sorted(comparator).collect(Collectors.toList());
    }

    /**
     * Сортировка по приоритету (HIGH -> MEDIUM -> LOW или наоборот).
     */
    public List<Task> sortByPriority(boolean highestFirst) {
        Comparator<Task> comparator = Comparator.comparing(Task::getPriority);
        if (highestFirst) {
            comparator = comparator.reversed();
        }
        return tasks.stream().sorted(comparator).collect(Collectors.toList());
    }

    // ==================== Сохранение и Загрузка (CSV / TXT / Binary) ====================

    /**
     * Сохранение всех задач в CSV/TXT файл с помощью FileManager.
     */
    public void saveToCsv(File file) throws IOException {
        FileManager.saveTasksToCsv(file, tasks);
    }

    /**
     * Загрузка задач из CSV/TXT файла с помощью FileManager.
     */
    public void loadFromCsv(File file) throws IOException {
        List<Task> loadedTasks = FileManager.loadTasksFromCsv(file);
        setTasks(loadedTasks);
    }

    /**
     * Сохранение всех задач в бинарный файл через стандартную сериализацию Java.
     */
    public void saveToFile(File file) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(tasks);
        }
    }

    /**
     * Загрузка задач из бинарного файла.
     */
    @SuppressWarnings("unchecked")
    public void loadFromFile(File file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            List<Task> loadedTasks = (List<Task>) ois.readObject();
            setTasks(loadedTasks);
        }
    }
}
