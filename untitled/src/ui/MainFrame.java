package ui;

import controller.TaskController;
import model.Priority;
import model.Task;
import model.TaskStatus;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
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

    public MainFrame(TaskController controller) {
        super("Менеджер задач");
        this.controller = controller;
        this.tableModel = new TaskTableModel();

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(950, 650);
        setLocationRelativeTo(null);

        initComponents();
        setupWindowClosing();
    }

    private void setupWindowClosing() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Делегируем автосохранение контроллеру
                controller.autoSaveTasks();
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

        // Обработчики событий (UI делегирует логику Контроллеру)
        addButton.addActionListener(e -> {
            TaskDialog dialog = new TaskDialog(this, "Новая задача", null);
            dialog.setVisible(true);
            if (dialog.isConfirmed()) {
                controller.addTask(dialog.getTask());
                applyFilters();
            }
        });

        editButton.addActionListener(e -> {
            int selectedRow = taskTable.getSelectedRow();
            if (selectedRow >= 0) {
                Task selectedTask = tableModel.getTaskAt(selectedRow);
                TaskDialog dialog = new TaskDialog(this, "Редактировать задачу", selectedTask);
                dialog.setVisible(true);
                if (dialog.isConfirmed()) {
                    controller.updateTask(selectedTask.getId(), dialog.getTask());
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
                    controller.deleteTask(selectedTask.getId());
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
                    controller.saveToFile(fileChooser.getSelectedFile());
                    JOptionPane.showMessageDialog(this, "Задачи успешно сохранены!");
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
                    controller.loadFromFile(fileChooser.getSelectedFile());
                    resetFilters();
                    JOptionPane.showMessageDialog(this, "Задачи успешно загружены!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Ошибка загрузки: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        applyFilterButton.addActionListener(e -> applyFilters());
        resetFilterButton.addActionListener(e -> resetFilters());
    }

    public void applyFilters() {
        String query = searchField.getText();
        int statusIndex = statusComboBox.getSelectedIndex();
        int priorityIndex = priorityComboBox.getSelectedIndex();

        // Запрашиваем отфильтрованные данные у контроллера, а не фильтруем сами
        List<Task> filteredTasks = controller.getFilteredTasks(query, statusIndex, priorityIndex);
        tableModel.setTasks(filteredTasks);
    }
    
    private void resetFilters() {
        searchField.setText("");
        statusComboBox.setSelectedIndex(0);
        priorityComboBox.setSelectedIndex(0);
        applyFilters();
    }
}
