package service;

import model.Priority;
import model.Task;
import model.TaskStatus;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс для работы с сохранением и загрузкой задач в текстовые файлы (.csv / .txt).
 */
public class FileManager {
    public static final String DEFAULT_FILE_NAME = "tasks.csv";
    private static final String CSV_HEADER = "id,title,description,priority,status,createdAt,dueDate";

    /**
     * Сохраняет список задач в файл формата CSV/TXT.
     *
     * @param file  целевой файл
     * @param tasks список задач для сохранения
     * @throws IOException при ошибках записи
     */
    public static void saveTasksToCsv(File file, List<Task> tasks) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("Файл не может быть null.");
        }
        if (tasks == null) {
            tasks = new ArrayList<>();
        }

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.write(CSV_HEADER);
            writer.newLine();

            for (Task task : tasks) {
                String line = String.format("%s,%s,%s,%s,%s,%s,%s",
                        escapeCsv(task.getId()),
                        escapeCsv(task.getTitle()),
                        escapeCsv(task.getDescription()),
                        escapeCsv(task.getPriority() != null ? task.getPriority().name() : Priority.MEDIUM.name()),
                        escapeCsv(task.getStatus() != null ? task.getStatus().name() : TaskStatus.TODO.name()),
                        escapeCsv(task.getCreatedAt() != null ? task.getCreatedAt().toString() : ""),
                        escapeCsv(task.getDueDate() != null ? task.getDueDate().toString() : "")
                );
                writer.write(line);
                writer.newLine();
            }
        }
    }

    /**
     * Загружает список задач из файла формата CSV/TXT.
     *
     * @param file файл для чтения
     * @return список загруженных задач
     * @throws IOException при ошибках чтения
     */
    public static List<Task> loadTasksFromCsv(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new FileNotFoundException("Файл не найден: " + (file != null ? file.getAbsolutePath() : "null"));
        }

        List<Task> tasks = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            List<String> records = readCsvRecords(reader);
            if (records.isEmpty()) {
                return tasks;
            }

            // Первая строка — заголовок
            int startIndex = 0;
            String firstRecord = records.get(0).trim();
            if (firstRecord.toLowerCase().startsWith("id,") || firstRecord.equalsIgnoreCase(CSV_HEADER)) {
                startIndex = 1;
            }

            for (int i = startIndex; i < records.size(); i++) {
                String record = records.get(i).trim();
                if (record.isEmpty()) {
                    continue;
                }

                List<String> fields = parseCsvLine(record);
                if (fields.size() < 2) {
                    continue; // Пропускаем некорректные строки
                }

                String id = fields.get(0).trim();
                String title = fields.size() > 1 ? fields.get(1) : "";
                String description = fields.size() > 2 ? fields.get(2) : "";
                Priority priority = fields.size() > 3 ? parsePriority(fields.get(3)) : Priority.MEDIUM;
                TaskStatus status = fields.size() > 4 ? parseStatus(fields.get(4)) : TaskStatus.TODO;
                LocalDateTime createdAt = fields.size() > 5 ? parseDateTime(fields.get(5)) : LocalDateTime.now();
                LocalDateTime dueDate = fields.size() > 6 ? parseDateTime(fields.get(6)) : null;

                if (createdAt == null) {
                    createdAt = LocalDateTime.now();
                }

                Task task = new Task(id, title, description, priority, status, createdAt, dueDate);
                tasks.add(task);
            }
        }

        return tasks;
    }

    /**
     * Считывает записи CSV с учетом возможных многострочных значений внутри кавычек.
     */
    private static List<String> readCsvRecords(BufferedReader reader) throws IOException {
        List<String> records = new ArrayList<>();
        StringBuilder currentRecord = new StringBuilder();
        String line;
        boolean inQuotes = false;

        while ((line = reader.readLine()) != null) {
            for (int i = 0; i < line.length(); i++) {
                if (line.charAt(i) == '"') {
                    inQuotes = !inQuotes;
                }
            }

            if (currentRecord.length() > 0) {
                currentRecord.append("\n");
            }
            currentRecord.append(line);

            if (!inQuotes) {
                records.add(currentRecord.toString());
                currentRecord.setLength(0);
            }
        }

        if (currentRecord.length() > 0) {
            records.add(currentRecord.toString());
        }

        return records;
    }

    /**
     * Разбирает строку CSV на отдельные поля с учетом экранирования кавычек.
     */
    public static List<String> parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++; // Пропускаем сдвоенную кавычку
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString());
        return tokens;
    }

    /**
     * Экранирует значение для сохранения в формате CSV.
     */
    private static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private static Priority parsePriority(String value) {
        if (value == null || value.trim().isEmpty()) {
            return Priority.MEDIUM;
        }
        String clean = value.trim();
        for (Priority p : Priority.values()) {
            if (p.name().equalsIgnoreCase(clean) || p.getDisplayName().equalsIgnoreCase(clean)) {
                return p;
            }
        }
        return Priority.MEDIUM;
    }

    private static TaskStatus parseStatus(String value) {
        if (value == null || value.trim().isEmpty()) {
            return TaskStatus.TODO;
        }
        String clean = value.trim();
        for (TaskStatus s : TaskStatus.values()) {
            if (s.name().equalsIgnoreCase(clean) || s.getDisplayName().equalsIgnoreCase(clean)) {
                return s;
            }
        }
        return TaskStatus.TODO;
    }

    private static LocalDateTime parseDateTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
