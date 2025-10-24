package ru.tasktracker.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task {
    private final Integer id;
    private final String taskName;
    private final String taskDescription;
    private final StatusTask statusTask;
    private final TypeTask type;
    private final Duration duration;
    private final LocalDateTime startTime;

    public Task() {
        this.id = null;
        this.taskName = "";
        this.taskDescription = "";
        this.statusTask = StatusTask.NEW;
        this.type = TypeTask.TASK;
        this.duration = Duration.ZERO;
        this.startTime = null;
    }

    public Task(String taskName,
                String taskDescription,
                LocalDateTime startTime,
                Duration duration) {
        this.type = TypeTask.TASK;
        this.id = null;
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.statusTask = StatusTask.NEW;
        this.startTime = startTime;
        this.duration = duration;
    }

    public Task(Integer id, String taskName,
                String taskDescription,
                StatusTask statusTask,
                LocalDateTime startTime,
                Duration duration) {
        this.type = TypeTask.TASK;
        this.id = id;
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.statusTask = statusTask;
        this.startTime = startTime;
        this.duration = duration;
    }

    public Task(TypeTask type, String taskName,
                String taskDescription,
                StatusTask statusTask,
                LocalDateTime startTime,
                Duration duration) {
        this.type = type;
        this.id = null;
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.statusTask = statusTask;
        this.startTime = startTime;
        this.duration = duration;
    }

    public Task(TypeTask type, String taskName,
                String taskDescription,
                LocalDateTime startTime,
                Duration duration) {
        this.type = type;
        this.id = null;
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.statusTask = StatusTask.NEW;
        this.startTime = startTime;
        this.duration = duration;
    }

    public Task(TypeTask type, Integer id, String taskName,
                String taskDescription,
                StatusTask statusTask,
                LocalDateTime startTime,
                Duration duration) {
        this.type = type;
        this.id = id;
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.statusTask = statusTask;
        this.startTime = startTime;
        this.duration = duration;
    }

    public TypeTask getType() {
        return type;
    }

    public Integer getId() {
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

    public Duration getDuration() {
        return duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        if (startTime == null || duration == null) {
            return null;
        }
        return startTime.plus(duration);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "id " + id + ": " + taskName + " | " + taskDescription + " (" + statusTask + ")";
    }

}

