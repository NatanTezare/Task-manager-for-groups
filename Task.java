package todolist;

import java.time.LocalDate;

public class Task {
    private String title;
    private String description;
    private LocalDate dueDate;
    private Priority priority;
    private Category category;
    private Status status;
    private int progressPercent;       // NEW: From class diagram
    private GroupMember assignedTo;    // NEW: From class diagram

    // MODIFIED: Update the constructor
    public Task(String title, String description, LocalDate dueDate, Priority priority, Category category, GroupMember assignedTo) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.category = category;
        this.assignedTo = assignedTo;
        this.status = Status.NOT_STARTED; // Default status
        this.progressPercent = 0;         // Default progress
    }

    // --- Getters and Setters ---
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    // NEW: Getters and setters for new fields
    public int getProgressPercent() { return progressPercent; }
    public void setProgressPercent(int progressPercent) { this.progressPercent = progressPercent; }

    public GroupMember getAssignedTo() { return assignedTo; }
    public void setAssignedTo(GroupMember assignedTo) { this.assignedTo = assignedTo; }
}