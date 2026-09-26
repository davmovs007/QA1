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
    private static final Color WINDOW_BG = new Color(246, 248, 252);
    private static final Color TEXT_PRIMARY = new Color(30, 41, 59);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Color PRIMARY = new Color(37, 99, 235);

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
        getContentPane().setBackground(WINDOW_BG);

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
        taskTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskTable.setRowHeight(40);
        taskTable.setShowHorizontalLines(true);
        taskTable.setShowVerticalLines(false);
        taskTable.setGridColor(BORDER);
        taskTable.setSelectionBackground(new Color(219, 234, 254));
        taskTable.setSelectionForeground(TEXT_PRIMARY);
        taskTable.setIntercellSpacing(new Dimension(0, 1));

        // Настройка заголовков таблицы
        JTableHeader header = taskTable.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(TEXT_MUTED);
        header.setPreferredSize(new Dimension(0, 42));
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
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 16, 16, 16),
                BorderFactory.createLineBorder(BORDER)
        ));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // ==================== ВЕРХНЯЯ ПАНЕЛЬ ====================
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setBorder(new EmptyBorder(18, 22, 12, 22));
        topContainer.setBackground(WINDOW_BG);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel titleLabel = new JLabel("Мои задачи");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel subtitleLabel = new JLabel("Планируйте день и держите все дела под контролем");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLabel.setForeground(TEXT_MUTED);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(3));
        titlePanel.add(subtitleLabel);
        titlePanel.add(Box.createVerticalStrut(9));

        JPanel titleAccent = new JPanel();
        titleAccent.setBackground(PRIMARY);
        titleAccent.setPreferredSize(new Dimension(46, 3));
        titleAccent.setMaximumSize(new Dimension(46, 3));
        titleAccent.setAlignmentX(Component.CENTER_ALIGNMENT);
        titlePanel.add(titleAccent);

        topContainer.add(titlePanel);
        topContainer.add(Box.createVerticalStrut(14));

        // 1. Панель действий
        JPanel actionToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        actionToolbar.setOpaque(false);

        // Всегда передаем Color.BLACK для цвета текста
        JButton addButton = createStyledButton("＋  Добавить", PRIMARY, Color.WHITE);
        JButton editButton = createStyledButton("✎  Редактировать", Color.WHITE, TEXT_PRIMARY);
        JButton deleteButton = createStyledButton("♲  Удалить", new Color(254, 242, 242), new Color(185, 28, 28));
        JButton saveButton = createStyledButton("↓  Сохранить", Color.WHITE, TEXT_PRIMARY);
        JButton loadButton = createStyledButton("↑  Загрузить", Color.WHITE, TEXT_PRIMARY);

        actionToolbar.add(addButton);
        actionToolbar.add(editButton);
        actionToolbar.add(deleteButton);
        actionToolbar.add(Box.createHorizontalStrut(10));
        actionToolbar.add(saveButton);
        actionToolbar.add(loadButton);

        // 2. Панель фильтрации
        JPanel filterToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        filterToolbar.setOpaque(false);
        filterToolbar.setBorder(new EmptyBorder(4, 0, 0, 0));

        JLabel searchLabel = new JLabel("Поиск");
        searchLabel.setForeground(TEXT_MUTED);
        filterToolbar.add(searchLabel);

        searchField = new JTextField(14);
        styleInput(searchField);
        filterToolbar.add(searchField);

        JLabel statusLabel = new JLabel("Статус:");
        statusLabel.setForeground(TEXT_MUTED);
        filterToolbar.add(statusLabel);

        statusComboBox = new JComboBox<>();
        statusComboBox.addItem("Все статусы");
        for (TaskStatus status : TaskStatus.values()) {
            statusComboBox.addItem(status.getDisplayName());
        }
        styleComboBox(statusComboBox);
        filterToolbar.add(statusComboBox);

        JLabel priorityLabel = new JLabel("Приоритет:");
        priorityLabel.setForeground(TEXT_MUTED);
        filterToolbar.add(priorityLabel);

        priorityComboBox = new JComboBox<>();
        priorityComboBox.addItem("Все приоритеты");
        for (Priority priority : Priority.values()) {
            priorityComboBox.addItem(priority.getDisplayName());
        }
        styleComboBox(priorityComboBox);
        filterToolbar.add(priorityComboBox);

        JButton applyFilterButton = createStyledButton("Применить", new Color(219, 234, 254), new Color(30, 64, 175));
        JButton resetFilterButton = createStyledButton("Сбросить", Color.WHITE, TEXT_PRIMARY);

        filterToolbar.add(applyFilterButton);
        filterToolbar.add(resetFilterButton);

        topContainer.add(actionToolbar);
        topContainer.add(filterToolbar);
        add(topContainer, BorderLayout.NORTH);

        // ==================== НИЖНЯЯ ПАНЕЛЬ СТАТУСА ====================
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(new EmptyBorder(10, 22, 10, 22));
        bottomPanel.setBackground(new Color(241, 245, 249));

        statusBarLabel = new JLabel("Всего задач: 0");
        statusBarLabel.setForeground(TEXT_MUTED);
        statusBarLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
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
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setMargin(new Insets(7, 14, 7, 14));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.equals(PRIMARY) ? PRIMARY : BORDER, 1, true),
                new EmptyBorder(3, 6, 3, 6)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(bg.equals(PRIMARY) ? new Color(29, 78, 216) : new Color(241, 245, 249));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bg);
            }
        });
        return button;
    }

    private void styleInput(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(6, 9, 6, 9)
        ));
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(new Font("SansSerif", Font.PLAIN, 13));
        comboBox.setForeground(TEXT_PRIMARY);
        comboBox.setBackground(Color.WHITE);
        comboBox.setBorder(BorderFactory.createLineBorder(BORDER, 1, true));
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

            c.setForeground(TEXT_PRIMARY);

            if (isSelected) {
                c.setBackground(new Color(219, 234, 254));
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
