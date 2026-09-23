package ui;

import model.Priority;
import model.Task;
import model.TaskStatus;
import service.FileManager;
import service.TaskManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class MainFrame extends JFrame {
    private static final String AUTO_SAVE_FILE = FileManager.DEFAULT_FILE_NAME;

    private final TaskManager taskManager;
    private JTable taskTable;
    private TaskTableModel tableModel;

    // Filter components
    private JTextField searchField;
    private JComboBox<String> statusComboBox;
    private JComboBox<String> priorityComboBox;
    private JLabel statusBarLabel;

    public MainFrame() {
        super("Менеджер задач");
        taskManager = new TaskManager();
        tableModel = new TaskTableModel();

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1000, 680);
        setMinimumSize(new Dimension(850, 500));
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
                JOptionPane.showMessageDialog(
                        this,
                        "Не удалось загрузить автоматически сохраненные задачи:\n" + ex.getMessage(),
                        "Предупреждение при автозагрузке",
                        JOptionPane.WARNING_MESSAGE
                );
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
                autoSaveTasks();
                dispose();
                System.exit(0);
            }
        });
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // ==================== ТАБЛИЦА ====================
        taskTable = new JTable(tableModel);
        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskTable.setRowHeight(34);
        taskTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        taskTable.setShowHorizontalLines(true);
        taskTable.setShowVerticalLines(false);
        taskTable.setGridColor(new Color(230, 233, 238));
        taskTable.setSelectionBackground(new Color(224, 231, 255));
        taskTable.setSelectionForeground(new Color(30, 27, 75));

        // Настройка заголовков таблицы
        JTableHeader header = taskTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(new Color(51, 65, 85));
        header.setPreferredSize(new Dimension(0, 38));
        header.setReorderingAllowed(false);

        // Настройка ширины колонок
        taskTable.getColumnModel().getColumn(0).setPreferredWidth(70);  // ID
        taskTable.getColumnModel().getColumn(1).setPreferredWidth(260); // Название
        taskTable.getColumnModel().getColumn(2).setPreferredWidth(110); // Приоритет
        taskTable.getColumnModel().getColumn(3).setPreferredWidth(120); // Статус
        taskTable.getColumnModel().getColumn(4).setPreferredWidth(130); // Создана
        taskTable.getColumnModel().getColumn(5).setPreferredWidth(130); // Дедлайн

        // Применяем красивый стилизованный рендерер ячеек
        taskTable.setDefaultRenderer(Object.class, new TaskTableCellRenderer());

        // Двойной клик по строке для быстрого редактирования
        taskTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && taskTable.getSelectedRow() >= 0) {
                    editSelectedTask();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(taskTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // ==================== ВЕРХНЯЯ ПАНЕЛЬ ====================
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setBorder(new EmptyBorder(12, 12, 8, 12));
        topContainer.setBackground(new Color(248, 250, 252));

        // 1. Панель действий
        JPanel actionToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        actionToolbar.setOpaque(false);

        JButton addButton = createStyledButton("➕ Добавить", new Color(37, 99, 235), Color.WHITE);
        JButton editButton = createStyledButton("✏️ Редактировать", new Color(241, 245, 249), new Color(15, 23, 42));
        JButton deleteButton = createStyledButton("🗑️ Удалить", new Color(254, 226, 226), new Color(185, 28, 28));
        JButton saveButton = createStyledButton("💾 Сохранить", new Color(241, 245, 249), new Color(15, 23, 42));
        JButton loadButton = createStyledButton("📂 Загрузить", new Color(241, 245, 249), new Color(15, 23, 42));

        actionToolbar.add(addButton);
        actionToolbar.add(editButton);
        actionToolbar.add(deleteButton);
        actionToolbar.add(Box.createHorizontalStrut(10));
        actionToolbar.add(saveButton);
        actionToolbar.add(loadButton);

        // 2. Панель фильтрации
        JPanel filterToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        filterToolbar.setOpaque(false);
        filterToolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)),
                new EmptyBorder(6, 0, 0, 0)
        ));

        JLabel searchLabel = new JLabel("🔍 Поиск:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        filterToolbar.add(searchLabel);

        searchField = new JTextField(14);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setMargin(new Insets(4, 6, 4, 6));
        filterToolbar.add(searchField);

        JLabel statusLabel = new JLabel("Статус:");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        filterToolbar.add(statusLabel);

        statusComboBox = new JComboBox<>();
        statusComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusComboBox.addItem("Все статусы");
        for (TaskStatus status : TaskStatus.values()) {
            statusComboBox.addItem(status.getDisplayName());
        }
        filterToolbar.add(statusComboBox);

        JLabel priorityLabel = new JLabel("Приоритет:");
        priorityLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        filterToolbar.add(priorityLabel);

        priorityComboBox = new JComboBox<>();
        priorityComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        priorityComboBox.addItem("Все приоритеты");
        for (Priority priority : Priority.values()) {
            priorityComboBox.addItem(priority.getDisplayName());
        }
        filterToolbar.add(priorityComboBox);

        JButton applyFilterButton = createStyledButton("⚡ Применить", new Color(241, 245, 249), new Color(15, 23, 42));
        JButton resetFilterButton = createStyledButton("🔄 Сбросить", new Color(241, 245, 249), new Color(15, 23, 42));

        filterToolbar.add(applyFilterButton);
        filterToolbar.add(resetFilterButton);

        topContainer.add(actionToolbar);
        topContainer.add(filterToolbar);
        add(topContainer, BorderLayout.NORTH);

        // ==================== НИЖНЯЯ ПАНЕЛЬ СТАТУСА ====================
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(new EmptyBorder(8, 16, 8, 16));
        bottomPanel.setBackground(new Color(241, 245, 249));

        statusBarLabel = new JLabel("Всего задач: 0");
        statusBarLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusBarLabel.setForeground(new Color(71, 85, 105));
        bottomPanel.add(statusBarLabel, BorderLayout.WEST);

        add(bottomPanel, BorderLayout.SOUTH);

        // ==================== ОБРАБОТЧИКИ ====================
        addButton.addActionListener(e -> addNewTask());
        editButton.addActionListener(e -> editSelectedTask());
        deleteButton.addActionListener(e -> deleteSelectedTask());
        saveButton.addActionListener(e -> saveTasksToFile());
        loadButton.addActionListener(e -> loadTasksFromFile());
        applyFilterButton.addActionListener(e -> applyFilters());
        resetFilterButton.addActionListener(e -> resetFilters());
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFocusPainted(false);
        button.setMargin(new Insets(6, 12, 6, 12));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(4, 8, 4, 8)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void addNewTask() {
        try {
            TaskDialog dialog = new TaskDialog(this, "Новая задача", null);
            dialog.setVisible(true);
            if (dialog.isConfirmed()) {
                Task nt = dialog.getTask();
                if (nt != null) {
                    taskManager.addTask(nt.getTitle(), nt.getDescription(), nt.getPriority(), nt.getStatus(), nt.getDueDate());
                    applyFilters();
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Ошибка при добавлении задачи:\n" + ex.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void editSelectedTask() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow >= 0) {
            try {
                Task selectedTask = tableModel.getTaskAt(selectedRow);
                if (selectedTask == null) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Не удалось получить выбранную задачу.",
                            "Предупреждение",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }
                TaskDialog dialog = new TaskDialog(this, "Редактировать задачу", selectedTask);
                dialog.setVisible(true);
                if (dialog.isConfirmed()) {
                    Task updated = dialog.getTask();
                    if (updated != null) {
                        boolean success = taskManager.updateTask(
                                selectedTask.getId(),
                                updated.getTitle(),
                                updated.getDescription(),
                                updated.getPriority(),
                                updated.getStatus(),
                                updated.getDueDate()
                        );
                        if (success) {
                            applyFilters();
                        } else {
                            JOptionPane.showMessageDialog(
                                    this,
                                    "Задача с указанным ID не найдена.",
                                    "Ошибка обновления",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Ошибка при редактировании задачи:\n" + ex.getMessage(),
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Выберите задачу в таблице для редактирования.",
                    "Предупреждение",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void deleteSelectedTask() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow >= 0) {
            try {
                Task selectedTask = tableModel.getTaskAt(selectedRow);
                if (selectedTask == null) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Не удалось получить выбранную задачу.",
                            "Предупреждение",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        "Вы действительно хотите удалить задачу \"" + selectedTask.getTitle() + "\"?",
                        "Подтверждение удаления",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    boolean deleted = taskManager.deleteTask(selectedTask.getId());
                    if (deleted) {
                        applyFilters();
                        JOptionPane.showMessageDialog(
                                this,
                                "Задача успешно удалена.",
                                "Информация",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    } else {
                        JOptionPane.showMessageDialog(
                                this,
                                "Не удалось удалить задачу (возможно, она уже была удалена).",
                                "Ошибка",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Ошибка при удалении задачи:\n" + ex.getMessage(),
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Выберите задачу в таблице для удаления.",
                    "Предупреждение",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void saveTasksToFile() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Сохранить задачи в файл");
            fileChooser.setFileFilter(new FileNameExtensionFilter("CSV / TXT файлы (*.csv, *.txt)", "csv", "txt"));
            fileChooser.setSelectedFile(new File("tasks.csv"));

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (file == null) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Файл не выбран.",
                            "Предупреждение",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }
                if (!file.getName().toLowerCase().endsWith(".csv") && !file.getName().toLowerCase().endsWith(".txt")) {
                    file = new File(file.getAbsolutePath() + ".csv");
                }
                taskManager.saveToCsv(file);
                JOptionPane.showMessageDialog(
                        this,
                        "Задачи успешно сохранены в файл:\n" + file.getAbsolutePath(),
                        "Успешное сохранение",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Ошибка при сохранении в файл:\n" + ex.getMessage(),
                    "Ошибка сохранения",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void loadTasksFromFile() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Загрузить задачи из файла");
            fileChooser.setFileFilter(new FileNameExtensionFilter("CSV / TXT файлы (*.csv, *.txt)", "csv", "txt"));

            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (file == null || !file.exists() || !file.isFile()) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Выбранный файл не существует или недоступен для чтения.",
                            "Ошибка файла",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
                taskManager.loadFromCsv(file);
                resetFilters();
                JOptionPane.showMessageDialog(
                        this,
                        "Задачи успешно загружены из файла:\n" + file.getAbsolutePath(),
                        "Успешная загрузка",
                        JOptionPane.INFORMATION_MESSAGE
                    );
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Ошибка при загрузке из файла:\n" + ex.getMessage(),
                    "Ошибка загрузки",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void applyFilters() {
        List<Task> result = taskManager.getAllTasks();

        // 1. Поиск по тексту (название / описание)
        String query = searchField.getText().trim().toLowerCase();
        if (!query.isEmpty()) {
            result = result.stream()
                    .filter(t -> (t.getTitle() != null && t.getTitle().toLowerCase().contains(query)) ||
                                 (t.getDescription() != null && t.getDescription().toLowerCase().contains(query)))
                    .collect(Collectors.toList());
        }

        // 2. Фильтрация по статусу
        int statusIndex = statusComboBox.getSelectedIndex();
        if (statusIndex > 0) {
            TaskStatus selectedStatus = TaskStatus.values()[statusIndex - 1];
            result = result.stream()
                    .filter(t -> t.getStatus() == selectedStatus)
                    .collect(Collectors.toList());
        }

        // 3. Фильтрация по приоритету
        int priorityIndex = priorityComboBox.getSelectedIndex();
        if (priorityIndex > 0) {
            Priority selectedPriority = Priority.values()[priorityIndex - 1];
            result = result.stream()
                    .filter(t -> t.getPriority() == selectedPriority)
                    .collect(Collectors.toList());
        }

        tableModel.setTasks(result);
        updateStatusBar(result.size());
    }

    private void resetFilters() {
        searchField.setText("");
        statusComboBox.setSelectedIndex(0);
        priorityComboBox.setSelectedIndex(0);
        refreshTable();
    }

    private void refreshTable() {
        List<Task> all = taskManager.getAllTasks();
        tableModel.setTasks(all);
        updateStatusBar(all.size());
    }

    private void updateStatusBar(int displayedCount) {
        int total = taskManager.getTaskCount();
        long highCount = taskManager.getAllTasks().stream().filter(t -> t.getPriority() == Priority.HIGH).count();
        long doneCount = taskManager.getAllTasks().stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();
        statusBarLabel.setText(String.format("Показано: %d из %d задач  |  🔴 Высокий приоритет: %d  |  ✅ Выполнено: %d",
                displayedCount, total, highCount, doneCount));
    }

    /**
     * Стилизованный рендерер ячеек таблицы с цветовой индикацией приоритетов и статусов.
     */
    private class TaskTableCellRenderer extends DefaultTableCellRenderer {
        private final EmptyBorder padding = new EmptyBorder(4, 10, 4, 10);

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(padding);

            Task task = tableModel.getTaskAt(row);
            if (task == null) {
                return c;
            }

            if (isSelected) {
                c.setBackground(new Color(224, 231, 255));
                c.setForeground(new Color(30, 27, 75));
                return c;
            }

            // Чередование фона четных и нечетных строк по умолчанию
            Color defaultBg = (row % 2 == 0) ? Color.WHITE : new Color(248, 250, 252);
            c.setBackground(defaultBg);
            c.setForeground(new Color(15, 23, 42));

            // Колонка 2: Приоритет (красный фон для высокого приоритета, желтый для среднего, зеленый для низкого)
            if (column == 2) {
                if (task.getPriority() == Priority.HIGH) {
                    c.setBackground(new Color(254, 226, 226)); // Мягкий красный фон
                    c.setForeground(new Color(185, 28, 28));   // Темно-красный текст
                    setFont(getFont().deriveFont(Font.BOLD));
                } else if (task.getPriority() == Priority.MEDIUM) {
                    c.setBackground(new Color(254, 243, 199)); // Мягкий желтый фон
                    c.setForeground(new Color(180, 83, 9));    // Темно-желтый текст
                    setFont(getFont().deriveFont(Font.PLAIN));
                } else if (task.getPriority() == Priority.LOW) {
                    c.setBackground(new Color(209, 250, 229)); // Мягкий зеленый фон
                    c.setForeground(new Color(4, 120, 87));    // Темно-зеленый текст
                    setFont(getFont().deriveFont(Font.PLAIN));
                }
            }
            // Колонка 3: Статус
            else if (column == 3) {
                if (task.getStatus() == TaskStatus.DONE) {
                    c.setBackground(new Color(220, 252, 231)); // Светло-зеленый
                    c.setForeground(new Color(22, 101, 52));
                } else if (task.getStatus() == TaskStatus.IN_PROGRESS) {
                    c.setBackground(new Color(224, 242, 254)); // Светло-голубой
                    c.setForeground(new Color(3, 105, 161));
                } else {
                    c.setBackground(new Color(241, 245, 249)); // Светло-серый
                    c.setForeground(new Color(71, 85, 105));
                }
            }

            return c;
        }
    }
}
