package todolist;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class TaskTableModel extends AbstractTableModel {
    private final List<Task> taskList;
    // MODIFIED: Add new column names
    private final String[] columnNames = {"Title", "Description", "Category", "Due Date", "Priority", "Status", "Assigned To", "Progress (%)"};

    public TaskTableModel(List<Task> taskList) {
        this.taskList = taskList;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public int getRowCount() {
        return taskList.size();
    }
    
    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Task task = taskList.get(rowIndex);
        switch (columnIndex) {
            case 0: return task.getTitle();
            case 1: return task.getDescription();
            case 2: return task.getCategory().getName();
            case 3: return task.getDueDate();
            case 4: return task.getPriority();
            case 5: return task.getStatus();
            // NEW: Cases for the new columns
            case 6:
                return (task.getAssignedTo() != null) ? task.getAssignedTo().getName() : "Unassigned";
            case 7:
                return task.getProgressPercent();
            default:
                return null;
        }
    }
}