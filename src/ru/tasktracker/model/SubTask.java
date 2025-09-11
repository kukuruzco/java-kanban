package ru.tasktracker.model;

import java.util.ArrayList;

public class SubTask extends Task {
    private final int epicId;

    public SubTask(String taskName, String taskDescription, int epicId) {
        super(-1, taskName, taskDescription, StatusTask.NEW);
        this.epicId = epicId;
    }

    public SubTask(int id, String taskName, String taskDescription, StatusTask statusTask, int epicId) {
        super(id, taskName, taskDescription, statusTask);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    public SubTask withId(int newId) {
        return new SubTask(newId, getTaskName(), getTaskDescription(), getStatusTask(), epicId);
    }

    public SubTask withName(String newName) {
        return new SubTask(getId(), newName, getTaskDescription(), getStatusTask(), epicId);
    }

    public SubTask withDescription(String newDescription) {
        return new SubTask(getId(), getTaskName(), newDescription, getStatusTask(), epicId);
    }

    public SubTask withStatus(StatusTask newStatus) {
        return new SubTask(getId(), getTaskName(), getTaskDescription(), newStatus, epicId);
    }

    public SubTask withEpicId(int newEpicId) {
        return new SubTask(getId(), getTaskName(), getTaskDescription(), getStatusTask(), newEpicId);
    }

    @Override
    public String toString() {
        return super.toString() + " | EpicID: " + epicId;
    }
}
