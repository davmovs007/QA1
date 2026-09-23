package ui;

import model.Task;
import javax.swing.table.AbstractTableModel;
import java.util.List;

public class TaskTableModel extends AbstractTableModel {
    private final String[] columnNames = {"ID", "Название", "Приоритет", "Статус", "Создана", "Дедлайн"};
    private List<Task> tasks;

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        fireTableDataChanged();
    }

    public Task getTaskAt(int rowIndex) {
        if (tasks != null && rowIndex >= 0 && rowIndex < tasks.size()) {
            return tasks.get(rowIndex);
        }
        return null;
    }

    @Override
    public int getRowCount() {
        return tasks == null ? 0 : tasks.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Task task = getTaskAt(rowIndex);
        if (task == null) {
            return null;
        }
        switch (columnIndex) {
            case 0: return task.getId();
            case 1: return task.getTitle();
            case 2: return task.getPriority() != null ? task.getPriority().getDisplayName() : "";
            case 3: return task.getStatus() != null ? task.getStatus().getDisplayName() : "";
            case 4: return task.getFormattedCreatedAt();
            case 5: return task.getFormattedDueDate();
            default: return null;
        }
    }
}
