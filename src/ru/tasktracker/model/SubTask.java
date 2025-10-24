package ru.tasktracker.model;

import java.time.Duration;
import java.time.LocalDateTime;

public class SubTask extends Task {
    private final Integer epicId;

    public SubTask() {
        super();
        this.epicId = null;
    }

    public SubTask(String taskName, String taskDescription, Integer epicId, LocalDateTime startTime, Duration duration) {
        super(TypeTask.SUBTASK, taskName, taskDescription, StatusTask.NEW, startTime, duration);
        this.epicId = epicId;
    }

    public SubTask(Integer id, String taskName, String taskDescription, StatusTask statusTask, Integer epicId,
                   LocalDateTime startTime, Duration duration) {
        super(TypeTask.SUBTASK, id, taskName, taskDescription, statusTask, startTime, duration);
        this.epicId = epicId;
    }

    public Integer getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return super.toString() + " | epicId: " + epicId;
    }
}
