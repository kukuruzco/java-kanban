package ru.tasktracker.model;

import java.util.ArrayList;
import java.util.Objects;

public class Epic extends Task {
    private ArrayList<Integer> subtaskIds = new ArrayList<>();

    public Epic(String taskName, String taskDescription) {
        super(-1, taskName, taskDescription, StatusTask.NEW);
    }

    public Epic(int id, String taskName, String taskDescription, StatusTask statusTask) {
        super(id, taskName, taskDescription, statusTask);
    }

    public ArrayList<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void setSubtaskIds(ArrayList<Integer> subtaskIds) {
        this.subtaskIds = subtaskIds;
    }

    public StatusTask updateStatus(ArrayList<SubTask> epicSubtasks) {
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
        return Objects.hash(getId(), getTaskName(), getTaskDescription(),
                getStatusTask(), subtaskIds);
    }

}
