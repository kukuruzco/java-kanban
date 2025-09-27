package ru.tasktracker.model;

public class SubTask extends Task {
    private final int epicId;

    public SubTask(String taskName, String taskDescription, int epicId) {
        super(TypeTask.SUBTASK, taskName, taskDescription, StatusTask.NEW);
        this.epicId = epicId;
    }

    public SubTask(int id, String taskName, String taskDescription, StatusTask statusTask, int epicId) {
        super(TypeTask.SUBTASK, id, taskName, taskDescription, statusTask);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    public SubTask withid(int newid) {
        return new SubTask(newid, getTaskName(), getTaskDescription(), getStatusTask(), epicId);
    }

    @Override
    public String toString() {
        return super.toString() + " | epicId: " + epicId;
    }
}
