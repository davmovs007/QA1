package ui;

import model.Priority;
import model.Task;
import model.TaskStatus;
import service.FileManager;
import service.TaskManager;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class MainFrame extends JFrame {
    private static final String AUTO_SAVE_FILE = FileManager.DEFAULT_FILE_NAME;

    private TaskManager taskManager;
    private JTable taskTable;
    private TaskTableModel tableModel;

    // Filter components
    private JTextField searchField;
    private JComboBox<String> statusComboBox;
    private JComboBox<String> priorityComboBox;

    public MainFrame() {
        super("Менеджер задач");
        taskManager = new TaskManager();
        tableModel = new TaskTableModel();

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(950, 650);
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

        // Панель для кнопок и фильтров
        JPanel topPanel = new JPanel(new GridLayout(2, 1));

        // 1. Панель действий
        JPanel actionToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Добавить");
        JButton editButton = new JButton("Редактировать");
        JButton deleteButton = new JButton("Удалить");
        JButton saveButton = new JButton("Сохранить");
        JButton loadButton = new JButton("Загрузить");
        
        actionToolbar.add(addButton);
        actionToolbar.add(editButton);
        actionToolbar.add(deleteButton);
        actionToolbar.add(new JSeparator(SwingConstants.VERTICAL));
        actionToolbar.add(saveButton);
        actionToolbar.add(loadButton);

        // 2. Панель фильтрации
        JPanel filterToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        filterToolbar.add(new JLabel("Поиск:"));
        searchField = new JTextField(15);
        filterToolbar.add(searchField);
        
        filterToolbar.add(Box.createHorizontalStrut(10));
        filterToolbar.add(new JLabel("Статус:"));
        statusComboBox = new JComboBox<>();
        statusComboBox.addItem("Все статусы");
        for (TaskStatus status : TaskStatus.values()) {
            statusComboBox.addItem(status.getDisplayName());
        }
        filterToolbar.add(statusComboBox);
        
        filterToolbar.add(Box.createHorizontalStrut(10));
        filterToolbar.add(new JLabel("Приоритет:"));
        priorityComboBox = new JComboBox<>();
        priorityComboBox.addItem("Все приоритеты");
        for (Priority priority : Priority.values()) {
            priorityComboBox.addItem(priority.getDisplayName());
        }
        filterToolbar.add(priorityComboBox);
        
        JButton applyFilterButton = new JButton("Применить фильтры");
        JButton resetFilterButton = new JButton("Сбросить");
        
        filterToolbar.add(Box.createHorizontalStrut(10));
        filterToolbar.add(applyFilterButton);
        filterToolbar.add(resetFilterButton);

        topPanel.add(actionToolbar);
        topPanel.add(filterToolbar);

        add(topPanel, BorderLayout.NORTH);

        // Оброботчики событий
        addButton.addActionListener(e -> {
            TaskDialog dialog = new TaskDialog(this, "Новая задача", null);
            dialog.setVisible(true);
            if (dialog.isConfirmed()) {
                Task nt = dialog.getTask();
                taskManager.addTask(nt.getTitle(), nt.getDescription(), nt.getPriority(), nt.getStatus(), nt.getDueDate());
                applyFilters(); // Обновляем с учетом фильтров
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
                    applyFilters();
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
                    applyFilters();
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
                    resetFilters(); // Сбрасываем фильтры при загрузке новых данных
                    JOptionPane.showMessageDialog(this, "Задачи успешно загружены из файла:\n" + file.getName());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Ошибка загрузки: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        applyFilterButton.addActionListener(e -> applyFilters());
        
        resetFilterButton.addActionListener(e -> resetFilters());
    }

    private void applyFilters() {
        List<Task> result = taskManager.getAllTasks();

        // 1. Поиск по тексту (название / описание)
        String query = searchField.getText().trim().toLowerCase();
        if (!query.isEmpty()) {
            result = result.stream()
                    .filter(t -> t.getTitle().toLowerCase().contains(query) || 
                                 t.getDescription().toLowerCase().contains(query))
                    .collect(Collectors.toList());
        }

        // 2. Фильтрация по статусу
        int statusIndex = statusComboBox.getSelectedIndex();
        if (statusIndex > 0) { // 0 - "Все статусы"
            TaskStatus selectedStatus = TaskStatus.values()[statusIndex - 1];
            result = result.stream()
                    .filter(t -> t.getStatus() == selectedStatus)
                    .collect(Collectors.toList());
        }

        // 3. Фильтрация по приоритету
        int priorityIndex = priorityComboBox.getSelectedIndex();
        if (priorityIndex > 0) { // 0 - "Все приоритеты"
            Priority selectedPriority = Priority.values()[priorityIndex - 1];
            result = result.stream()
                    .filter(t -> t.getPriority() == selectedPriority)
                    .collect(Collectors.toList());
        }

        tableModel.setTasks(result);
    }
    
    private void resetFilters() {
        searchField.setText("");
        statusComboBox.setSelectedIndex(0);
        priorityComboBox.setSelectedIndex(0);
        refreshTable();
    }

    private void refreshTable() {
        tableModel.setTasks(taskManager.getAllTasks());
    }
}
