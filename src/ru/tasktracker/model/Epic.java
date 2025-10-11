package ru.tasktracker.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {
    private List<Integer> subTaskIds = new ArrayList<>();
    private LocalDateTime endTime;

    public Epic(String taskName, String taskDescription) {
        super(TypeTask.EPIC, taskName, taskDescription, StatusTask.NEW, null, null);
    }

    public Epic(int id, String taskName, String taskDescription, StatusTask statusTask) {
        super(TypeTask.EPIC, id, taskName, taskDescription, statusTask, null, null);
    }

    public Epic(int id, String taskName, String taskDescription, StatusTask statusTask,
                List<Integer> subTaskIds) {
        super(TypeTask.EPIC, id, taskName, taskDescription, statusTask, null, null);
        this.subTaskIds = new ArrayList<>(subTaskIds);
        this.endTime = null;
    }

    public Epic(int id, String taskName, String taskDescription, StatusTask statusTask,
                List<Integer> subTaskIds, LocalDateTime startTime, Duration duration, LocalDateTime endTime) {
        super(TypeTask.EPIC, id, taskName, taskDescription, statusTask, startTime, duration);
        this.subTaskIds = new ArrayList<>(subTaskIds);
        this.endTime = endTime;
    }

    public List<Integer> getSubTaskIds() {
        return subTaskIds;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public StatusTask updateStatus(List<SubTask> epicSubtasks) {
        if (epicSubtasks.isEmpty()) {
            return StatusTask.NEW;  // Нет подзадач -> статус NEW
        }

        int newCount = 0;
        int doneCount = 0;
        int inProgressCount = 0;

        for (SubTask subtask : epicSubtasks) {
            switch (subtask.getStatusTask()) {
                case NEW:
                    newCount++;
                    break;
                case DONE:
                    doneCount++;
                    break;
                case IN_PROGRESS:
                    inProgressCount++;
                    break;
            }
        }

        if (inProgressCount > 0) {
            return StatusTask.IN_PROGRESS;  // Хотя бы одна IN_PROGRESS -> эпик IN_PROGRESS
        } else if (doneCount == epicSubtasks.size()) {
            return StatusTask.DONE;         // Все DONE -> эпик DONE
        } else if (newCount == epicSubtasks.size()) {
            return StatusTask.NEW;          // Все NEW -> эпик NEW
        }
        return StatusTask.IN_PROGRESS;     // Смешанные NEW и DONE -> IN_PROGRESS
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Epic epic = (Epic) o;
        return Objects.equals(getId(), epic.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

}
