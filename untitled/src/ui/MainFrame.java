package ui;

import model.Task;
import service.FileManager;
import service.TaskManager;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

public class MainFrame extends JFrame {
    private static final String AUTO_SAVE_FILE = FileManager.DEFAULT_FILE_NAME;

    private TaskManager taskManager;
    private JTable taskTable;
    private TaskTableModel tableModel;

    public MainFrame() {
        super("Менеджер задач");
        taskManager = new TaskManager();
        tableModel = new TaskTableModel();

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Автоматическая загрузка задач при запуске
        loadAutoSavedTasks();

        initComponents();
        setupWindowClosing();
        refreshTable();
    }

    private void loadAutoSavedTasks() {
        File file = new File(AUTO_SAVE_FILE);
        if (file.exists()) {
            try {
                taskManager.loadFromCsv(file);
            } catch (Exception ex) {
                System.err.println("Не удалось выполнить автозагрузку задач: " + ex.getMessage());
            }
        }
    }

    private void autoSaveTasks() {
        try {
            File file = new File(AUTO_SAVE_FILE);
            taskManager.saveToCsv(file);
        } catch (Exception ex) {
            System.err.println("Не удалось выполнить автосохранение задач: " + ex.getMessage());
        }
    }

    private void setupWindowClosing() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Автоматическое сохранение задач при закрытии программы
                autoSaveTasks();
                dispose();
                System.exit(0);
            }
        });
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        taskTable = new JTable(tableModel);
        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(taskTable), BorderLayout.CENTER);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Добавить");
        JButton editButton = new JButton("Редактировать");
        JButton deleteButton = new JButton("Удалить");
        JButton saveButton = new JButton("Сохранить");
        JButton loadButton = new JButton("Загрузить");
        
        JTextField searchField = new JTextField(15);
        JButton searchButton = new JButton("Поиск");

        toolbar.add(addButton);
        toolbar.add(editButton);
        toolbar.add(deleteButton);
        toolbar.add(new JSeparator(SwingConstants.VERTICAL));
        toolbar.add(saveButton);
        toolbar.add(loadButton);
        toolbar.add(new JSeparator(SwingConstants.VERTICAL));
        toolbar.add(searchField);
        toolbar.add(searchButton);

        add(toolbar, BorderLayout.NORTH);

        addButton.addActionListener(e -> {
            TaskDialog dialog = new TaskDialog(this, "Новая задача", null);
            dialog.setVisible(true);
            if (dialog.isConfirmed()) {
                Task nt = dialog.getTask();
                taskManager.addTask(nt.getTitle(), nt.getDescription(), nt.getPriority(), nt.getStatus(), nt.getDueDate());
                refreshTable();
            }
        });

        editButton.addActionListener(e -> {
            int selectedRow = taskTable.getSelectedRow();
            if (selectedRow >= 0) {
                Task selectedTask = tableModel.getTaskAt(selectedRow);
                TaskDialog dialog = new TaskDialog(this, "Редактировать задачу", selectedTask);
                dialog.setVisible(true);
                if (dialog.isConfirmed()) {
                    Task updated = dialog.getTask();
                    taskManager.updateTask(selectedTask.getId(), updated.getTitle(), updated.getDescription(), updated.getPriority(), updated.getStatus(), updated.getDueDate());
                    refreshTable();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Выберите задачу для редактирования.");
            }
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = taskTable.getSelectedRow();
            if (selectedRow >= 0) {
                Task selectedTask = tableModel.getTaskAt(selectedRow);
                int confirm = JOptionPane.showConfirmDialog(this, "Вы уверены, что хотите удалить задачу?", "Удаление", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    taskManager.deleteTask(selectedTask.getId());
                    refreshTable();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Выберите задачу для удаления.");
            }
        });

        saveButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Сохранить задачи в файл");
            fileChooser.setFileFilter(new FileNameExtensionFilter("CSV / TXT файлы (*.csv, *.txt)", "csv", "txt"));
            fileChooser.setSelectedFile(new File("tasks.csv"));

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    File file = fileChooser.getSelectedFile();
                    if (!file.getName().toLowerCase().endsWith(".csv") && !file.getName().toLowerCase().endsWith(".txt")) {
                        file = new File(file.getAbsolutePath() + ".csv");
                    }
                    taskManager.saveToCsv(file);
                    JOptionPane.showMessageDialog(this, "Задачи успешно сохранены в файл:\n" + file.getName());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Ошибка сохранения: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        loadButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Загрузить задачи из файла");
            fileChooser.setFileFilter(new FileNameExtensionFilter("CSV / TXT файлы (*.csv, *.txt)", "csv", "txt"));

            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    File file = fileChooser.getSelectedFile();
                    taskManager.loadFromCsv(file);
                    refreshTable();
                    JOptionPane.showMessageDialog(this, "Задачи успешно загружены из файла:\n" + file.getName());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Ошибка загрузки: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        searchButton.addActionListener(e -> {
            String query = searchField.getText();
            tableModel.setTasks(taskManager.search(query));
        });
    }

    private void refreshTable() {
        tableModel.setTasks(taskManager.getAllTasks());
    }
}
