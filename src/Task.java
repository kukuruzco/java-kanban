import java.util.Objects;

public class Task {
    private final int id;
    private final String taskName;
    private final String taskDescription;
    private final StatusTask statusTask;

    public Task(String taskName, String taskDescription) {
        this.id = -1;
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.statusTask = StatusTask.NEW;
    }

    protected Task(int id, String taskName, String taskDescription, StatusTask statusTask) {
        this.id = id;
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.statusTask = statusTask;
    }

    public int getId() {
        return id;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public StatusTask getStatusTask() {
        return statusTask;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ID " + id + ": " + taskName + " | " + taskDescription + " (" + statusTask + ")";
    }

}
