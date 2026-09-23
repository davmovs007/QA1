package ui;

import model.Priority;
import model.Task;
import model.TaskStatus;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TaskDialog extends JDialog {
    private JTextField titleField;
    private JTextArea descriptionArea;
    private JComboBox<Priority> priorityComboBox;
    private JComboBox<TaskStatus> statusComboBox;
    private JTextField dueDateField;
    private boolean isConfirmed = false;
    private Task resultTask = null;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public TaskDialog(Frame owner, String title, Task taskToEdit) {
        super(owner, title, true);
        setLayout(new BorderLayout());
        setSize(400, 350);
        setLocationRelativeTo(owner);

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        formPanel.add(new JLabel("Название:"));
        titleField = new JTextField();
        formPanel.add(titleField);

        formPanel.add(new JLabel("Описание:"));
        descriptionArea = new JTextArea(3, 20);
        formPanel.add(new JScrollPane(descriptionArea));

        formPanel.add(new JLabel("Приоритет:"));
        priorityComboBox = new JComboBox<>(Priority.values());
        formPanel.add(priorityComboBox);

        formPanel.add(new JLabel("Статус:"));
        statusComboBox = new JComboBox<>(TaskStatus.values());
        formPanel.add(statusComboBox);

        formPanel.add(new JLabel("Дедлайн (дд.ММ.гггг ЧЧ:мм):"));
        dueDateField = new JTextField();
        formPanel.add(dueDateField);

        if (taskToEdit != null) {
            titleField.setText(taskToEdit.getTitle());
            descriptionArea.setText(taskToEdit.getDescription());
            priorityComboBox.setSelectedItem(taskToEdit.getPriority());
            statusComboBox.setSelectedItem(taskToEdit.getStatus());
            dueDateField.setText(taskToEdit.getFormattedDueDate().equals("Без срока") ? "" : taskToEdit.getFormattedDueDate());
        }

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel();
        JButton saveButton = new JButton("Сохранить");
        JButton cancelButton = new JButton("Отмена");

        saveButton.addActionListener(e -> onSave());
        cancelButton.addActionListener(e -> dispose());

        buttonsPanel.add(saveButton);
        buttonsPanel.add(cancelButton);
        add(buttonsPanel, BorderLayout.SOUTH);
    }

    private void onSave() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Название не может быть пустым!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalDateTime dueDate = null;
        String dueDateText = dueDateField.getText().trim();
        if (!dueDateText.isEmpty()) {
            try {
                dueDate = LocalDateTime.parse(dueDateText, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "Неверный формат даты! Используйте: дд.ММ.гггг ЧЧ:мм", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        resultTask = new Task(
                "", // ID will be assigned by TaskManager
                title,
                descriptionArea.getText().trim(),
                (Priority) priorityComboBox.getSelectedItem(),
                (TaskStatus) statusComboBox.getSelectedItem(),
                dueDate
        );

        isConfirmed = true;
        dispose();
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }

    public Task getTask() {
        return resultTask;
    }
}
