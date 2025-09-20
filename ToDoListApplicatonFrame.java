package todolist;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ToDoListApplicatonFrame extends JFrame {

    private final List<Task> taskList = new ArrayList<>();
    private final List<Category> categoryList = new ArrayList<>();
    private final List<GroupMember> memberList = new ArrayList<>();

    private TaskTableModel tableModel;
    private JTable taskTable;
    private TableRowSorter<TaskTableModel> sorter;
    private JComboBox<Category> categoryFilter;
    private JComboBox<Object> statusFilter; // Changed to JComboBox<Object>

    public ToDoListApplicatonFrame() {
        setTitle("Project To-Do List");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initializeData();
        initUI();
    }

    private void initializeData() {
        categoryList.addAll(Arrays.asList(new Category("GUI"), new Category("System Modeling"), new Category("Documentation"), new Category("Testing")));
        memberList.add(new GroupLeader("Dr. Smith (Leader)"));
        memberList.add(new GroupMember("Alice"));
        memberList.add(new GroupMember("Bob"));

        taskList.add(new Task("Design Main Frame", "Design layout using Swing.", LocalDate.now().plusDays(5), Priority.HIGH, categoryList.get(0), memberList.get(1)));
        taskList.add(new Task("Create Class Diagram", "Draw UML class diagram.", LocalDate.now().plusDays(2), Priority.HIGH, categoryList.get(1), memberList.get(2)));
        taskList.add(new Task("Write User Manual", "Document user features.", LocalDate.now().plusDays(10), Priority.MEDIUM, categoryList.get(2), memberList.get(1)));
        taskList.add(new Task("Implement Button Listeners", "Add listeners to UI buttons.", LocalDate.now().plusDays(7), Priority.MEDIUM, categoryList.get(0), null));

        taskList.get(0).setProgressPercent(50);
        taskList.get(0).setStatus(Status.IN_PROGRESS);
        taskList.get(2).setProgressPercent(100);
        taskList.get(2).setStatus(Status.COMPLETED);
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        

        JPanel controlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBorder(BorderFactory.createTitledBorder("Management"));
        JButton addButton = new JButton("Add Task");
        addButton.addActionListener(e -> showTaskDialog(null));
        JButton updateButton = new JButton("Update Task");
        updateButton.addActionListener(e -> updateSelectedTask());
        JButton deleteButton = new JButton("Delete Task");
        deleteButton.addActionListener(e -> deleteSelectedTask());
        JButton manageCategoriesButton = new JButton("Manage Categories");
        manageCategoriesButton.addActionListener(e -> showManageCategoriesDialog());
        JButton viewWorkloadButton = new JButton("View Workload");
        JButton manageMembersButton = new JButton("Manage Members");
        manageMembersButton.addActionListener(e -> showManageMembersDialog());
        buttonPanel.add(manageMembersButton);
        viewWorkloadButton.addActionListener(e -> showWorkloadDialog());
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(manageCategoriesButton);
        buttonPanel.add(viewWorkloadButton);
        gbc.gridx = 0; gbc.gridy = 0;
        controlPanel.add(buttonPanel, gbc);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filters"));
        categoryFilter = new JComboBox<>();
        
        
        // --- FIX 1: Update how the status filter is created ---
        statusFilter = new JComboBox<>();
        statusFilter.addItem("All");
        for (Status status : Status.values()) {
            statusFilter.addItem(status);
        }
        
        updateCategoryComboBoxes();
        filterPanel.add(new JLabel("Category:"));
        filterPanel.add(categoryFilter);
        filterPanel.add(new JLabel("Status:"));
        filterPanel.add(statusFilter);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.WEST;
        controlPanel.add(filterPanel, gbc);

        mainPanel.add(controlPanel, BorderLayout.NORTH);

        tableModel = new TaskTableModel(taskList);
        taskTable = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        taskTable.setRowSorter(sorter);
        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskTable.setRowHeight(25);
        taskTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        // Custom renderer to color rows
        taskTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    int modelRow = table.convertRowIndexToModel(row);
                    Task task = taskList.get(modelRow);

                    // THIS IS THE LINE THAT MAKES COMPLETED TASKS GREEN
                    if (task.getStatus() == Status.COMPLETED) {
                        c.setBackground(new Color(220, 255, 220)); // Light green
                    } 
                    // This part handles other colors, like for high priority
                    else if (task.getStatus() == Status.IN_PROGRESS) {
                         c.setBackground(new Color(255, 255, 220)); // Light yellow
                    }
                    else {
                        c.setBackground(table.getBackground()); // Default color
                    }
                }
                return c;
            }
        });
        mainPanel.add(new JScrollPane(taskTable), BorderLayout.CENTER);

        Action applyFilters = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { applyTableFilters(); }
        };
        categoryFilter.addActionListener(applyFilters);
        statusFilter.addActionListener(applyFilters);

        add(mainPanel);
    }

    private void applyTableFilters() {
        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        
        // Category Filter (Column 2)
        Object selectedCategory = categoryFilter.getSelectedItem();
        if (selectedCategory instanceof Category && !"All".equalsIgnoreCase(((Category) selectedCategory).getName())) {
            filters.add(RowFilter.regexFilter("^" + Pattern.quote(((Category) selectedCategory).getName()) + "$", 2));
        }

        // --- FIX 2: Update the status filter logic ---
        Object selectedStatusItem = statusFilter.getSelectedItem();
        if (selectedStatusItem instanceof Status) {
            String statusDisplayName = selectedStatusItem.toString();
            filters.add(RowFilter.regexFilter("^" + Pattern.quote(statusDisplayName) + "$", 5));
        }
        
        sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
    }

    private void updateSelectedTask() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelIndex = taskTable.convertRowIndexToModel(selectedRow);
            showTaskDialog(taskList.get(modelIndex));
        } else {
            JOptionPane.showMessageDialog(this, "Please select a task to update.", "No Task Selected", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    // Add this entire new method to your ToDoListApplicatonFrame class

private void showManageMembersDialog() {
    JDialog manageDialog = new JDialog(this, "Manage Group Members", true);
    manageDialog.setSize(350, 400);
    manageDialog.setLayout(new BorderLayout(10, 10));
    manageDialog.setLocationRelativeTo(this);

    // Create a list model to easily add/remove items from the display
    DefaultListModel<GroupMember> listModel = new DefaultListModel<>();
    memberList.forEach(listModel::addElement);

    JList<GroupMember> memberJList = new JList<>(listModel);
    memberJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    manageDialog.add(new JScrollPane(memberJList), BorderLayout.CENTER);

    // --- Create Buttons ---
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    JButton addButton = new JButton("Add Member...");
    JButton deleteButton = new JButton("Delete Selected");
    JButton closeButton = new JButton("Close");

    // --- "Add Member" Button Logic ---
    addButton.addActionListener(e -> {
        String name = JOptionPane.showInputDialog(manageDialog, "Enter new member's name:", "Add Member", JOptionPane.PLAIN_MESSAGE);
        if (name != null && !name.trim().isEmpty()) {
            GroupMember newMember = new GroupMember(name.trim());
            if (!memberList.contains(newMember)) {
                memberList.add(newMember); // Add to the main data list
                listModel.addElement(newMember); // Add to the visual list in the dialog
            } else {
                JOptionPane.showMessageDialog(manageDialog, "This member already exists.", "Duplicate Member", JOptionPane.WARNING_MESSAGE);
            }
        }
    });

    // --- "Delete Selected" Button Logic ---
    deleteButton.addActionListener(e -> {
        GroupMember selectedMember = memberJList.getSelectedValue();
        if (selectedMember == null) {
            JOptionPane.showMessageDialog(manageDialog, "Please select a member to delete.", "Nothing Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // IMPORTANT: Safety check to see if the member is assigned to any tasks
        boolean isMemberAssigned = taskList.stream()
                .anyMatch(task -> selectedMember.equals(task.getAssignedTo()));

        if (isMemberAssigned) {
            JOptionPane.showMessageDialog(manageDialog, "Cannot delete '" + selectedMember.getName() + "'.\nThey are assigned to one or more tasks.", "Deletion Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Prevent deleting the leader
        if (selectedMember instanceof GroupLeader) {
             JOptionPane.showMessageDialog(manageDialog, "Cannot delete the group leader.", "Deletion Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Confirmation dialog
        int choice = JOptionPane.showConfirmDialog(manageDialog, "Are you sure you want to delete '" + selectedMember.getName() + "'?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            memberList.remove(selectedMember); // Remove from the main data list
            listModel.removeElement(selectedMember); // Remove from the visual list
        }
    });

    // --- "Close" Button Logic ---
    closeButton.addActionListener(e -> manageDialog.dispose());

    buttonPanel.add(addButton);
    buttonPanel.add(deleteButton);
    buttonPanel.add(closeButton);
    manageDialog.add(buttonPanel, BorderLayout.SOUTH);
    manageDialog.setVisible(true);
}

    private void deleteSelectedTask() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelIndex = taskTable.convertRowIndexToModel(selectedRow);
            int choice = JOptionPane.showConfirmDialog(this, "Delete this task?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                taskList.remove(modelIndex);
                tableModel.fireTableDataChanged();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a task to delete.", "No Task Selected", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void showTaskDialog(Task taskToUpdate) {
        JDialog dialog = new JDialog(this, taskToUpdate == null ? "Add Task" : "Update Task", true);
        dialog.setSize(500, 600);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JTextField titleField = new JTextField(20);
        JTextArea descArea = new JTextArea(5, 20);
        JTextField dueDateField = new JTextField(10);
        JComboBox<Priority> priorityBox = new JComboBox<>(Priority.values());
        JComboBox<Category> categoryBox = new JComboBox<>(categoryList.toArray(new Category[0]));
        JComboBox<Status> statusBox = new JComboBox<>(Status.values());
        JComboBox<GroupMember> memberBox = new JComboBox<>(memberList.toArray(new GroupMember[0]));
        JSpinner progressSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));

        int y = 0;
        formPanel.add(new JLabel("Title:"), gbc(0, y)); formPanel.add(titleField, gbc(1, y++));
        formPanel.add(new JLabel("Description:"), gbc(0, y)); formPanel.add(new JScrollPane(descArea), gbc(1, y++, true));
        formPanel.add(new JLabel("Due Date (YYYY-MM-DD):"), gbc(0, y)); formPanel.add(dueDateField, gbc(1, y++));
        formPanel.add(new JLabel("Priority:"), gbc(0, y)); formPanel.add(priorityBox, gbc(1, y++));
        formPanel.add(new JLabel("Category:"), gbc(0, y)); formPanel.add(categoryBox, gbc(1, y++));
        formPanel.add(new JLabel("Assigned To:"), gbc(0, y)); formPanel.add(memberBox, gbc(1, y++));
        formPanel.add(new JLabel("Progress (%):"), gbc(0, y)); formPanel.add(progressSpinner, gbc(1, y++));

        if (taskToUpdate != null) {
            formPanel.add(new JLabel("Status:"), gbc(0, y)); formPanel.add(statusBox, gbc(1, y));
        }

        if (taskToUpdate != null) {
            titleField.setText(taskToUpdate.getTitle());
            descArea.setText(taskToUpdate.getDescription());
            dueDateField.setText(taskToUpdate.getDueDate().toString());
            priorityBox.setSelectedItem(taskToUpdate.getPriority());
            categoryBox.setSelectedItem(taskToUpdate.getCategory());
            statusBox.setSelectedItem(taskToUpdate.getStatus());
            memberBox.setSelectedItem(taskToUpdate.getAssignedTo());
            progressSpinner.setValue(taskToUpdate.getProgressPercent());
        } else {
            dueDateField.setText(LocalDate.now().toString());
            memberBox.setSelectedItem(null);
        }
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            try {
                String title = titleField.getText().trim();
                if (title.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Title cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                Task task = (taskToUpdate == null) ? new Task("", "", LocalDate.now(), Priority.LOW, categoryList.get(0), null) : taskToUpdate;
                task.setTitle(title);
                task.setDescription(descArea.getText().trim());
                task.setDueDate(LocalDate.parse(dueDateField.getText().trim()));
                task.setPriority((Priority) priorityBox.getSelectedItem());
                task.setCategory((Category) categoryBox.getSelectedItem());
                task.setAssignedTo((GroupMember) memberBox.getSelectedItem());
                task.setProgressPercent((Integer) progressSpinner.getValue());
                
                if (taskToUpdate == null) {
                    taskList.add(task);
                } else {
                    task.setStatus((Status) statusBox.getSelectedItem());
                }
                
                tableModel.fireTableDataChanged();
                dialog.dispose();
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid date format. Use YYYY-MM-DD.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(saveButton);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private GridBagConstraints gbc(int x, int y, boolean fillBoth) {
        GridBagConstraints g = gbc(x, y);
        if (fillBoth) {
            g.fill = GridBagConstraints.BOTH;
            g.weightx = 1.0;
            g.weighty = 1.0;
        }
        return g;
    }
    private GridBagConstraints gbc(int x, int y) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = x;
        g.gridy = y;
        g.insets = new Insets(5, 5, 5, 5);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        if(x == 1) g.weightx = 1.0;
        return g;
    }

    private void showWorkloadDialog() {
        JDialog workloadDialog = new JDialog(this, "Member Workload", true);
        workloadDialog.setSize(400, 300);
        workloadDialog.setLocationRelativeTo(this);
        
        StringBuilder workloadText = new StringBuilder("Member Workload Summary:\n\n");
        
        Map<GroupMember, List<Task>> tasksByMember = taskList.stream()
                .filter(task -> task.getAssignedTo() != null)
                .collect(Collectors.groupingBy(Task::getAssignedTo));

        for (GroupMember member : memberList) {
            List<Task> assignedTasks = tasksByMember.getOrDefault(member, Collections.emptyList());
            workloadText.append("▶ ").append(member.getName()).append(": ")
                        .append(assignedTasks.size()).append(" task(s)\n");
        }
        
        long unassignedCount = taskList.stream().filter(task -> task.getAssignedTo() == null).count();
        workloadText.append("\n▶ Unassigned: ").append(unassignedCount).append(" task(s)");
        
        JTextArea textArea = new JTextArea(workloadText.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setMargin(new Insets(10, 10, 10, 10));
        
        workloadDialog.add(new JScrollPane(textArea));
        workloadDialog.setVisible(true);
    }

    private void showManageCategoriesDialog() {
        JDialog manageDialog = new JDialog(this, "Manage Categories", true);
        manageDialog.setSize(350, 400);
        manageDialog.setLayout(new BorderLayout(10, 10));
        manageDialog.setLocationRelativeTo(this);

        DefaultListModel<Category> listModel = new DefaultListModel<>();
        categoryList.forEach(listModel::addElement);
        JList<Category> categoryJList = new JList<>(listModel);
        manageDialog.add(new JScrollPane(categoryJList), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton addButton = new JButton("Add...");
        JButton deleteButton = new JButton("Delete");
        JButton closeButton = new JButton("Close");

        addButton.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(manageDialog, "Category Name:");
            if (name != null && !name.trim().isEmpty()) {
                Category newCategory = new Category(name.trim());
                if (!categoryList.contains(newCategory)) {
                    categoryList.add(newCategory);
                    listModel.addElement(newCategory);
                    updateCategoryComboBoxes();
                } else {
                    JOptionPane.showMessageDialog(manageDialog, "Category exists.", "Duplicate", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        deleteButton.addActionListener(e -> {
            Category selected = categoryJList.getSelectedValue();
            if (selected != null) {
                boolean isUsed = taskList.stream().anyMatch(task -> selected.equals(task.getCategory()));
                if (isUsed) {
                    JOptionPane.showMessageDialog(manageDialog, "Cannot delete a category that is in use.", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    categoryList.remove(selected);
                    listModel.removeElement(selected);
                    updateCategoryComboBoxes();
                }
            } else {
                 JOptionPane.showMessageDialog(manageDialog, "Please select a category to delete.", "Nothing Selected", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        closeButton.addActionListener(e -> manageDialog.dispose());
        
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(closeButton);
        manageDialog.add(buttonPanel, BorderLayout.SOUTH);
        manageDialog.setVisible(true);
    }
    
    private void updateCategoryComboBoxes() {
        Category selected = (Category) categoryFilter.getSelectedItem();
        categoryFilter.removeAllItems();
        categoryFilter.addItem(new Category("All"));
        categoryList.forEach(categoryFilter::addItem);
        if (selected != null) {
            categoryFilter.setSelectedItem(selected);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ToDoListApplicatonFrame().setVisible(true));
    }
}