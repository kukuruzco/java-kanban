package ru.tasktracker.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task {
    // УБИРАЕМ статический счетчик - он должен быть в менеджере
    private Integer id; // меняем на Integer и убираем final
    private final String taskName;
    private final String taskDescription;
    private StatusTask statusTask; // убираем final для возможности обновления
    private final TypeTask type;
    private final Duration duration;
    private final LocalDateTime startTime;

    // Конструктор по умолчанию - для десериализации Gson
    public Task() {
        this.id = null;
        this.taskName = "";
        this.taskDescription = "";
        this.statusTask = StatusTask.NEW;
        this.type = TypeTask.TASK;
        this.duration = Duration.ZERO;
        this.startTime = null;
    }

    // Конструктор для создания новой задачи (БЕЗ ID)
    public Task(String taskName,
                String taskDescription,
                LocalDateTime startTime,
                Duration duration) {
        this.id = null; // Явно null - ID назначит менеджер
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.statusTask = StatusTask.NEW;
        this.type = TypeTask.TASK;
        this.startTime = startTime;
        this.duration = duration;
    }

    // Конструктор для обновления задачи (С ID)
    public Task(Integer id, String taskName,
                String taskDescription,
                StatusTask statusTask,
                LocalDateTime startTime,
                Duration duration) {
        this.id = id; // ID передан явно - это обновление
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.statusTask = statusTask;
        this.type = TypeTask.TASK;
        this.startTime = startTime;
        this.duration = duration;
    }

    // Конструкторы для подзадач (аналогично)
    public Task(TypeTask type, String taskName,
                String taskDescription,
                StatusTask statusTask,
                LocalDateTime startTime,
                Duration duration) {
        this.id = null; // null для новой задачи
        this.type = type;
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
        this.id = null; // null для новой задачи
        this.type = type;
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
        this.id = id; // ID для обновления
        this.type = type;
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.statusTask = statusTask;
        this.startTime = startTime;
        this.duration = duration;
    }

    // Добавляем сеттер для ID (чтобы менеджер мог установить ID)
    public void setId(Integer id) {
        this.id = id;
    }

    // Добавляем сеттер для статуса (для обновлений)
    public void setStatusTask(StatusTask statusTask) {
        this.statusTask = statusTask;
    }

    // Геттеры остаются без изменений
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