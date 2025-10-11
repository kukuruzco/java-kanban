package ru.tasktracker.model;

import java.time.Duration;
import java.time.LocalDateTime;

public class SubTask extends Task {
    private final int epicId;

    public SubTask(String taskName, String taskDescription, int epicId, LocalDateTime startTime, Duration duration) {
        super(TypeTask.SUBTASK, taskName, taskDescription, StatusTask.NEW, startTime, duration);
        this.epicId = epicId;
    }

    public SubTask(int id, String taskName, String taskDescription, StatusTask statusTask, int epicId,
                   LocalDateTime startTime, Duration duration) {
        super(TypeTask.SUBTASK, id, taskName, taskDescription, statusTask, startTime, duration);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return super.toString() + " | epicId: " + epicId;
    }
}
