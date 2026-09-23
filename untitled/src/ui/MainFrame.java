package ui;

import controller.TaskController;
import model.Priority;
import model.Task;
import model.TaskStatus;
import service.FileManager;

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

public class MainFrame extends JFrame {
    private final TaskController controller;
    private JTable taskTable;
    private TaskTableModel tableModel;

    // Filter components
    private JTextField searchField;
    private JComboBox<String> statusComboBox;
    private JComboBox<String> priorityComboBox;
    private JLabel statusBarLabel;

    public MainFrame(TaskController controller) {
        super("Менеджер задач");
        this.controller = controller;
        this.tableModel = new TaskTableModel();

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1000, 680);
        setMinimumSize(new Dimension(850, 500));
        setLocationRelativeTo(null);

        initComponents();
        setupWindowClosing();
    }

    private void setupWindowClosing() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controller.autoSaveTasks();
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
        taskTable.setShowHorizontalLines(true);
        taskTable.setShowVerticalLines(false);
        taskTable.setGridColor(new Color(230, 233, 238));
        taskTable.setSelectionBackground(new Color(224, 231, 255));
        taskTable.setSelectionForeground(Color.BLACK); // Устанавливаем черный цвет выделения

        // Настройка заголовков таблицы
        JTableHeader header = taskTable.getTableHeader();
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(Color.BLACK); // Чёрный цвет для заголовка
        header.setPreferredSize(new Dimension(0, 38));
        header.setReorderingAllowed(false);

        // Настройка ширины колонок
        taskTable.getColumnModel().getColumn(0).setPreferredWidth(70);  // ID
        taskTable.getColumnModel().getColumn(1).setPreferredWidth(260); // Название
        taskTable.getColumnModel().getColumn(2).setPreferredWidth(110); // Приоритет
        taskTable.getColumnModel().getColumn(3).setPreferredWidth(120); // Статус
        taskTable.getColumnModel().getColumn(4).setPreferredWidth(130); // Создана
        taskTable.getColumnModel().getColumn(5).setPreferredWidth(130); // Дедлайн

        // Применяем красивый стилизованный рендерер ячеек (иконки, черный текст)
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

        // Всегда передаем Color.BLACK для цвета текста
        JButton addButton = createStyledButton("➕ Добавить", new Color(200, 230, 255), Color.BLACK);
        JButton editButton = createStyledButton("✏️ Редактировать", new Color(241, 245, 249), Color.BLACK);
        JButton deleteButton = createStyledButton("🗑️ Удалить", new Color(254, 226, 226), Color.BLACK);
        JButton saveButton = createStyledButton("💾 Сохранить", new Color(241, 245, 249), Color.BLACK);
        JButton loadButton = createStyledButton("📂 Загрузить", new Color(241, 245, 249), Color.BLACK);

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
        searchLabel.setForeground(Color.BLACK);
        filterToolbar.add(searchLabel);

        searchField = new JTextField(14);
        searchField.setMargin(new Insets(4, 6, 4, 6));
        searchField.setForeground(Color.BLACK);
        filterToolbar.add(searchField);

        JLabel statusLabel = new JLabel("Статус:");
        statusLabel.setForeground(Color.BLACK);
        filterToolbar.add(statusLabel);

        statusComboBox = new JComboBox<>();
        statusComboBox.addItem("Все статусы");
        for (TaskStatus status : TaskStatus.values()) {
            statusComboBox.addItem(status.getDisplayName());
        }
        statusComboBox.setForeground(Color.BLACK);
        filterToolbar.add(statusComboBox);

        JLabel priorityLabel = new JLabel("Приоритет:");
        priorityLabel.setForeground(Color.BLACK);
        filterToolbar.add(priorityLabel);

        priorityComboBox = new JComboBox<>();
        priorityComboBox.addItem("Все приоритеты");
        for (Priority priority : Priority.values()) {
            priorityComboBox.addItem(priority.getDisplayName());
        }
        priorityComboBox.setForeground(Color.BLACK);
        filterToolbar.add(priorityComboBox);

        JButton applyFilterButton = createStyledButton("⚡ Применить", new Color(241, 245, 249), Color.BLACK);
        JButton resetFilterButton = createStyledButton("🔄 Сбросить", new Color(241, 245, 249), Color.BLACK);

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
        statusBarLabel.setForeground(Color.BLACK); // Устанавливаем черный цвет для строки состояния
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
        button.setBackground(bg);
        button.setForeground(fg); // fg is ALWAYS Color.BLACK now based on calls
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
                    controller.addTask(nt);
                    applyFilters();
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ошибка при добавлении задачи:\n" + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editSelectedTask() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow >= 0) {
            try {
                Task selectedTask = tableModel.getTaskAt(selectedRow);
                if (selectedTask == null) return;
                
                TaskDialog dialog = new TaskDialog(this, "Редактировать задачу", selectedTask);
                dialog.setVisible(true);
                if (dialog.isConfirmed()) {
                    Task updated = dialog.getTask();
                    if (updated != null) {
                        controller.updateTask(selectedTask.getId(), updated);
                        applyFilters();
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Ошибка при редактировании задачи:\n" + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Выберите задачу в таблице для редактирования.", "Предупреждение", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteSelectedTask() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow >= 0) {
            try {
                Task selectedTask = tableModel.getTaskAt(selectedRow);
                if (selectedTask == null) return;

                int confirm = JOptionPane.showConfirmDialog(this,
                        "Вы действительно хотите удалить задачу \"" + selectedTask.getTitle() + "\"?",
                        "Подтверждение удаления", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                if (confirm == JOptionPane.YES_OPTION) {
                    controller.deleteTask(selectedTask.getId());
                    applyFilters();
                    JOptionPane.showMessageDialog(this, "Задача успешно удалена.", "Информация", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Ошибка при удалении задачи:\n" + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Выберите задачу в таблице для удаления.", "Предупреждение", JOptionPane.WARNING_MESSAGE);
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
                if (file == null) return;
                controller.saveToFile(file);
                JOptionPane.showMessageDialog(this, "Задачи сохранены:\n" + file.getAbsolutePath(), "Успешно", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ошибка при сохранении:\n" + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTasksFromFile() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Загрузить задачи из файла");
            fileChooser.setFileFilter(new FileNameExtensionFilter("CSV / TXT файлы (*.csv, *.txt)", "csv", "txt"));

            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (file == null || !file.exists()) return;
                controller.loadFromFile(file);
                resetFilters();
                JOptionPane.showMessageDialog(this, "Задачи успешно загружены.", "Успех", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ошибка при загрузке:\n" + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void applyFilters() {
        String query = searchField.getText();
        int statusIndex = statusComboBox.getSelectedIndex();
        int priorityIndex = priorityComboBox.getSelectedIndex();

        List<Task> filteredTasks = controller.getFilteredTasks(query, statusIndex, priorityIndex);
        tableModel.setTasks(filteredTasks);
        
        statusBarLabel.setText("Показано: " + filteredTasks.size() + " задач");
    }

    private void resetFilters() {
        searchField.setText("");
        statusComboBox.setSelectedIndex(0);
        priorityComboBox.setSelectedIndex(0);
        applyFilters();
    }

    /**
     * Стилизованный рендерер ячеек таблицы с черным текстом везде.
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

            // Фокус и цвет текста ВСЕГДА черный, как просил пользователь!
            c.setForeground(Color.BLACK); 

            if (isSelected) {
                c.setBackground(new Color(224, 231, 255));
                return c;
            }
            
            Color defaultBg = (row % 2 == 0) ? Color.WHITE : new Color(248, 250, 252);
            c.setBackground(defaultBg);

            // Колонка 2: Приоритет (Только фон, без изменения цвета текста)
            if (column == 2) {
                if (task.getPriority() == Priority.HIGH) {
                    c.setBackground(new Color(254, 226, 226)); // Мягкий красный фон
                } else if (task.getPriority() == Priority.MEDIUM) {
                    c.setBackground(new Color(254, 243, 199)); // Мягкий желтый фон
                } else if (task.getPriority() == Priority.LOW) {
                    c.setBackground(new Color(209, 250, 229)); // Мягкий зеленый фон
                }
            }
            // Колонка 3: Статус (Только фон)
            else if (column == 3) {
                if (task.getStatus() == TaskStatus.DONE) {
                    c.setBackground(new Color(220, 252, 231)); 
                } else if (task.getStatus() == TaskStatus.IN_PROGRESS) {
                    c.setBackground(new Color(224, 242, 254)); 
                } else {
                    c.setBackground(new Color(241, 245, 249)); 
                }
            }

            return c;
        }
    }
}
