package ui;

import model.Priority;
import model.Task;
import model.TaskStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
        setSize(480, 440);
        setLocationRelativeTo(owner);
        setResizable(false);

        // Основная панель с отступами
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(20, 24, 15, 24));
        mainPanel.setBackground(new Color(250, 250, 252));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 13);

        // 1. Поле Название
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        JLabel titleLabel = new JLabel("Название *:");
        titleLabel.setFont(labelFont);
        mainPanel.add(titleLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        titleField = new JTextField();
        titleField.setFont(fieldFont);
        titleField.setMargin(new Insets(5, 8, 5, 8));
        mainPanel.add(titleField, gbc);

        // 2. Поле Описание
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel descLabel = new JLabel("Описание:");
        descLabel.setFont(labelFont);
        mainPanel.add(descLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        descriptionArea = new JTextArea(4, 20);
        descriptionArea.setFont(fieldFont);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setMargin(new Insets(6, 8, 6, 8));
        JScrollPane descScrollPane = new JScrollPane(descriptionArea);
        descScrollPane.setBorder(BorderFactory.createLineBorder(new Color(210, 214, 220)));
        mainPanel.add(descScrollPane, gbc);

        // 3. Поле Приоритет
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        JLabel priorityLabel = new JLabel("Приоритет:");
        priorityLabel.setFont(labelFont);
        mainPanel.add(priorityLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        priorityComboBox = new JComboBox<>(Priority.values());
        priorityComboBox.setFont(fieldFont);
        priorityComboBox.setPreferredSize(new Dimension(200, 32));
        mainPanel.add(priorityComboBox, gbc);

        // 4. Поле Статус
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.3;
        JLabel statusLabel = new JLabel("Статус:");
        statusLabel.setFont(labelFont);
        mainPanel.add(statusLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        statusComboBox = new JComboBox<>(TaskStatus.values());
        statusComboBox.setFont(fieldFont);
        statusComboBox.setPreferredSize(new Dimension(200, 32));
        mainPanel.add(statusComboBox, gbc);

        // 5. Поле Дедлайн
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.3;
        JLabel dueDateLabel = new JLabel("Дедлайн:");
        dueDateLabel.setFont(labelFont);
        mainPanel.add(dueDateLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        dueDateField = new JTextField();
        dueDateField.setFont(fieldFont);
        dueDateField.setMargin(new Insets(5, 8, 5, 8));
        dueDateField.setToolTipText("Формат: дд.ММ.гггг ЧЧ:мм (например: 31.12.2025 18:00)");
        mainPanel.add(dueDateField, gbc);

        // Подсказка формата дедлайна
        gbc.gridx = 1;
        gbc.gridy = 5;
        JLabel hintLabel = new JLabel("Формат: дд.ММ.гггг ЧЧ:мм (например, 31.12.2025 18:00)");
        hintLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hintLabel.setForeground(new Color(108, 117, 125));
        mainPanel.add(hintLabel, gbc);

        // Заполнение при редактировании
        if (taskToEdit != null) {
            titleField.setText(taskToEdit.getTitle());
            descriptionArea.setText(taskToEdit.getDescription());
            priorityComboBox.setSelectedItem(taskToEdit.getPriority());
            statusComboBox.setSelectedItem(taskToEdit.getStatus());
            dueDateField.setText(taskToEdit.getFormattedDueDate().equals("Без срока") ? "" : taskToEdit.getFormattedDueDate());
        }

        add(mainPanel, BorderLayout.CENTER);

        // Панель кнопок
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        buttonsPanel.setBackground(new Color(242, 244, 247));
        buttonsPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(222, 226, 230)));

        JButton cancelButton = new JButton("Отмена");
        cancelButton.setFont(fieldFont);
        cancelButton.setPreferredSize(new Dimension(100, 34));

        JButton saveButton = new JButton("Сохранить");
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveButton.setBackground(new Color(37, 99, 235));
        saveButton.setForeground(Color.BLACK);
        saveButton.setFocusPainted(false);
        saveButton.setPreferredSize(new Dimension(110, 34));

        saveButton.addActionListener(e -> onSave());
        cancelButton.addActionListener(e -> dispose());

        buttonsPanel.add(cancelButton);
        buttonsPanel.add(saveButton);
        add(buttonsPanel, BorderLayout.SOUTH);
    }

    private void onSave() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Название задачи не может быть пустым!\nПожалуйста, укажите название.",
                    "Ошибка валидации",
                    JOptionPane.WARNING_MESSAGE
            );
            titleField.requestFocus();
            return;
        }

        LocalDateTime dueDate = null;
        String dueDateText = dueDateField.getText().trim();
        if (!dueDateText.isEmpty()) {
            try {
                dueDate = LocalDateTime.parse(dueDateText, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(
                        this,
                        "Неверный формат даты дедлайна!\nИспользуйте: дд.ММ.гггг ЧЧ:мм\nНапример: 31.12.2025 18:00",
                        "Некорректная дата",
                        JOptionPane.ERROR_MESSAGE
                );
                dueDateField.requestFocus();
                return;
            }
        }

        try {
            resultTask = new Task(
                    "",
                    title,
                    descriptionArea.getText().trim(),
                    (Priority) priorityComboBox.getSelectedItem(),
                    (TaskStatus) statusComboBox.getSelectedItem(),
                    dueDate
            );

            isConfirmed = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Ошибка при сохранении задачи:\n" + ex.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }

    public Task getTask() {
        return resultTask;
    }
}
